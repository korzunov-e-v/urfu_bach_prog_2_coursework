package bot;

import database.HibernateUtil;
import database.models.Group;
import database.models.User;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

class Queries {
    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static User getUser(long userId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<User> userCriteriaQuery = builder.createQuery(User.class);
            Root<User> root = userCriteriaQuery.from(User.class);
            userCriteriaQuery.select(root).where(builder.equal(root.get("tgId"), userId));
            TypedQuery<User> query = session.createQuery(userCriteriaQuery);
            List<User> userList = query.getResultList();

            if (userList.size() > 0) {
                return userList.get(0);
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
                System.out.println("User created: " + user);
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
}
