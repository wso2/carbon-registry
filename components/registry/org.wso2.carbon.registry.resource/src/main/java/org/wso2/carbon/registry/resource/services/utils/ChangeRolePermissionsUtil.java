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
import org.wso2.carbon.registry.common.eventing.RegistryEvent;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.AccessControlConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

public class ChangeRolePermissionsUtil {

    private static final Log log = LogFactory.getLog(ChangeRolePermissionsUtil.class);

    public static void changeRolePermissions(UserRegistry userRegistry,
                                             String resourcePath, String permissionString)
            throws Exception {

        AuthorizationManager accessControlAdmin ;
        UserRealm realm;
        try {
            realm = userRegistry.getUserRealm();
            accessControlAdmin = realm.getAuthorizationManager();

        } catch (Exception e) {
            String msg = "Couldn't get access control admin for changing authorizations. Caused by: " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        if (!realm.getAuthorizationManager().isUserAuthorized(userRegistry.getUserName(),resourcePath,
                AccessControlConstants.AUTHORIZE)) {
            String msg = userRegistry.getUserName()+" is not allowed to authorize resource " + resourcePath;
            log.error(msg);
            throw new RegistryException(msg);
        }

        try {

            String[] rolePermissions = permissionString.split("\\|");

            for (int i = 0; i < rolePermissions.length; i++) {

                String notificationResponse = "The following changes have been made.";
                if (rolePermissions[i].trim().length() == 0) {
                    continue;
                }

                String[] permissions = rolePermissions[i].split(":");
                String permRole = permissions[0];

                RealmConfiguration realmConfig = realm.getRealmConfiguration();
                if (!permRole.equals(realmConfig.getAdminRoleName())) {
                    accessControlAdmin.clearRoleAuthorization(permRole, resourcePath, ActionConstants.GET);
                    accessControlAdmin.clearRoleAuthorization(permRole, resourcePath, ActionConstants.PUT);
                    accessControlAdmin.clearRoleAuthorization(permRole, resourcePath, ActionConstants.DELETE);
                    accessControlAdmin.clearRoleAuthorization(permRole, resourcePath, AccessControlConstants.AUTHORIZE);
                }

                for (int j = 1; j < permissions.length; j++) {
                    String[] permission = permissions[j].split("\\^");

                    String action = permission[0];
                    String checked = permission[1];

                    if (action.equals("ra")) {
                        if (checked.equals("true")) {
                            accessControlAdmin.authorizeRole(permRole, resourcePath, ActionConstants.GET);
                            notificationResponse += " READ: Allowed.";
                        }
                    } else if (action.equals("rd")) {
                        if (checked.equals("true")) {
                            accessControlAdmin.denyRole(permRole, resourcePath, ActionConstants.GET);
                            notificationResponse += " READ: Denied.";
                        }
                    } else if (action.equals("wa")) {
                        if (checked.equals("true")) {
                            accessControlAdmin.authorizeRole(permRole, resourcePath, ActionConstants.PUT);
                            notificationResponse += " WRITE: Allowed.";
                        }
                    } else if (action.equals("wd")) {
                        if (checked.equals("true")) {
                            accessControlAdmin.denyRole(permRole, resourcePath, ActionConstants.PUT);
                            notificationResponse += " WRITE: Denied.";
                        }
                    } else if (action.equals("da")) {
                        if (checked.equals("true")) {
                            accessControlAdmin.authorizeRole(permRole, resourcePath, ActionConstants.DELETE);
                            notificationResponse += " DELETE: Allowed.";
                        }
                    } else if (action.equals("dd")) {
                        if (checked.equals("true")) {
                            accessControlAdmin.denyRole(permRole, resourcePath, ActionConstants.DELETE);
                            notificationResponse += " DELETE: Denied.";
                        }
                    } else if (action.equals("aa")) {
                        if (checked.equals("true")) {
                            accessControlAdmin.authorizeRole(permRole, resourcePath, AccessControlConstants.AUTHORIZE);
                            notificationResponse += " AUTHORIZE: Allowed.";
                        }
                    } else if (action.equals("ad")) {
                        if (checked.equals("true")) {
                            accessControlAdmin.denyRole(permRole, resourcePath, AccessControlConstants.AUTHORIZE);
                            notificationResponse += " AUTHORIZE: Denied.";
                        }
                    }
                }

                String message = "The permissions have been changed for the role " + permRole + " on resource " +
                        resourcePath + ". " + notificationResponse;
                boolean isResource;
                try {
                    isResource = !(userRegistry.get(resourcePath) instanceof Collection);
                } catch (RegistryException e) {
                    isResource = true;
                }
                RegistryEvent<String> event = new RegistryEvent<String>(message);
                if (isResource) {
//                    event.setTopic(resourcePath + RegistryEvent.TOPIC_SEPARATOR + "ResourceUpdated");
                    event.setTopic(RegistryEvent.TOPIC_SEPARATOR + "ResourceUpdated" + resourcePath);
                } else {
//                    event.setTopic(resourcePath + RegistryEvent.TOPIC_SEPARATOR + "CollectionUpdated");
                    event.setTopic(RegistryEvent.TOPIC_SEPARATOR + "CollectionUpdated" + resourcePath);
                }
                event.setTenantId(userRegistry.getTenantId());
                CommonUtil.notify(event, userRegistry, resourcePath);
            }

            String msg = "Role authorizations performed successfully.";
            log.debug(msg);
        } catch (UserStoreException e) {
            String msg = "Couldn't set authorizations. Caused by: " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }
}
