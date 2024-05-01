package bot;

import database.HibernateUtil;
import database.models.Group;
import database.models.Marketplace;
import database.models.Product;
import database.models.User;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

import static bot.NotificationBot.ProductCreationStatus;
import static org.hibernate.resource.transaction.spi.TransactionStatus.COMMITTED;

class Queries {
    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static User getUser(long userId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<User> criteriaQuery = builder.createQuery(User.class);
            Root<User> root = criteriaQuery.from(User.class);
            criteriaQuery.select(root).where(builder.equal(root.get("tgId"), userId));
            TypedQuery<User> query = session.createQuery(criteriaQuery);
            List<User> result = query.getResultList();

            if (result.size() > 0) {
                return result.get(0);
            } else {
                return null;
            }
        }
    }

    static User getOrCreateUser(long userId, String username) {
        User user = getUser(userId);

        if (user == null) {
            try (Session session = sessionFactory.openSession()) {
                Transaction transaction = session.beginTransaction();
                user = new User(username, userId);
                session.persist(user);
                transaction.commit();
            }
        } else {
            System.out.println("User found: " + user);
        }
        return user;
    }

    static List<Group> getGroups(long userId) {
        User user = getUser(userId);
        List<Group> groups = null;

        if (user == null) {
            System.out.println("User not found! " + userId);
        } else {
            groups = user.getGroups();
        }
        return groups;
    }

    static Marketplace getMarketplace(String baseUrl) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Marketplace> criteriaQuery = builder.createQuery(Marketplace.class);
            Root<Marketplace> root = criteriaQuery.from(Marketplace.class);
            criteriaQuery.select(root).where(builder.equal(root.get("base_url"), baseUrl));
            TypedQuery<Marketplace> query = session.createQuery(criteriaQuery);
            List<Marketplace> result = query.getResultList();

            if (result.size() > 0) {
                return result.get(0);
            } else {
                return null;
            }
        }
    }

    static Group getGroup(long groupId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Group> criteriaQuery = builder.createQuery(Group.class);
            Root<Group> root = criteriaQuery.from(Group.class);
            criteriaQuery.select(root).where(builder.equal(root.get("owner_id"), groupId));
            TypedQuery<Group> query = session.createQuery(criteriaQuery);
            List<Group> result = query.getResultList();

            if (result.size() > 0) {
                return result.get(0);
            } else {
                return null;
            }
        }
    }

    static ProductCreationStatus addProduct(long userId, long groupId, String productUrl) {
        // TODO: check productUrl
        // TODO: parse (get name, price)

        String productName = null;
        String marketplaceBaseUrl = null;

        Marketplace marketplace = getMarketplace(marketplaceBaseUrl);
        Group group = getGroup(groupId);


        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(new Product(productName, marketplace, group));
            transaction.commit();

            if (transaction.getStatus() == COMMITTED) {
                return ProductCreationStatus.SUCCESS;
            } else {
                return ProductCreationStatus.FAILED;
            }
        }

        // TODO: write to mongo

    }
}
