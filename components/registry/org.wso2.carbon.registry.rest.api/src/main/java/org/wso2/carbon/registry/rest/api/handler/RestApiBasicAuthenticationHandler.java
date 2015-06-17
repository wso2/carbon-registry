package org.wso2.carbon.registry.rest.api.handler;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.rest.api.exception.RestApiBasicAuthenticationException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.TreeMap;

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

        TreeMap<String, ArrayList> httpHeadersTreemap =
                (TreeMap<String, ArrayList>) message.get(Message.PROTOCOL_HEADERS);
        ArrayList authHeaderList = httpHeadersTreemap.get("Authorization");

        if (authHeaderList == null){
            return null;
        }

        String authParam = ((String) authHeaderList.get(0));

        if (!authParam.startsWith("Basic ")){
            return null;
        }

        String credentials = authParam.substring(6).trim();
        String decodedCredentials = new String(new Base64().decode(credentials.getBytes()));
        String[] usernameAndPassword = decodedCredentials.split(":");
        String userName = usernameAndPassword[0];
        String password = usernameAndPassword[1];

        try {
            if (authenticate(userName, password)){
                return null;
            }
        } catch (RestApiBasicAuthenticationException e) {
            /* Upon an occurrence of exception log the caught exception
             * and return a HTTP response with 500 server error response */
            log.error("Could not authenticate user : " + userName + "against carbon userStore", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).build();
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
        userName = tenantAwareUserName + "@" + tenantDomain;

        int tenantId = 0;
        try {
            tenantId = IdentityUtil.getTenantIdOFUser(userName);
        } catch (IdentityException e) {
            throw new RestApiBasicAuthenticationException(
                    "Identity exception thrown while getting tenant ID for user : " + userName, e);
        }

        // tenantId == -1, means an invalid tenant.
        if(tenantId == -1){
            if (log.isDebugEnabled()) {
                log.debug("Basic authentication request with an invalid tenant : " + userName);
            }
            return false;
        }

        RealmService realmService = RegistryContext.getBaseInstance().getRealmService();
        UserStoreManager userStoreManager = null;
        boolean authStatus = false;

        try {
            userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            authStatus = userStoreManager.authenticate(tenantAwareUserName, password);
        } catch (UserStoreException e) {
            throw new RestApiBasicAuthenticationException(
                    "User store exception thrown while authenticating user : " + userName, e);
        }

        if(log.isDebugEnabled()){
            log.debug("Basic authentication request completed. " +
                    "Username : " + userName +
                    ", Authentication State : " + authStatus);
        }

        if (authStatus){
            /* Upon successful authentication existing thread local carbon context
             * is updated to mimic the authenticated user */

            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setUsername(userName);
            carbonContext.setTenantId(tenantId);
            carbonContext.setTenantDomain(tenantDomain);
        }
        return authStatus;

    }
}