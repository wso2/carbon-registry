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

//import edu.emory.mathcs.backport.java.util.Arrays;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.PropertyModel;
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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * This class handle the properties related to the REST verbs GET,POST and DELETE.
 */
@Path("/properties")
@Api(value = "/properties",
     description = "Rest api for doing operations on resource properties",
     produces = MediaType.APPLICATION_JSON)
public class Properties extends PaginationCalculation<PropertyModel> {

    private Log log = LogFactory.getLog(Properties.class);

    /**
     * This method get the properties on the requested resource.
     *
     * @param resourcePath - Path of the resource in the registry.
     * @param start        - Starting page number.
     * @param size         - Number of records to be retrieved.
     * @return - array of properties, HTTP 200 OK.
     */
    @GET
    @Produces("application/json")
    @ApiOperation(value = "Get all properties on a resource",
                  httpMethod = "GET",
                  notes = "Fetch all properties on a resource",
                  response = PropertyModel.class,
                  responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Found the properties and returned in body"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Given specific resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response getProperties(@QueryParam("path") String resourcePath,
                                  @QueryParam("start") int start,
                                  @QueryParam("size") int size,
                                  @HeaderParam("X-JWT-Assertion") String JWTToken) {
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext
                (PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        java.util.Properties properties;
        try {
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(resourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + resourcePath).build();
            }
            properties = registry.get(resourcePath).getProperties();
            return getPaginatedResults(getPropertyModels(properties), start, size, "", "");

        } catch (RegistryException e) {
            log.error("Failed to get properties from " + resourcePath, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * This method add array of JSON property model for the specified resource
     *
     * @param resourcePath - Resource path
     * @param addProperty  - Array of PropertyModel objects
     * @return - HTTP 204 No Content.
     */
    @POST
    @Consumes("application/json")
    @ApiOperation(value = "Add properties to a resource",
                  httpMethod = "POST",
                  notes = "Add properties to a resource")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Properties added successfully"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Specified resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response addProperties(@QueryParam("path") String resourcePath,
                                  PropertyModel[] addProperty,
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
            Resource resource = registry.get(resourcePath);
            for (PropertyModel model : addProperty) {
                resource.setProperty(model.getName(), Arrays.asList(model.getValue()));
            }
            registry.put(resourcePath, resource);
            return Response.status(Response.Status.NO_CONTENT).build();

        } catch (RegistryException e) {
            log.error("user is not allowed to add properties to a resource", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    private PropertyModel[] getPropertyModels(java.util.Properties properties) {

        List<PropertyModel> list = new ArrayList<PropertyModel>();
        Enumeration<Object> propName = properties.keys();
        while (propName.hasMoreElements()) {
            PropertyModel propModel = new PropertyModel();
            String property = propName.nextElement().toString();
            String propValue = properties.get(property).toString();
            propValue = propValue.substring(propValue.indexOf('[') + 1, propValue.indexOf(']'));
            String[] propString = propValue.split(",");
            propModel.setName(property);
            propModel.setValue(propString);
            list.add(propModel);
        }
        return list.toArray(new PropertyModel[list.size()]);
    }

    @Override
    protected Response getPaginatedResults(PropertyModel[] propertyModels, int start, int size,
                                           String sortBy, String sortOrder) {
        if (start == 0 && size == 0) {
            return Response.ok(propertyModels).build();
        }
        PropertyModel[] paginatedModels;
        if (propertyModels.length < size + start) {

            paginatedModels = new PropertyModel[propertyModels.length - start];
            System.arraycopy(propertyModels, start, paginatedModels, 0, (propertyModels.length - start));

        } else {
            paginatedModels = new PropertyModel[size];
            System.arraycopy(paginatedModels, start, paginatedModels, 0, size);
        }
        return Response.ok(paginatedModels).build();
    }
}
