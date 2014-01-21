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
package org.wso2.carbon.registry.social.api.people.relationship;

import org.wso2.carbon.registry.social.api.SocialDataException;

/**
 * This interface handles Social relationships & relationship requests
 * <p/>
 * Implement this interface to according to the persistence storage of social data
 */

public interface RelationshipManager {
    /**
     * Creates a relationship request from viewer to owner
     * The userId of viewer should be added to the pending relationship requests of the owner
     *
     * @param viewer The userId of the person who is viewing the owner's profile
     * @param owner  The userId of the person whose profile is being viewed
     * @return
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public boolean requestRelationship(String viewer, String owner) throws SocialDataException;

    /**
     * @param viewer The userId of the person who is viewing the owner's profile
     * @param owner  The userId of the person whose profile is being viewed
     * @return The relationship status between viewer and owner {NONE,PENDING_REQUEST,CONNECTED}
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */

    public String getRelationshipStatus(String viewer, String owner) throws SocialDataException;

    /**
     * @param owner The userId of the person whose pending relationship list to be retrieved
     * @return An array of userId strings from whom the owner has received relationship requests
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public String[] getPendingRelationshipRequests(String owner) throws SocialDataException;


    /**
     * Creates a relationship between viewer and owner
     *
     * @param viewer
     * @param owner
     * @return true if the relationship request was accepted succesfully
     *         false if the relationship request was not accepted successfully
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public boolean acceptRelationshipRequest(String viewer, String owner)
            throws SocialDataException;

    /**
     * @param loggedUser The userId of the user whose relationship list to be retrieved
     * @return An array of userIds of users with whom the loggedUser has a relationship
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */

    public String[] getRelationshipList(String loggedUser) throws SocialDataException;

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

    public boolean ignoreRelationship(String viewer, String owner) throws SocialDataException;

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
    public boolean removeRelationship(String owner, String viewer) throws SocialDataException;
}