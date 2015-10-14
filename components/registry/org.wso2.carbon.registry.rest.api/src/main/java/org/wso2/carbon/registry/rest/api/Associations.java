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
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.AssociationModel;
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * his class is to handle the associations related REST verb GET.
 */
@Path("/associations")
@Api(value = "/associations",
     description = "Rest api for doing operations on associations",
     produces = MediaType.APPLICATION_JSON)
public class Associations extends PaginationCalculation<Association> {

    private Log log = LogFactory.getLog(Associations.class);

    /**
     * This method add the array of association sent as payload with the request for the given source
     *
     * @param sourcePath  - Path of the source resource which is going to add the associations.
     * @param association - JSON array of association objects[{"target":"<target path>","type":"<association type>"}]
     * @return Response HTTP 204 No Content.
     */

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @ApiOperation(value = "Add a set of associations to a resource",
                  httpMethod = "POST",
                  notes = "Add a set of associations to a resource")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Associations added successfully"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Specified resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response addAssociations(@QueryParam("path") String sourcePath, AssociationModel[] association,
                                    @HeaderParam("X-JWT-Assertion") String JWTToken) {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext(carbonContext, JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            // check for resource exist
            if (!registry.resourceExists(sourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(RestAPIConstants.RESOURCE_NOT_FOUND).build();
            }
            for (AssociationModel associationInput : association) {
                if (registry.resourceExists(associationInput.getTarget())) {
                    registry.addAssociation(sourcePath, associationInput.getTarget(), associationInput.getType());
                }
            }
        } catch (RegistryException e) {
            log.error("Failed add associations to a resource", e);
            Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    /**
     * This method takes the following parameters:
     *
     * @param sourcePath - Path of the resource,
     * @param type-      - Type of association,
     * @param start-     - Start page number,
     * @param size-      - Number of records to be retrieved
     * @return Response the array of AssociationModel. HTTP 200 OK.
     */
    @GET
    @Produces("application/json")
    @ApiOperation(value = "Get all associations on a resource",
                  httpMethod = "GET",
                  notes = "Fetch all associations on a resource",
                  response = AssociationModel.class,
                  responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Found the associations and returned in body"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Associations for given resource were not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response getAssociations(@QueryParam("path") String sourcePath,
                                    @QueryParam("type") String type,
                                    @QueryParam("start") int start,
                                    @QueryParam("size") int size,
                                    @HeaderParam("X-JWT-Assertion") String JWTToken) {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext(carbonContext, JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Association[] associations;
        Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
        try {
            if (!registry.resourceExists(sourcePath)) {
                // if resource not found
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + sourcePath).build();
            }

            if (type != null) {
                associations = registry.getAssociations(sourcePath, type);
            } else {
                associations = registry.getAllAssociations(sourcePath);
            }
        } catch (RegistryException e) {
            log.error("User does not have required permission to access the resource", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        return getPaginatedResults(associations, start, size, "", "");
    }

    @Override
    protected Response getPaginatedResults(Association[] associations, int start, int size,
                                           String sortBy, String sortOrder) {

        Association[] paginatedAssociations;
        List<AssociationModel> associationModels = new ArrayList<AssociationModel>();

        if (start == 0 && size == 0) {
            for (Association association : associations) {
                associationModels.add(new AssociationModel(association));
            }
            return Response.ok(associationModels.toArray(new AssociationModel[associationModels.size()])).build();

        } else if (associations.length < start) {

            return Response.status(Response.Status.BAD_REQUEST).build();

        } else if (associations.length < start + size) {

            paginatedAssociations = new Association[associations.length - start];
            System.arraycopy(associations, start, paginatedAssociations, 0, (associations.length - start));
        } else {
            paginatedAssociations = new Association[size];
            System.arraycopy(associations, start, paginatedAssociations, 0, size);
        }
        for (Association association : paginatedAssociations) {
            associationModels.add(new AssociationModel(association));
        }
        return Response.ok(associationModels.toArray(new AssociationModel[associationModels.size()])).build();

    }
}
