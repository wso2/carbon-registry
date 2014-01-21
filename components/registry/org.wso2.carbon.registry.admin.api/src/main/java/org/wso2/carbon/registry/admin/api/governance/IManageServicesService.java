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
package org.wso2.carbon.registry.admin.api.governance;

import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * This provides functionality to manage services stored on the repository. The content of the
 * service managed, depends on the configuration of the service user interface which can be defined
 * through {@link IConfigureServiceUIService}.
 * <br />
 * <b>Statistics:</b>
 * <ul>
 * <li>addService</li>
 * </ul>
 */
public interface IManageServicesService {

    /**
     * Method to determine whether the given user can make changes to a service at the given
     * resource path.
     *
     * @param path the resource path.
     *
     * @return true if the resource at the given path can be changed.
     *
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean canChange(String path) throws Exception;

    /**
     * Method to add a new service to the repository.
     *
     * @param info the service details that will be added.
     *
     * @return the path of the service..
     * @throws RegistryException if the operation failed due to an unexpected error.
     */
    String addService(String info) throws RegistryException;

    /**
     * Method to obtain the content of the service by the given name for editing.
     *
     * @param name the name of the service to edit.
     *
     * @return the content of the service resource.
     *
     * @throws RegistryException if the operation failed.
     */
    String editService(String name) throws RegistryException;

    /**
     * Method to obtain the path at which services are stored on the repository.
     *
     * @return the path at which services are stored.
     *
     * @throws RegistryException if the operation failed.
     */
    String getServicePath() throws RegistryException;

    /**
     * Method to obtain the service configuration.
     *
     * @return the string config of the service
     *
     * @throws RegistryException if the operation failed.
     */
    String getServiceConfiguration()throws RegistryException;

    /**
     * Method to update the service configuration of a service
     *
     * @param content the content of the config
     *
     * @return a boolean value
     *
     * @throws RegistryException if the operation failed.
     */
    boolean saveServiceConfiguration(String content) throws RegistryException;
}
