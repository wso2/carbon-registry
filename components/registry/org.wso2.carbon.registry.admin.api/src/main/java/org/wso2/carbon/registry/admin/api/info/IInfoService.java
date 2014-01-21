/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.admin.api.info;

import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * Provides functionality to access the community features available around each registry resource
 * and collection.
 * <br />
 * <b>Statistics:</b>
 * <ul>
 * <li>subscribe</li>
 * <li>subscribeREST</li>
 * <li>unsubscribe</li>
 * </ul>
 *
 * @param <CommentBean>      contains a list of comments that were made against this resource. This
 *                           bean also contains whether the current view is the standard view or
 *                           whether it is the version view of a resource. Also, if there were any
 *                           exceptions during the process of retrieving the comments, they too will
 *                           be available on this bean.
 * @param <TagBean>          contains a list of tags that were added against this resource. This
 *                           bean also contains whether the current view is the standard view or
 *                           whether it is the version view of a resource. Also, if there were any
 *                           exceptions during the process of retrieving the tags, they too will be
 *                           available on this bean.
 * @param <RatingBean>       contains a list of ratings that were given to this resource. This
 *                           bean also contains whether the current view is the standard view or
 *                           whether it is the version view of a resource. Also, if there were any
 *                           exceptions during the process of retrieving the ratings, they too will
 *                           be available on this bean. The ratings bean displays the ratings for
 *                           the resource (or collection) that was given by the current user, and
 *                           also the average rating given by all the users.
 * @param <EventTypeBean>    contains a list of events that is available for this resource. This
 *                           bean also contains details of any exceptions during the process of
 *                           retrieving the event types.
 * @param <SubscriptionBean> contains a list of subscriptions that were added against this resource.
 *                           This bean also contains whether the current view is the standard view
 *                           or whether it is the version view of a resource. Also, if there were
 *                           any exceptions during the process of retrieving the tags, they too will
 *                           be available on this bean. In addition to that, this bean contains the
 *                           user name of the currently logged-in user, and also the level of access
 *                           the user has. Users who can read resources will have an access level of
 *                           '1'. Users who can delete resources will have an access level of '2'.
 *                           Users who can grant privileges to other users will have an access level
 *                           of '3'.
 */
public interface IInfoService<CommentBean, TagBean, RatingBean, EventTypeBean, SubscriptionBean> {

    /**
     * Method to obtain a list of comments that were made against a resource/collection.
     *
     * @param path      the resource path of this resource/collection.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @return a bean containing the list of comments.
     * @throws RegistryException if the operation failed.
     */
    CommentBean getComments(String path, String sessionId) throws RegistryException;

    /**
     * Method to add a new comment to this a resource/collection.
     *
     * @param comment   the new comment.
     * @param path      the resource path of this resource/collection.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @throws RegistryException if the operation failed.
     */
    void addComment(String comment, String path, String sessionId) throws RegistryException;

    /**
     * Method to remove a comment that has been added to a resource/collection.
     *
     * @param commentPath the path of the comment to be removed.
     * @param sessionId   a session identifier. This parameter is not used in the current API.
     *
     * @throws RegistryException if the operation failed.
     */
    void removeComment(String commentPath, String sessionId) throws RegistryException;

    /**
     * Method to obtain a list of tags that were made against a resource/collection.
     *
     * @param path      the resource path of this resource/collection.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @return a bean containing the list of tags.
     * @throws RegistryException if the operation failed.
     */
    TagBean getTags(String path, String sessionId) throws RegistryException;

    /**
     * Method to add a new tag to a resource/collection.
     *
     * @param tag       the new tag.
     * @param path      the resource path of this resource/collection.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @throws RegistryException if the operation failed.
     */
    void addTag(String tag, String path, String sessionId) throws RegistryException;

    /**
     * Method to remove a tag that has been added to a resource/collection.
     *
     * @param tag       the tag to be removed.
     * @param path      the resource path of this resource/collection.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @throws RegistryException if the operation failed.
     */
    void removeTag(String tag, String path, String sessionId) throws RegistryException;

    /**
     * Method to obtain a list of ratings that were made against a resource/collection.
     *
     * @param path      the resource path of this resource/collection.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @return a bean containing the list of ratings.
     * @throws RegistryException if the operation failed.
     */
    RatingBean getRatings(String path, String sessionId) throws RegistryException;

    /**
     * Method to add a rating to a resource/collection.
     *
     * @param rating    the rating.
     * @param path      the resource path of this resource/collection.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @throws RegistryException if the operation failed.
     */
    void rateResource(String rating, String path, String sessionId) throws RegistryException;

    /**
     * Method to obtain a list of event types that are available for a resource/collection.
     *
     * @param path      the resource path of this resource/collection.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @return a bean containing the list of event types.
     * @throws RegistryException if the operation failed.
     */
    EventTypeBean getEventTypes(String path, String sessionId) throws RegistryException;

    /**
     * Method to obtain a list of subscriptions that were made against a resource/collection.
     *
     * @param path      the resource path of this resource/collection.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     * @return a bean containing the list of subscriptions.
     *
     * @throws RegistryException if the operation failed.
     */
    SubscriptionBean getSubscriptions(String path, String sessionId) throws RegistryException;

    /**
     * Method to add a subscription to a resource/collection.
     *
     * @param endpoint  the endpoint to which the notification should be delivered.
     * @param eventName the name of the event that you need to subscribe to.
     * @param path      the resource path of this resource/collection.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @return the subscription that was added.
     * @throws RegistryException if the operation failed.
     */
    SubscriptionBean subscribe(String path, String endpoint, String eventName, String sessionId)
            throws RegistryException;

    /**
     * Method to add a REST subscription to a resource/collection.
     *
     * @param endpoint  the endpoint to which the notification should be delivered.
     * @param eventName the name of the event that you need to subscribe to.
     * @param path      the resource path of this resource/collection.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @return the subscription that was added.
     * @throws RegistryException if the operation failed.
     */
    SubscriptionBean subscribeREST(String path, String endpoint, String eventName, String sessionId)
            throws RegistryException;

    /**
     * Method to determine whether the given path is a resource or collection.
     *
     * @param path      the resource path of this resource/collection.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @return true if the given path contains a resource.
     * @throws RegistryException if the operation failed.
     */
    boolean isResource(String path, String sessionId) throws RegistryException;

    /**
     * Method to obtain the remote url of the given resource or collection. This method should only
     * be used for resources that are made available through a remote mount.
     *
     * @param path      the resource path of this resource/collection.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @return the remote url of the given resource or collection.
     * @throws RegistryException if the operation failed.
     */
    String getRemoteURL(String path, String sessionId) throws RegistryException;

    /**
     * Method to verify an e-mail address.
     *
     * @param data      data used for the purpose of e-mail address verificiation.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @return the e-mail address verified.
     * @throws RegistryException if the operation failed.
     */
    String verifyEmail(String data, String sessionId) throws RegistryException;

    /**
     * Method to remove a subscription to a resource/collection.
     *
     * @param path      the resource path of this resource/collection.
     * @param id        the subscription identifier
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @return true if the subscription was successfully removed, or false if not.
     * @throws RegistryException if the operation failed.
     */
    boolean unsubscribe(String path, String id, String sessionId) throws RegistryException;

    /**
     * Method to determine whether the given username identifies a valid user on the system.
     *
     * @param username  the username for which the validity should be determined.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @return true if the user is valid, or false if not.
     * @throws RegistryException if the operation failed.
     */
    boolean isUserValid(String username, String sessionId) throws RegistryException;

    /**
     * Method to determine whether the given user has a valid user profile on the system.
     *
     * @param username  the username for which the validity should be determined.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @return true if the user has a valid user profile, or false if not.
     * @throws RegistryException if the operation failed.
     */
    boolean isProfileExisting(String username, String sessionId) throws RegistryException;

    /**
     * Method to determine whether the given role identifies a valid role on the system.
     *
     * @param role      the role for which the validity should be determined.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @return true if the role is valid, or false if not.
     * @throws RegistryException if the operation failed.
     */
    boolean isRoleValid(String role, String sessionId) throws RegistryException;

    /**
     * Method to determine whether the given role has a valid profile on the system.
     *
     * @param role      the role for which the validity should be determined.
     * @param sessionId a session identifier. This parameter is not used in the current API.
     *
     * @return true if the role has a valid profile, or false if not.
     * @throws RegistryException if the operation failed.
     */
    boolean isRoleProfileExisting(String role, String sessionId) throws RegistryException;

}
