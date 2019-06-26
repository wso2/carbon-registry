/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.deployment.synchronizer.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.deployment.synchronizer.ArtifactRepository;
import org.wso2.carbon.registry.deployment.synchronizer.utils.RegistryServiceReferenceHolder;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.deployment.synchronizer.RegistryBasedArtifactRepository;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.registry.deployment.synchronizer", 
         immediate = true)
public class RegistryDeploymentSynchronizerComponent {

    private static final Log log = LogFactory.getLog(RegistryDeploymentSynchronizerComponent.class);

    private ServiceRegistration registryDepSynServiceRegistration;

    @Activate
    protected void activate(ComponentContext context) {
        // Register the Registry Based Artifact Repository
        ArtifactRepository registryBasedArtifactRepository = new RegistryBasedArtifactRepository();
        registryDepSynServiceRegistration = context.getBundleContext().registerService(ArtifactRepository.class.getName(), registryBasedArtifactRepository, null);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (registryDepSynServiceRegistration != null) {
            registryDepSynServiceRegistration.unregister();
            registryDepSynServiceRegistration = null;
        }
        log.debug("Registry Deployment synchronizer component deactivated");
    }

    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService service) {
        if (log.isDebugEnabled()) {
            log.debug("Deployment synchronizer component bound to the registry service");
        }
        RegistryServiceReferenceHolder.setRegistryService(service);
    }

    protected void unsetRegistryService(RegistryService service) {
        if (log.isDebugEnabled()) {
            log.debug("Deployment synchronizer component unbound from the registry service");
        }
        RegistryServiceReferenceHolder.setRegistryService(null);
    }

    /**
     * This method is used to set configuration context service.
     *
     * @param service   configuration context service.
     */
    @Reference(
             name = "configuration.context.service", 
             service = org.wso2.carbon.utils.ConfigurationContextService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService service) {
        if (log.isDebugEnabled()) {
            log.debug("carbon-registry deployment synchronizer component bound to the configuration context service");
        }
        RegistryServiceReferenceHolder.setConfigurationContextService(service);
    }

    /**
     * This method is used to unset configuration context service.
     *
     * @param service   configuration context service.
     */
    protected void unsetConfigurationContextService(ConfigurationContextService service) {
        if (log.isDebugEnabled()) {
            log.debug("carbon-registry deployment synchronizer component unbound from the configuration context service");
        }
        RegistryServiceReferenceHolder.setEventingService(null);
    }
}

