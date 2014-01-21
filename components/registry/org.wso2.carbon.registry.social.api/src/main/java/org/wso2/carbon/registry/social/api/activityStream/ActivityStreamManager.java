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
package org.wso2.carbon.registry.social.api.activityStream;

import org.wso2.carbon.registry.social.api.SocialDataException;
import org.wso2.carbon.registry.social.api.utils.FilterOptions;

import java.util.Set;


public interface ActivityStreamManager {
    /**
     * Persists the activity for the given userId
     *
     * @param userId   The userId of the person whose activity to be stored
     * @param activity The Activity object of the user to be stored
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public void saveActivity(String userId, ActivityEntry activity) throws SocialDataException;

    /**
     * Removes the activity of the given userId
     *
     * @param userId     The userId of the person whose activity to be removed
     * @param activityId The id of the activity to be removed
     * @param appId      The appId of the activity
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public void deleteActivity(String userId, String activityId, String appId)
            throws SocialDataException;


    /**
     * Returns a list of activities that correspond to the passed in users and group.
     *
     * @param userIds The set of ids of the people to fetch activities for.
     * @param groupId Indicates whether to fetch activities for a group.
     * @param appId   The app id.
     * @param fields  The fields to return. Empty set implies all
     * @param options The sorting/filtering/pagination options
     * @return a response item with the list of activities.
     * @throws SocialDataException if any.
     */
    public ActivityEntry[] getActivityEntries(String[] userIds, String groupId, String appId,
                                              Set<String> fields, FilterOptions options)
            throws SocialDataException;

    /**
     * Returns a set of activities for the passed in user and group that corresponds to a list of
     * activityIds.
     *
     * @param userId      The set of ids of the people to fetch activities for.
     * @param groupId     Indicates whether to fetch activities for a group.
     * @param appId       The app id.
     * @param fields      The fields to return. Empty set implies all
     * @param options     The sorting/filtering/pagination options
     * @param activityIds The set of activity ids to fetch.
     * @return a response item with the list of activities.
     * @throws SocialDataException if any.
     */
    public ActivityEntry[] getActivityEntries(String userId, String groupId,
                                              String appId, Set<String> fields,
                                              FilterOptions options, String[] activityIds)
            throws SocialDataException;


    /**
     * Returns an activity for the passed in user and group that corresponds to a single
     * activityId.
     *
     * @param userId     The set of ids of the people to fetch activities for.
     * @param groupId    Indicates whether to fetch activities for a group.
     * @param appId      The app id.
     * @param fields     The fields to return. Empty set implies all
     * @param activityId The activity id to fetch.
     * @return a response item with the list of activities.
     * @throws SocialDataException if any.
     */
    public ActivityEntry getActivityEntry(String userId, String groupId, String appId,
                                          Set<String> fields, String activityId)
            throws SocialDataException;

    /**
     * Deletes the activity for the passed in user and group that corresponds to the activityId.
     *
     * @param userId      The user.
     * @param groupId     The group.
     * @param appId       The app id.
     * @param activityIds A list of activity ids to delete.
     * @return a response item containing any errors
     * @throws SocialDataException if any.
     */
    public void deleteActivityEntries(String userId, String groupId, String appId,
                                      Set<String> activityIds) throws SocialDataException;

    /**
     * Updates the specified Activity.
     *
     * @param userId   The id of the person to update the activity for
     * @param activity The updated activity
     * @return a response item containing any errors
     * @throws SocialDataException if any
     */
    public void updateActivityEntry(String userId, ActivityEntry activity)
            throws SocialDataException;

    /**
     * Creates the passed in activity for the passed in user and group. Once createActivity is called,
     * getActivities will be able to return the Activity.
     *
     * @param userId   The id of the person to create the activity for.
     * @param groupId  The group.
     * @param appId    The app id.
     * @param fields   The fields to return.
     * @param activity The activity to create.
     * @throws SocialDataException if any.
     */
    public void createActivityEntry(String userId, String groupId, String appId,
                                    Set<String> fields, ActivityEntry activity)
            throws SocialDataException;
}
