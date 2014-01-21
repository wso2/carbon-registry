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

@Deprecated
public class AddUserPermissionUtil {

    private static final Log log = LogFactory.getLog(AddUserPermissionUtil.class);

    public static void addUserPermission(
            String pathToAuthorize,
            String userToAuthorize,
            String actionToAuthorize,
            String permissionType) throws Exception {

        throw new UnsupportedOperationException("This operation is no longer supported");

        /*UserRealm userRealm ;
        try {
            UserRegistry userRegistry = CommonUtil.getRegistry();
            userRealm = userRegistry.getUserRealm();
            userRealm.getAuthorizationManager();

        } catch (Exception e) {
            String msg =
                    "Couldn't get access control admin for changing authorizations. Caused by: " +
                            e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        try {
            String notificationResponse = "The following authorization has been added.";
            if (actionToAuthorize.equals("2")) {

                if (permissionType.equals("1")) {
                    userRealm.getAuthorizationManager().authorizeUser(userToAuthorize, pathToAuthorize, ActionConstants.GET);
                    notificationResponse += " READ: Allowed.";
                } else {
                    userRealm.getAuthorizationManager().denyUser(userToAuthorize, pathToAuthorize, ActionConstants.GET);
                    notificationResponse += " READ: Denied.";
                }
            }

            if (actionToAuthorize.equals("3")) {

                if (permissionType.equals("1")) {
                    userRealm.getAuthorizationManager().authorizeUser(userToAuthorize, pathToAuthorize, ActionConstants.PUT);
                    notificationResponse += " WRITE: Allowed.";
                } else {
                    userRealm.getAuthorizationManager().denyUser(userToAuthorize, pathToAuthorize, ActionConstants.PUT);
                    notificationResponse += " WRITE: Denied.";
                }
            }

            if (actionToAuthorize.equals("4")) {

                if (permissionType.equals("1")) {
                    userRealm.getAuthorizationManager().authorizeUser(userToAuthorize, pathToAuthorize, ActionConstants.DELETE);
                    notificationResponse += " DELETE: Allowed.";
                } else {
                    userRealm.getAuthorizationManager().denyUser(userToAuthorize, pathToAuthorize, ActionConstants.DELETE);
                    notificationResponse += " DELETE: Denied.";
                }
            }

            if (actionToAuthorize.equals("5")) {

                if (permissionType.equals("1")) {
                    userRealm.getAuthorizationManager().authorizeUser(userToAuthorize, pathToAuthorize, AccessControlConstants.AUTHORIZE);
                    notificationResponse += " AUTHORIZE: Allowed.";
                } else {
                    userRealm.getAuthorizationManager().denyUser(userToAuthorize, pathToAuthorize, AccessControlConstants.AUTHORIZE);
                    notificationResponse += " AUTHORIZE: Denied.";
                }
            }

            String message = "Permissions have been added for the user " + userToAuthorize + " on resource " +
                    pathToAuthorize + ". " + notificationResponse;
            boolean isResource;
            try {
                isResource = !(CommonUtil.getRegistry().get(pathToAuthorize) instanceof Collection);
            } catch (RegistryException e) {
                isResource = true;
            }
            RegistryEvent<String> event = new RegistryEvent<String>(message);
            if (isResource) {
                event.setTopic(pathToAuthorize + RegistryEvent.TOPIC_SEPARATOR + "ResourceUpdated");
            } else {
                event.setTopic(pathToAuthorize + RegistryEvent.TOPIC_SEPARATOR + "CollectionUpdated");
            }
            event.setTenantId(CommonUtil.getRegistry().getTenantId());
            CommonUtil.notify(event, CommonUtil.getRegistry(), pathToAuthorize);

            String msg = "User authorization performed successfully.";
            log.debug(msg);

        } catch (UserStoreException e) {
            String msg = "Failed to add user permission.";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }*/
    }
}
