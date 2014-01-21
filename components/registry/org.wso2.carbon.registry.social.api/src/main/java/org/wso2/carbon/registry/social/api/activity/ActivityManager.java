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
package org.wso2.carbon.registry.social.api.activity;

import org.wso2.carbon.registry.social.api.SocialDataException;
import org.wso2.carbon.registry.social.api.utils.FilterOptions;

import java.util.Set;

/**
 * This interface handles social activities
 * <p/>
 * Implement this interface to according to the persistence storage of social data
 */

public interface ActivityManager {

    /**
     * Persists the activity for the given userId
     *
     * @param userId       The userId of the person whose activity to be stored
     * @param userActivity The Activity object of the user to be stored
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public void saveActivity(String userId, Activity userActivity) throws SocialDataException;

    /**
     * Removes the activity of the given userId
     *
     * @param userId       The userId of the person whose activity to be removed
     * @param activityId   The id of the activity to be removed
     * @param appId        The appId of the activity
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public void deleteActivity(String userId, String activityId,String appId)
            throws SocialDataException;

    /**
     * Updates the activity of the given userId
     *
     * @param userId       The userId of the person whose activity to be modified
     * @param userActivity The activity to be modified
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */

    public void updateActivity(String userId, Activity userActivity) throws SocialDataException;

    /**
     * Returns an array of Activity objects to the passed parameters
     *
     * @param userIds The set of userIds for which activities to be fetched
     * @param groupId The groupId
     * @param appId   The appId of the activities to be fetched
     * @param fields  The fields to return. Empty set implies all
     * @param options The sorting/filtering/pagination options
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public Activity[] getActivities(String[] userIds, String groupId, String appId,
                                    Set<String> fields,FilterOptions options)
            throws SocialDataException;

    /**
     * Returns an array of Activity objects to the passed activityIds
     *
     * @param userId      The userId of the  person whose activity to be fetched
     * @param groupId     The groupId
     * @param appId       The appId of the activities to be fetched
     * @param fields      The fields to return. Empty set implies all
     * @param options     The sorting/filtering/pagination options
     * @param activityIds The ids of the activities to be fetched
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public Activity[] getActivities(String userId, String groupId, String appId, Set<String> fields,
                                    FilterOptions options, String[] activityIds)
            throws SocialDataException;

    /**
     * Returns the Activity object for the given user
     *
     * @param userId     The userId of the  person whose activity to be fetched
     * @param groupId    The groupId
     * @param appId      The appId of the activity
     * @param fields     The fields to return. Empty set implies all
     * @param activityId The activityId of the activity to be fetched
     * @return A Activity object for the given parameters
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public Activity getActivity(String userId, String groupId, String appId, Set<String> fields,
                                String activityId) throws SocialDataException;

    /**
     * Deletes the activities for the given activityIds
     *
     * @param userId      The userId of the person whose activities to be deleted
     * @param groupId     The groupId
     * @param appId       The appId
     * @param activityIds The activityIds to be deleted
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public void deleteActivities(String userId, String groupId, String appId,
                                 Set<String> activityIds) throws SocialDataException;

    /**
     * Creates an Activity object for the given person using the given details
     *
     * @param userId     The userId of the person to create the activity for
     * @param groupId    The groupId
     * @param appId      The appId of the activity
     * @param fields     Fields of the activity to be created
     * @param activity   The activity to create
     * @return An Activity object for the given userId with given details
     * @throws org.wso2.carbon.registry.social.api.SocialDataException
     *
     */
    public void createActivity(String userId, String groupId, String appId, Set<String> fields,
                               Activity activity) throws SocialDataException;

}
