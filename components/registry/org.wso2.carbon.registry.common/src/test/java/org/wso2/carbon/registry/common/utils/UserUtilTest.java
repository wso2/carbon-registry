/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.common.utils;

import junit.framework.TestCase;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.AccessControlConstants;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class UserUtilTest extends TestCase {

    public void testIsPutAllowed() throws Exception {
        String resourcePath = "/_system/governance/trunk";
        String userName = "admin";
        UserRegistry userRegistry = mock(UserRegistry.class);
        UserRealm userRealm = mock(UserRealm.class);
        AuthorizationManager authorizationManager = mock(AuthorizationManager.class);
        when(userRegistry.getUserRealm()).thenReturn(userRealm);
        when(userRealm.getAuthorizationManager()).thenReturn(authorizationManager);
        when(userRealm.getAuthorizationManager().isUserAuthorized(userName, resourcePath, ActionConstants.PUT))
                .thenReturn(true);
        assertTrue(UserUtil.isPutAllowed(userName, resourcePath, userRegistry));
    }

    public void testIsPutAllowedErrorCase() throws Exception {
        UserRegistry userRegistry = mock(UserRegistry.class);
        UserRealm userRealm = mock(UserRealm.class);
        when(userRegistry.getUserRealm()).thenReturn(userRealm);
        UserStoreException userStoreException = new UserStoreException("Custom Put Error");
        when(userRealm.getAuthorizationManager()).thenThrow(userStoreException);
        try {
            UserUtil.isPutAllowed("admin", "/_system/governance", userRegistry);
        } catch (RegistryException e) {
            assertEquals(
                    "Could not the permission details for the user: admin for the resource: /_system/governance. " +
                            "Caused by: Custom Put Error",
                    e.getMessage());
        }
    }

    public void testIsDeleteAllowed() throws Exception {
        String resourcePath = "/_system/governance/trunk";
        String userName = "admin";
        UserRegistry userRegistry = mock(UserRegistry.class);
        UserRealm userRealm = mock(UserRealm.class);
        AuthorizationManager authorizationManager = mock(AuthorizationManager.class);
        when(userRegistry.getUserRealm()).thenReturn(userRealm);
        when(userRealm.getAuthorizationManager()).thenReturn(authorizationManager);
        when(userRealm.getAuthorizationManager().isUserAuthorized(userName, resourcePath, ActionConstants.DELETE))
                .thenReturn(true);
        assertTrue(UserUtil.isDeleteAllowed(userName, resourcePath, userRegistry));
    }

    public void testIsDeleteAllowedErrorCase() throws Exception {
        UserRegistry userRegistry = mock(UserRegistry.class);
        UserRealm userRealm = mock(UserRealm.class);
        when(userRegistry.getUserRealm()).thenReturn(userRealm);
        UserStoreException userStoreException = new UserStoreException("Custom Delete Error");
        when(userRealm.getAuthorizationManager()).thenThrow(userStoreException);
        try {
            UserUtil.isDeleteAllowed("admin", "/_system/governance", userRegistry);
        } catch (RegistryException e) {
            assertEquals(
                    "Could not the permission details for the user: admin for the resource: /_system/governance. " +
                            "Caused by: Custom Delete Error",
                    e.getMessage());
        }
    }

    public void testIsGetAllowed() throws Exception {
        String resourcePath = "/_system/governance/trunk";
        String userName = "admin";
        UserRegistry userRegistry = mock(UserRegistry.class);
        UserRealm userRealm = mock(UserRealm.class);
        AuthorizationManager authorizationManager = mock(AuthorizationManager.class);
        when(userRegistry.getUserRealm()).thenReturn(userRealm);
        when(userRealm.getAuthorizationManager()).thenReturn(authorizationManager);
        when(userRealm.getAuthorizationManager().isUserAuthorized(userName, resourcePath, ActionConstants.GET))
                .thenReturn(true);
        assertTrue(UserUtil.isGetAllowed(userName, resourcePath, userRegistry));
    }

    public void testIsGetAllowedErrorCase() throws Exception {
        UserRegistry userRegistry = mock(UserRegistry.class);
        UserRealm userRealm = mock(UserRealm.class);
        when(userRegistry.getUserRealm()).thenReturn(userRealm);
        UserStoreException userStoreException = new UserStoreException("Custom Get Error");
        when(userRealm.getAuthorizationManager()).thenThrow(userStoreException);
        try {
            UserUtil.isGetAllowed("admin", "/_system/governance", userRegistry);
        } catch (RegistryException e) {
            assertEquals(
                    "Could not the permission details for the user: admin for the resource: /_system/governance. " +
                            "Caused by: Custom Get Error",
                    e.getMessage());
        }
    }

    public void testIsAuthorizeAllowed() throws Exception {
        String resourcePath = "/_system/governance/trunk";
        String userName = "admin";
        UserRegistry userRegistry = mock(UserRegistry.class);
        UserRealm userRealm = mock(UserRealm.class);
        AuthorizationManager authorizationManager = mock(AuthorizationManager.class);
        when(userRegistry.getUserRealm()).thenReturn(userRealm);
        when(userRealm.getAuthorizationManager()).thenReturn(authorizationManager);
        when(userRealm.getAuthorizationManager()
                     .isUserAuthorized(userName, resourcePath, AccessControlConstants.AUTHORIZE))
                .thenReturn(true);
        assertTrue(UserUtil.isAuthorizeAllowed(userName, resourcePath, userRegistry));
    }

    public void testIsAuthorizeAllowedErrorCase() throws Exception {
        UserRegistry userRegistry = mock(UserRegistry.class);
        UserRealm userRealm = mock(UserRealm.class);
        when(userRegistry.getUserRealm()).thenReturn(userRealm);
        UserStoreException userStoreException = new UserStoreException("Custom Authorize Error");
        when(userRealm.getAuthorizationManager()).thenThrow(userStoreException);
        try {
            UserUtil.isAuthorizeAllowed("admin", "/_system/governance", userRegistry);
        } catch (RegistryException e) {
            assertEquals(
                    "Could not the permission details for the user: admin for the resource: /_system/governance. " +
                            "Caused by: Custom Authorize Error",
                    e.getMessage());
        }
    }
}