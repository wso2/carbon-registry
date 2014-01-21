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
package org.wso2.carbon.registry.admin.api.resource;

/**
 * This provides functionality to be used by an implementation of a custom user interface that is
 * used in place of the standard user interface for a resource on the Management Console.
 */
@SuppressWarnings("unused")
public interface ICustomUIService extends ITextResourceManagementService {

    /**
     * Checks whether the currently logged in user is authorized to perform the given action on the
     * given path.
     *
     * @param path   path of the resource or collection
     * @param action action to check the authorization
     *
     * @return true if authorized, false if not authorized
     */
    boolean isAuthorized(String path, String action);
}
