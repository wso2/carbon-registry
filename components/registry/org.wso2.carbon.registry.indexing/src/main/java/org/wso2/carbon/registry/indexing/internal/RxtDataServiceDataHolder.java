/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.registry.indexing.internal;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.indexing.utils.RxtDataLoadUtils;

import java.util.HashMap;
import java.util.List;

/**
 * This class acts as the data holder class to rxt data service.
 */
public class RxtDataServiceDataHolder {

    private static RxtDataServiceDataHolder instance = new RxtDataServiceDataHolder();
    // Map to keep the rxt unbounded table entries.
    private static HashMap<String, List<String>> RxtDetails = new HashMap<>();
    private RegistryService registryService;

    public static RxtDataServiceDataHolder getInstance() {
        return instance;
    }

    /**
     * This method is used to set registry service
     *
     * @param registryService registry service
     */
    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    /**
     * This method is used to get registry service.
     *
     * @return
     */
    public RegistryService getRegistryService() {
        return registryService;
    }

    /**
     * This method is used to set rxt details.
     *
     * @param rxtDetails map of rxt details.
     * @throws RegistryException
     */
    public void setRxtDetails(HashMap<String, List<String>> rxtDetails) throws RegistryException {
        if (rxtDetails != null) {
            RxtDetails = rxtDetails;
        } else {
            RxtDetails = RxtDataLoadUtils.getRxtData(getRegistryService());
        }
    }

    /**
     * This method is used to get rxt details.
     *
     * @return  map of rxt details.
     */
    public HashMap<String, List<String>> getRxtDetails() {
        return RxtDetails;
    }
}
