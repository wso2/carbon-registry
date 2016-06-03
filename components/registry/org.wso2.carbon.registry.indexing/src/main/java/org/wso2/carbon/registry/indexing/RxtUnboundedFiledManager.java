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
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.internal.RxtDataServiceDataHolder;
import org.wso2.carbon.registry.indexing.utils.RxtUnboundedDataLoadUtils;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.HashMap;
import java.util.List;

/**
 * This class is used to set rxt unbounded filed details to the memory.
 */
public class RxtUnboundedFiledManager extends AbstractAdmin {

    private static RxtUnboundedFiledManager rxtUnboundedFiledManagerInstance = new RxtUnboundedFiledManager();
    private static HashMap<Integer, HashMap<String, List<String>>> allTenantsUnboundedFields = new HashMap<>();

    public static RxtUnboundedFiledManager getInstance() {
        return rxtUnboundedFiledManagerInstance;
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
     * This method is used to update a specific tenants unbounded fields.
     *
     * @param rxtConfig rxt configuration
     * @throws RegistryException
     */
    public void setActiveTenantsUnboundedFields(String rxtConfig) throws RegistryException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        UserRegistry registry = RxtDataServiceDataHolder.getInstance().getRegistryService().getRegistry();
        HashMap<String, List<String>> tenantRxtUnboundedEntries = RxtUnboundedDataLoadUtils.getRxtData(registry);
        allTenantsUnboundedFields.put(tenantId, tenantRxtUnboundedEntries);
    }

    /**
     * This method is used to set a specific tenants unbounded filed values.
     *
     * @param tenantId      tenant Id
     * @param rxtUnboundedFiledMap    rxt unbounded filed map.
     */
    public void setTenantsUnboundedFields(Integer tenantId, HashMap<String, List<String>> rxtUnboundedFiledMap){
        allTenantsUnboundedFields.put(tenantId, rxtUnboundedFiledMap);
    }
}
