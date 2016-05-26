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
import org.wso2.carbon.registry.indexing.utils.RxtDataLoadUtils;

/**
 * @scr.component name="rxt.data.service.component"" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 */
public class RxtDataServiceComponent {

    private static final Log log = LogFactory.getLog(RxtDataServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("RXTDataServiceComponent bundle is activated");
            }
            RxtDataServiceDataHolder.getInstance().setRxtDetails(null);

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
     * @param registryService   registry service
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
     * @param registryService   registry service
     */
    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Un setting RegistryService");
        }
        RxtDataServiceDataHolder.getInstance().setRegistryService(null);
    }
}
