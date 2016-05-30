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

import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * This class acts as the data holder class to rxt data service.
 */
public class RxtDataServiceDataHolder {

    private static RxtDataServiceDataHolder instance = new RxtDataServiceDataHolder();
    private RegistryService registryService;
    private TenantRegistryLoader tenantRegistryLoader;
    private RealmService realmService;

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
     * This method is used to set TenantRegistryLoader
     *
     * @param tenantRegistryLoader tenantRegistryLoader
     */
    public void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        this.tenantRegistryLoader = tenantRegistryLoader;
    }

    /**
     * This method is used to get TenantRegistryLoader.
     *
     * @return tenantRegistryLoader
     */
    public TenantRegistryLoader getTenantRegistryLoader() {
        return tenantRegistryLoader;
    }

    /**
     * This method is used to set RealmService
     *
     * @param realmService tenantRegistryLoader
     */
    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    /**
     * This method is used to get RealmService.
     *
     * @return tenantRegistryLoader
     */
    public RealmService getRealmService() {
        return realmService;
    }
}
