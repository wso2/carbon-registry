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
 * This provides functionality to list metadata information on the repository, and display them on
 * the Management Console.
 * <br />
 * <b>Statistics:</b>
 * <ul>
 * <li>listservices</li>
 * <li>listwsdls</li>
 * <li>listpolicies</li>
 * <li>listschema</li>
 * </ul>
 *
 * @param <ServiceBean> a bean containing the list of services on the repository.
 * @param <WSDLBean>    a bean containing the list of WSDLs on the repository.
 * @param <PolicyBean>  a bean containing the list of policies on the repository.
 * @param <SchemaBean>  a bean containing the list of schema on the repository.
 */
public interface IListMetadataService<ServiceBean, WSDLBean, PolicyBean, SchemaBean> {

    /**
     * Method to list the services on the repository.
     *
     * @param criteria the filter criteria to be used should be a valid XML, which confirms to the
     *                 service configuration defined through {@link IConfigureServiceUIService}.
     *
     * @return a bean containing the list of services on the repository.
     * @throws RegistryException if the operation failed.
     */
    ServiceBean listservices(String criteria)throws RegistryException;

    /**
     * Method to list the WSDLs on the repository.
     *
     * @return a bean containing the list of WSDLs on the repository.
     * @throws RegistryException if the operation failed.
     */
    WSDLBean listwsdls()throws RegistryException;

    /**
     * Method to list the policies on the repository.
     *
     * @return a bean containing the list of policies on the repository.
     * @throws RegistryException if the operation failed.
     */
    PolicyBean listpolicies()throws RegistryException;

    /**
     * Method to list the schema on the repository.
     *
     * @return a bean containing the list of schema on the repository.
     * @throws RegistryException if the operation failed.
     */
    SchemaBean listschema()throws RegistryException;

    /**
     * Get all the states from the LC
     *
     * @param LCName
     * @return
     */
    String[] getAllLifeCycleState(String LCName);
}
