/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webservices.restful;

import entity.Forum;
import entity.ForumPost;
import entity.ForumUser;
import error.NoAccessRightException;
import error.NoResultException;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.enterprise.context.RequestScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import session.UserSessionLocal;
import util.enumeration.AccessRightEnum;
import static util.enumeration.AccessRightEnum.ADMIN;

/**
 * REST Web Service
 *
 * @author valenciateh
 */
@Path("users")
@RequestScoped
public class UsersResource {

    @EJB
    private UserSessionLocal userSessionLocal;

    /**
     * Creates a new instance of ForumUsersResource
     */
    public UsersResource() {
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ForumUser createUser(ForumUser u) {
        u.setCreated(new Date());
        u.setAccessRight(AccessRightEnum.NORMAL_USER);
        userSessionLocal.createUser(u);
        return u;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ForumUser> getUsers() {
        //u.setCreated(new Date());
        List<ForumUser> users = userSessionLocal.searchUsers(null);

        for (ForumUser u : users) {
            u.setForums(null);
            u.setThreads(null);
            u.setPosts(null);
        }
        return users;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("id") Long uId) {
        try {
            ForumUser u = userSessionLocal.getUser(uId);
            u.setForums(null);
            u.setThreads(null);
            u.setPosts(null);
            return Response.status(200).entity(u).type(MediaType.APPLICATION_JSON).build();
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "Not found")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        }
    } //end getUser

    @GET
    @Path("/{id}/posts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ForumPost> getUserPosts(@PathParam("id") Long uId) throws NoResultException {

        try {
            ForumUser u = userSessionLocal.getUser(uId);
            List<ForumPost> userPosts = u.getPosts();
            for (ForumPost p: userPosts) {
                p.setThread(null);
                p.setUser(null);
            }
            u.setPosts(null);
            u.setThreads(null);
            return userPosts;
        } catch (NoResultException e) {
            throw e;
        }

    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editUser(@PathParam("id") Long uId, ForumUser u) {
        u.setId(uId);
        try {
            userSessionLocal.updateUser(u);
            return Response.status(204).build();
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "Not found")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        }
    } //end editUser -- iffy: viewAllUsers doesn't get updated

    @PUT
    @Path("/{id}/block/{aId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response blockUser(@PathParam("id") Long uId, @PathParam("aId") Long aId) throws NoAccessRightException {

        try {
            System.out.println("Current admin id: " + aId + " user ID: " + uId);
            ForumUser admin = userSessionLocal.getUser(aId);
            System.out.println("Admin is " + admin);
            if (admin.getAccessRight() == ADMIN) {
                userSessionLocal.blockUser(aId, uId);
                return Response.status(204).build();
            } else {
                throw new NoAccessRightException("You do not have the administrative privileges to block this user.");
            }
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "User not found")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        }
    }

    @PUT
    @Path("/{id}/unblock/{aId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response unblockUser(@PathParam("id") Long uId, @PathParam("aId") Long aId) throws NoAccessRightException {

        try {
            System.out.println("Current admin id: " + aId + " user ID: " + uId);
            ForumUser admin = userSessionLocal.getUser(aId);
            System.out.println("Admin is " + admin);
            if (admin.getAccessRight() == ADMIN) {
                userSessionLocal.unblockUser(aId, uId);
                return Response.status(204).build();
            } else {
                throw new NoAccessRightException("You do not have the administrative privileges to block this user.");
            }
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "User not found")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        }
    }
    
    @PUT
    @Path("/{id}/promote/{aId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response promoteUser(@PathParam("id") Long uId, @PathParam("aId") Long aId) throws NoAccessRightException {

        try {
            System.out.println("Current admin id: " + aId + " user ID: " + uId);
            ForumUser admin = userSessionLocal.getUser(aId);
            System.out.println("Admin is " + admin);
            if (admin.getAccessRight() == ADMIN) {
                userSessionLocal.promoteUser(aId, uId);
                return Response.status(204).build();
            } else {
                throw new NoAccessRightException("You do not have the administrative privileges to promote this user.");
            }
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "User not found")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        }
    }
    
    @PUT
    @Path("/{id}/demote/{aId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response demoteUser(@PathParam("id") Long uId, @PathParam("aId") Long aId) throws NoAccessRightException {

        try {
            System.out.println("Current admin id: " + aId + " user ID: " + uId);
            ForumUser admin = userSessionLocal.getUser(aId);
            System.out.println("Admin is " + admin);
            if (admin.getAccessRight() == ADMIN) {
                userSessionLocal.demoteUser(aId, uId);
                return Response.status(204).build();
            } else {
                throw new NoAccessRightException("You do not have the administrative privileges to promote this user.");
            }
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "User not found")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        }
    }

}
