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
package org.wso2.carbon.registry.admin.api.profiles;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.core.UserStoreException;

/**
 * Provides functionality to load and store user profile details in to the registry.
 *
 * @param <ProfilesBean> contains details about the various profiles of a given user on the system.
 *                       This also contains the username.
 */
public interface IProfilesAdminService<ProfilesBean> {

    /**
     * Method to add a new user profile to the registry.
     *
     * @param path the path to add the user profile.
     *
     * @return true if the operation succeeded, or false if not.
     * @throws RegistryException  if an error occurred while accessing the registry.
     * @throws UserStoreException if an error occurred while accessing the user realm.
     */
    boolean putUserProfile(String path)throws RegistryException, UserStoreException;

    /**
     * Method to retrieve profile details from the registry.
     *
     * @param path the path to add the user profile.
     *
     * @return the user profile bean.
     * @throws RegistryException  if an error occurred while accessing the registry.
     * @throws UserStoreException if an error occurred while accessing the user realm.
     */
    ProfilesBean getUserProfile(String path) throws RegistryException,UserStoreException;
}
