package session;

import entity.ForumPost;
import error.NoResultException;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author valenciateh
 */
@Local
public interface PostSessionLocal {

    public ForumPost getPost(Long postId) throws NoResultException;

    public void addPostToThread(Long id, Long threadId, ForumPost post) throws NoResultException;

    public List<ForumPost> getPostsFromThread(Long threadId) throws NoResultException;

    public void deletePost(Long postId) throws NoResultException;

    public void editPost(ForumPost post) throws NoResultException;
    
    public List<ForumPost> searchPosts(String content);

}
