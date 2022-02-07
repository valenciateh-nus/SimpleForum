package managedbean;

import entity.Forum;
import entity.ForumThread;
import entity.ForumUser;
import entity.ForumPost;
import error.NoAccessRightException;
import error.NoResultException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpSession;
import session.ForumSessionLocal;
import session.PostSessionLocal;
import session.ThreadSessionLocal;
import session.UserSessionLocal;
import util.enumeration.AccessRightEnum;
import static util.enumeration.AccessRightEnum.ADMIN;
import static util.enumeration.AccessRightEnum.BLOCKED;
import static util.enumeration.AccessRightEnum.NORMAL_USER;
import util.enumeration.ThreadStatus;
import static util.enumeration.ThreadStatus.ACTIVE;
import static util.enumeration.ThreadStatus.CLOSED;

@Named(value = "forumManagedBean")
@SessionScoped
public class ForumManagedBean implements Serializable {

    @EJB
    private UserSessionLocal userSessionLocal;
    @EJB
    private ForumSessionLocal forumSessionLocal;
    @EJB
    private ThreadSessionLocal threadSessionLocal;
    @EJB
    private PostSessionLocal postSessionLocal;

    private ForumUser loggedInUser;

    private String username;
    private byte gender;
    private Date dob;
    private Date created;
    private String password;
    private AccessRightEnum accessRight;
    private Long id = -1L;

    private String adminUsername;
    private byte adminGender;
    private Date adminDob;
    private Date adminCreated;
    private String adminPassword;
    private AccessRightEnum adminAccessRight;
    private Long adminId;

    private Long loggedInAdminId = -1L;

    private ForumUser otherUser;
    private String otherUsername;
    private byte otherGender;
    private Date otherDob;
    private Date otherCreated;
    private String otherPassword;
    private AccessRightEnum otherAccessRight;
    private Long otherId;

    private Forum forum;
    private Long forumId;
    private String forumCategory;
    private String forumDescription;

    private ForumThread thread;
    private Long threadId;
    private String threadTitle;
    private String threadDescription;
    private ThreadStatus threadStatus;

    private ForumPost post;
    private Long postId;
    private String postTitle;
    private String postContent;

    private List<ForumUser> users;
    private List<Forum> forums;
    private List<ForumThread> threads;
    private List<ForumPost> posts;

    private String redirectUrl;
    private String adminRedirectUrl;

    public ForumManagedBean() {
    }

    @PostConstruct
    public void init() {
        setUsers(userSessionLocal.searchUsers(null));
        setForums(forumSessionLocal.searchForums(null));
        setThreads(threadSessionLocal.searchThreads(null));
        setPosts(postSessionLocal.searchPosts(null));
    }

    public String login() throws NoResultException {

        FacesContext context = FacesContext.getCurrentInstance();

        boolean valid = false;

        Long userId = userSessionLocal.verifyLogin(username, password);

        if (userId > 0) {
            id = userId;
            this.loggedInUser = userSessionLocal.getUser(id);;
            gender = loggedInUser.getGender();
            dob = loggedInUser.getDob();
            created = loggedInUser.getCreated();
            accessRight = loggedInUser.getAccessRight();

            if (accessRight == ADMIN) {
                this.loggedInAdminId = id;
            }
            System.out.println("Login successful! Welcome, " + username + " your id is " + id);
            context.addMessage(null, new FacesMessage("Success", "Successfully logged in to user account"));
            return "secret/forumPage.xhtml?faces-redirect=true";
        } else {
            username = null;
            password = null;
            id = -1L;
            System.out.println("Invalid login details");
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to login user"));
            return null;
        }

    }

    public String logout() {

        username = null;
        password = null;
        id = -1L;
        gender = -1;
        dob = null;
        created = null;
        accessRight = null;
        loggedInUser = null;

        loggedInAdminId = -1L;
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        HttpSession session = (HttpSession) ec.getSession(false);
        session.invalidate();

        try {
            ec.redirect(ec.getRequestContextPath());
            System.out.println("Logging out.");
        } catch (Exception e) {
            System.out.println("Redirect to the login page failed");
        }
        return "/index.xhtml?faces-redirect=true";
    }

    public void createUser(ActionEvent evt) {

        FacesContext context = FacesContext.getCurrentInstance();

        ForumUser user = new ForumUser();
        user.setUsername(username);
        user.setGender(gender);
        user.setDob(dob);
        user.setPassword(password);
        user.setCreated(new Date());
        AccessRightEnum accessRightEnum = NORMAL_USER;
        user.setAccessRight(accessRightEnum);

        try {
            userSessionLocal.createUser(user);
            this.loggedInUser = user;
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to create user"));
            return;
        }

        init();
    }

    public void editUser(ActionEvent evt) {

        FacesContext context = FacesContext.getCurrentInstance();

        System.out.println("The user that is being edited has the name " + loggedInUser.getUsername()
                + " and is changing to " + username);
        loggedInUser.setUsername(username);
        loggedInUser.setGender(gender);
        loggedInUser.setDob(dob);
        loggedInUser.setPassword(password);

        username = loggedInUser.getUsername();
        gender = loggedInUser.getGender();
        dob = loggedInUser.getDob();
        created = loggedInUser.getCreated();
        password = loggedInUser.getPassword();

        try {
            userSessionLocal.updateUser(loggedInUser);
            System.out.println("Successfully updated user.");
            init();
            context.addMessage(null, new FacesMessage("Success", "Successfully edited user"));

        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to edit user"));
            return;
        }

    }

    public void checkUsername(FacesContext context, UIComponent component, Object value) {

        String name = (String) value;
        List<ForumUser> users = userSessionLocal.searchUsers(name);
        if (!users.isEmpty()) {
            System.out.println("A user has this username already");
            throw new ValidatorException(new FacesMessage("Username already exists. Choose another."));
        }
    }

    public void checkAdminUsername(FacesContext context, UIComponent component, Object value) {

        String name = (String) value;
        List<ForumUser> users = userSessionLocal.searchUsers(name);
        if (!users.isEmpty()) {
            System.out.println("A user has this username already");
            throw new ValidatorException(new FacesMessage("Username already exists. Choose another."));
        }
    }

    public void checkEditedUsername(FacesContext context, UIComponent component, Object value) {

        String previousUsername = this.loggedInUser.getUsername().toLowerCase();
        String name = (String) value;
        List<ForumUser> users = userSessionLocal.searchUsers(name);

        if (!users.isEmpty()) {
            System.out.println("Previous username " + previousUsername);
            System.out.println("Current username " + name);
            System.out.println(previousUsername.equals(name));
            if (!previousUsername.equals(name.toLowerCase())) {
                for (ForumUser u : users) {
                    System.out.println("User who has this username is " + u.getUsername());
                }

                System.out.println("A user has this username already");
                throw new ValidatorException(new FacesMessage("Username already exists. Choose another."));
            } else {
                System.out.println("Equals");
            }
        }
    }

    public void createAdministrator(ActionEvent evt) throws NoAccessRightException {

        FacesContext context = FacesContext.getCurrentInstance();
        System.out.println("Current user is " + loggedInUser.getUsername() + " with access " + loggedInUser.getAccessRight().toString());
        if (this.loggedInUser.getAccessRight() == ADMIN) {
            ForumUser user = new ForumUser();
            user.setUsername(adminUsername);
            user.setGender(adminGender);
            user.setDob(adminDob);
            user.setPassword(adminPassword);
            user.setCreated(new Date());

            AccessRightEnum accessRightEnum = ADMIN;
            user.setAccessRight(accessRightEnum);

            userSessionLocal.createUser(user);
            context.addMessage(null, new FacesMessage("Success", "Successfully created administrator account"));
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to create administrator account."));
            throw new NoAccessRightException("You do not have the administrative privileges to create an administrator account.");
        }

        init();
    }

    public void loadOtherUser() {

        FacesContext context = FacesContext.getCurrentInstance();

        try {
            this.otherUser = userSessionLocal.getUser(otherId);
            otherUsername = this.otherUser.getUsername();
            otherGender = this.otherUser.getGender();
            otherDob = this.otherUser.getDob();
            otherCreated = this.otherUser.getCreated();
            otherAccessRight = this.otherUser.getAccessRight();
            otherId = this.otherUser.getId();
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to load user"));
        }

    }

    public void editOtherUserStatus(ActionEvent evt) {

        FacesContext context = FacesContext.getCurrentInstance();
        otherUser.setAccessRight(accessRight);
        System.out.println("The new access right is " + accessRight.toString());

        try {
            userSessionLocal.updateUser(otherUser);
            System.out.println("Successfully edited user status.");
            context.addMessage(null, new FacesMessage("Success", "Successfully edited user status"));

        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to edit user status"));
            return;
        }
        init();

    }

    public void blockUser() {
        FacesContext context = FacesContext.getCurrentInstance();

        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String userIdStr = params.get("userId");
        Long userId = Long.parseLong(userIdStr);
        try {
            userSessionLocal.blockUser(loggedInUser.getId(), userId);
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to block user"));
            return;
        }
        context.addMessage(null, new FacesMessage("Success", "Successfully blocked user"));
        init();
    }

    public void unblockUser() {
        FacesContext context = FacesContext.getCurrentInstance();

        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String userIdStr = params.get("userId");
        Long userId = Long.parseLong(userIdStr);
        try {
            userSessionLocal.unblockUser(loggedInUser.getId(), userId);
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to unblock user"));
            return;
        }
        context.addMessage(null, new FacesMessage("Success", "Successfully unblocked user"));
        init();
    }

    public void promoteUser() throws NoResultException {
        FacesContext context = FacesContext.getCurrentInstance();

        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String userIdStr = params.get("userId");
        Long userId = Long.parseLong(userIdStr);
        try {
            userSessionLocal.promoteUser(loggedInUser.getId(), userId);
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to promote user"));
            return;
        }
        context.addMessage(null, new FacesMessage("Success", "Successfully promoted user"));
        init();
    }

    public void demoteUser() throws NoResultException {
        FacesContext context = FacesContext.getCurrentInstance();

        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String userIdStr = params.get("userId");
        Long userId = Long.parseLong(userIdStr);
        try {
            userSessionLocal.demoteUser(loggedInUser.getId(), userId);
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to demote user"));
            return;
        }
        context.addMessage(null, new FacesMessage("Success", "Successfully demoted admin."));
        init();
    }

    public void createForum(ActionEvent evt) throws NoAccessRightException, NoResultException {

        FacesContext context = FacesContext.getCurrentInstance();

        if (this.loggedInUser.getAccessRight() == ADMIN) {
            Forum newForum = new Forum();
            newForum.setCategory(forumCategory);
            newForum.setDescription(forumDescription);
            this.forum = newForum;

            try {
                forumSessionLocal.addForum(this.loggedInUser.getId(), forum);
                System.out.println("Add forum successful");
                init();
                context.addMessage(null, new FacesMessage("Success", "Successfully created forum"));
            } catch (NoResultException ex) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to create forum."));
                return;
            }
        } else {
            throw new NoAccessRightException("Account credentials invalid.");
        }

    }

    public void checkForumCategory(FacesContext context, UIComponent component, Object value) {

        String category = (String) value;

        List<Forum> forums = forumSessionLocal.searchForums(category);
        if (!forums.isEmpty()) {
            System.out.println("A forum has this category already");
            throw new ValidatorException(new FacesMessage("Forum category already exists. Choose another."));
        }
    }

    public void checkEditedForumCategory(FacesContext context, UIComponent component, Object value) {

        String category = (String) value;

        if (this.forumCategory.equals(category)) {
            System.out.println("Category unchanged.");
            return;
        }

        List<Forum> forums = forumSessionLocal.searchForums(category);
        if (!forums.isEmpty()) {
            System.out.println("A forum has this category already");
            throw new ValidatorException(new FacesMessage("Forum category already exists. Choose another."));
        }
    }

    public void deleteForum() {

        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String forumIdStr = params.get("forumId");
        Long currentForumId = Long.parseLong(forumIdStr);

        try {
            forumSessionLocal.deleteForum(currentForumId);
            context.addMessage(null, new FacesMessage("Success", "Successfully deleted forum"));
            init();
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to delete forum"));
            return;
        }

    }

    public void loadForum() {

        FacesContext context = FacesContext.getCurrentInstance();
        this.redirectUrl = "/secret/viewForum.xhtml?forumId=" + this.forumId;
        this.adminRedirectUrl = "/secret/admin/viewAdminForum.xhtml?forumId=" + this.forumId;

        try {
            this.forum = forumSessionLocal.getForum(forumId);
            forumId = this.forum.getId();
            forumCategory = this.forum.getCategory();
            forumDescription = this.forum.getDescription();
            this.threads = this.forum.getThreads();
            context.addMessage(null, new FacesMessage("Success", "Successfully loaded forum"));
            this.redirectUrl = "/secret/viewForum.xhtml?forumId=" + this.forumId;
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to load forum"));
        }

    }

    public void editForum() throws NoAccessRightException {

        FacesContext context = FacesContext.getCurrentInstance();

        if (this.loggedInUser.getAccessRight() == ADMIN) {
            try {
                this.forum = forumSessionLocal.getForum(forumId);
                this.forum.setCategory(forumCategory);
                this.forum.setDescription(forumDescription);

                this.forumId = this.forum.getId();
                this.forumCategory = this.forum.getCategory();
                this.forumDescription = this.forum.getDescription();

                forumSessionLocal.editForum(this.forum);
                System.out.println("Successfully updated forum.");
                init();
                context.addMessage(null, new FacesMessage("Success", "Successfully edited forum"));
            } catch (Exception e) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to edit forum"));
            }
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to edit forum"));
            throw new NoAccessRightException("You do not have administrative privileges to edit the forum.");
        }

    }

    public void createThread(ActionEvent evt) throws NoResultException {

        FacesContext context = FacesContext.getCurrentInstance();
        Forum currentForum = forumSessionLocal.getForum(forumId);
        System.out.println("The thread is being added to the forum " + currentForum.getCategory());
        ForumUser currentUser = this.loggedInUser;

        Date newDate = new Date();
        ForumThread newThread = new ForumThread();
        newThread.setTitle(threadTitle);
        newThread.setDescription(threadDescription);
        newThread.setCreated(newDate);
        newThread.setUpdated(newDate);
        newThread.setForum(currentForum);
        newThread.setUser(currentUser);
        newThread.setStatus(ACTIVE);
        newThread.setReplies(0);

        this.thread = newThread;

        try {
            threadSessionLocal.addThreadToForum(currentUser.getId(), currentForum.getId(), newThread);
            System.out.println("Add thread successful");
            context.addMessage(null, new FacesMessage("Success", "Successfully created thread"));
            init();
            this.redirectUrl = "/secret/viewForum.xhtml?forumId=" + this.forumId;
        } catch (NoResultException ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to create thread."));
            return;
        }

    }

    public void loadThread() {

        FacesContext context = FacesContext.getCurrentInstance();
        this.redirectUrl = "/secret/viewThread.xhtml?threadId=" + this.threadId;
        this.adminRedirectUrl = "/secret/admin/viewUserThread.xhtml?threadId=" + this.threadId;

        try {
            this.thread = threadSessionLocal.getThread(threadId);
            System.out.println("Current thread is " + threadId + " with title " + thread.getTitle());
            this.threadId = this.thread.getId();
            this.threadDescription = this.thread.getDescription();
            this.threadTitle = this.thread.getTitle();

            this.posts = this.thread.getPosts();
            context.addMessage(null, new FacesMessage("Success", "Successfully loaded thread"));
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to load thread"));
        }

    }

    public void editThread() {

        FacesContext context = FacesContext.getCurrentInstance();

        ForumUser currentUser = this.loggedInUser;

        try {
            this.thread = threadSessionLocal.getThread(threadId);
            if (this.thread.getStatus() == ACTIVE && (this.thread.getUser().getId() == currentUser.getId() || currentUser.getAccessRight() == ADMIN)) {
                System.out.println("Current title is " + this.threadTitle + " with description " + this.threadDescription);
                this.thread.setTitle(threadTitle);
                this.thread.setDescription(threadDescription);

                threadSessionLocal.editThread(this.thread);
                System.out.println("Successfully updated thread.");
                context.addMessage(null, new FacesMessage("Success", "Successfully edited thread"));
                init();
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to edit thread"));
                if (this.thread.getStatus() == ACTIVE) {
                    throw new NoAccessRightException("You do not have the user or administrative privileges to edit the thread.");
                } else {
                    throw new NoAccessRightException("The thread has been closed.");
                }
            }
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to edit thread"));
        }

    }

    public void deleteThread() throws NoResultException, NoAccessRightException {

        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String threadIdStr = params.get("threadId");
        Long currentThreadId = Long.parseLong(threadIdStr);
        ForumThread thread = threadSessionLocal.getThread(currentThreadId);

        if (this.loggedInUser.getAccessRight() == ADMIN || this.loggedInUser.getId() == thread.getUser().getId()) {
            try {
                threadSessionLocal.deleteThread(currentThreadId);
                context.addMessage(null, new FacesMessage("Success", "Successfully deleted thread"));
                init();
            } catch (Exception e) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to delete thread"));
                return;
            }
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to delete thread"));
            throw new NoAccessRightException("Only the thread creator or an administrator can delete this thread.");
        }

    }

    public void closeThread() throws NoResultException, NoAccessRightException {

        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String threadIdStr = params.get("threadId");
        Long currentThreadId = Long.parseLong(threadIdStr);
        ForumThread thread = threadSessionLocal.getThread(currentThreadId);

        if (this.loggedInUser.getAccessRight() == ADMIN && thread.getStatus() == ACTIVE) {
            try {
                threadSessionLocal.closeThread(currentThreadId);
                context.addMessage(null, new FacesMessage("Success", "Successfully closed thread"));
                init();
            } catch (Exception e) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to close thread"));
                return;
            }
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to close thread"));
            if (thread.getStatus() == CLOSED) {
                System.out.println("Thread is already closed");
                return;
            }
            throw new NoAccessRightException("Only an administrator can close this thread.");
        }

    }

    public void openThread() throws NoResultException, NoAccessRightException {

        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String threadIdStr = params.get("threadId");
        Long currentThreadId = Long.parseLong(threadIdStr);
        ForumThread thread = threadSessionLocal.getThread(currentThreadId);

        if (this.loggedInUser.getAccessRight() == ADMIN && thread.getStatus() == CLOSED) {
            try {
                threadSessionLocal.openThread(currentThreadId);
                context.addMessage(null, new FacesMessage("Success", "Successfully opened thread"));
                init();
            } catch (Exception e) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to open thread"));
                return;
            }
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to open thread"));
            if (thread.getStatus() == ACTIVE) {
                System.out.println("Thread is already open");
                return;
            }
            throw new NoAccessRightException("Only an administrator can open this thread.");
        }

    }

    public void createPost(ActionEvent evt) throws NoResultException {

        FacesContext context = FacesContext.getCurrentInstance();
        ForumThread currentThread = forumSessionLocal.getThread(threadId);
        System.out.println("The post is being added to the thread " + currentThread.getTitle());
        ForumUser currentUser = this.loggedInUser;

        if (this.thread.getStatus() != ACTIVE) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to create post. Thread is inactive."));
            return;
        }

        Date newDate = new Date();
        System.out.println("This is fine");

        ForumPost newPost = new ForumPost();
        System.out.println(newPost.toString());
        System.out.println("New post has an id of " + newPost.getId());
        newPost.setContent(postContent);
        newPost.setPosted(newDate);
        newPost.setUpdated(newDate);

        this.post = newPost;

        try {
            postSessionLocal.addPostToThread(currentUser.getId(), currentThread.getId(), newPost);
            System.out.println("Add post successful");
            context.addMessage(null, new FacesMessage("Success", "Successfully created post"));
            init();
        } catch (NoResultException ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to create post."));
            return;
        }

    }

    public void loadPost() {

        FacesContext context = FacesContext.getCurrentInstance();

        this.redirectUrl = "/secret/viewThread.xhtml?threadId=" + this.threadId;
        this.adminRedirectUrl = "/secret/admin/viewUserThread.xhtml?threadId=" + this.threadId;

        try {
            this.post = postSessionLocal.getPost(postId);
            System.out.println("Current post is " + postId);
            this.postId = this.post.getId();
            this.postContent = this.post.getContent();

            context.addMessage(null, new FacesMessage("Success", "Successfully loaded post"));
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to load post"));
        }

    }

    public void editPost() {

        FacesContext context = FacesContext.getCurrentInstance();

        ForumUser currentUser = this.loggedInUser;

        System.out.println("The current user is " + currentUser);
        this.redirectUrl = "/secret/viewThread.xhtml?threadId=" + this.threadId;

        try {
            this.post = postSessionLocal.getPost(postId);
            if (this.thread.getStatus() == ACTIVE && (this.post.getUser().getId() == currentUser.getId() || currentUser.getAccessRight() == ADMIN)) {
                this.post.setContent(postContent);

                postSessionLocal.editPost(this.post);
                System.out.println("Successfully updated post.");
                context.addMessage(null, new FacesMessage("Success", "Successfully edited post"));
                init();
                this.redirectUrl = "/secret/viewThread.xhtml?threadId=" + this.threadId;
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to edit post"));
                if (this.thread.getStatus() == ACTIVE) {
                    throw new NoAccessRightException("You do not have the user or administrative privileges to edit the post.");
                } else {
                    throw new NoAccessRightException("The thread has been closed. No posts can be edited.");
                }
            }
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to edit post"));
        }

    }

    public void deletePost() throws NoResultException, NoAccessRightException {

        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String postIdStr = params.get("postId");
        System.out.println(postIdStr);
        Long currentPostId = Long.parseLong(postIdStr);
        ForumPost post = postSessionLocal.getPost(currentPostId);
        ForumUser currentUser = this.loggedInUser;

        if (this.thread.getStatus() == ACTIVE && (post.getUser().getId() == currentUser.getId() || currentUser.getAccessRight() == ADMIN)) {
            try {
                postSessionLocal.deletePost(currentPostId);
                context.addMessage(null, new FacesMessage("Success", "Successfully deleted post"));
                init();
            } catch (Exception e) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to delete post"));
                return;
            }
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to delete post"));
            throw new NoAccessRightException("Only the thread creator or an administrator can delete this post.");
        }

    }

    public ForumUser getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(ForumUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public byte getGender() {
        return gender;
    }

    public void setGender(byte gender) {
        this.gender = gender;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public AccessRightEnum getAccessRight() {
        return accessRight;
    }

    public void setAccessRight(AccessRightEnum accessRight) {
        this.accessRight = accessRight;
    }

    public List<ForumUser> getUsers() {
        return users;
    }

    public void setUsers(List<ForumUser> users) {
        this.users = users;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public byte getAdminGender() {
        return adminGender;
    }

    public void setAdminGender(byte adminGender) {
        this.adminGender = adminGender;
    }

    public Date getAdminDob() {
        return adminDob;
    }

    public void setAdminDob(Date adminDob) {
        this.adminDob = adminDob;
    }

    public Date getAdminCreated() {
        return adminCreated;
    }

    public void setAdminCreated(Date adminCreated) {
        this.adminCreated = adminCreated;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public AccessRightEnum getAdminAccessRight() {
        return adminAccessRight;
    }

    public void setAdminAccessRight(AccessRightEnum adminAccessRight) {
        this.adminAccessRight = adminAccessRight;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public ForumUser getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(ForumUser otherUser) {
        this.otherUser = otherUser;
    }

    public String getOtherUsername() {
        return otherUsername;
    }

    public void setOtherUsername(String otherUsername) {
        this.otherUsername = otherUsername;
    }

    public byte getOtherGender() {
        return otherGender;
    }

    public void setOtherGender(byte otherGender) {
        this.otherGender = otherGender;
    }

    public Date getOtherDob() {
        return otherDob;
    }

    public void setOtherDob(Date otherDob) {
        this.otherDob = otherDob;
    }

    public Date getOtherCreated() {
        return otherCreated;
    }

    public void setOtherCreated(Date otherCreated) {
        this.otherCreated = otherCreated;
    }

    public String getOtherPassword() {
        return otherPassword;
    }

    public void setOtherPassword(String otherPassword) {
        this.otherPassword = otherPassword;
    }

    public AccessRightEnum getOtherAccessRight() {
        return otherAccessRight;
    }

    public void setOtherAccessRight(AccessRightEnum otherAccessRight) {
        this.otherAccessRight = otherAccessRight;
    }

    public Long getOtherId() {
        return otherId;
    }

    public void setOtherId(Long otherId) {
        this.otherId = otherId;
    }

    public Long getForumId() {
        return forumId;
    }

    public void setForumId(Long forumId) {
        this.forumId = forumId;
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }

    public ForumThread getThread() {
        return thread;
    }

    public void setThread(ForumThread thread) {
        this.thread = thread;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public ForumPost getPost() {
        return post;
    }

    public void setPost(ForumPost post) {
        this.post = post;
    }

    public List<Forum> getForums() {
        return forums;
    }

    public void setForums(List<Forum> forums) {
        this.forums = forums;
    }

    public List<ForumThread> getThreads() {
        return threads;
    }

    public void setThreads(List<ForumThread> threads) {
        this.threads = threads;
    }

    public List<ForumPost> getPosts() {
        return posts;
    }

    public void setPosts(List<ForumPost> posts) {
        this.posts = posts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getForumCategory() {
        return forumCategory;
    }

    public void setForumCategory(String forumCategory) {
        this.forumCategory = forumCategory;
    }

    public String getForumDescription() {
        return forumDescription;
    }

    public void setForumDescription(String forumDescription) {
        this.forumDescription = forumDescription;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getThreadTitle() {
        return threadTitle;
    }

    public void setThreadTitle(String threadTitle) {
        this.threadTitle = threadTitle;
    }

    public String getThreadDescription() {
        return threadDescription;
    }

    public void setThreadDescription(String threadDescription) {
        this.threadDescription = threadDescription;
    }

    public ThreadStatus getThreadStatus() {
        return threadStatus;
    }

    public void setThreadStatus(ThreadStatus threadStatus) {
        this.threadStatus = threadStatus;
    }

    public Long getLoggedInAdminId() {
        return loggedInAdminId;
    }

    public void setLoggedInAdminId(Long loggedInAdminId) {
        this.loggedInAdminId = loggedInAdminId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getAdminRedirectUrl() {
        return adminRedirectUrl;
    }

    public void setAdminRedirectUrl(String adminRedirectUrl) {
        this.adminRedirectUrl = adminRedirectUrl;
    }

}
