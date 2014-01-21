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

package org.wso2.carbon.registry.servlet.internal;

import org.apache.abdera.protocol.server.servlet.AbderaServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.registry.app.ResourceServlet;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.servlet.UDDIServlet;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @scr.component name="org.wso2.carbon.registry.servlet" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="http.service" interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic"  bind="setHttpService" unbind="unsetHttpService"
 * @scr.reference name="registry.uddi" interface="org.wso2.carbon.registry.core.servlet.UDDIServlet"
 * cardinality="0..1" policy="dynamic"  bind="setJUDDIRegistryServlet"
 * unbind="unsetJUDDIRegistryServlet"
 */
public class RegistryAtomServiceComponent {

    private static Log log = LogFactory.getLog(RegistryAtomServiceComponent.class);

    private RegistryService registryService = null;
    private HttpService httpService = null;
    private UDDIServlet juddiRegistryServlet = null;
    private boolean juddiRegistryServletRegistered = false;
    private HttpContext defaultHttpContext = null;

    protected void activate(ComponentContext context) {
        try {
            registerServlet();
            log.debug("******* Registry APP bundle is activated ******* ");
        } catch (Throwable e) {
            log.error("******* Failed to activate Registry APP bundle ******* ", e);
        }
    }

    protected void deactivate(ComponentContext context) {
        httpService.unregister("/registry/atom");
        httpService.unregister("/registry/tags");
        httpService.unregister("/registry/resource");
        if (juddiRegistryServletRegistered) {
            httpService.unregister("/juddiv3");
            juddiRegistryServletRegistered = false;
        }
        log.debug("******* Registry APP bundle is deactivated ******* ");
    }

    private void registerServlet() throws Exception {

        if (registryService == null) {
            String msg = "Unable to Register Servlet. Registry Service Not Found.";
            log.error(msg);
            throw new Exception(msg);
        }

        if (!CarbonUtils.isRemoteRegistry()) {

            Dictionary servletParam = new Hashtable(2);
            servletParam.put("org.apache.abdera.protocol.server.Provider", "org.wso2.carbon.registry.app.RegistryProvider");
            httpService.registerServlet("/registry/atom", new AbderaServlet(), servletParam, defaultHttpContext);
            httpService.registerServlet("/registry/tags", new AbderaServlet(), servletParam, defaultHttpContext);
        }
        registerJUDDIServlet();
        httpService.registerServlet("/registry/resource", new ResourceServlet(), null, defaultHttpContext);
    }

    private void registerJUDDIServlet() {
        if (juddiRegistryServlet != null && httpService != null) {
            try {
                httpService.registerServlet("/juddiv3", juddiRegistryServlet, null, defaultHttpContext);
            } catch (Exception e) {
                log.error("Unable to register jUDDI servlet", e);
            }
            juddiRegistryServletRegistered = true;
        }
    }

    protected void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        this.registryService = null;
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
        this.defaultHttpContext = httpService.createDefaultHttpContext();
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    protected void setJUDDIRegistryServlet(UDDIServlet juddiRegistryServlet) {
        this.juddiRegistryServlet = juddiRegistryServlet;
        registerJUDDIServlet();
    }

    protected void unsetJUDDIRegistryServlet(UDDIServlet juddiRegistryServlet) {
        this.juddiRegistryServlet = null;
    }
}
