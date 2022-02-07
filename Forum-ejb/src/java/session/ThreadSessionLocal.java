/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session;

import error.NoResultException;
import java.util.List;
import javax.ejb.Local;
import entity.ForumThread;
import entity.ForumPost;

/**
 *
 * @author valenciateh
 */
@Local
public interface ThreadSessionLocal {

    public ForumThread getThread(Long threadId) throws NoResultException;

    public void addThreadToForum(Long id, Long forumId, ForumThread thread) throws NoResultException;

    public void openThread(Long threadId) throws NoResultException;

    public void closeThread(Long threadId) throws NoResultException;

    public List<ForumThread> getThreadsFromForum(Long forumId) throws NoResultException;

    public void deleteThread(Long threadId) throws NoResultException;

    public void editThread(ForumThread thread) throws NoResultException;
    
    public List<ForumPost> getPostsFromThread(Long threadId) throws NoResultException;
    
    public List<ForumThread> searchThreads(String title);

}
