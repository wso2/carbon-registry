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
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.AssociationModel;
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * This class is to handle the association related REST verbs POST and DELETE.
 */
@Path("/association")
public class Association extends RegistryRestSuper {

    private Log log = LogFactory.getLog(Association.class);

    /**
     * This method to add the association sent as query param in the request.
     *
     * @param sourcePath - path of the source resource.
     * @param targetPath - path of the target resource.
     * @return array of association model.HTTP 204 No Content.
     */
    @POST
    @Produces("application/json")
    public Response addAssociation(@QueryParam("path") String sourcePath,
                                   @QueryParam("targetPath") String targetPath,
                                   @QueryParam("type") String type,
                                   @HeaderParam("X-JWT-Assertion") String JWTToken) {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext(carbonContext, JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {

            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(sourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + sourcePath).build();
            }
            registry.addAssociation(sourcePath, targetPath, type);
            return Response.status(Response.Status.NO_CONTENT).build();

        } catch (RegistryException e) {
            log.error("user is not allowed to delete association on the resource", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * This method delete the association sent as query param in the request.
     *
     * @param sourcePath - path of the source resource.
     * @param targetPath - path of the target resource.
     * @return array of association model.HTTP 204 No Content.
     */
    @DELETE
    @Produces("application/json")
    public Response deleteAssociation(@QueryParam("path") String sourcePath,
                                      @QueryParam("targetPath") String targetPath,
                                      @QueryParam("type") String type,
                                      @HeaderParam("X-JWT-Assertion") String JWTToken) {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext(carbonContext, JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {

            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(sourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + sourcePath).build();
            }
            if (!registry.resourceExists(targetPath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + targetPath).build();
            }
            registry.removeAssociation(sourcePath, targetPath, type);
            return Response.status(Response.Status.NO_CONTENT).build();

        } catch (RegistryException e) {
            log.error("user is not allowed to delete association on the resource", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

}
