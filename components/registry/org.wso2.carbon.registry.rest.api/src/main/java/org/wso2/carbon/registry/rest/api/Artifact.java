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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

/**
 * This class is to handle the resources/collections according to the REST verbs GET,PUT and DELETE.
 */

@Path("/artifact")
@Api(value = "/artifact",
     description = "Rest api for doing operations on a resource",
     produces = MediaType.APPLICATION_JSON)
public class Artifact extends RegistryRestSuper {

    private Log log = LogFactory.getLog(Artifact.class);
    private static final String COLLECTION_MEDIA_TYPE = "application/atomcoll+xml";

    /**
     * This method get the resource content of the requested resource.
     * If the path is collection it returns the paths inside that collection.
     * If resource it return the content of the resource.
     *
     * @param path - Path of the resource/collection in the registry.
     * @return Response, if resource content stream, else resource paths array.HTTP 200 OK.
     */
    @GET
    @Path("/{path:.*}")
    @Produces("application/octet-stream")
    @ApiOperation(value = "Get content of a resource",
                  httpMethod = "GET",
                  notes = "Fetch content of a resource")//TODO add return type based on resource or collection
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Found the resource content and returned in body"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Given specific resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response getResource(@PathParam("path") List<PathSegment> path, @HeaderParam("X-JWT-Assertion") String JWTToken) {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext(carbonContext, JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {

            String resourcePath = getResourcePath(path);
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());

            if (!registry.resourceExists(resourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + resourcePath).type(RestAPIConstants.TYPE_JSON).build();
            }
            Resource resource = registry.get(resourcePath);
            // check whether the resource is a collection, if collection return the paths inside the collection.
            if (resource instanceof Collection) {

                Collection collection = (Collection) resource;
                String[] resourcePaths = collection.getChildren();
                return Response.ok().entity(resourcePaths).type(RestAPIConstants.TYPE_JSON)
                        .build();
            } else {
                // get the content of the resource as a stream
                String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
                return Response.ok(resource.getContentStream()).type(resource.getMediaType())
                        .header("Content-Disposition",
                                "attachment; filename=" + fileName).build();
            }
        } catch (RegistryException e) {
            log.error("Failed to get resource " + path, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * This method creates/update the resource sent as the payload to the registry.
     *
     * @param path          - List of path segment of the resource path after the (/resource) in the URI
     * @param contentStream - Resource content sent as stream
     * @return Response - HTTP 200 OK with the resource/collection path.
     */

    @PUT
    @Path("/{path:.*}")
    @Produces("application/json")
    @ApiOperation(value = "Create/Update a resource",
                  httpMethod = "PUT",
                  notes = "Create/Update a resource")
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Resource updated successfully"),
                            @ApiResponse(code = 400, message = "Media type mismatch"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Specified resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response createResource(@PathParam("path") List<PathSegment> path,
                                   InputStream contentStream,
                                   @HeaderParam("Content-Type") String contentType,
                                   @HeaderParam("X-JWT-Assertion") String JWTToken) {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext(carbonContext, JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {

            String resourcePath = getResourcePath(path);
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());

            if (registry.resourceExists(resourcePath)) {
                // if collection already exists return conflict
                if (contentType.contains(COLLECTION_MEDIA_TYPE)) {
                    return Response.status(Response.Status.CONFLICT).entity(
                            "Collection already exist " + resourcePath).build();
                }
                Resource resource = registry.get(resourcePath);
                if (contentType.equals(resource.getMediaType())) {
                    resource.setContent(contentStream);
                    registry.put(resourcePath, resource);
                    return Response.status(Response.Status.NO_CONTENT).entity("Updated : " + resourcePath).build();

                } else {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }

            } else {
                Resource resource;
                // check for collection media type
                if (contentType.equals(COLLECTION_MEDIA_TYPE)) {
                    resource = registry.newCollection();
                } else {
                    // otherwise create a resource instance
                    resource = registry.newResource();
                    resource.setMediaType(contentType);
                    resource.setContentStream(contentStream);
                }
                try {
                    registry.put(resourcePath, resource);
                    return Response.status(Response.Status.CREATED).entity("Created : " + resource.getPath()).build();
                } catch (RegistryException e) {
                    log.error("Failed to create/update resource on " + path, e);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            }
        } catch (RegistryException e) {
            log.error("Failed to create resource " + path, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * This method delete the requested resource.
     *
     * @param path - path segment of the resource path
     * @return Response - HTTP 204 No Content.
     */
    @DELETE
    @Path("/{path:.*}")
    @Produces("application/json")
    @ApiOperation(value = "Delete a resource",
                  httpMethod = "DELETE",
                  notes = "Delete a resource")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Resource deleted successfully"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Specified resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response deleteResource(@PathParam("path") List<PathSegment> path,
                                   @HeaderParam("X-JWT-Assertion") String JWTToken) {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext(carbonContext, JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String resourcePath = getResourcePath(path);
        Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());

        try {
            if (!registry.resourceExists(resourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + resourcePath).build();
            }
            // if resource exists delete the resource
            registry.delete(resourcePath);
            return Response.status(Response.Status.NO_CONTENT).build();

        } catch (RegistryException e) {
            log.error("Failed to delete resource " + path, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
