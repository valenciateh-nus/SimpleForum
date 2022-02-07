/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webservices.restful;

import entity.ForumPost;
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
import session.PostSessionLocal;
import session.UserSessionLocal;
import util.enumeration.AccessRightEnum;
import static util.enumeration.AccessRightEnum.ADMIN;

/**
 * REST Web Service
 *
 * @author valenciateh
 */
@Path("posts")
@RequestScoped
public class PostsResource {

    @EJB
    private PostSessionLocal postSessionLocal;
    @EJB
    private UserSessionLocal userSessionLocal;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ForumPost> getPosts() {

        List<ForumPost> posts = postSessionLocal.searchPosts(null);
        for (ForumPost p : posts) {
            p.setUser(null);
            p.setThread(null);
        }
        return posts;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getThread(@PathParam("id") Long pId) {
        try {
            ForumPost p = postSessionLocal.getPost(pId);
            p.setUser(null);
            p.setThread(null);

            return Response.status(200).entity(p).type(MediaType.APPLICATION_JSON).build();
        } catch (NoResultException e) {
            JsonObject exception = Json.createObjectBuilder()
                    .add("error", "Not found")
                    .build();

            return Response.status(404).entity(exception)
                    .type(MediaType.APPLICATION_JSON).build();
        }
    } //end getThread

    @DELETE
    @Path("/{id}/users/{user_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePost(@PathParam("id") Long pId, @PathParam("user_id") Long uId) {
        try {
            AccessRightEnum accessRight = userSessionLocal.getUser(uId).getAccessRight();
            if (accessRight == ADMIN || postSessionLocal.getPost(pId).getUser().getId() == uId ) {
                ForumPost post = postSessionLocal.getPost(pId);
                post.setThread(null);
                post.setUser(null);
                postSessionLocal.deletePost(pId);
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
