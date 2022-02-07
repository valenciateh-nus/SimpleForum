/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webservices.restful;

import entity.Forum;
import entity.ForumPost;
import entity.ForumThread;
import entity.ForumUser;
import error.NoAccessRightException;
import error.NoResultException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.enterprise.context.RequestScoped;
import javax.faces.view.ViewScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import session.ForumSessionLocal;
import session.ThreadSessionLocal;
import session.UserSessionLocal;
import util.enumeration.AccessRightEnum;
import static util.enumeration.AccessRightEnum.ADMIN;

/**
 * REST Web Service
 *
 * @author valenciateh
 */
@Path("forums")
@RequestScoped
public class ForumsResource {

    @EJB
    private ForumSessionLocal forumSessionLocal;
    @EJB
    private UserSessionLocal userSessionLocal;
    @EJB
    private ThreadSessionLocal threadSessionLocal;

    /**
     * Creates a new instance of ForumsResource
     */
    public ForumsResource() {
    }

    @POST
    @Path("/user/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Forum createForum(@PathParam("id") Long uId, Forum f) throws NoResultException, NoAccessRightException {

        forumSessionLocal.addForum(uId, f);
        f.setUser(null);
        f.setThreads(null);
        return f;

    } //end createCustomer

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Forum> getForums() {

        List<Forum> forums = forumSessionLocal.searchForums(null);

        for (Forum f : forums) {
            f.setUser(null);
            f.setThreads(null);
            /*
            List<ForumThread> forumThreads = f.getThreads();
            for (ForumThread t: forumThreads) {
                t.setForum(null);
                t.setUser(null);
            }
             */

        }
        return forums;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getForum(@PathParam("id") Long fId) {
        try {
            Forum f = forumSessionLocal.getForum(fId);
            f.setUser(null);
            f.setThreads(null);
            return Response.status(200).entity(f).type(MediaType.APPLICATION_JSON).build();
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "Not found")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        }
    } //end getForum

    @PUT
    @Path("/{id}/users/{user_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editForum(@PathParam("id") Long fId, @PathParam("user_id") Long uId, Forum f) {

        f.setId(fId);
        try {
            AccessRightEnum accessRight = userSessionLocal.getUser(uId).getAccessRight();
            if (accessRight == ADMIN) {
                f.setUser(userSessionLocal.getUser(uId));
                forumSessionLocal.editForum(f);
                return Response.status(204).build();
            } else {
                JsonObject exception = Json.createObjectBuilder()
                        .add("error", "No access right to edit forum")
                        .build();

                return Response.status(404).entity(exception)
                        .type(MediaType.APPLICATION_JSON).build();
            }
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "Not found")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        } catch (NoAccessRightException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "No access right to edit forum")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        }
    } //end editForum

    @DELETE
    @Path("/{id}/users/{user_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteForum(@PathParam("id") Long fId, @PathParam("user_id") Long uId) {
        try {
            AccessRightEnum accessRight = userSessionLocal.getUser(uId).getAccessRight();
            if (accessRight == ADMIN) {
                Forum forum = forumSessionLocal.getForum(fId);
                List<ForumThread> threads = forum.getThreads();
                for (ForumThread t : threads) {
                    t.setForum(null);
                    t.setUser(null);
                }
                forum.setThreads(null);
                forumSessionLocal.deleteForum(fId);
                return Response.status(204).build();
            } else {
                JsonObject exception = Json.createObjectBuilder()
                        .add("error", "No access right to delete forum")
                        .build();

                return Response.status(404).entity(exception)
                        .type(MediaType.APPLICATION_JSON).build();
            }
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "Customer or field not found")
                    .build();

            return Response.status(404).entity(exception).build();
        } catch (NoAccessRightException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "No access right to delete forum")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        }
    } //end deleteForum

    @GET
    @Path("/{id}/threads")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ForumThread> getForumThreads(@PathParam("id") Long fId) throws NoResultException {
        try {
            Forum f = forumSessionLocal.getForum(fId);
            List<ForumThread> threads = f.getThreads();
            for (ForumThread t : threads) {
                t.setForum(null);
                t.setPosts(null);
                t.setUser(null);
            }
            f.setUser(null);
            f.setThreads(null);
            return threads;
        } catch (NoResultException e) {
            throw e;
        }
    }

    @GET
    @Path("/{id}/threads/{tId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ForumThread getForumThread(@PathParam("id") Long fId, @PathParam("tId") Long tId) throws NoResultException {

        ForumThread th = null;
        try {
            Forum f = forumSessionLocal.getForum(fId);
            List<ForumThread> threads = f.getThreads();

            for (ForumThread t : threads) {
                if (t.getId() == tId) {
                    th = t;
                    t.setForum(null);
                    t.setPosts(null);
                    t.setUser(null);
                    break;
                }
            }
            f.setUser(null);
            f.setThreads(null);
            return th;
        } catch (NoResultException e) {
            throw e;
        }
    }

    /*
    @PUT
    @Path("/{id}/threads/{tId}/users/{user_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editThread(@PathParam("id") Long fId, @PathParam("tId") Long tId, @PathParam("user_id") Long uId, ForumThread newThread) throws NoResultException, NoAccessRightException {

        if (userSessionLocal.getUser(uId).getId() == threadCreatorId || userSessionLocal.getUser(uId).getAccessRight() == ADMIN) {
            try {
                newThread.setId(tId);
                newThread.setUpdated(new Date());
                th.setUser(userSessionLocal.getUser(uId));
                threadSessionLocal.editThread(th);
                return Response.status(204).build();
            } catch (NoResultException e) {
                JsonObject exception = Json.createObjectBuilder()
                        .add("error", "Not found")
                        .build();

                return Response.status(404).entity(exception)
                        .type(MediaType.APPLICATION_JSON).build();
            }
        } else {
            throw new NoAccessRightException("You do not have the administrative/user privileges to edit this thread.");
        }
    }
     */
    @GET
    @Path("/{id}/threads/{tId}/posts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ForumPost> getForumPosts(@PathParam("id") Long fId, @PathParam("tId") Long tId) throws NoResultException {

        List<ForumPost> p = null;
        ForumThread th = null;
        try {
            Forum f = forumSessionLocal.getForum(fId);
            List<ForumThread> threads = f.getThreads();
            for (ForumThread t : threads) {
                if (t.getId() == tId) {
                    th = t;
                    break;
                }
            }

            System.out.println("Current thread is " + th.getId());
            System.out.println("Current thread has " + th.getPosts().size());
            p = th.getPosts();
            for (ForumPost po : p) {
                po.setUser(null);
                po.setThread(null);
            }
            return p;
        } catch (NoResultException e) {
            throw e;
        }
    }
}
