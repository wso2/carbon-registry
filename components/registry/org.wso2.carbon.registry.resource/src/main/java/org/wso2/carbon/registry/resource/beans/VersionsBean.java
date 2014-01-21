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
public class VersionsBean {

    private String resourcePath;

    private String permalink;

    private boolean loggedIn;

    private boolean putAllowed;

    private VersionPath[] versionPaths;

    private String isWriteLocked;

    private String isDeleteLocked;

    private boolean isDeletePermissionAllowed;

    public boolean isDeletePermissionAllowed() {
        return isDeletePermissionAllowed;
    }

    public void setDeletePermissionAllowed(boolean deletePermissionAllowed) {
        isDeletePermissionAllowed = deletePermissionAllowed;
    }

    public String getWriteLocked() {
        return isWriteLocked;
    }

    public void setWriteLocked(String writeLocked) {
        isWriteLocked = writeLocked;
    }

    public String getDeleteLocked() {
        return isDeleteLocked;
    }

    public void setDeleteLocked(String deleteLocked) {
        isDeleteLocked = deleteLocked;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public boolean isPutAllowed() {
        return putAllowed;
    }

    public void setPutAllowed(boolean putAllowed) {
        this.putAllowed = putAllowed;
    }

    public VersionPath[] getVersionPaths() {
        return versionPaths;
    }

    public void setVersionPaths(VersionPath[] versionPaths) {
        this.versionPaths = versionPaths;
    }
}
