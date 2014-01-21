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
package org.wso2.carbon.registry.social.api.appdata;

import org.wso2.carbon.registry.social.api.SocialDataException;
import org.wso2.carbon.registry.social.api.utils.DataCollection;

import java.util.Map;
import java.util.Set;

/**
 * This interface handles appData related to people
 * <p/>
 * Implement this interface to according to the persistence storage of social data
 */
public interface AppDataManager {

    /**
     * Retrieves app data for the specified user list and group
     *
     * @param userIds A set of userIds whose app data to be retrieved
     * @param groupId The group
     * @param appId   The app
     * @param fields  The fields to return
     * @return A collection of appData for the given user list and group
     * @throws SocialDataException
     */
    public Map<String,Map<String,String>> getPersonData(String[] userIds, String groupId,
                                                        String appId, Set<String> fields)
            throws SocialDataException;

    /**
     * Deletes data for the specified user and group
     *
     * @param userId  The userId of the person whose app data to be removed
     * @param groupId The group
     * @param appId   The app
     * @param fields  The fields to delete. Empty implies all
     * @throws SocialDataException
     */
    public void deletePersonData(String userId, String groupId, String appId, Set<String> fields)
            throws SocialDataException;

    /**
     * Updates app data for the specified user and group with the new values
     *
     * @param userId  The userId of the person whose app data to be modified
     * @param groupId The group
     * @param appId   The app
     * @param fields  The fields to update. Empty implies all
     * @param values  The new values to set
     * @throws SocialDataException
     */
    public void updatePersonData(String userId, String groupId, String appId, Set<String> fields,
                                 Map<String, String> values) throws SocialDataException;

    /**
     * Save app data for the specified user with the given values
     *
     * @param userId  The userId of the person whose app data to be modified
     * @param appId   The app
     * @param values  The new values to set
     * @throws SocialDataException
     */
    public void savePersonData(String userId, String appId, Map<String, String> values)
            throws SocialDataException;
}
