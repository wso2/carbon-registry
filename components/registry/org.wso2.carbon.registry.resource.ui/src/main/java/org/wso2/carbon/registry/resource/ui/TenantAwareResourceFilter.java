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

package org.wso2.carbon.registry.resource.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter that intercepts requests to ResourceServlet and delegates to it.
 * Handles both super tenant URLs (/registry/resourceContent)
 * and tenant URLs (/t/{tenant}/registry/resourceContent).
 */
public class TenantAwareResourceFilter implements Filter {

    private static final String RESOURCE_CONTENT_PATH = "/registry/resourceContent";
    private static final Pattern TENANT_PATTERN = Pattern.compile("^(/t/[^/]+)(/registry/resourceContent.*)$");
    private HttpServlet delegateServlet;

    private static final Log log = LogFactory.getLog(TenantAwareResourceFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        log.debug("TenantAwareResourceFilter.init() called");
        try {
            // Load and instantiate the ResourceServlet
            delegateServlet = new ResourceServlet();
            if (log.isDebugEnabled()) {
                log.debug("ResourceServlet loaded successfully: " + delegateServlet);
            }
            // Create a minimal ServletConfig for initialization
            ServletConfig servletConfig = new ServletConfig() {
                @Override
                public String getServletName() {
                    return "ResourceServlet";
                }

                @Override
                public ServletContext getServletContext() {
                    return filterConfig.getServletContext();
                }

                @Override
                public String getInitParameter(String name) {
                    return null;
                }

                @Override
                public java.util.Enumeration<String> getInitParameterNames() {
                    return java.util.Collections.emptyEnumeration();
                }
            };

            delegateServlet.init(servletConfig);
            log.debug("TenantAwareResourceServlet initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize: " + e.getMessage(), e);
            throw new ServletException("Failed to initialize TenantAwareResourceServlet", e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        log.debug("TenantAwareResourceFilter.doFilter() called");
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            String requestURI = httpRequest.getRequestURI();
            if (log.isDebugEnabled()) {
                log.debug("requestURI: " + requestURI);
            }
            if (requestURI != null && requestURI.endsWith(RESOURCE_CONTENT_PATH)) {
                if (delegateServlet != null) {
                    // Ensure the session is actually retrieved from the original request
                    // BEFORE the wrap, to wake up the StandardSession.
                    httpRequest.getSession(false);

                    HttpServletRequest wrappedRequest = wrapRequestForTenant(httpRequest);
                    log.debug("Request dispatched to delegate servlet");
                    delegateServlet.service(wrappedRequest, httpResponse);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * Wraps the request to adjust paths for tenant URLs, allowing the delegate servlet to operate as if it's always
     * at the root context. This is crucial for session handling and servlet context
     * access, especially when the application is accessed via tenant-specific URLs.
     * The wrapping ensures that the delegate servlet can retrieve the session and servlet context correctly,
     * regardless of whether the request is for a tenant or the super tenant.
     * The pattern matches URLs of the form /t/{tenant}/registry/resourceContent and extracts the tenant part and the
     * path without the tenant. The wrapped request then overrides methods to adjust the context and servlet paths
     * accordingly.
     * This approach allows the delegate servlet to function seamlessly in a multi-tenant environment without needing
     * to be aware of tenant-specific URL structures.
     *
     * @param request
     * @return
     */
    private HttpServletRequest wrapRequestForTenant(final HttpServletRequest request) {
        final String requestURI = request.getRequestURI();
        final String contextPath = request.getContextPath();
        final String appRelativeUri =
                requestURI.startsWith(contextPath) ? requestURI.substring(contextPath.length()) : requestURI;
        Matcher matcher = TENANT_PATTERN.matcher(appRelativeUri);
        if (matcher.matches()) {
            final String pathWithoutTenant = matcher.group(2);

            return new HttpServletRequestWrapper(request) {
                @Override
                public String getRequestURI() {
                    return getContextPath() + getServletPath();
                }

                @Override
                public StringBuffer getRequestURL() {
                    String originalUri = request.getRequestURI();
                    StringBuffer originalUrl = request.getRequestURL();
                    return new StringBuffer(
                            originalUrl.substring(0, originalUrl.length() - originalUri.length()))
                            .append(getRequestURI());
                }

                @Override
                public String getContextPath() {
                    // Returning empty or the actual root context often helps the
                    // cookie-matching logic for the session
                    return "";
                }

                @Override
                public String getServletPath() {
                    return pathWithoutTenant;
                }

                @Override
                public HttpSession getSession() {
                    return request.getSession();
                }

                @Override
                public HttpSession getSession(boolean create) {
                    return request.getSession(create);
                }

                @Override
                public ServletContext getServletContext() {
                    // This is the key: force the delegate to use the root ServletContext
                    // where the delegate servlet was initialized.
                    return request.getServletContext();
                }
            };
        }
        return request;
    }

    @Override
    public void destroy() {
        if (delegateServlet != null) {
            delegateServlet.destroy();
        }
    }
}
