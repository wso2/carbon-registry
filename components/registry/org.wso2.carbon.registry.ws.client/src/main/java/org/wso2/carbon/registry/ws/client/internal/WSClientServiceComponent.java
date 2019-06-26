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
package org.wso2.carbon.registry.ws.client.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryProvider;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.ConfigurationContextService;
import java.util.Hashtable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Service Component for Client to WS API.
 */
@Component(
         name = "registry.ws.client.component", 
         immediate = true)
public class WSClientServiceComponent {

    private WSClientDataHolder dataHolder = WSClientDataHolder.getInstance();

    private static Log log = LogFactory.getLog(WSClientServiceComponent.class);

    private ServiceRegistration serviceRegistration = null;

    @Activate
    protected void activate(ComponentContext context) {
        RegistryProvider provider = new RegistryProvider() {

            private WSRegistryServiceClient client = null;

            private ScheduledExecutorService scheduledExecutor;

            public Registry getRegistry(String registryURL, String username, String password) throws RegistryException {
                if (client != null) {
                    return client;
                }
                if (registryURL != null && username != null && password != null) {
                    if (registryURL.endsWith("/")) {
                        registryURL = registryURL.substring(0, registryURL.length() - 1);
                    }
                    String serverURL = registryURL.substring(0, registryURL.indexOf("/registry")) + "/services/";
                    RegistryUtils.setTrustStoreSystemProperties();
                    client = new WSRegistryServiceClient(serverURL, username, password, dataHolder.getConfigurationContext());
                    startExecutor(100000);
                    return client;
                }
                throw new RegistryException("Unable to create an instance of a WS Registry");
            }

            private void startExecutor(int timePeriod) {
                if (scheduledExecutor == null) {
                    scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
                    scheduledExecutor.scheduleWithFixedDelay(new Runnable() {

                        @Override
                        public void run() {
                            client = null;
                        }
                    }, timePeriod, timePeriod, TimeUnit.MILLISECONDS);
                }
            }
        };
        Hashtable<String, String> ht = new Hashtable<String, String>();
        ht.put("type", "ws");
        serviceRegistration = context.getBundleContext().registerService(RegistryProvider.class.getName(), provider, ht);
        if (log.isDebugEnabled()) {
            log.info("Registry WS Client bundle is activated");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
        if (log.isDebugEnabled()) {
            log.info("Registry WS Client bundle is deactivated");
        }
    }

    @Reference(
             name = "config.context.service", 
             service = org.wso2.carbon.utils.ConfigurationContextService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        dataHolder.setConfigurationContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        dataHolder.setConfigurationContext(null);
    }
}

