package bot;

import database.HibernateUtil;
import database.models.Group;
import database.models.Product;
import database.models.Product.MarketplaceEnum;
import database.models.User;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import marketplace.Marketplace;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

import static bot.NotificationBot.ProductCreationStatus;
import static bot.NotificationBot.GroupCreationStatus;
import static bot.NotificationBot.GroupDeletionStatus;

class Queries {

    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static User getUser(long tgId) {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = builder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);
        criteriaQuery.select(root).where(builder.equal(root.get("tgId"), tgId));
        TypedQuery<User> query = session.createQuery(criteriaQuery);
        List<User> result = query.getResultList();

        if (result.size() > 0) {
            return result.get(0);
        } else {
            return null;
        }
    }

    static User getOrCreateUser(long tgId, String username) {
        Session session = sessionFactory.getCurrentSession();
        User user = getUser(tgId);

        if (user == null) {
            user = new User(username, tgId);
            session.persist(user);
        } else {
            System.out.println("User found: " + user);
        }

        return user;
    }

    static List<Group> getGroups(long tgId) {
        User user = getUser(tgId);
        List<Group> groups = null;

        if (user == null) {
            System.out.println("User not found! " + tgId);
        } else {
            groups = user.getGroups().stream().toList();
        }

        return groups;
    }

    static Group getGroupById(long groupId) {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Group> criteriaQuery = builder.createQuery(Group.class);
        Root<Group> root = criteriaQuery.from(Group.class);
        criteriaQuery.select(root).where(builder.equal(root.get("id"), groupId));
        TypedQuery<Group> query = session.createQuery(criteriaQuery);
        List<Group> result = query.getResultList();

        if (result.size() > 0) {
            return result.get(0);
        } else {
            return null;
        }
    }

    static Group getGroupByName(long tgId, String name) {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Group> criteriaQuery = builder.createQuery(Group.class);
        Root<Group> root = criteriaQuery.from(Group.class);

        Predicate ownerPredicate = builder.equal(root.get("owner"), getUser(tgId));
        Predicate namePredicate = builder.equal(root.get("name"), name);
        Predicate predicate = builder.and(ownerPredicate, namePredicate);
        criteriaQuery.select(root).where(predicate);

        TypedQuery<Group> query = session.createQuery(criteriaQuery);
        List<Group> result = query.getResultList();

        if (result.size() > 0) {
            return result.get(0);
        } else {
            return null;
        }
    }

    static ProductCreationStatus addProduct(long tgId, long groupId, String productUrl) {
        Session session = sessionFactory.getCurrentSession();

        Marketplace marketplace = Marketplace.getInstance(productUrl);
        if (marketplace == null) {
            return ProductCreationStatus.UNEXPECTED_MARKET;
        }
        Marketplace.ValidationStatus validationStatus = marketplace.validateProductUrl(productUrl);
        switch (validationStatus) {
            case ERROR -> {
                return ProductCreationStatus.FAILED;
            }
            case UNEXPECTED_URL -> {
                return ProductCreationStatus.UNEXPECTED_URL;
            }
            case NO_PRODUCT -> {
                return ProductCreationStatus.NO_PRODUCT;
            }
            case OK -> {}
        }

        // todo: parse (get name, price)

        String productName = "Название продукта";   // todo
        MarketplaceEnum marketplaceType = marketplace.getMarketplaceType();
        Group group = getGroupById(groupId);

        if (group == null) {
            return ProductCreationStatus.FAILED;
        }

        if (group.getOwner().getTgId() != tgId) {
            return ProductCreationStatus.FORBIDDEN;
        }

        session.persist(new Product(productName, marketplaceType, group));

        // todo: write to mongo
        return ProductCreationStatus.SUCCESS;
    }

    static GroupCreationStatus addGroup(long tgId, String groupName) {
        Session session = sessionFactory.getCurrentSession();

        Group group = getGroupByName(tgId, groupName);

        if (group != null) {
            return GroupCreationStatus.ALREADY_EXISTS;
        } else {
            group = new Group(groupName, getUser(tgId));
            session.persist(group);
        }
        return GroupCreationStatus.SUCCESS;
    }

    static GroupDeletionStatus deleteGroup(long tgId, long groupId) {
        Session session = sessionFactory.getCurrentSession();

        Group group = session.get(Group.class, groupId);

        if (group != null) {
            if (group.getOwner().getTgId() != tgId) {
                return GroupDeletionStatus.FORBIDDEN;
            }
            session.remove(group);
            return GroupDeletionStatus.SUCCESS;
        } else {
            return GroupDeletionStatus.NOT_FOUND;
        }
    }
}
