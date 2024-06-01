package scheduler;

import database.HibernateUtil;
import database.models.Product;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import marketplace.Marketplace;
import marketplace.exceptions.MarketplaceException;
import mongo.MongoUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;


public class Scheduler {

    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    public static void main() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        int initialDelaySeconds = 0;
        int repeatIntervalSeconds = 3600;
        scheduler.scheduleAtFixedRate(new Runner(), initialDelaySeconds, repeatIntervalSeconds, TimeUnit.SECONDS);
    }

    private static class Runner implements Runnable {
        @Override
        public void run() {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<Product> products = getAllProducts();

            List<Callable<String>> callableList = new ArrayList<>();
            for (Product product : products) {
                Callable<String> callableTask = () -> {
                    try {
                        Marketplace marketplace = Marketplace.getInstance(product.getProductUrl());
                        double price = marketplace.getPrice();
                        MongoUtil.addRecord(product.getId(), "available", price);
                    } catch (MarketplaceException e) {
                        System.out.println("Error updating price, productId=" + product.getId());
                        e.printStackTrace();
                    }
                    return null;
                };
                callableList.add(callableTask);
            }
            try {
                executor.invokeAll(callableList);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    static List<Product> getAllProducts() {
        Session session = sessionFactory.openSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Product> criteriaQuery = builder.createQuery(Product.class);
        Root<Product> root = criteriaQuery.from(Product.class);
        criteriaQuery.select(root);
        TypedQuery<Product> query = session.createQuery(criteriaQuery);
        List<Product> result = query.getResultList().stream().toList();
        session.close();
        return result;
    }
}
