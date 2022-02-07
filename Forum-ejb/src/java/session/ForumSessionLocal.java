package session;

import entity.Forum;
import entity.ForumThread;
import error.NoAccessRightException;
import error.NoResultException;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author valenciateh
 */
@Local
public interface ForumSessionLocal {

    public Forum getForum(Long forumId) throws NoResultException;

    public ForumThread getThread(Long threadId) throws NoResultException;

    public void addForum(Long id, Forum forum) throws NoResultException, NoAccessRightException;

    public void editForum(Forum forum) throws NoResultException, NoAccessRightException;
    
    public void deleteForum(Long forumId) throws NoResultException, NoAccessRightException;

    public List<Forum> searchForums(String category);

}
