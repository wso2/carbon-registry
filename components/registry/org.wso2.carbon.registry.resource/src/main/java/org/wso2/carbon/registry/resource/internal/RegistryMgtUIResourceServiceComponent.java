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
package org.wso2.carbon.registry.resource.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.common.eventing.NotificationService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.resource.download.DownloadManagerService;
import org.wso2.carbon.registry.resource.services.utils.ContentUtil;
import org.wso2.carbon.registry.resource.servlets.ResourceServlet;
import javax.servlet.Servlet;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.registry.resource", 
         immediate = true)
public class RegistryMgtUIResourceServiceComponent {

    private static Log log = LogFactory.getLog(RegistryMgtUIResourceServiceComponent.class);

    private ResourceDataHolder dataHolder = ResourceDataHolder.getInstance();

    private RegistryService registryService = null;

    private ServiceRegistration serviceRegistration = null;

    @Activate
    protected void activate(ComponentContext context) {
        try {
            registerServlet(context.getBundleContext());
            log.debug("******* Registry Resources UI Management bundle is activated ******* ");
        } catch (Throwable e) {
            log.error("******* Failed to activate Registry Resources UI Management bundle ******* ", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.debug("******* Registry Resources UI Management bundle is deactivated ******* ");
    }

    public void registerServlet(BundleContext bundleContext) throws Exception {
        if (registryService == null) {
            String msg = "Unable to Register Servlet. Registry Service Not Found.";
            log.error(msg);
            throw new Exception(msg);
        }
        // Using HTTP Whiteboard pattern for servlet registration
        Dictionary<String, Object> properties = new Hashtable<>();
        
        // HTTP Whiteboard servlet pattern - URL mapping
        properties.put("osgi.http.whiteboard.servlet.pattern", "/registry/resources");
        
        // Servlet init parameters using whiteboard pattern
        // The init parameter key format: osgi.http.whiteboard.servlet.init.<param-name>
        properties.put("osgi.http.whiteboard.servlet.init.org.apache.abdera.protocol.server.Provider", 
                      "org.wso2.carbon.registry.app.RegistryProvider");
        
        // Use default context for the servlet
        properties.put("osgi.http.whiteboard.context.select", "(osgi.http.whiteboard.context.name=default)");
        
        // Create servlet instance
        ResourceServlet resourceServlet = new ResourceServlet();
        
        // Register servlet using whiteboard pattern
        serviceRegistration = bundleContext.registerService(Servlet.class.getName(), resourceServlet, properties);
        
        // Store registry service in ResourceDataHolder so servlet can access it
        // This replaces the servlet-attributes behavior from the old HTTP Service API
        dataHolder.setRegistryService(registryService);
    }

    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
        dataHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        this.registryService = null;
        dataHolder.setRegistryService(null);
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
    }

    @Reference(
             name = "registry.notification.service", 
             service = org.wso2.carbon.registry.common.eventing.NotificationService.class, 
             cardinality = ReferenceCardinality.OPTIONAL, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryNotificationService")
    protected void setRegistryNotificationService(NotificationService notificationService) {
        dataHolder.setRegistryNotificationService(notificationService);
    }

    protected void unsetRegistryNotificationService(NotificationService notificationService) {
        dataHolder.setRegistryNotificationService(null);
    }

    @Reference(
             name = "registry.download.service", 
             service = org.wso2.carbon.registry.resource.download.DownloadManagerService.class, 
             cardinality = ReferenceCardinality.OPTIONAL, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetDownloadManagerService")
    protected void setDownloadManagerService(DownloadManagerService downloadManagerService) {
        ContentUtil.setDownloadManagerService(downloadManagerService);
    }

    protected void unsetDownloadManagerService(DownloadManagerService downloadManagerService) {
        ContentUtil.setDownloadManagerService(null);
    }
}

