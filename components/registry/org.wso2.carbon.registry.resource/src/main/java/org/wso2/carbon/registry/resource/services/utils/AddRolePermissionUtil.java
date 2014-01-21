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

package org.wso2.carbon.registry.resource.services.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.registry.core.utils.AccessControlConstants;
import org.wso2.carbon.registry.common.eventing.RegistryEvent;

public class AddRolePermissionUtil {

    private static final Log log = LogFactory.getLog(AddRolePermissionUtil.class);

    public static void addRolePermission(
            UserRegistry userRegistry,
            String pathToAuthorize,
            String roleToAuthorize,
            String actionToAuthorize,
            String permissionType) throws Exception {

        UserRealm userRealm ;
        try {
            userRealm = userRegistry.getUserRealm();
            userRealm.getAuthorizationManager();
        } catch (Exception e) {
            String msg =
                    "Couldn't get access control admin for changing authorizations. Caused by: " +
                            e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        if (!userRealm.getAuthorizationManager().isUserAuthorized(userRegistry.getUserName(),pathToAuthorize,
                AccessControlConstants.AUTHORIZE)) {
            String msg = userRegistry.getUserName()+" is not allowed to authorize resource " + pathToAuthorize;
            log.error(msg);
            throw new RegistryException(msg);
        }

        try {
            String notificationResponse = "The following authorization has been added.";
            if (actionToAuthorize.equals("2")) {

                if (permissionType.equals("1")) {
                    userRealm.getAuthorizationManager().authorizeRole(roleToAuthorize, pathToAuthorize, ActionConstants.GET);
                    notificationResponse += " READ: Allowed.";
                } else {
                    userRealm.getAuthorizationManager().denyRole(roleToAuthorize, pathToAuthorize, ActionConstants.GET);
                    notificationResponse += " READ: Denied.";
                }
            }

            if (actionToAuthorize.equals("3")) {

                if (permissionType.equals("1")) {
                    userRealm.getAuthorizationManager().authorizeRole(roleToAuthorize, pathToAuthorize, ActionConstants.PUT);
                    notificationResponse += " WRITE: Allowed.";
                } else {
                    userRealm.getAuthorizationManager().denyRole(roleToAuthorize, pathToAuthorize, ActionConstants.PUT);
                    notificationResponse += " WRITE: Denied.";
                }
            }

            if (actionToAuthorize.equals("4")) {

                if (permissionType.equals("1")) {
                    userRealm.getAuthorizationManager().authorizeRole(roleToAuthorize, pathToAuthorize, ActionConstants.DELETE);
                    notificationResponse += " DELETE: Allowed.";
                } else {
                    userRealm.getAuthorizationManager().denyRole(roleToAuthorize, pathToAuthorize, ActionConstants.DELETE);
                    notificationResponse += " DELETE: Denied.";
                }
            }

            if (actionToAuthorize.equals("5")) {

                if (permissionType.equals("1")) {
                    userRealm.getAuthorizationManager().authorizeRole(roleToAuthorize, pathToAuthorize, AccessControlConstants.AUTHORIZE);
                    notificationResponse += " AUTHORIZE: Allowed.";
                } else {
                    userRealm.getAuthorizationManager().denyRole(roleToAuthorize, pathToAuthorize, AccessControlConstants.AUTHORIZE);
                    notificationResponse += " AUTHORIZE: Denied.";
                }
            }

            String message = "Permissions have been added for the role " + roleToAuthorize + " on resource " +
                    pathToAuthorize + ". " + notificationResponse;
            boolean isResource;
            try {
                isResource = !(userRegistry.get(pathToAuthorize) instanceof Collection);
            } catch (RegistryException e) {
                isResource = true;
            }
            RegistryEvent<String> event = new RegistryEvent<String>(message);
            if (isResource) {
//                event.setTopic(pathToAuthorize + RegistryEvent.TOPIC_SEPARATOR + "ResourceUpdated");
                event.setTopic(RegistryEvent.TOPIC_SEPARATOR + "ResourceUpdated"+pathToAuthorize);
            } else {
//                event.setTopic(pathToAuthorize + RegistryEvent.TOPIC_SEPARATOR + "CollectionUpdated");
                event.setTopic(RegistryEvent.TOPIC_SEPARATOR + "CollectionUpdated"+pathToAuthorize);
            }
            event.setTenantId(userRegistry.getTenantId());
            CommonUtil.notify(event, userRegistry, pathToAuthorize);

            String msg = "Role authorization performed successfully.";
            log.debug(msg);

        } catch (UserStoreException e) {
            String msg = "Failed to add role permissions. " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }
}
