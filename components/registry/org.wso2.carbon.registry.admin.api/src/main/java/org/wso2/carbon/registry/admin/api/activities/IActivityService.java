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
package org.wso2.carbon.registry.admin.api.activities;

import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * Provides functionality to list activities on the back-end registry instance.
 *
 * @param <ActivityBean> a bean object containing the list of registry activities.
 */
public interface IActivityService<ActivityBean> {

    /**
     * Method to obtain the list of activities that took place on the back-end registry.
     * 
     * @param userName     the name of the user for which we are interested in obtaining the list
     *                     of activities.
     * @param resourcePath the resource path which we are interested in.
     * @param fromDate     this parameter can be used to obtain activities starting from a given
     *                     date.
     * @param toDate       this parameter can be used to obtain activities up to a given date.
     * @param filter       the type of filter to be used, to only get activities of a particular
     *                     type.
     * @param pageStr      a string used for pagination. This parameter is not used in the current
     *                     search API.
     * @param sessionId    a session identifier. This parameter is not used in the current search
     *                     API.
     *
     * @return a list of activities that match the given search criteria.
     * @throws RegistryException if the operation failed.
     */
    ActivityBean getActivities(String userName, String resourcePath, String fromDate,
                               String toDate, String filter, String pageStr, String sessionId)
            throws RegistryException;
}
