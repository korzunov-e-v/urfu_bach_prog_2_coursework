package bot;

import database.HibernateUtil;
import database.models.Group;
import database.models.Marketplace;
import database.models.Product;
import database.models.User;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

import static bot.NotificationBot.ProductCreationStatus;
import static bot.NotificationBot.GroupCreationStatus;
import static bot.NotificationBot.GroupDeletionStatus;

class Queries {
    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static User getUser(long userId) {
        Session session = sessionFactory.getCurrentSession();

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

    static User getOrCreateUser(long userId, String username) {
        Session session = sessionFactory.getCurrentSession();
        User user = getUser(userId);

        if (user == null) {
            user = new User(username, userId);
            session.persist(user);
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
            groups = user.getGroups().stream().toList();
        }

        return groups;
    }

    static Marketplace getMarketplace(String baseUrl) {
        Session session = sessionFactory.getCurrentSession();

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

    // TODO
    static Group getGroupByOwner(long ownerId) {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Group> criteriaQuery = builder.createQuery(Group.class);
        Root<Group> root = criteriaQuery.from(Group.class);
        criteriaQuery.select(root).where(builder.equal(root.get("ownerId"), ownerId));
        TypedQuery<Group> query = session.createQuery(criteriaQuery);
        List<Group> result = query.getResultList();

        if (result.size() > 0) {
            return result.get(0);
        } else {
            return null;
        }
    }

    static Group getGroupByName(long userId, String name) {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Group> criteriaQuery = builder.createQuery(Group.class);
        Root<Group> root = criteriaQuery.from(Group.class);

        Predicate ownerPredicate = builder.equal(root.get("owner"), getUser(userId));
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

    static ProductCreationStatus addProduct(long userId, long groupId, String productUrl) {
        Session session = sessionFactory.getCurrentSession();
        // TODO: check productUrl
        // TODO: parse (get name, price)

        String productName = null;
        String marketplaceBaseUrl = null;

        Marketplace marketplace = getMarketplace(marketplaceBaseUrl);
        Group group = getGroupByOwner(groupId); // TODO

        session.persist(new Product(productName, marketplace, group));

        // TODO: write to mongo
        return ProductCreationStatus.SUCCESS;
    }

    static GroupCreationStatus addGroup(long userId, String groupName) {
        Session session = sessionFactory.getCurrentSession();

        Group group = getGroupByName(userId, groupName);

        if (group != null) {
            return GroupCreationStatus.ALREADY_EXISTS;
        } else {
            group = new Group(groupName, getUser(userId));
            session.persist(group);
        }
        return GroupCreationStatus.SUCCESS;
    }

    static GroupDeletionStatus deleteGroup(long userId, long groupId) {
        Session session = sessionFactory.getCurrentSession();

        Group group = session.get(Group.class, groupId);

        if (group != null) {
            if (group.getOwner().getId() != userId) {
                return GroupDeletionStatus.FORBIDDEN;
            }
            session.remove(group);
            return GroupDeletionStatus.SUCCESS;
        } else {
            return GroupDeletionStatus.NOT_FOUND;
        }
    }
}
