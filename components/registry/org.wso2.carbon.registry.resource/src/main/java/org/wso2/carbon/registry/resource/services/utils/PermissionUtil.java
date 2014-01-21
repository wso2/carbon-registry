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
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.config.RegistryConfiguration;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.beans.PermissionBean;
import org.wso2.carbon.registry.resource.beans.PermissionEntry;
import org.wso2.carbon.registry.common.utils.UserUtil;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.registry.core.utils.AccessControlConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionUtil {

    private static final Log log = LogFactory.getLog(PermissionUtil.class);

    public static PermissionBean getPermissions(UserRegistry userRegistry, String path) throws Exception {

        ResourcePath resourcePath = new ResourcePath(path);
        String userName = userRegistry.getUserName();
        PermissionBean permissionsBean = new PermissionBean();
        permissionsBean.setPathWithVersion(resourcePath.getPathWithVersion());
        permissionsBean.setVersionView(!resourcePath.isCurrentVersion());

        UserRealm userRealm = userRegistry.getUserRealm();

        String[] userNames = userRealm.getUserStoreManager().listUsers("*", 100);

        // remove the admin and system
        ArrayList<String> filteredUserNames = new ArrayList<String>();

        RealmConfiguration realmConfig = userRealm.getRealmConfiguration();
        String systemUserName = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        String adminUserName = realmConfig.getAdminUserName();
        for (String userN: userNames) {
            if (userN.equals(adminUserName) ||
                    userN.equals(systemUserName)) {
                continue;
            }
            filteredUserNames.add(userN);
        }

        userNames = filteredUserNames.toArray(new String[filteredUserNames.size()]);

        permissionsBean.setUserNames(userNames);

        String[] roleNames = userRealm.getUserStoreManager().getRoleNames();
        // remove the admin role
        ArrayList<String> filteredRoleNames = new ArrayList<String>();

        String adminRoleName = realmConfig.getAdminRoleName();
        for (String roleN: roleNames) {
            if (roleN.equals(adminRoleName)) {
                continue;
            }
            filteredRoleNames.add(roleN);
        }
        // adding anonymous role
        filteredRoleNames.add(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME);

        roleNames = filteredRoleNames.toArray(new String[filteredRoleNames.size()]);
        permissionsBean.setRoleNames(roleNames);

        String authorizationPath = path;
        if (path.indexOf("?") > 0) {
            authorizationPath = path.split("\\?")[0];
        } else if (path.indexOf(RegistryConstants.URL_SEPARATOR) > 0) {
            authorizationPath = path.split("\\;")[0];
        }

        if (UserUtil.isPutAllowed(userName, authorizationPath, userRegistry)) {
            permissionsBean.setPutAllowed(true);
        } else {
            permissionsBean.setPutAllowed(false);
        }

        if (UserUtil.isDeleteAllowed(userName, authorizationPath, userRegistry)) {
            permissionsBean.setDeleteAllowed(true);
        } else {
            permissionsBean.setDeleteAllowed(false);
        }

        if (UserUtil.isAuthorizeAllowed(userName, authorizationPath, userRegistry)) {
            permissionsBean.setAuthorizeAllowed(true);
        } else {
            permissionsBean.setAuthorizeAllowed(false);
        }

        permissionsBean.setUserPermissions(getUserPermissions(userRealm, path));
        permissionsBean.setRolePermissions(getRolePermissions(userRealm, path));

        return permissionsBean;
    }

    private static PermissionEntry[] getUserPermissions(UserRealm userRealm, String path)
            throws UserStoreException {

        Map <String, PermissionEntry> userPermissionMap = new HashMap <String, PermissionEntry>();

        AuthorizationManager authorizer = userRealm.getAuthorizationManager();

        RealmConfiguration realmConfig = userRealm.getRealmConfiguration();
        String systemUserName = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        String adminUserName = realmConfig.getAdminUserName();
        String[] raUsers = authorizer.getExplicitlyAllowedUsersForResource(path, ActionConstants.GET);
        for (String raUser : raUsers) {
            if (raUser.equals(systemUserName) || raUser.equals(adminUserName)) {
                continue;
            }
            if (userPermissionMap.containsKey(raUser)) {
                PermissionEntry permission = userPermissionMap.get(raUser);
                permission.setReadAllow(true);
            } else {
                PermissionEntry permission = new PermissionEntry();
                permission.setUserName(raUser);
                permission.setReadAllow(true);
                userPermissionMap.put(raUser, permission);
            }
        }

        String[] rdUsers = authorizer.getExplicitlyDeniedUsersForResource(path, ActionConstants.GET);
        for (String rdUser : rdUsers) {
            if (rdUser.equals(systemUserName) || rdUser.equals(adminUserName)) {
                continue;
            }
            if (userPermissionMap.containsKey(rdUser)) {
                PermissionEntry permission = userPermissionMap.get(rdUser);
                permission.setReadDeny(true);
            } else {
                PermissionEntry permission = new PermissionEntry();
                permission.setUserName(rdUser);
                permission.setReadDeny(true);
                userPermissionMap.put(rdUser, permission);
            }
        }

        String[] waUsers = authorizer.getExplicitlyAllowedUsersForResource(path, ActionConstants.PUT);
        for (String waUser : waUsers) {
            if (waUser.equals(systemUserName) || waUser.equals(adminUserName)) {
                continue;
            }
            if (userPermissionMap.containsKey(waUser)) {
                PermissionEntry permission = userPermissionMap.get(waUser);
                permission.setWriteAllow(true);
            } else {
                PermissionEntry permission = new PermissionEntry();
                permission.setUserName(waUser);
                permission.setWriteAllow(true);
                userPermissionMap.put(waUser, permission);
            }
        }
        
        String[] wdUsers = authorizer.getExplicitlyDeniedUsersForResource(path, ActionConstants.PUT);
        for (String wdUser : wdUsers) {
            if (wdUser.equals(systemUserName) || wdUser.equals(adminUserName)) {
                continue;
            }
            if (userPermissionMap.containsKey(wdUser)) {
                PermissionEntry permission = userPermissionMap.get(wdUser);
                permission.setWriteDeny(true);
            } else {
                PermissionEntry permission = new PermissionEntry();
                permission.setUserName(wdUser);
                permission.setWriteDeny(true);
                userPermissionMap.put(wdUser, permission);
            }
        }

        String[] daUsers = authorizer.getExplicitlyAllowedUsersForResource(path, ActionConstants.DELETE);
        for (String daUser : daUsers) {
            if (daUser.equals(systemUserName) || daUser.equals(adminUserName)) {
                continue;
            }
            if (userPermissionMap.containsKey(daUser)) {
                PermissionEntry permission = userPermissionMap.get(daUser);
                permission.setDeleteAllow(true);
            } else {
                PermissionEntry permission = new PermissionEntry();
                permission.setUserName(daUser);
                permission.setDeleteAllow(true);
                userPermissionMap.put(daUser, permission);
            }
        }

        String[] ddUsers = authorizer.getExplicitlyDeniedUsersForResource(path, ActionConstants.DELETE);
        for (String ddUser : ddUsers) {
            if (ddUser.equals(systemUserName) || ddUser.equals(adminUserName)) {
                continue;
            }
            if (userPermissionMap.containsKey(ddUser)) {
                PermissionEntry permission = userPermissionMap.get(ddUser);
                permission.setDeleteDeny(true);
            } else {
                PermissionEntry permission = new PermissionEntry();
                permission.setUserName(ddUser);
                permission.setDeleteDeny(true);
                userPermissionMap.put(ddUser, permission);
            }
        }


        String[] aaUsers = authorizer.
                getExplicitlyAllowedUsersForResource(path, AccessControlConstants.AUTHORIZE);
        for (String aaUser : aaUsers) {
            if (aaUser.equals(systemUserName) || aaUser.equals(adminUserName)) {
                continue;
            }
            if (userPermissionMap.containsKey(aaUser)) {
                PermissionEntry permission = userPermissionMap.get(aaUser);
                permission.setAuthorizeAllow(true);
            } else {
                PermissionEntry permission = new PermissionEntry();
                permission.setUserName(aaUser);
                permission.setAuthorizeAllow(true);
                userPermissionMap.put(aaUser, permission);
            }
        }
        String[] adUsers = authorizer.
                getExplicitlyDeniedUsersForResource(path, AccessControlConstants.AUTHORIZE);
        for (String adUser : adUsers) {
            if (adUser.equals(systemUserName) || adUser.equals(adminUserName)) {
                continue;
            }
            if (userPermissionMap.containsKey(adUser)) {
                PermissionEntry permission = userPermissionMap.get(adUser);
                permission.setAuthorizeDeny(true);
            } else {
                PermissionEntry permission = new PermissionEntry();
                permission.setUserName(adUser);
                permission.setAuthorizeDeny(true);
                userPermissionMap.put(adUser, permission);
            }
        }


        List <PermissionEntry> permissionEntryList =
                new ArrayList <PermissionEntry> (userPermissionMap.values());
        return permissionEntryList.toArray(new PermissionEntry[permissionEntryList.size()]);
    }

    private static PermissionEntry[] getRolePermissions(UserRealm userRealm, String path)
            throws UserStoreException {

        Map <String, PermissionEntry> rolePermissionMap = new HashMap <String, PermissionEntry> ();

        AuthorizationManager authorizer = userRealm.getAuthorizationManager();
        RealmConfiguration realmConfig = userRealm.getRealmConfiguration();
        String adminRoleName = realmConfig.getAdminRoleName();

        String[] raRoles = authorizer.getAllowedRolesForResource(path, ActionConstants.GET);
        for (String raRole : raRoles) {
            if (raRole.equals(adminRoleName)) {
                continue;
            }
            if (rolePermissionMap.containsKey(raRole)) {
                PermissionEntry permission = rolePermissionMap.get(raRole);
                permission.setReadAllow(true);
            } else {
                PermissionEntry permission = new PermissionEntry();
                permission.setUserName(raRole);
                permission.setReadAllow(true);
                rolePermissionMap.put(raRole, permission);
            }
        }

        String[] rdRoles = authorizer.getDeniedRolesForResource(path, ActionConstants.GET);
        for (String rdRole : rdRoles) {
            if (rolePermissionMap.containsKey(rdRole)) {
                PermissionEntry permission = rolePermissionMap.get(rdRole);
                permission.setReadDeny(true);
            } else {
                PermissionEntry permission = new PermissionEntry();
                permission.setUserName(rdRole);
                permission.setReadDeny(true);
                rolePermissionMap.put(rdRole, permission);
            }
        }


        String[] waRoles = authorizer.getAllowedRolesForResource(path, ActionConstants.PUT);
        for (String waRole : waRoles) {
            if (waRole.equals(adminRoleName)) {
                continue;
            }
            if (rolePermissionMap.containsKey(waRole)) {
                PermissionEntry permission = rolePermissionMap.get(waRole);
                permission.setWriteAllow(true);
            } else {
                PermissionEntry permission = new PermissionEntry();
                permission.setUserName(waRole);
                permission.setWriteAllow(true);
                rolePermissionMap.put(waRole, permission);
            }
        }

        String[] wdRoles = authorizer.getDeniedRolesForResource(path, ActionConstants.PUT);
        for (String wdRole : wdRoles) {
            if (rolePermissionMap.containsKey(wdRole)) {
                PermissionEntry permission = rolePermissionMap.get(wdRole);
                permission.setWriteDeny(true);
            } else {
                PermissionEntry permission = new PermissionEntry();
                permission.setUserName(wdRole);
                permission.setWriteDeny(true);
                rolePermissionMap.put(wdRole, permission);
            }
        }
        
        String[] daRoles = authorizer.getAllowedRolesForResource(path, ActionConstants.DELETE);
        for (String daRole : daRoles) {
            if (daRole.equals(adminRoleName)) {
                continue;
            }
            if (rolePermissionMap.containsKey(daRole)) {
                PermissionEntry permission = rolePermissionMap.get(daRole);
                permission.setDeleteAllow(true);
            } else {
                PermissionEntry permission = new PermissionEntry();
                permission.setUserName(daRole);
                permission.setDeleteAllow(true);
                rolePermissionMap.put(daRole, permission);
            }
        }
        
        String[] ddRoles = authorizer.getDeniedRolesForResource(path, ActionConstants.DELETE);
        for (String ddRole : ddRoles) {
            if (rolePermissionMap.containsKey(ddRole)) {
                PermissionEntry permission = rolePermissionMap.get(ddRole);
                permission.setDeleteDeny(true);
            } else {
                PermissionEntry permission = new PermissionEntry();
                permission.setUserName(ddRole);
                permission.setDeleteDeny(true);
                rolePermissionMap.put(ddRole, permission);
            }
        }


        String[] aaRoles = authorizer.
                getAllowedRolesForResource(path, AccessControlConstants.AUTHORIZE);
        for (String aaRole : aaRoles) {
            if (aaRole.equals(adminRoleName)) {
                continue;
            }
            if (rolePermissionMap.containsKey(aaRole)) {
                PermissionEntry permission = rolePermissionMap.get(aaRole);
                permission.setAuthorizeAllow(true);
            } else {
                PermissionEntry permission = new PermissionEntry();
                permission.setUserName(aaRole);
                permission.setAuthorizeAllow(true);
                rolePermissionMap.put(aaRole, permission);
            }
        }
        
        String[] adRoles = authorizer.
                getDeniedRolesForResource(path, AccessControlConstants.AUTHORIZE);
        for (String adRole : adRoles) {
            if (rolePermissionMap.containsKey(adRole)) {
                PermissionEntry permission = rolePermissionMap.get(adRole);
                permission.setAuthorizeDeny(true);
            } else {
                PermissionEntry permission = new PermissionEntry();
                permission.setUserName(adRole);
                permission.setAuthorizeDeny(true);
                rolePermissionMap.put(adRole, permission);
            }
        }


        List <PermissionEntry> permissionEntryList =
                new ArrayList <PermissionEntry> (rolePermissionMap.values());
        return permissionEntryList.toArray(new PermissionEntry[permissionEntryList.size()]);
    }
}
