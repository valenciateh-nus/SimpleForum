package session;

import entity.ForumUser;
import error.NoResultException;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import static util.enumeration.AccessRightEnum.ADMIN;
import static util.enumeration.AccessRightEnum.BLOCKED;
import static util.enumeration.AccessRightEnum.NORMAL_USER;

/**
 *
 * @author valenciateh
 */
@Stateless
public class UserSession implements UserSessionLocal {

    @PersistenceContext
    private EntityManager em;

    @Override
    public ForumUser getUser(Long id) throws NoResultException {
        ForumUser user = em.find(ForumUser.class, id);

        if (user != null) {
            return user;
        } else {
            throw new NoResultException("User not found.");
        }
    }

    @Override
    public void createUser(ForumUser user) {
        em.persist(user);
    }

    @Override
    public void updateUser(ForumUser user) throws NoResultException {

        ForumUser oldUser = getUser(user.getId());

        oldUser.setUsername(user.getUsername());
        oldUser.setDob(user.getDob());
        oldUser.setGender(user.getGender());
        oldUser.setPassword(user.getPassword());
        oldUser.setAccessRight(user.getAccessRight());

    }


    @Override
    public List<ForumUser> viewListOfUsers(Long currentUserId) throws NoResultException {

        ForumUser user = getUser(currentUserId);

        if (user != null) {
            if (user.getAccessRight() == ADMIN) {
                Query q = em.createQuery("SELECT u FROM ForumUser u");
                return q.getResultList();
            } else {
                throw new NoResultException("You do not have the administrative privilege to view list of users.");
            }
        } else {
            throw new NoResultException("View List of Users not done");
        }

    }

    @Override
    public void blockUser(Long currentUserId, Long userId) throws NoResultException {

        ForumUser admin = getUser(currentUserId);
        ForumUser user = getUser(userId);

        if (admin.getAccessRight() == ADMIN) {
            user.setAccessRight(BLOCKED);
        } else {
            throw new NoResultException("Block user not allowed. You do not have the administrative privileges to do so.");
        }

    }

    @Override
    public void unblockUser(Long currentUserId, Long userId) throws NoResultException {

        ForumUser admin = getUser(currentUserId);
        ForumUser user = getUser(userId);

        if (admin.getAccessRight() == ADMIN) {
            user.setAccessRight(NORMAL_USER);
        } else {
            throw new NoResultException("Unblock user not allowed. You do not have the administrative privileges to do so.");
        }
    }
    
    @Override
    public void promoteUser(Long currentUserId, Long userId) throws NoResultException {

        ForumUser admin = getUser(currentUserId);
        ForumUser user = getUser(userId);

        if (admin.getAccessRight() == ADMIN) {
            user.setAccessRight(ADMIN);
        } else {
            throw new NoResultException("Promote user not allowed. You do not have the administrative privileges to do so.");
        }

    }
    
    @Override
    public void demoteUser(Long currentUserId, Long userId) throws NoResultException {

        ForumUser admin = getUser(currentUserId);
        ForumUser user = getUser(userId);

        if (admin.getAccessRight() == ADMIN) {
            user.setAccessRight(NORMAL_USER);
        } else {
            throw new NoResultException("Demote user not allowed. You do not have the administrative privileges to do so.");
        }
    }

    
    @Override
    public List<ForumUser> searchUsers(String username) {
        Query q;
        if (username != null) {
            q = em.createQuery("SELECT u FROM ForumUser u WHERE "
                    + "LOWER(u.username) LIKE :username");
            q.setParameter("username", "%" + username.toLowerCase() + "%");
        } else {
            q = em.createQuery("SELECT u FROM ForumUser u");
        }

        return q.getResultList();
    }

    @Override
    public Long verifyLogin(String username, String password) {
        
        List<ForumUser> users = searchUsers(username);
        for (ForumUser u: users) {
            System.out.println("User is " + u.getUsername() + " with password " + u.getPassword());
        }
        
        if (users.isEmpty()) {
            return -1L;
        } else {
            //verify password
            System.out.println("matches");
            ForumUser user = users.get(0);
            System.out.println("The current user is " + user.getUsername() + " with access " + user.getAccessRight().toString());
            if (password.equals(user.getPassword()) && user.getAccessRight() != BLOCKED) {
                System.out.println("The user id is " + user.getId());
                return user.getId();
            } else{
                System.out.println("The user is blocked.");
            }
            return -1L;
        }
    }
    
}
