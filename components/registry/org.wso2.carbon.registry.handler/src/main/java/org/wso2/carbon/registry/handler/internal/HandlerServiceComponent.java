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
package org.wso2.carbon.registry.handler.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.service.SimulationService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.handler.util.CommonUtil;
import org.wso2.carbon.registry.handler.listener.HandlerLoader;
import org.wso2.carbon.core.services.callback.LoginSubscriptionManagerService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.registry.handler", 
         immediate = true)
public class HandlerServiceComponent {

    private static Log log = LogFactory.getLog(HandlerServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        try {
            log.debug("******* Handler Management Service bundle is activated ******* ");
        } catch (Exception e) {
            log.debug("******* Failed to activate Handler Management Service bundle ******* ");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.debug("******* Handler Management Service bundle is deactivated ******* ");
    }

    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        CommonUtil.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        CommonUtil.setRegistryService(null);
    }

    @Reference(
             name = "registry.simulation.service", 
             service = org.wso2.carbon.registry.core.service.SimulationService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetSimulationService")
    protected void setSimulationService(SimulationService simulationService) {
        CommonUtil.setSimulationService(simulationService);
    }

    protected void unsetSimulationService(SimulationService simulationService) {
        CommonUtil.setSimulationService(null);
    }

    @Reference(
             name = "login.subscription.service", 
             service = org.wso2.carbon.core.services.callback.LoginSubscriptionManagerService.class, 
             cardinality = ReferenceCardinality.OPTIONAL, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetLoginSubscriptionManagerService")
    protected void setLoginSubscriptionManagerService(LoginSubscriptionManagerService loginManager) {
        log.debug("******* LoginSubscriptionManagerService is set ******* ");
        loginManager.subscribe(new HandlerLoader());
    }

    protected void unsetLoginSubscriptionManagerService(LoginSubscriptionManagerService loginManager) {
        log.debug("******* LoginSubscriptionManagerService is unset ******* ");
    }
}

