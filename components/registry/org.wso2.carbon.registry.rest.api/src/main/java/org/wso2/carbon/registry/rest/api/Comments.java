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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.CommentModel;
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * This class retrieves the comments of the requested resource.
 */
@Path("/comments")
public class Comments extends PaginationCalculation<Comment> {

    private Log log = LogFactory.getLog(Comments.class);

    /**
     * This method get the comments on the requested resource based on the pagination properties.
     *
     * @param resourcePath - Path of the resource in the registry.
     * @param start        - Starting page number.
     * @param size         - Number of records to be retrieved
     * @return array of CommentModel objects, HTTP 200 OK.
     */
    @GET
    @Produces("application/json")
    public Response getComments(@QueryParam("path") String resourcePath,
                                @QueryParam("start") int start,
                                @QueryParam("size") int size,
                                @HeaderParam("X-JWT-Assertion") String JWTToken) {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext(carbonContext, JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if (!ValidationUtils.validatePagination(start, size)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        Comment[] comments = new Comment[0];
        try {
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(resourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + resourcePath).build();

            }
            comments = registry.getComments(resourcePath);

        } catch (RegistryException e) {
            log.error("Failed to get comments of the resource " + resourcePath, e);

        }
        return getPaginatedResults(comments, start, size, "", "");
    }

    @Override
    protected Response getPaginatedResults(Comment[] comments, int start, int size, String sortBy, String sortOrder) {

        Comment[] paginatedComments;
        List<CommentModel> commentModels = new ArrayList<CommentModel>();

        if (start == 0 && size == 0) {
            for (Comment comment : comments) {
                commentModels.add(new CommentModel(comment));
            }
            return Response.ok(commentModels.toArray(new CommentModel[commentModels.size()])).build();
        }
        if (comments.length < start) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (comments.length < size + start) {
            paginatedComments = new Comment[comments.length - start];
            System.arraycopy(comments, start, paginatedComments, 0, (comments.length - start));
        } else {
            paginatedComments = new Comment[size];
            System.arraycopy(comments, start, paginatedComments, 0, size);
        }
        for (Comment comment : paginatedComments) {
            commentModels.add(new CommentModel(comment));
        }
        return Response.ok(commentModels.toArray(new CommentModel[commentModels.size()])).build();
    }
}
