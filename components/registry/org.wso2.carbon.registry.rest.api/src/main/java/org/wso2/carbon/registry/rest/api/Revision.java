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
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * This class is to handle the resource revision related to REST verbs GET,POST and DELETE.
 */

@Path("/revision")
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
