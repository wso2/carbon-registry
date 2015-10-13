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
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class is to handle the resource revision related to REST verbs GET,POST and DELETE.
 */

@Path("/revision")
@Api(value = "/revision",
     description = "Rest api for doing operations on a resource revision",
     produces = MediaType.APPLICATION_JSON)
public class Revision extends RegistryRestSuper {

    private Log log = LogFactory.getLog(Revision.class);

    /**
     * This method get a revision on the requested resource(using revision id).
     *
     * @param path - Path of the resource in the registry
     * @return Response - resource
     */
    @GET
    @Produces("application/octet-stream")
    @ApiOperation(value = "Get content of a resource revision",
                  httpMethod = "GET",
                  notes = "Fetch content of a resource")//TODO add return type based on resource or collection
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Found the revisioned resource content and returned in body"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Given specific resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response getRevision(@QueryParam("path") String path,
                                @QueryParam("id") long revisionId,
                                @HeaderParam("X-JWT-Assertion") String JWTToken) {

        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext
                (PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(path)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + path).build();
            }
            String revisionPath = getRevisionPath(path, revisionId);
            Resource resource = registry.get(revisionPath);
            if (resource instanceof Collection) {
                //If it is a collection , return the versioned paths.
                Collection versionCollection = (Collection) resource;
                String[] versionPaths = versionCollection.getChildren();
                return Response.ok().entity(versionPaths).type("application/json").build();
            }
            return Response.ok().entity(resource.getContent()).type(resource.getMediaType()).build();

        } catch (RegistryException e) {
            log.error("Failed to get version " + revisionId + "of resource " + path, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * This method is to create a revision of a given resource.
     *
     * @param path - Path of the resource in the registry
     */
    @POST
    @Produces("application/json")
    @ApiOperation(value = "Create a resource revision",
                  httpMethod = "POST",
                  notes = "Create a resource revision")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Resource revision created successfully"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Specified resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response createRevision(@QueryParam("path") String path,
                                   @HeaderParam("X-JWT-Assertion") String JWTToken) {

        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext
                (PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(path)) {
                return Response.status(Response.Status.NOT_FOUND).entity(RestAPIConstants.RESOURCE_NOT_FOUND).build();
            }
            registry.createVersion(path);
            return Response.status(Response.Status.NO_CONTENT).build();

        } catch (RegistryException e) {
            log.error("Failed to delete version ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * This method is to delete a given revision (using revision id).
     *
     * @param path path of the resource in the registry
     */
    @DELETE
    @Produces("application/json")
    @ApiOperation(value = "Delete a resource revision",
                  httpMethod = "DELETE",
                  notes = "Delete a resource revision")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Resource revision deleted successfully"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Specified resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response deleteRevision(@QueryParam("path") String path,
                                   @QueryParam("id") long versionID,
                                   @HeaderParam("X-JWT-Assertion") String JWTToken) {

        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext
                (PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String versionPath = getRevisionPath(path, versionID);

        try {
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(versionPath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(RestAPIConstants.RESOURCE_NOT_FOUND).build();
            }
            registry.removeVersionHistory(path, versionID);
            return Response.status(Response.Status.NO_CONTENT).build();

        } catch (RegistryException e) {
            log.error("Failed to delete version ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    private String getRevisionPath(String resourcePath, long versionID) {
        /* /_system/governance/test4;version:3 */
        return resourcePath + ";version:" + versionID;
    }
}
