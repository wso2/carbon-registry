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

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class to handle the rate relate REST verbs POST
 */
@Path("/rate")
@Api(value = "/rate",
     description = "Rest api to rate a resource",
     produces = MediaType.APPLICATION_JSON)
public class Rate extends RegistryRestSuper {

    private Log log = LogFactory.getLog(Rate.class);

    /**
     * This method put a rating to a resource
     *
     * @param resourcePath - path of the resource in the registry space
     * @param value        - user's rating
     * @return Response RatingModel - object eg:{"average":<value>,"myRating":<value>}
     */
    @POST
    @Produces("application/json")
    @ApiOperation(value = "Add a rate to a resource",
                  httpMethod = "POST",
                  notes = "Add a rate to a resource",
                  response = float.class)
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Rate added successfully"),
                            @ApiResponse(code = 401, message = "Invalid credentials provided"),
                            @ApiResponse(code = 404, message = "Specified resource not found"),
                            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response rateResource(@QueryParam("path") String resourcePath,
                                 @QueryParam("value") int value,
                                 @HeaderParam("X-JWT-Assertion") String JWTToken) {
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext
                (PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(resourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            registry.rateResource(resourcePath, value);
            float averageRating = registry.getAverageRating(resourcePath);

            return Response.ok(averageRating).build();
        } catch (RegistryException e) {
            log.error("user doesn't have permission to rate a resource", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}

