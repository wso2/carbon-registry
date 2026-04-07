
/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.registry.resource.ui.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.wso2.carbon.registry.resource.ui.ResourceServlet;
import org.wso2.carbon.registry.resource.ui.TenantAwareResourceFilter;

@Component(
        name = "org.wso2.carbon.registry.resource.ui.internal.ResourceUIServiceComponent",
        immediate = true)
/**
 * OSGi component that registers the ResourceServlet and TenantAwareResourceFilter using the HTTP Whiteboard pattern.
 */
public class ResourceUIServiceComponent {
    private static final Log log = LogFactory.getLog(ResourceUIServiceComponent.class);
    private ServiceRegistration serviceRegistration = null;
    private ServiceRegistration filterServiceRegistration = null;

    @Activate
    protected void activate(ComponentContext ctxt) {
        log.debug("Activating ResourceUIServiceComponent");
        BundleContext bundleContext = ctxt.getBundleContext();
        // Using HTTP Whiteboard pattern for servlet registration
        Dictionary<String, Object> properties = new Hashtable<>();

        // HTTP Whiteboard servlet pattern - URL mapping
        properties.put("osgi.http.whiteboard.servlet.pattern", "/registry/resourceContent");

        // Use default context for the servlet
        properties.put("osgi.http.whiteboard.context.select", "(osgi.http.whiteboard.context.name=carbonContext)");
        properties.put(Constants.SERVICE_RANKING, Integer.valueOf(220));
        // Create servlet instance
        ResourceServlet resourceServlet = new ResourceServlet();
        // Register servlet using whiteboard pattern
        serviceRegistration = bundleContext.registerService(Servlet.class.getName(), resourceServlet, properties);

        Filter tenantAwareResourceFilter = new TenantAwareResourceFilter();
        Dictionary<String, Object> parameters = new Hashtable<>();
        parameters.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN, "/t/*");
        parameters.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_NAME, "TenantAwareResourceFilter");
        parameters.put(Constants.SERVICE_RANKING, Integer.valueOf(210));
        parameters.put("osgi.http.whiteboard.context.select", "(osgi.http.whiteboard.context.name=carbonContext)");
        filterServiceRegistration =
                bundleContext.registerService("javax.servlet.Filter", tenantAwareResourceFilter, parameters);

    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        log.debug("Deactivating ResourceUIServiceComponent");
        serviceRegistration.unregister();
        filterServiceRegistration.unregister();
    }
}
