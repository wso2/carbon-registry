/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.carbon.registry.admin.api.jmx;

/**
 * Method to obtain audit logs.
 */
public interface IActivityService {

    /**
     * Retrieves list of activities for the given user.
     *
     * @param username the username.
     *
     * @return list of activities.
     */
    String[] getActivitiesForUser(String username);

    /**
     * Retrieves list of activities for the given path.
     *
     * @param path the resource/collection path.
     *
     * @return list of activities.
     */
    String[] getActivitiesForPath(String path);

    /**
     * Retrieves list of all activities.
     *
     * @return list of activities.
     */
    String[] getList();

}
