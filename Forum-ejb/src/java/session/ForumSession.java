package session;

import entity.Forum;
import entity.ForumPost;
import entity.ForumThread;
import entity.ForumUser;
import error.NoAccessRightException;
import error.NoResultException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import static util.enumeration.AccessRightEnum.ADMIN;
import static util.enumeration.ThreadStatus.ACTIVE;
import static util.enumeration.ThreadStatus.CLOSED;

/**
 *
 * @author valenciateh
 */
@Stateless
public class ForumSession implements ForumSessionLocal {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private UserSessionLocal userSessionLocal;
    @EJB
    private ThreadSessionLocal threadSessionLocal;

    @Override
    public Forum getForum(Long forumId) throws NoResultException {

        Forum forum = em.find(Forum.class, forumId);

        return forum;
    }

    @Override
    public ForumThread getThread(Long threadId) throws NoResultException {

        ForumThread thread = em.find(ForumThread.class, threadId);

        return thread;
    }

    @Override
    public void addForum(Long id, Forum forum) throws NoResultException, NoAccessRightException { //Add a forum: only admin

        System.out.println("The user id is " + id);
        ForumUser user = userSessionLocal.getUser(id);
        System.out.println("The user name is " + user.getUsername());
        if (forum != null) {
            if (user.getAccessRight() == ADMIN) {
                em.persist(forum);
                user.getForums().add(forum);
                forum.setUser(user); //admin who created the forum
            } else {
                throw new NoAccessRightException("Add forum not allowed! You do not have the administrative privilege to do so.");
            }
        } else {
            throw new NoResultException("Forum not added");
        }
    }

    @Override
    public void editForum(Forum forum) throws NoResultException, NoAccessRightException { //Edit Forum: Only admin

        Forum f = getForum(forum.getId());

        if (f != null) {
            f.setCategory(forum.getCategory());
            f.setDescription(forum.getDescription());
        } else {
            throw new NoResultException("Forum not edited.");
        }
        
    }

    @Override
    public void deleteForum(Long forumId) throws NoResultException, NoAccessRightException {

        Forum forum = getForum(forumId);
        ForumUser user = userSessionLocal.getUser(forum.getUser().getId());

        if (getForum(forumId) != null) {
            if (user.getAccessRight() == ADMIN) {

                List<ForumThread> threads = forum.getThreads();
                for (ForumThread t : threads) {
                    Long tId = t.getId();
                    t.setForum(null);
                }

                ForumUser forumCreator = forum.getUser();
                ArrayList<Forum> forumsCreated = forumCreator.getForums();
                for (Forum f : forumsCreated) {
                    if (f.getId() == forumId) {
                        forumsCreated.remove(f);
                        break;
                    }
                }
                forumCreator.setForums(forumsCreated);

                em.remove(forum);
            } else {
                throw new NoAccessRightException("Edit forum not allowed! You do not have the administrative privilege to do so.");
            }
        } else {
            throw new NoResultException("Forum not edited");
        }
    }

    @Override
    public List<Forum> searchForums(String category) {

        Query q;
        if (category != null) {
            q = em.createQuery("SELECT f FROM Forum f WHERE "
                    + "LOWER(f.category) LIKE :category");
            q.setParameter("category", "%" + category.toLowerCase() + "%");
        } else {
            q = em.createQuery("SELECT f FROM Forum f");
        }

        return q.getResultList();
    }

}
