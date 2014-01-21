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
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.rest.api.model.TagModel;
import org.wso2.carbon.registry.rest.api.security.RestAPIAuthContext;
import org.wso2.carbon.registry.rest.api.security.RestAPISecurityUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/tags")
public class Tags extends PaginationCalculation<Tag> {

    private Log log = LogFactory.getLog(Tags.class);

    /**
     * This method tags associated with the given resource
     *
     * @param resourcePath resource path
     * @param start        starting page number
     * @param size         number of tags to be fetched
     * @return JSON tag model eg: {"tags":[<array of tag names]}
     */
    @GET
    @Produces("application/json")
    public Response getTags(@QueryParam("path") String resourcePath,
                            @QueryParam("start") int start,
                            @QueryParam("size") int size,
                            @HeaderParam("X-JWT-Assertion") String JWTToken) {

        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext
                (PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if (resourcePath == null || "".equals(resourcePath)) {
            //Return tagsCloud, therefore no need pagination.
            return getAllTags();
        }
        org.wso2.carbon.registry.core.Tag[] tags = new org.wso2.carbon.registry.core.Tag[0];
        try {
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(resourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(RestAPIConstants.RESOURCE_NOT_FOUND).build();
            }

            tags = registry.getTags(resourcePath);

        } catch (RegistryException e) {
            log.error("Failed to get tags on resource " + resourcePath, e);
            Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        //Need paginate, because it return tags of a resource
        return getPaginatedResults(tags, start, size, "", "");

    }

    /**
     * This method add array of tags to the specified resource
     *
     * @param resourcePath - Resource path
     * @param tags         - eg:{"tags":[<array of tag names>]}
     * @return HTTP 204 No Content ,if success.
     */
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response addTags(@QueryParam("path") String resourcePath,
                            TagModel tags,
                            @HeaderParam("X-JWT-Assertion") String JWTToken) {

        RestAPIAuthContext authContext = RestAPISecurityUtils.getAuthContext
                (PrivilegedCarbonContext.getThreadLocalCarbonContext(), JWTToken);

        if (!authContext.isAuthorized()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
            Registry registry = getUserRegistry(authContext.getUserName(), authContext.getTenantId());
            if (!registry.resourceExists(resourcePath)) {
                return Response.status(Response.Status.NOT_FOUND).entity(RestAPIConstants.RESOURCE_NOT_FOUND).build();
            }

            String[] tagsOnResource = tags.getTags();
            for (String aTagsOnResource : tagsOnResource) {
                registry.applyTag(resourcePath, aTagsOnResource);
            }
            return Response.status(Response.Status.NO_CONTENT).build();

        } catch (RegistryException e) {
            log.error("user doesn't have permission to put the tags for the given resource", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * get all the tags in the registry space.
     */
    private Response getAllTags() {
        if (setTagSearchQuery()) {
            Collection collection;
            HashMap<String, String[]> tagCloud = new HashMap<String, String[]>();
            try {
                // execute the custom query
                collection =
                        super.getUserRegistry()
                                .executeQuery(RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                                        RegistryConstants.QUERIES_COLLECTION_PATH +
                                        "/tags",
                                        Collections.<String, String>emptyMap());
                // create the tagList, extract the tag name from collection and
                // add it to tagList if
                // does not exist already
                List<String> tagList = new ArrayList<String>();
                for (String fullTag : collection.getChildren()) {
                    String tag = fullTag.split(";")[1].split(":")[1];
                    if (!tagList.contains(tag)) {
                        tagList.add(tag);
                    }
                }
                int i = 0;
                // iterate through the list and create a String array.
                Iterator<String> itr = tagList.iterator();
                String[] allTags = new String[tagList.size()];
                while (itr.hasNext()) {
                    allTags[i] = itr.next();
                    i++;
                }
                tagCloud.put("tagCloud", allTags);
            } catch (RegistryException e) {
                log.error(e.getCause(), e);
                Response.status(Response.Status.UNAUTHORIZED).build();
            }
            return Response.ok(tagCloud).build();
        } else {
            log.warn("user is not authorized to access the resource");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * custom query for all the tags search is set as a resource and saved at
     * the config registry.space
     */
    private boolean setTagSearchQuery() {
        if (log.isDebugEnabled()) {
            log.debug("tag search customs query is set");
        }
        String tagsQueryPath =
                RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                        RegistryConstants.QUERIES_COLLECTION_PATH + "/tags";
        try {
            if (!super.getUserRegistry().resourceExists(tagsQueryPath)) {
                // set-up query for tag-search.
                Resource resource = super.getUserRegistry().newResource();
                resource.setContent("SELECT RT.REG_TAG_ID FROM REG_RESOURCE_TAG RT ORDER BY "
                        + "RT.REG_TAG_ID");
                resource.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
                resource.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                        RegistryConstants.TAGS_RESULT_TYPE);
                super.getUserRegistry().put(tagsQueryPath, resource);
            }
            return true;
        } catch (RegistryException e) {
            log.error(e.getCause(), e);
            return false;
        }
    }

    @Override
    protected Response getPaginatedResults(Tag[] tags, int start, int size, String sortBy, String sortOrder) {
        org.wso2.carbon.registry.core.Tag[] paginatedTags;

        String[] tagNames = new String[tags.length];
        if (size == 0 && start == 0) {
            for (int i = 0; i < tags.length; i++) {
                tagNames[i] = tags[i].getTagName();
            }
            TagModel tagModel = new TagModel();
            tagModel.setTags(tagNames);
            return Response.ok(tagModel).build();
        }

        if (tags.length < start) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (tags.length < start + size) {
            paginatedTags = new org.wso2.carbon.registry.core.Tag[tags.length - start];
            System.arraycopy(tags, start, paginatedTags, 0, tags.length - start);

        } else {
            paginatedTags = new org.wso2.carbon.registry.core.Tag[size];
            System.arraycopy(tags, start, paginatedTags, 0, size);
        }
        String[] paginatedTagNames = new String[paginatedTags.length];
        for (int i = 0; i < paginatedTags.length; i++) {
            paginatedTagNames[i] = paginatedTags[i].getTagName();
        }
        TagModel paginatedTagModel = new TagModel();
        paginatedTagModel.setTags(paginatedTagNames);
        return Response.ok(paginatedTagModel).build();
    }
}
