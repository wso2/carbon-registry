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
import org.wso2.carbon.registry.core.TaggedResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.TaggedResourcePathModel;
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is to handle the Tag related REST verbs GET , POST and DELETE.
 */

@Path("/tag")
public class Tag extends PaginationCalculation<TaggedResourcePath> {

    private Log log = LogFactory.getLog(Tag.class);
    private String tag = null;

    /**
     * This method retrieves the resource paths for a given tag name
     *
     * @param tagName - Name of the tag
     * @param start   - Page start number
     * @param size    - Number of records to be fetched
     * @return JSON object eg: {"path":[<array of resource paths tagged by the
     *         tagname>]}protected HTTP 200 OK.
     */
    @GET
    @Produces("application/json")
    public Response getTaggedResources(@QueryParam("name") String tagName,
                                       @QueryParam("start") int start,
                                       @QueryParam("size") int size,
                                       @HeaderParam("X-JWT-Assertion") String JWTToken) {
        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext
                (PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {

            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            TaggedResourcePath[] resourcePaths = registry.getResourcePathsWithTag(tagName);
            return getPaginatedResults(resourcePaths, start, size, "", "");

        } catch (RegistryException e) {
            log.error("Failed to get resource path having tag : " + tagName, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    /**
     * This method add array of tags to the specified resource
     *
     * @param resourcePath - Resource path
     * @return HTTP 204 No Content if success
     */
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response addTag(@QueryParam("path") String resourcePath,
                           @QueryParam("name") String tagText,
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
            registry.applyTag(resourcePath, tagText);
            return Response.status(Response.Status.NO_CONTENT).build();

        } catch (RegistryException e) {
            log.error("user doesn't have permission to put the tags for the given resource", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * This method deletes the specified tag on the given resource
     *
     * @param resourcePath - Path of the resource.
     * @param tagName      - Name of the tag
     * @return HTTP 204 No Content response, if success.
     */
    @DELETE
    @Produces("application/json")
    public Response deleteTag(@QueryParam("path") String resourcePath,
                              @QueryParam("name") String tagName,
                              @HeaderParam("X-JWT-Assertion") String JWTToken) {

        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext
                (PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {

            boolean tagFound = false;
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(resourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        RestAPIConstants.RESOURCE_NOT_FOUND + resourcePath).build();
            }
            org.wso2.carbon.registry.core.Tag[] tags = registry.getTags(resourcePath);
            for (org.wso2.carbon.registry.core.Tag tag1 : tags) {
                // if tag has been found remove the tag,set the tag found
                // variable to true
                if (tagName.equals(tag1.getTagName())) {
                    registry.removeTag(resourcePath, tagName);
                    tagFound = true;
                }
            }
            if (tagFound) {
                // if tag deleted
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                log.debug("tag not found");
                // if the specified tag is not found,returns http 404
                return Response.status(Response.Status.NOT_FOUND).build();
            }

        } catch (RegistryException e) {
            log.error("Failed to  delete a tag " + tagName + " " + "on resource " + resourcePath, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    protected Response getPaginatedResults(TaggedResourcePath[] taggedResourcePaths, int start, int size,
                                           String sortBy, String sortOrder) {

        List<TaggedResourcePathModel> resourcePathModelList = new ArrayList<TaggedResourcePathModel>();
        TaggedResourcePath[] paginatedResourcePaths;

        if (size == 0 && start == 0) {
            for (TaggedResourcePath resourcePath : taggedResourcePaths) {
                resourcePathModelList.add(new TaggedResourcePathModel(resourcePath));
            }
            return Response.ok(resourcePathModelList.toArray(
                    new TaggedResourcePathModel[resourcePathModelList.size()])).build();
        }
        if (taggedResourcePaths.length < start) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (taggedResourcePaths.length < size + start) {
            paginatedResourcePaths = new TaggedResourcePath[taggedResourcePaths.length - start];
            System.arraycopy(taggedResourcePaths, start, paginatedResourcePaths, 0, taggedResourcePaths.length - start);

        } else {
            paginatedResourcePaths = new TaggedResourcePath[size];
            System.arraycopy(taggedResourcePaths, start, paginatedResourcePaths, 0, size);
        }
        for (TaggedResourcePath resourcePath : paginatedResourcePaths) {
            resourcePathModelList.add(new TaggedResourcePathModel(resourcePath));
        }
        return Response.ok(resourcePathModelList.toArray(
                new TaggedResourcePathModel[resourcePathModelList.size()])).build();
    }
}

