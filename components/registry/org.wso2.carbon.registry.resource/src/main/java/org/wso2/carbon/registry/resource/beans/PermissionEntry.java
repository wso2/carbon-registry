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

public class PermissionEntry {

    private String userName;
    private boolean readAllow;
    private boolean readDeny;
    private boolean writeAllow;
    private boolean writeDeny;
    private boolean deleteAllow;
    private boolean deleteDeny;
    private boolean authorizeAllow;
    private boolean authorizeDeny;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isReadAllow() {
        return readAllow;
    }

    public void setReadAllow(boolean readAllow) {
        this.readAllow = readAllow;
    }

    public boolean isReadDeny() {
        return readDeny;
    }

    public void setReadDeny(boolean readDeny) {
        this.readDeny = readDeny;
    }

    public boolean isWriteAllow() {
        return writeAllow;
    }

    public void setWriteAllow(boolean writeAllow) {
        this.writeAllow = writeAllow;
    }

    public boolean isWriteDeny() {
        return writeDeny;
    }

    public void setWriteDeny(boolean writeDeny) {
        this.writeDeny = writeDeny;
    }

    public boolean isDeleteAllow() {
        return deleteAllow;
    }

    public void setDeleteAllow(boolean deleteAllow) {
        this.deleteAllow = deleteAllow;
    }

    public boolean isDeleteDeny() {
        return deleteDeny;
    }

    public void setDeleteDeny(boolean deleteDeny) {
        this.deleteDeny = deleteDeny;
    }

    public boolean isAuthorizeAllow() {
        return authorizeAllow;
    }

    public void setAuthorizeAllow(boolean authorizeAllow) {
        this.authorizeAllow = authorizeAllow;
    }

    public boolean isAuthorizeDeny() {
        return authorizeDeny;
    }

    public void setAuthorizeDeny(boolean authorizeDeny) {
        this.authorizeDeny = authorizeDeny;
    }
}
