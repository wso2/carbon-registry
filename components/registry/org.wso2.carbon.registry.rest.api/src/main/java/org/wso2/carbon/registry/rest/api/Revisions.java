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
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class is to handle the revisions of the given resource according to the REST verb GET.
 */
@Path("/revisions")
@Api(value = "/revisions",
     description = "Rest api for doing operations on resource revisions",
     produces = MediaType.APPLICATION_JSON)
public class Revisions extends PaginationCalculation<String> {

    private Log log = LogFactory.getLog(Revisions.class);

    /**
     * This method get revisions on the requested resource.
     *
     * @param path  - Path of the resource in the registry
     * @param start - Starting page number
     * @param size  - Number of records to be retrieved
     * @return array of version IDs
     */
    @GET
    @Produces("application/json")
    @ApiOperation(value = "Get all revisions of a resource",
                  httpMethod = "GET",
                  notes = "Fetch all revisions of a resource",
                  response = String.class,
                  responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Found the revisions IDs and returned in body"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Given specific comment not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response getRevisions(@QueryParam("path") String path,
                                 @QueryParam("start") int start,
                                 @QueryParam("size") int size,
                                 @HeaderParam("X-JWT-Assertion") String JWTToken) {

        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext
                (PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!ValidationUtils.validatePagination(start, size)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        String[] result;
        try {

            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(path)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + RestAPIConstants.RESOURCE_NOT_FOUND).build();
            }
            result = registry.getVersions(path);
            return getPaginatedResults(result, start, size, "", "");

        } catch (RegistryException e) {
            log.error("User does not have required permission to access the resource", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    protected Response getPaginatedResults(String[] paths, int start, int size, String sortBy, String sortOrder) {

        String[] paginatedPaths;
        if (start == 0 && size == 0) {
            return Response.status(Response.Status.OK).entity(paths).build();
        }
        if (paths.length < size + start) {
            paginatedPaths = new String[paths.length - start];
            System.arraycopy(paths, start, paginatedPaths, 0, paths.length - start);
        } else {
            paginatedPaths = new String[size];
            System.arraycopy(paths, start, paginatedPaths, 0, size);
        }
        return Response.status(Response.Status.OK).entity(paginatedPaths).build();
    }
}
