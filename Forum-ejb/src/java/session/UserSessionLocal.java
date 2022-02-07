/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session;

import entity.Forum;
import entity.ForumPost;
import entity.ForumThread;
import javax.ejb.Local;
import entity.ForumUser;
import error.NoResultException;
import java.util.List;

/**
 *
 * @author valenciateh
 */
@Local
public interface UserSessionLocal {
    
    public ForumUser getUser(Long id) throws NoResultException; //View a user profile: can get anyone's profile -- yourself included
    
    public void createUser(ForumUser user); //register user
    
    public void updateUser(ForumUser user) throws NoResultException; //edit own profile
    
    public List<ForumUser> viewListOfUsers(Long currentUserId) throws NoResultException;
    
    public void blockUser(Long currentUserId, Long userId) throws NoResultException;
    
    public void unblockUser(Long currentUserId, Long userId) throws NoResultException;
    
    public List<ForumUser> searchUsers(String name);
    
    public Long verifyLogin(String username, String password);
    
    public void promoteUser(Long currentUserId, Long userId) throws NoResultException;
    
    public void demoteUser(Long currentUserId, Long userId) throws NoResultException;
    
}
