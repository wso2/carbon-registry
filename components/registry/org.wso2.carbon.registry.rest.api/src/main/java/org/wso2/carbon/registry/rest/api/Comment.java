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

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.CommentModel;
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;

/**
 * This class is to handle REST verbs GET , PSST and DELETE.
 */
@Path("/comment")
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
