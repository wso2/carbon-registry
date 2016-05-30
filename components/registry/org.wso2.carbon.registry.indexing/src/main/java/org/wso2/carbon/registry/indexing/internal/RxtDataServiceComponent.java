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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.indexing.RxtDataManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="rxt.data.service.component"" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="tenant.registryloader" interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1" policy="dynamic" bind="setTenantRegistryLoader" unbind="unsetTenantRegistryLoader"
 * @scr.reference name="realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 */
public class RxtDataServiceComponent {

    private static final Log log = LogFactory.getLog(RxtDataServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("RXTDataServiceComponent bundle is activated");
            }
            RxtDataManager.getInstance().setAllTenantsUnboundedFields();

        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.error("Failed to activate ServerAdmin bundle", e);
            }
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("ServerAdmin bundle is deactivated");
    }

    /**
     * This method is used to set registry service
     *
     * @param registryService registry service
     */
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting RegistryService");
        }
        RxtDataServiceDataHolder.getInstance().setRegistryService(registryService);
    }

    /**
     * This method is used to unset registry service.
     *
     * @param registryService registry service
     */
    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Un setting RegistryService");
        }
        RxtDataServiceDataHolder.getInstance().setRegistryService(null);
    }

    /**
     * This method is used to set TenantRegistryLoader
     *
     * @param tenantRegistryLoader tenantRegistryLoader
     */
    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        if (log.isDebugEnabled()) {
            log.debug("Setting TenantRegistryLoader");
        }
        RxtDataServiceDataHolder.getInstance().setTenantRegistryLoader(tenantRegistryLoader);
    }

    /**
     * This method is used to unset registry service.
     *
     * @param tenantRegistryLoader tenantRegistryLoader
     */
    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        if (log.isDebugEnabled()) {
            log.debug("Un setting TenantRegistryLoader");
        }
        RxtDataServiceDataHolder.getInstance().setTenantRegistryLoader(null);
    }

    /**
     * This method is used to set RealmService
     *
     * @param realmService RealmService
     */
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting RealmService");
        }
        RxtDataServiceDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * This method is used to unset RealmService.
     *
     * @param realmService RealmService
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Un setting TenantRegistryLoader");
        }
        RxtDataServiceDataHolder.getInstance().setRealmService(null);
    }
}
