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

package org.wso2.carbon.registry.resource.beans;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class PermissionBean {

    private boolean putAllowed;

    private boolean deleteAllowed;

    private boolean authorizeAllowed;

    private boolean versionView;

    private String pathWithVersion;

    private String[] userNames;

    private String[] roleNames;

    private PermissionEntry[] userPermissions;

    private PermissionEntry[] rolePermissions;

    public boolean isPutAllowed() {
        return putAllowed;
    }

    public void setPutAllowed(boolean putAllowed) {
        this.putAllowed = putAllowed;
    }

    public boolean isDeleteAllowed() {
        return deleteAllowed;
    }

    public void setDeleteAllowed(boolean deleteAllowed) {
        this.deleteAllowed = deleteAllowed;
    }

    public boolean isAuthorizeAllowed() {
        return authorizeAllowed;
    }

    public void setAuthorizeAllowed(boolean authorizeAllowed) {
        this.authorizeAllowed = authorizeAllowed;
    }

    public boolean isVersionView() {
        return versionView;
    }

    public void setVersionView(boolean versionView) {
        this.versionView = versionView;
    }

    public String getPathWithVersion() {
        return pathWithVersion;
    }

    public void setPathWithVersion(String pathWithVersion) {
        this.pathWithVersion = pathWithVersion;
    }

    public String[] getUserNames() {
        return userNames;
    }

    public void setUserNames(String[] userNames) {
        this.userNames = userNames;
    }

    public String[] getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(String[] roleNames) {
        this.roleNames = roleNames;
    }

    public PermissionEntry[] getUserPermissions() {
        return userPermissions;
    }

    public void setUserPermissions(PermissionEntry[] userPermissions) {
        this.userPermissions = userPermissions;
    }

    public PermissionEntry[] getRolePermissions() {
        return rolePermissions;
    }

    public void setRolePermissions(PermissionEntry[] rolePermissions) {
        this.rolePermissions = rolePermissions;
    }
}
