package session;

import entity.ForumThread;
import entity.ForumUser;
import entity.ForumPost;
import error.NoResultException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author valenciateh
 */
@Stateless
public class PostSession implements PostSessionLocal {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private UserSessionLocal userSessionLocal;

    @EJB
    private ThreadSessionLocal threadSessionLocal;

    @Override
    public ForumPost getPost(Long postId) throws NoResultException {
        ForumPost post = em.find(ForumPost.class, postId);

        if (post != null) {
            return post;
        } else {
            throw new NoResultException("Post not found!");
        }

    }

    @Override
    public void addPostToThread(Long id, Long threadId, ForumPost post) throws NoResultException {
        System.out.println("User is " + id + " Thread is is " + threadId + " Post is " + post.getContent() + post.getId());

        ForumUser user = userSessionLocal.getUser(id);
        em.persist(user);
        System.out.println("user is " + user.getUsername());

        ForumThread thread = threadSessionLocal.getThread(threadId);
        em.persist(thread);
        System.out.println("thread is " + thread.getTitle());

        ArrayList<ForumPost> userPosts = user.getPosts();
        post.setUser(user);
        userPosts.add(post);
        user.setPosts(userPosts);

        ArrayList<ForumPost> threadPosts = thread.getPosts();
        threadPosts.add(post);
        thread.setPosts(threadPosts);
        int numberOfPostsToThread = thread.getReplies() + 1;
        System.out.println("Number of replies now " + numberOfPostsToThread);
        thread.setReplies(numberOfPostsToThread);
        thread.setUpdated(post.getPosted());

        post.setUser(user);
        post.setThread(thread);

    }

    @Override
    public List<ForumPost> getPostsFromThread(Long threadId) throws NoResultException {

        ForumThread thread = threadSessionLocal.getThread(threadId);
        return thread.getPosts();

    }

    @Override
    public void deletePost(Long postId) throws NoResultException {

        ForumPost post = getPost(postId);

        ForumThread thread = post.getThread();
        thread.getPosts().remove(post);
        int numberOfReplies = thread.getReplies() - 1;
        thread.setReplies(numberOfReplies);

        ForumUser user = post.getUser();
        user.getPosts().remove(post);

        em.remove(post);
    }

    @Override
    public void editPost(ForumPost post) throws NoResultException {

        Date newDate = new Date();
        ForumThread thread = post.getThread();
        ForumPost oldPost = getPost(post.getId());

        ArrayList<ForumPost> threadPosts = thread.getPosts();
        for (ForumPost tPost : threadPosts) {
            if (tPost.getId() == post.getId()) {
                tPost.setContent(post.getContent());
                tPost.setUpdated(newDate);
                thread.setUpdated(newDate);
                break;
            }
        }
        thread.setPosts(threadPosts);

        ForumUser user = post.getUser();
        ArrayList<ForumPost> userPosts = user.getPosts();
        for (ForumPost uPost : userPosts) {
            if (uPost.getId() == post.getId()) {
                uPost.setContent(post.getContent());
                uPost.setUpdated(newDate);
                post.setUser(user);
                break;
            }
        }
        user.setPosts(userPosts);

        oldPost.setContent(post.getContent());
        oldPost.setUpdated(newDate);

    }

    @Override
    public List<ForumPost> searchPosts(String content) {

        Query q;
        if (content != null) {
            q = em.createQuery("SELECT p FROM ForumPost p WHERE "
                    + "LOWER(p.content) LIKE :content");
            q.setParameter("content", "%" + content.toLowerCase() + "%");
        } else {
            q = em.createQuery("SELECT p FROM ForumPost p");
        }

        return q.getResultList();
    }

}
