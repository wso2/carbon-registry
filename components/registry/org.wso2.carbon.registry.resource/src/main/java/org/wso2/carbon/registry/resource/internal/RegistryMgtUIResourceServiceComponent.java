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
import org.osgi.service.http.HttpService;
import org.wso2.carbon.registry.common.eventing.NotificationService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.resource.download.DownloadManagerService;
import org.wso2.carbon.registry.resource.services.utils.CommonUtil;
import org.wso2.carbon.registry.resource.services.utils.ContentUtil;
import org.wso2.carbon.registry.resource.servlets.ResourceServlet;

import javax.servlet.Servlet;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @scr.component name="org.wso2.carbon.registry.resource" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="http.service" interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic" bind="setHttpService" unbind="unsetHttpService"
 * @scr.reference name="registry.notification.service"
 * interface="org.wso2.carbon.registry.common.eventing.NotificationService" cardinality="0..1"
 * policy="dynamic" bind="setRegistryNotificationService" unbind="unsetRegistryNotificationService"
 * @scr.reference name="registry.download.service"
 * interface="org.wso2.carbon.registry.resource.download.DownloadManagerService" cardinality="0..1"
 * policy="dynamic" bind="setDownloadManagerService" unbind="unsetDownloadManagerService"
 */
public class RegistryMgtUIResourceServiceComponent {

    private static Log log = LogFactory.getLog(RegistryMgtUIResourceServiceComponent.class);

    private RegistryService registryService = null;
    private HttpService httpService = null;
    private ServiceRegistration serviceRegistration = null;

    protected void activate(ComponentContext context) {
        try {
            registerServlet(context.getBundleContext());
            log.debug("******* Registry Resources UI Management bundle is activated ******* ");
        } catch (Throwable e) {
            log.error("******* Failed to activate Registry Resources UI Management bundle ******* ", e);
        }
    }

    protected void deactivate(ComponentContext context) {
        log.debug("******* Registry Resources UI Management bundle is deactivated ******* ");
    }

    public void registerServlet(BundleContext bundleContext) throws Exception {

        if (registryService == null) {
            String msg = "Unable to Register Servlet. Registry Service Not Found.";
            log.error(msg);
            throw new Exception(msg);
        }

        Dictionary servletParam = new Hashtable(2);
        servletParam.put("org.apache.abdera.protocol.server.Provider", "org.wso2.carbon.registry.app.RegistryProvider");
        Dictionary servletAttributes = new Hashtable(2);
        servletAttributes.put("registry", registryService);
        Dictionary params = new Hashtable(2);
        params.put("servlet-params", servletParam);
        params.put("url-pattern", "/registry/resources");
        params.put("servlet-attributes", servletAttributes);

        ResourceServlet resourceServlet = new ResourceServlet();
        // The HTTP Service must be available for this operation
        serviceRegistration = bundleContext.registerService(Servlet.class.getName(), resourceServlet, params);
    }

    protected void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
        CommonUtil.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        this.registryService = null;
        CommonUtil.setRegistryService(null);
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
    }

    protected void setRegistryNotificationService(NotificationService notificationService) {
        CommonUtil.setRegistryNotificationService(notificationService);
    }

    protected void unsetRegistryNotificationService(NotificationService notificationService) {
        CommonUtil.setRegistryNotificationService(null);
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    protected void setDownloadManagerService(DownloadManagerService downloadManagerService) {
        ContentUtil.setDownloadManagerService(downloadManagerService);
    }

    protected void unsetDownloadManagerService(DownloadManagerService downloadManagerService) {
        ContentUtil.setDownloadManagerService(null);
    }

}

