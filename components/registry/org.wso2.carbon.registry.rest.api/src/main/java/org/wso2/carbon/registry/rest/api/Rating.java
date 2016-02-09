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
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.RatingModel;
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class is to handle the rating related REST verbs GET,DELETE.
 */
@Path("/rating")
@Api(value = "/rating",
     description = "Rest api for operating on a  rating a resource",
     produces = MediaType.APPLICATION_JSON)
public class Rating extends RegistryRestSuper {

    private Log log = LogFactory.getLog(Rating.class);

    /**
     * This method get the rating given for the requested resource.
     *
     * @param resourcePath - Path of the resource in the registry.
     * @return Response RatingModel object. HTTP 200 OK.
     */
    @GET
    @Produces("application/json")
    @ApiOperation(value = "Get user's rating and overall rating on a resource",
                  httpMethod = "GET",
                  notes = "Fetch user's rating and overall rating on a resource",
                  response = RatingModel.class)
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "Found user's rating and overall rating and returned in body"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Given specific resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response getRating(@QueryParam("path") String resourcePath,
                              @HeaderParam("X-JWT-Assertion") String JWTToken) {

        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext
                (PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Response response;
        try {
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(resourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + resourcePath).build();
            }
            int userRating = registry.getRating(resourcePath, authContext.getUserName());
            float avgRating = registry.getAverageRating(resourcePath);
            RatingModel result = new RatingModel(userRating, avgRating);
            return Response.ok().entity(result).build();

        } catch (RegistryException e) {
            log.error("User does not have permission to read the rating of the resource", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * this method delete the user's rating on the given resource
     *
     * @param resourcePath - path of the resource
     * @return Response     - HTTP 204 No Content
     */
    @DELETE
    @Produces("application/json")
    @ApiOperation(value = "Delete the user's rating on the given resource",
                  httpMethod = "DELETE",
                  notes = "Delete the user's rating on the given resource")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "User's rating deleted successfully"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Specified resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response removeRating(@QueryParam("path") String resourcePath,
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
            // set the user specific rating to 0
            registry.rateResource(resourcePath, 0);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (RegistryException e) {
            log.error("Failed to remove rating on  resource " + resourcePath, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
