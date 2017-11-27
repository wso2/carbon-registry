/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.rest.api.handler;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.auth.HttpAuthHeader;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.rest.api.exception.RestApiBasicAuthenticationException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;

public class RestApiBasicAuthenticationHandler implements RequestHandler {

    protected Log log = LogFactory.getLog(RestApiBasicAuthenticationHandler.class);

    /**
     * Implementation of RequestHandler.handleRequest method.
     * This method retrieves userName and password from Basic auth header,
     * and tries to authenticate against carbon user store
     *
     * Upon successful authentication allows process to proceed to retrieve requested REST resource
     * Upon invalid credentials returns a HTTP 401 UNAUTHORIZED response to client
     * Upon receiving a userStoreExceptions or IdentityException returns HTTP 500 internal server error to client
     *
     * @param message
     * @param classResourceInfo
     * @return Response
     */
    public Response handleRequest(Message message, ClassResourceInfo classResourceInfo) {
        if (log.isDebugEnabled()) {
            log.debug("Registry REST API Basic authentication handler execution started");
        }

        AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);
        if (policy != null && HttpAuthHeader.AUTH_TYPE_BASIC.equals(policy.getAuthorizationType())) {
            try {
                if (authenticate(policy.getUserName(), policy.getPassword())) {
                    return null;
                }
            } catch (RestApiBasicAuthenticationException e) {
                /* Upon an occurrence of exception log the caught exception
                 * and return a HTTP response with 500 server error response */
                log.error("Could not authenticate user : " + policy.getUserName() + "against carbon userStore", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            return Response.status(Response.Status.UNAUTHORIZED).header("WWW-Authenticate", HttpAuthHeader.AUTH_TYPE_BASIC).build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).header("WWW-Authenticate", HttpAuthHeader.AUTH_TYPE_BASIC).build();
    }

    /**
     * Checks whether a given userName:password combination authenticates correctly against carbon userStore
     * Upon successful authentication returns true, false otherwise
     *
     * @param userName
     * @param password
     * @return
     * @throws RestApiBasicAuthenticationException wraps and throws exceptions occur when trying to authenticate
     *                                             the user
     */
    private boolean authenticate(String userName, String password) throws RestApiBasicAuthenticationException {
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(userName);
        String userNameWithTenantDomain = tenantAwareUserName + "@" + tenantDomain;

        RealmService realmService = RegistryContext.getBaseInstance().getRealmService();
        TenantManager mgr = realmService.getTenantManager();

        int tenantId = 0;
        try {
            tenantId = mgr.getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new RestApiBasicAuthenticationException(
                    "Identity exception thrown while getting tenant ID for user : " + userNameWithTenantDomain, e);
        }

        // tenantId == -1, means an invalid tenant.
        if(tenantId == -1){
            if (log.isDebugEnabled()) {
                log.debug("Basic authentication request with an invalid tenant : " + userNameWithTenantDomain);
            }
            return false;
        }

        UserStoreManager userStoreManager = null;
        boolean authStatus = false;

        try {
            userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            authStatus = userStoreManager.authenticate(tenantAwareUserName, password);
        } catch (UserStoreException e) {
            throw new RestApiBasicAuthenticationException(
                    "User store exception thrown while authenticating user : " + userNameWithTenantDomain, e);
        }

        if(log.isDebugEnabled()){
            log.debug("Basic authentication request completed. " +
                    "Username : " + userNameWithTenantDomain +
                    ", Authentication State : " + authStatus);
        }

        if (authStatus){
            /* Upon successful authentication existing thread local carbon context
             * is updated to mimic the authenticated user */

            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setUsername(tenantAwareUserName);
            carbonContext.setTenantId(tenantId);
            carbonContext.setTenantDomain(tenantDomain);
        }
        return authStatus;

    }
}