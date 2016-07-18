/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.indexing.service;

import org.wso2.carbon.registry.common.TermData;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;


public interface TermsQuerySearchService {
    /**
     * Method to search for results matching the given parameters from the registry instance
     * provided.
     *
     * @param registry the registry instance to be used.
     * @param input    the input for the search
     *
     * @return search results.
     * @throws RegistryException if the operation failed.
     */
    public TermData[] search(UserRegistry registry, String input, String facetField) throws RegistryException;

    /**
     * Method to search for results matching the given parameters for the given tenant.
     *
     * @param tenantId the identifier of the tenant.
     * @param input    the input for the search
     *
     * @return search results.
     * @throws RegistryException if the operation failed.
     */
    public TermData[] search(int tenantId, String input, String facetField) throws RegistryException;

    /**
     * Method to search for results matching the given parameters.
     *
     * @param input the input for the search
     *
     * @return search results.
     * @throws RegistryException if the operation failed.
     */
    public TermData[] search(String input, String facetField) throws RegistryException;

}
