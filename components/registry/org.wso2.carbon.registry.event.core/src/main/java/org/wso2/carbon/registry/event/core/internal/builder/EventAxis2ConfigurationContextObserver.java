/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.event.core.internal.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.event.core.EventBroker;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

import java.util.HashSet;
import java.util.Set;

public class EventAxis2ConfigurationContextObserver extends AbstractAxis2ConfigurationContextObserver {
	
    private static Log log = LogFactory.getLog(EventAxis2ConfigurationContextObserver.class);

    private EventBroker eventBroker;

    private Set<Integer> loadedTenants;

    public EventAxis2ConfigurationContextObserver() {
        this.loadedTenants = new HashSet<Integer>();
    }

    @Override
    public void creatingConfigurationContext(int tenantId) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            if (!this.loadedTenants.contains(tenantId)) {
                this.eventBroker.initializeTenant();
                this.loadedTenants.add(tenantId);
            }
        } catch (Exception e) {
            log.error("Error in setting tenant information", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void setEventBroker(EventBroker eventBroker) {
        this.eventBroker = eventBroker;
    }
    
}
