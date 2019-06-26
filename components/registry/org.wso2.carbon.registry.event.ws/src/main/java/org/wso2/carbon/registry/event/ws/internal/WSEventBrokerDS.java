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
package org.wso2.carbon.registry.event.ws.internal;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.event.core.EventBroker;
import org.wso2.carbon.registry.event.core.internal.util.EventBrokerHolder;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "wsevent.component", 
         immediate = true)
public class WSEventBrokerDS {

    @Activate
    protected void activate(ComponentContext context) {
        WSEventBrokerHolder.getInstance().registerWSEventDispatcher();
    }

    @Reference(
             name = "eventbroker.service", 
             service = org.wso2.carbon.registry.event.core.EventBroker.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unSetEventBroker")
    protected void setEventBroker(EventBroker eventBroker) {
        WSEventBrokerHolder.getInstance().registerEventBroker(eventBroker);
    }

    protected void unSetEventBroker(EventBroker eventBroker) {
    }

    @Reference(
             name = "configurationcontext.service", 
             service = org.wso2.carbon.utils.ConfigurationContextService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        WSEventBrokerHolder.getInstance().registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
    }
}

