/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.social.impl.people.relationship;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.social.api.SocialDataException;
import org.wso2.carbon.registry.social.api.people.relationship.RelationshipManager;
import org.wso2.carbon.registry.social.impl.SocialImplConstants;
import org.wso2.carbon.registry.social.impl.internal.SocialDSComponent;

import java.util.List;


/**
 * An implementation of the {@link org.wso2.carbon.registry.social.api.people.relationship.RelationshipManager} interface
 * <p>
 * This implementation uses the {@link org.wso2.carbon.registry.core.Registry} Associations to store relationships
 * </p>
 * <p>
 * <p/>
 * A two-way registry association is created to form a relationship
 * </p>
 * <p>
 * Association is made with user profiles /users/{userId1}   and /users/{userId2}
 * </p>
 */


public class RelationshipManagerImpl implements RelationshipManager {
    private static Log log = LogFactory.getLog(RelationshipManagerImpl.class);
    private Registry registry = null;


    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Registry getRegistry() throws RegistryException {
        if (this.registry != null) {
            return this.registry;
        } else {
            return SocialDSComponent.getRegistry();
        }
    }

    /**
     * Creates a relationship request from viewer to owner
     * The userId of viewer should be added to the pending relationship requests of the owner
     *
     * @param viewer The userId of the person who is viewing the owner's profile
     * @param owner  The userId of the person whose profile is being viewed
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public boolean requestRelationship(String viewer, String owner) throws SocialDataException {
        boolean result = false;
        if (!(viewer == null || owner == null || viewer.trim().equals("") ||
              owner.trim().equals(""))) {
            try {
                registry = getRegistry();
                String resourcePath = SocialImplConstants.USER_REGISTRY_ROOT + owner +
                                      SocialImplConstants.PENDING_RELATIONSHIP_REQUEST_PATH;
                Resource ownerResource;
                if (registry.resourceExists(resourcePath)) {
                    ownerResource = registry.get(resourcePath);
                } else {
                    ownerResource = registry.newCollection();
                }
                ownerResource.addProperty(SocialImplConstants.RELATIONSHIP_REQUESTS_PROPERTY,
                                          viewer);
                registry.put(resourcePath, ownerResource);
                result = true;
            } catch (RegistryException e) {
                log.error(e.getMessage(), e);
                throw new SocialDataException(
                        "Error while requesting relationship from " + viewer + " to " + owner, e);
            }
        }
        return result;
    }

    /**
     * @param viewer The userId of the person who is viewing the owner's profile
     * @param owner  The userId of the person whose profile is being viewed
     * @return The relationship status between viewer and owner {NONE,PENDING_REQUEST,CONNECTED}
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public String getRelationshipStatus(String viewer, String owner) throws SocialDataException {
        // viewer - the person who views the profile
        // owner - the person whose profile is being viewed
        // viewer=owner when viewer views his/her profile

        if (viewer == null || owner == null || viewer.trim().equals("") || owner.trim().equals("")) {
            return null;
        }
        if (owner.equals(viewer)) {
            // no relationship with in the same person
            return SocialImplConstants.RELATIONSHIP_STATUS_SELF;
        }
        try {
            registry = getRegistry();
            String ownerPath = SocialImplConstants.USER_REGISTRY_ROOT + owner;
            String viewerPath = SocialImplConstants.USER_REGISTRY_ROOT + viewer;

            List<String> property;
            Association[] friends;
            /* Checks if both has a relationship */
            if (registry.getAssociations(viewerPath,
                                         SocialImplConstants.ASS_TYPE_RELATIONSHIP) != null) {
                friends = registry.getAssociations(viewerPath,
                                                   SocialImplConstants.ASS_TYPE_RELATIONSHIP);
                for (Association data : friends) {
                    if (data.getDestinationPath().equals(ownerPath)) {
                        /* both have a relationship */
                        return SocialImplConstants.RELATIONSHIP_STATUS_FRIEND;
                    }
                }
            }
            /* check whether the owner has a request from the viewer  */
            String resourcePath = SocialImplConstants.USER_REGISTRY_ROOT + owner +
                                  SocialImplConstants.PENDING_RELATIONSHIP_REQUEST_PATH;

            if (registry.resourceExists(resourcePath) && (property = registry.get(resourcePath).
                    getPropertyValues(SocialImplConstants.RELATIONSHIP_REQUESTS_PROPERTY)) != null &&
                property.contains(viewer)) {
                // means that the viewer has requested a relationship from the owner of the profile
                // and it has not been accepted yet
                return SocialImplConstants.RELATIONSHIP_STATUS_REQUEST_PENDING;
            }
            /* check whether the owner requested relationship with viewer */
            resourcePath = SocialImplConstants.USER_REGISTRY_ROOT + viewer +
                           SocialImplConstants.PENDING_RELATIONSHIP_REQUEST_PATH;

            if (registry.resourceExists(resourcePath) && (property = registry.get(resourcePath).
                    getPropertyValues(SocialImplConstants.RELATIONSHIP_REQUESTS_PROPERTY)) != null &&
                property.contains(owner)) {
                // means that the viewer has received a request from the owner of the profile
                return SocialImplConstants.RELATIONSHIP_STATUS_REQUEST_RECEIVED;
            }
            // if no such relationship
            return SocialImplConstants.RELATIONSHIP_STATUS_NONE;
            

        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException(
                    "Error while retrieving relationship status between users " + viewer + " and "
                    + owner, e);
        }
    }

    /**
     * @param owner The userId of the person whose pending relationship list to be retrieved
     * @return An array of userId strings from whom the owner has received relationship requests
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public String[] getPendingRelationshipRequests(String owner) throws SocialDataException {
        String[] result = null;
        try {
            registry = getRegistry();
            String resourcePath = SocialImplConstants.USER_REGISTRY_ROOT + owner +
                                  SocialImplConstants.PENDING_RELATIONSHIP_REQUEST_PATH;
            Resource registryResource;
            if (registry.resourceExists(resourcePath)) {
                registryResource = registry.get(resourcePath);
                List<String> pendingRequests = registryResource.getPropertyValues(
                        SocialImplConstants.RELATIONSHIP_REQUESTS_PROPERTY);
                if(pendingRequests!=null){
                result = new String[pendingRequests.size()];
                result = pendingRequests.toArray(result);
                }                
            }
        }
        catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException(
                    "Error while retrieving pending relationship requests for user " + owner, e);
        }
        return result;
    }

    /**
     * Creates a relationship between viewer and owner
     *
     * @param viewer The userId of the viewer
     * @param owner  The userId of the owner
     * @return true if the relationship request was accepted succesfully
     *         false if the relationship request was not accepted successfully
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public boolean acceptRelationshipRequest(String viewer, String owner)
            throws SocialDataException {

        try {
            registry = getRegistry();
            String viewerResourcePath = SocialImplConstants.USER_REGISTRY_ROOT + viewer; /*   /users/{viewer}/    */
            String ownerResourcePath = SocialImplConstants.USER_REGISTRY_ROOT + owner;  /*    /users/{owner}/     */
            if (!registry.resourceExists(viewerResourcePath)) {
                registry.put(viewerResourcePath, registry.newCollection());
            }
            if (!registry.resourceExists(ownerResourcePath)) {
                registry.put(ownerResourcePath, registry.newCollection());
            }
            /* two-way association between profiles */
            registry.addAssociation(viewerResourcePath, ownerResourcePath,
                                    SocialImplConstants.ASS_TYPE_RELATIONSHIP);
            registry.addAssociation(ownerResourcePath, viewerResourcePath,
                                    SocialImplConstants.ASS_TYPE_RELATIONSHIP);
            /* remove owner from viewer's pending requests list */
            viewerResourcePath = viewerResourcePath +
                                 SocialImplConstants.PENDING_RELATIONSHIP_REQUEST_PATH;
            if (registry.resourceExists(viewerResourcePath)) {
                Resource resource = registry.get(viewerResourcePath);
                resource.removePropertyValue(SocialImplConstants.RELATIONSHIP_REQUESTS_PROPERTY,
                                             owner);
                registry.put(viewerResourcePath, resource);
            }
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException(
                    "Error while accepting relationship request from " + owner + " to " + viewer, e);
        }
        return true;
    }

    /**
     * @param loggedUser The userId of the user whose relationship list to be retrieved
     * @return An array of userIds of users with whom the loggedUser has a relationship
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public String[] getRelationshipList(String loggedUser) throws SocialDataException {
        String[] result , newResult=null;
        int index = 0;
        Association[] relationships ;
        try {
            registry = getRegistry();
            String profilePath = SocialImplConstants.USER_REGISTRY_ROOT + loggedUser;
            relationships = registry.getAssociations(profilePath,
                                                     SocialImplConstants.ASS_TYPE_RELATIONSHIP);
            if(relationships!=null) {
            result = new String[relationships.length];
            for (Association relation : relationships) {
                if (relation.getSourcePath().equals(profilePath) &&
                    relation.getDestinationPath() != null) {
                    result[index++] = getUserName(relation.getDestinationPath());
                }
            }
            newResult = new String[index];
            System.arraycopy(result, 0, newResult, 0, index);
            }
        }
        catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException(
                    "Error while retrieving relationship list for user " + loggedUser, e);
        }
        return newResult;
    }

    private String getUserName(String profilePath) throws SocialDataException {
        String userName ;
        userName = profilePath.substring(7); // because of '/users/'

        return userName;
    }

    /**
     * Ignores the relationship request from viewer to owner
     * Implementation should remove the userId of viewer from the pending-relationship-requests list of the owner
     *
     * @param viewer The userId of the person who has requested for a relationship with owner
     * @param owner  The userId of the person to whom the viewer has sent a relationship request
     * @return true if the relationship request was ignored
     *         false if the relationship request was not ignored successfully
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public boolean ignoreRelationship(String viewer, String owner) throws SocialDataException {
        /* remove owner from viewer's pending requests */
        boolean result = false;
        if (!(viewer == null || owner == null || viewer.trim().equals("") ||
              owner.trim().equals(""))) {
            try {
                registry = getRegistry();
                String resourcePath = SocialImplConstants.USER_REGISTRY_ROOT + viewer +
                                      SocialImplConstants.PENDING_RELATIONSHIP_REQUEST_PATH;
                Resource ownerResource;
                if (registry.resourceExists(resourcePath)) {
                    ownerResource = registry.get(resourcePath);
                    if (ownerResource.getPropertyValues(SocialImplConstants.
                            RELATIONSHIP_REQUESTS_PROPERTY).indexOf(owner) >= 0) {
                        // remove property
                        ownerResource.removePropertyValue(SocialImplConstants
                                .RELATIONSHIP_REQUESTS_PROPERTY, owner);
                        registry.put(resourcePath, ownerResource);
                    }

                    result = true;
                }

            } catch (RegistryException e) {
                log.error(e.getMessage(), e);
                throw new SocialDataException("Error while ignoring relationship request from "
                                              + owner + " to " + viewer, e);
            }
        }
        return result;
    }

     /**
     * Removes the relationship between owner and viewer
     * Implementation should remove the association between owner and viewer
     *
     * @param owner  The userId of the person to whom the viewer has sent a relationship request
     * @param viewer The userId of the person who has requested for a relationship with owner
     * @return true if the relationship was removed successfully
     *         false if the relationship is not removed
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public boolean removeRelationship(String owner, String viewer) throws SocialDataException {
        try {
            registry = getRegistry();
            String viewerResourcePath = SocialImplConstants.USER_REGISTRY_ROOT + viewer; /*   /users/{viewer}/    */
            String ownerResourcePath = SocialImplConstants.USER_REGISTRY_ROOT + owner;  /*    /users/{owner}/     */
            if (!registry.resourceExists(viewerResourcePath)) {
                registry.put(viewerResourcePath, registry.newCollection());
            }
            if (!registry.resourceExists(ownerResourcePath)) {
                registry.put(ownerResourcePath, registry.newCollection());
            }
            /* remove two-way association between profiles */

            registry.removeAssociation(viewerResourcePath, ownerResourcePath,
                                       SocialImplConstants.ASS_TYPE_RELATIONSHIP);
            registry.removeAssociation(ownerResourcePath, viewerResourcePath,
                                       SocialImplConstants.ASS_TYPE_RELATIONSHIP);

        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException(
                    "Error while removing relationship between " + owner + " and " + viewer, e);
        }
        return true;
    }

}
