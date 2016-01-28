/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.metadata.server.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metadata.server.api.MetadataStore;
import org.wso2.carbon.metadata.server.impl.MetadataStoreImpl;

/**
 * Activator class for carbon metadata.
 *
 * @since 1.0.0
 */
public class CarbonMetadataBundleActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(CarbonMetadataBundleActivator.class);
    private ManagedService configAdminService;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        DataHolder.getInstance().setBundleContext(bundleContext);
        ServiceReference reference = bundleContext.getServiceReference(ManagedService.class);
        if (reference != null) {
            //configuring logging using the config admin service
            configAdminService = (ManagedService) bundleContext.getService(reference);
        } else {
            //Configuration admin service is a must to start carbon core.
            throw new IllegalStateException("Cannot start carbon core bundle since configuration " +
                    "admin service is not available");
        }
        MetadataStore metadataStore = new MetadataStoreImpl();
        bundleContext.registerService(MetadataStore.class.getName(), metadataStore, null);

        DataHolder.getInstance().setMetadataStore(metadataStore);
        logger.debug("Carbon metadata bundle is started successfully");

    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

        logger.debug("Carbon metadata bundle is stopped successfully");
    }
}
