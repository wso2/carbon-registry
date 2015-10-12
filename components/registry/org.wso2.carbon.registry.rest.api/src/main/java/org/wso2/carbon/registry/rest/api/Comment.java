/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.rest.api;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.CommentModel;
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class is to handle REST verbs GET , PSST and DELETE.
 */
@Path("/comment")
@Api(value = "/comment",
     description = "Rest api for doing operations on a single comment",
     produces = MediaType.APPLICATION_JSON)
//@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class Comment extends RegistryRestSuper {

    private Log log = LogFactory.getLog(Comment.class);

    /**
     * This method get a specific comment of the given resource
     *
     * @param resourcePath - Registry path of the resource.
     * @param commentId    - Comment id.
     * @return CommentModel JSON object. HTTP 200 OK.
     */
    @GET
    @Produces("application/json")
    @ApiOperation(value = "Get specific comment",
                  httpMethod = "GET",
                  notes = "Fetch details about a specific comment",
                  response = CommentModel.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "Authorization",
                                         value = "Header to provide basic authentication value",
                                         dataType = "string",
                                         paramType = "header") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Found the specific comment and returned in body"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Given specific comment not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response getComment(@QueryParam("path") String resourcePath,
                               @QueryParam("id") long commentId,
                               @HeaderParam("X-JWT-Assertion") String JWTToken) {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext(carbonContext, JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(resourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + resourcePath).build();
            }
            // get all the comments on a resource
            org.wso2.carbon.registry.core.Comment[] result = registry.getComments(resourcePath);
            String commentPath = resourcePath + ";comments:" + commentId;
            CommentModel message = null;
            int size = result.length;
            for (int i = size - 1; i >= 0; i--) {
                String path = result[i].getCommentPath();
                if (path.equals(commentPath)) {
                    message = new CommentModel(result[i]);
                    break;
                }
            }
            if (message == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(message).build();
        } catch (RegistryException e) {
            log.error("user is not allowed to get a specific comment on a resource", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * This method to add comment for given resource.
     *
     * @param resourcePath - Resource path.
     * @param commentText  - Comment to be added.
     * @return CommentModel object. HTTP 204 No Content.
     */
    @POST
    @Produces("application/json")
    @ApiOperation(value = "Add a comment to a resource",
                  httpMethod = "POST",
                  notes = "Add a comment to a resource")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Comment added successfully"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Specified resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response addComment(@QueryParam("path") String resourcePath,
                               String commentText,
                               @HeaderParam("X-JWT-Assertion") String JWTToken) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext(carbonContext, JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            // check for the existence of the resource
            if (!registry.resourceExists(resourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + resourcePath).build();
            }
            registry.addComment(resourcePath, new org.wso2.carbon.registry.core.Comment(commentText));
            return Response.status(Response.Status.NO_CONTENT).build();

        } catch (RegistryException e) {
            log.error("Failed to edit comment on resource " + resourcePath, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * This method to update the given comment  (using comment id).
     *
     * @param resourcePath - resource path
     * @param commentId    - id of the specific comment
     * @param commentText  - comment to be added
     * @return CommentModel object. HTTP 204 No Content.
     */
    @PUT
    @Produces("application/json")
    @ApiOperation(value = "Update an already added comment",
                  httpMethod = "PUT",
                  notes = "Update an already added comment")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Comment updated successfully"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Specified resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response editComment(@QueryParam("path") String resourcePath,
                                @QueryParam("id") long commentId, String commentText,
                                @HeaderParam("X-JWT-Assertion") String JWTToken) {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext(carbonContext, JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            // check for the existence of the resource
            if (!registry.resourceExists(resourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + resourcePath).build();
            }
            String commentPath = resourcePath + ";comments:" + commentId;
            registry.editComment(commentPath, commentText);
            return Response.status(Response.Status.NO_CONTENT).build();

        } catch (RegistryException e) {
            log.error("Failed to edit comment on resource " + resourcePath, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * This method deletes the specific comment(using id) on the given resource
     *
     * @param resourcePath - registry path of the resource
     * @param commentId    - ID of the comment to be deleted
     * @return Response , HTTP 204 No Content.
     */
    @DELETE
    @Produces("application/json")
    @ApiOperation(value = "Delete a comment",
                  httpMethod = "DELETE",
                  notes = "Delete a comment")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Comment deleted successfully"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Specified resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response deleteComment(@QueryParam("path") String resourcePath,
                                  @QueryParam("id") long commentId,
                                  @HeaderParam("X-JWT-Assertion") String JWTToken) {
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext
                (PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(resourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + resourcePath).build();
            }
            String commentPath = resourcePath + ";comments:" + commentId;
            registry.removeComment(commentPath);
            return Response.status(Response.Status.NO_CONTENT).build();

        } catch (RegistryException e) {
            log.error("user is not allowed to delete the specified comment on a resource", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
