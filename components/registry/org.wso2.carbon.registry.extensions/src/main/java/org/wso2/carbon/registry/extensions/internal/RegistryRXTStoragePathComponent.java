/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.extensions.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.extensions.services.RXTStoragePathService;
import org.wso2.carbon.registry.extensions.services.RXTStoragePathServiceImpl;
import org.wso2.carbon.registry.extensions.services.Utils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@SuppressWarnings({ "unused", "JavaDoc" })
@Component(
         name = "org.wso2.carbon.registry.extensions", 
         immediate = true)
public class RegistryRXTStoragePathComponent {

    private static Log log = LogFactory.getLog(RegistryRXTStoragePathComponent.class);

    private RegistryService registryService;

    private ServiceRegistration extensionServiceRegistration = null;

    /**
     * Method to activate bundle.
     * @param context osgi component context.
     */
    @Activate
    protected void activate(ComponentContext context) {
        RXTStoragePathService service = new RXTStoragePathServiceImpl();
        extensionServiceRegistration = context.getBundleContext().registerService(RXTStoragePathService.class.getName(), service, null);
        Utils.setRxtService(service);
        if (log.isDebugEnabled()) {
            log.debug("******* Registry Extensions bundle is activated ******* ");
        }
    }

    /**
     * Method to deactivate bundle.
     * @param context osgi component context.
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (extensionServiceRegistration != null) {
            extensionServiceRegistration.unregister();
            extensionServiceRegistration = null;
        }
        Utils.setRxtService(null);
        if (log.isDebugEnabled()) {
            log.debug("******* Registry Extensions bundle is deactivated ******* ");
        }
    }

    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        this.registryService = null;
    }
}

