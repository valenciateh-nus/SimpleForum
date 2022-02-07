package session;

import entity.Forum;
import entity.ForumThread;
import entity.ForumUser;
import entity.ForumPost;
import error.NoResultException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import static util.enumeration.ThreadStatus.ACTIVE;
import static util.enumeration.ThreadStatus.CLOSED;

/**
 *
 * @author valenciateh
 */
@Stateless
public class ThreadSession implements ThreadSessionLocal {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private ForumSessionLocal forumSessionLocal;

    @EJB
    private UserSessionLocal userSessionLocal;

    @Override
    public ForumThread getThread(Long threadId) throws NoResultException {

        ForumThread thread = em.find(ForumThread.class, threadId);

        return thread;
    }

    @Override
    public void addThreadToForum(Long id, Long forumId, ForumThread thread) throws NoResultException {

        ForumUser user = userSessionLocal.getUser(id);
        em.persist(user);

        Forum forum = forumSessionLocal.getForum(forumId);
        em.persist(forum);

        ArrayList<ForumThread> userThreads = user.getThreads();
        thread.setUser(user);
        userThreads.add(thread);
        user.setThreads(userThreads);

        ArrayList<ForumThread> forumThreads = forum.getThreads();
        thread.setForum(forum);
        forumThreads.add(thread);
        forum.setThreads(forumThreads);

    }

    @Override
    public void openThread(Long threadId) throws NoResultException {

        ForumThread thread = getThread(threadId);
        em.persist(thread);
        thread.setStatus(ACTIVE);

        Forum forum = thread.getForum();
        ArrayList<ForumThread> forumThreads = forum.getThreads();
        for (ForumThread fThread : forumThreads) {
            if (fThread.getId() == threadId) {
                fThread.setStatus(ACTIVE);
                break;
            }
        }
        forum.setThreads(forumThreads);

        ForumUser user = thread.getUser();
        em.persist(user);
        ArrayList<ForumThread> userThreads = user.getThreads();
        for (ForumThread uThread : userThreads) {
            if (uThread.getId() == threadId) {
                uThread.setStatus(ACTIVE);
                break;
            }
        }
        user.setThreads(userThreads);

    }

    @Override
    public void closeThread(Long threadId) throws NoResultException {

        ForumThread thread = getThread(threadId);
        em.persist(thread);

        thread.setStatus(CLOSED);

        Forum forum = thread.getForum();
        ArrayList<ForumThread> forumThreads = forum.getThreads();
        for (ForumThread fThread : forumThreads) {
            if (fThread.getId() == threadId) {
                fThread.setStatus(CLOSED);
                break;
            }
        }
        forum.setThreads(forumThreads);

        ForumUser user = thread.getUser();
        em.persist(user);
        ArrayList<ForumThread> userThreads = user.getThreads();
        for (ForumThread uThread : userThreads) {
            if (uThread.getId() == threadId) {
                uThread.setStatus(CLOSED);
                break;
            }
        }
        user.setThreads(userThreads);
    }

    @Override
    public List<ForumThread> getThreadsFromForum(Long forumId) throws NoResultException {

        Forum forum = forumSessionLocal.getForum(forumId);
        return forum.getThreads();

    }

    @Override
    public void deleteThread(Long threadId) throws NoResultException {

        ForumThread thread = getThread(threadId);

        List<ForumPost> posts = thread.getPosts();
        for (ForumPost p : posts) {
            p.setThread(null);
            em.remove(p);
        }

        Forum forum = thread.getForum();
        forum.getThreads().remove(thread);

        ForumUser user = thread.getUser();
        user.getThreads().remove(thread);

        em.remove(thread);

    }

    @Override
    public void editThread(ForumThread thread) throws NoResultException {

        ForumThread oldThread = getThread(thread.getId());
        Forum forum = thread.getForum();
        System.out.println("The forum is " + forum);
        ArrayList<ForumThread> forumThreads = forum.getThreads();
        for (ForumThread fThread : forumThreads) {
            if (fThread.getId() == thread.getId()) {
                fThread.setDescription(thread.getDescription());
                fThread.setTitle(thread.getTitle());
                break;
            }
        }
        forum.setThreads(forumThreads);

        ForumUser user = thread.getUser();
        System.out.println("The user is " + user);
        ArrayList<ForumThread> userThreads = user.getThreads();
        for (ForumThread uThread : forumThreads) {
            if (uThread.getId() == thread.getId()) {
                uThread.setDescription(thread.getDescription());
                uThread.setTitle(thread.getTitle());
                break;
            }
        }
        user.setThreads(userThreads);
        oldThread.setDescription(thread.getDescription());
        oldThread.setTitle(thread.getTitle());

    }

    @Override
    public List<ForumPost> getPostsFromThread(Long threadId) throws NoResultException {

        ForumThread thread = getThread(threadId);
        return thread.getPosts();

    }

    @Override
    public List<ForumThread> searchThreads(String title) {

        Query q;
        if (title != null) {
            q = em.createQuery("SELECT t FROM ForumThread t WHERE "
                    + "LOWER(t.title) LIKE :title");
            q.setParameter("title", "%" + title.toLowerCase() + "%");
        } else {
            q = em.createQuery("SELECT t FROM ForumThread t");
        }

        return q.getResultList();

    }

}
