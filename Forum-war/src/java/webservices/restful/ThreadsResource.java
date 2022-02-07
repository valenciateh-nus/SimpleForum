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
import java.util.List;
import javax.ejb.EJB;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.enterprise.context.RequestScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.DELETE;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import session.ThreadSessionLocal;
import session.UserSessionLocal;
import util.enumeration.AccessRightEnum;
import static util.enumeration.AccessRightEnum.ADMIN;

/**
 * REST Web Service
 *
 * @author valenciateh
 */
@Path("threads")
@RequestScoped
public class ThreadsResource {

    @EJB
    private ThreadSessionLocal threadSessionLocal;
    @EJB
    private UserSessionLocal userSessionLocal;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ForumThread> getThreads() {

        List<ForumThread> threads = threadSessionLocal.searchThreads(null);
        for (ForumThread t : threads) {
            t.setUser(null);
            t.setForum(null);
            t.setPosts(null);
        }
        return threads;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getThread(@PathParam("id") Long tId) {
        try {
            ForumThread t = threadSessionLocal.getThread(tId);
            t.setUser(null);
            t.setForum(null);
            t.setPosts(null);
            return Response.status(200).entity(t).type(MediaType.APPLICATION_JSON).build();
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "Not found")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        }
    } //end getThread

    @GET
    @Path("/{id}/posts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ForumPost> getThreadPosts(@PathParam("id") Long tId) throws NoResultException {

        try {
            ForumThread t = threadSessionLocal.getThread(tId);
            List<ForumPost> posts = t.getPosts();
            for (ForumPost p : posts) {
                p.setThread(null);
                p.setUser(null);
            }
            t.setUser(null);
            t.setPosts(null);
            return posts;
        } catch (NoResultException e) {
            throw e;
        }
    }

    @PUT
    @Path("/{id}/close")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response closeThread(@PathParam("id") Long tId) throws NoAccessRightException {

        try {
            ForumThread thread = threadSessionLocal.getThread(tId);
            threadSessionLocal.closeThread(tId);
            return Response.status(204).build();

        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "Thread not found")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        }
    }
    
    @PUT
    @Path("/{id}/open")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response openThread(@PathParam("id") Long tId) throws NoAccessRightException {

        try {
            ForumThread thread = threadSessionLocal.getThread(tId);
            threadSessionLocal.openThread(tId);
            return Response.status(204).build();

        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "Thread not found")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        }
    }

    @PUT
    @Path("/{id}/users/{user_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editThread(@PathParam("id") Long tId, @PathParam("user_id") Long uId, ForumThread t) throws NoAccessRightException {

        t.setId(tId);
        System.out.println("Current user id is " + uId);
        try {
            System.out.println(threadSessionLocal.getThread(tId).getUser().getId() + " this is the ID");
            if (threadSessionLocal.getThread(tId).getUser().getId() == uId || userSessionLocal.getUser(uId).getAccessRight() == ADMIN) {
                Forum forum = threadSessionLocal.getThread(tId).getForum();
                System.out.println("The forum in this is " + forum);
                t.setForum(forum);
                t.setUser(threadSessionLocal.getThread(tId).getUser());
                threadSessionLocal.editThread(t);
                //t.setPosts(null);
                //t.setUser(null);
                //t.setForum(null);
                return Response.status(204).build();
            } else {
                throw new NoAccessRightException("You do not have the administrative/user privileges to edit this thread!");
            }
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "Not found")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        }
    } //end editThread

    @DELETE
    @Path("/{id}/users/{user_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteThread(@PathParam("id") Long tId, @PathParam("user_id") Long uId) {
        try {
            AccessRightEnum accessRight = userSessionLocal.getUser(uId).getAccessRight();
            if (accessRight == ADMIN || threadSessionLocal.getThread(tId).getUser().getId() == uId ) {
                ForumThread thread = threadSessionLocal.getThread(tId);
                List<ForumPost> posts = thread.getPosts();
                for (ForumPost p : posts) {
                    p.setThread(null);
                    p.setUser(null);
                }
                thread.setPosts(null);
                threadSessionLocal.deleteThread(tId);
                return Response.status(204).build();
            } else {
                JsonObject exception = Json.createObjectBuilder()
                        .add("error", "No access right to delete thread")
                        .build();

                return Response.status(404).entity(exception)
                        .type(MediaType.APPLICATION_JSON).build();
            }
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "User not found")
                    .build();

            return Response.status(404).entity(exception).build();
        }
    } //end deleteThread

}
