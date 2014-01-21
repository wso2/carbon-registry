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

/**
 * This provides functionality to manage various lifecycle (or aspects which are lifecycles)
 * configurations on the registry.
 * <br />
 * <b>Statistics:</b>
 * <ul>
 * <li>createLifecycle</li>
 * <li>updateLifecycle</li>
 * <li>deleteLifecycle</li>
 * </ul>
 *
 * @param <LifecycleBean> a bean that can be used to manage a lifecycle configuration.
 *                                     Please note that a lifecycle configuration can be modified
 *                                     only if it is not currently being used.
 */
public interface ILifecycleManagementService<LifecycleBean> {

    /**
     * Method to obtain the location on the repository where lifecycle configurations are stored.
     *
     * @return the location on the repository where lifecycle configurations are stored.
     * @throws Exception if the operation failed.
     */
    String getLifecyclesCollectionLocation() throws Exception;

    /**
     * Method to set the location on the repository where lifecycle configurations are stored.
     *
     * @param location the location on the repository where lifecycle configurations are stored.
     * @throws Exception if the operation failed.
     */
    void setLifecyclesCollectionLocation(String location) throws Exception;

    /**
     * Method to obtain a list of currently configured lifecycles.
     *
     * @return the list of lifecycles.
     * @throws Exception if the operation failed.
     */
    String[] getLifecycleList() throws Exception;

    /**
     * Method to obtain a configuration bean for the given lifecycle.
     *
     * @param name the name of the lifecycle configuration.
     *
     * @return the corresponding lifecycle configuration bean.
     *
     * @throws Exception if the operation failed.
     */
    LifecycleBean getLifecycleBean(String name) throws Exception;

    /**
     * Method to obtain the lifecycle configuration of the given lifecycle.
     *
     * @param name the name of the lifecycle configuration.
     *
     * @return the corresponding lifecycle configuration XML.
     * @throws Exception if the operation failed.
     */
    String getLifecycleConfiguration(String name) throws Exception;

    /**
     * Method to create a new lifecycle configuration.
     *
     * @param configuration the string configuration. The name of the lifecycle will be determined
     *             from the provided configuration.
     *
     * @return whether the operation was successful or not.
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean createLifecycle(String configuration) throws Exception;

    /**
     * Method to update an existing lifecycle configuration.
     *
     * @param oldName the name of the existing lifecycle configuration.
     * @param configuration   the string configuration containing the updated configuration. if the
     *                new lifecycle configuration has a new name, the old lifecycle configuration
     *                will be deleted and a new one will be added. If the names were the same, the
     *                existing configuration will be updated instead.
     *
     * @return whether the operation was successful or not.
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean updateLifecycle(String oldName, String configuration) throws Exception;

    /**
     * Method to delete an existing lifecycle configuration.
     *
     * @param name the name of the existing lifecycle configuration to be deleted.
     *
     * @return whether the operation was successful or not.
     * @throws Exception if the operation failed due to an unexpected error.
     */
    boolean deleteLifecycle(String name) throws Exception;

    /**
     * Method to determine whether the lifecycle configuration by the given name is currently being
     * used.
     *
     * @param name the name of an existing lifecycle configuration.
     *
     * @return if the given lifecycle configuration is being used this method will return 'true'. If
     *         not, this method will return 'false'.
     * @throws Exception if the operation failed.
     */
    boolean isLifecycleNameInUse(String name) throws Exception;

    /**
     * Method to parse the given lifecycle configuration XML and generate a lifecycle configuration
     * bean.
     *
     * @param configuration the configuration of the lifecycle in XML.
     *
     * @return a bean corresponding to the given configuration.
     *
     * @throws Exception if the operation failed.
     */
    boolean parseConfiguration(String configuration) throws Exception;

    /**
     * Method to retrieve the version of the defined lifecycle configuration
     *
     * @param name the name of the lifecycle resource.
     *
     * @return a string which is the version.
     *
     * @throws Exception if the operation failed.
     */

    String getLifecycleConfigurationVersion(String name) throws Exception;
}
