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
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.deployment.synchronizer.RegistryBasedArtifactRepository;
import org.wso2.carbon.registry.deployment.synchronizer.internal.utils.RegistryServiceReferenceHolder;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.registry.deployment.synchronizer" immediate="true"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="registry.service" immediate="true"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class RegistryDeploymentSynchronizerComponent {

    private static final Log log = LogFactory.getLog(RegistryDeploymentSynchronizerComponent.class);
    private ServiceRegistration registryDepSynServiceRegistration;

    protected void activate(ComponentContext context) {

        //Register the Registry Based Artifact Repository
        ArtifactRepository registryBasedArtifactRepository =  new RegistryBasedArtifactRepository();
        registryDepSynServiceRegistration =
                context.getBundleContext().registerService(ArtifactRepository.class.getName(),
                                                           registryBasedArtifactRepository, null);
    }

    protected void deactivate(ComponentContext context) {

        if(registryDepSynServiceRegistration != null){
            registryDepSynServiceRegistration.unregister();
            registryDepSynServiceRegistration = null;
        }
        log.debug("Registry Deployment synchronizer component deactivated");
    }

    protected void setConfigurationContextService(ConfigurationContextService service) {
        if (log.isDebugEnabled()) {
            log.debug("Registry deployment synchronizer component bound to the " +
                    "configuration context service");
        }
        RegistryServiceReferenceHolder.setConfigurationContextService(service);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService service) {
        if (log.isDebugEnabled()) {
            log.debug("Registry deployment synchronizer component unbound from the " +
                    "configuration context service");
        }
        RegistryServiceReferenceHolder.setConfigurationContextService(null);
    }

    protected void setRegistryService(RegistryService service) {
        if (log.isDebugEnabled()) {
            log.debug("Registry deployment synchronizer component bound to the registry service");
        }
        RegistryServiceReferenceHolder.setRegistryService(service);
    }

    protected void unsetRegistryService(RegistryService service) {
        if (log.isDebugEnabled()) {
            log.debug("Registry deployment synchronizer component unbound from the registry service");
        }
        RegistryServiceReferenceHolder.setRegistryService(null);
    }

    /**
     *
 * @scr.reference name="registry.eventing.service" immediate="true"
 * interface="org.wso2.carbon.registry.eventing.services.EventingService" cardinality="0..1"
 * policy="dynamic" bind="setEventingService" unbind="unsetEventingService"
     */
    /*protected void setEventingService(EventingService service) {
        if (log.isDebugEnabled()) {
            log.debug("Deployment synchronizer component bound to the registry eventing service");
        }
        ServiceReferenceHolder.setEventingService(service);

        // Call delayed auto checkout initialization
        // Because we are not waiting on the EventingService, the auto checkout feature
        // of some synchronizers may not get initialized at the first time
        if (observerRegistration != null) {
            DeploymentSynchronizationManager.getInstance().initDelayedAutoCheckout();
        }
    }

    protected void unsetEventingService(EventingService service) {
        if (log.isDebugEnabled()) {
            log.debug("Deployment synchronizer component unbound from the registry eventing service");
        }
        ServiceReferenceHolder.setEventingService(null);
    }*/
}
