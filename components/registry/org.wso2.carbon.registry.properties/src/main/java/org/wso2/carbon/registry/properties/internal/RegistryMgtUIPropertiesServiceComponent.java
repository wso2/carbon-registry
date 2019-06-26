/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.properties.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.registry.properties", 
         immediate = true)
public class RegistryMgtUIPropertiesServiceComponent {

    private static Log log = LogFactory.getLog(RegistryMgtUIPropertiesServiceComponent.class);

    private PropertiesDataHolder dataHolder = PropertiesDataHolder.getInstance();

    /**
     * Method to trigger when the OSGI component become active.
     *
     * @param context the component context
     */
    @Activate
    protected void activate(ComponentContext context) {
        log.debug("******* Registry Properties UI Management bundle is activated ******* ");
    }

    /**
     * Method to trigger when the OSGI component become inactive.
     *
     * @param context the component context
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.debug("******* Registry Properties UI Management bundle is deactivated ******* ");
    }

    /**
     * Method to trigger when the registry OSGI service is available
     *
     * @param registryService the registry service.
     */
    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(registryService);
    }

    /**
     * Method to trigger when the registry OSGI service is unavailable
     *
     * @param registryService the registry service.
     */
    protected void unsetRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(null);
    }
}

