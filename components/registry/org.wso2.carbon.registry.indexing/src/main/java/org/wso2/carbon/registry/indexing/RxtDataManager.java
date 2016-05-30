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

package org.wso2.carbon.registry.indexing;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.internal.RxtDataServiceDataHolder;
import org.wso2.carbon.registry.indexing.utils.RxtDataLoadUtils;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.HashMap;
import java.util.List;

/**
 * This class is used to set rxt unbounded filed details to the memory.
 */
public class RxtDataManager extends AbstractAdmin {

    private static RxtDataManager rxtDataManagerInstance = new RxtDataManager();
    private static HashMap<Integer, HashMap<String, List<String>>> allTenantsUnboundedFields = new HashMap<>();

    public static RxtDataManager getInstance() {
        return rxtDataManagerInstance;
    }

    /**
     * This method is used to get unbounded rxt filed values from memory.
     *
     * @return unbounded rxt filed values.
     */
    public HashMap<Integer, HashMap<String, List<String>>> getTenantsUnboundedFileds() {
        return allTenantsUnboundedFields;
    }

    /**
     * This method is used to set unbounded rxt filed values to memory.
     *
     * @throws RegistryException
     */
    public void setAllTenantsUnboundedFields() throws RegistryException {

        try {
            TenantManager tenantManager = RxtDataServiceDataHolder.getInstance().getRealmService().getTenantManager();
            Tenant[] tenants = tenantManager.getAllTenants();

            for (Tenant tenant : tenants) {
                int tenantId = tenant.getId();
                tenant = tenantManager.getTenant(tenant.getId());
                RxtDataServiceDataHolder.getInstance().getTenantRegistryLoader().loadTenantRegistry(tenantId);
                UserRegistry registry = RxtDataServiceDataHolder.getInstance()
                        .getRegistryService().getRegistry(tenant.getAdminName(), tenantId);
                HashMap<String, List<String>> rxtDetails = RxtDataLoadUtils.getRxtData(registry);
                allTenantsUnboundedFields.put(tenantId, rxtDetails);
            }
            // Add super tenant's rxt unbounded fields
            UserRegistry registry = RxtDataServiceDataHolder.getInstance().getRegistryService().getRegistry();
            HashMap<String, List<String>> rxtDetails = RxtDataLoadUtils.getRxtData(registry);
            allTenantsUnboundedFields.put(-1234, rxtDetails);
        } catch (UserStoreException e) {
            throw new RegistryException("Error while getting all tenant list", e);
        }
    }

    /**
     * This method is used to update a specific tenants unbounded fields.
     *
     * @param rxtConfig rxt configuration
     * @throws RegistryException
     */
    public void setActiveTenantsUnboundedFields(String rxtConfig) throws RegistryException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        UserRegistry registry = RxtDataServiceDataHolder.getInstance().getRegistryService().getRegistry();
        HashMap<String, List<String>> superTenantRxtUnboundedEntries = RxtDataLoadUtils.getRxtData(registry);
        allTenantsUnboundedFields.put(tenantId, superTenantRxtUnboundedEntries);
    }
}
