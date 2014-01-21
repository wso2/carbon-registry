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
package org.wso2.carbon.registry.admin.api.properties;

import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * Provides functionality to manage resource properties.
 *
 * @param <PropertiesBean> This bean contains a list of properties, for a given resource.
 */
public interface IPropertiesAdminService<PropertiesBean,RetentionBean> {

    /**
     * Method to return all the properties of a given resource.
     *
     * @param path      path of the resource.
     * @param viewProps currently supported values "true", "false". if "true" show system
     *                  properties.
     *
     * @return The properties bean.
     * @throws RegistryException throws if there is an error.
     */
    PropertiesBean getProperties(String path, String viewProps) throws RegistryException;

    /**
     * Method to get resource retention properties of a given resource
     * @param path path of the resource
     * @return RetentionBean which encapsulates retention properties
     * @throws RegistryException
     */
    RetentionBean getRetentionProperties(String path) throws RegistryException;

    /**
     * Method to add a property, if there already exist a property with the same name, this
     * will add the value to the existing property name. (So please remove the old property with
     * the same name before calling this method).
     *
     * @param path  path of the resource.
     * @param name  property name.
     * @param value property value.
     * @throws RegistryException throws if there is an error.
     */
    void setProperty(String path, String name, String value) throws RegistryException;

    /**
     * Method to set resource retention properties of a resource
     * @param path Path of the resource
     * @param bean RetentionBean which encapsulates retention properties
     * @return true if operation succeeds false otherwise
     * @throws RegistryException
     */
    boolean setRetentionProperties(String path, RetentionBean bean) throws RegistryException ;

    /**
     * Method to update a property (This removes the old property with the oldName)
     *
     * @param path    path of the resource.
     * @param name    property name.
     * @param value   property value.
     * @param oldName old name of the property.
     *
     * @throws RegistryException throws if there is an error.
     */
    void updateProperty(String path, String name, String value, String oldName) throws
            RegistryException;

    /**
     * Method to remove property.
     *
     * @param path path of the resource.
     * @param name property name.
     *
     * @throws RegistryException throws if there is an error.
     */
    void removeProperty(String path, String name) throws RegistryException;
}
