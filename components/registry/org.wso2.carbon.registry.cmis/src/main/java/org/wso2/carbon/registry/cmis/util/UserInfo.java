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

package org.wso2.carbon.registry.cmis.util;

public class UserInfo {
    
    private String userName;
    private String tenantDomain;
    private String ip;

    public UserInfo(String ip, String userName, String tDomain) {
        this.ip = ip;
        this.userName = userName;
        this.tenantDomain = tDomain;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfo userInfo = (UserInfo) o;

        if (ip != null ? !ip.equals(userInfo.ip) : userInfo.ip != null) return false;
        if (tenantDomain != null ? !tenantDomain.equals(userInfo.tenantDomain) : userInfo.tenantDomain != null)
            return false;
        if (userName != null ? !userName.equals(userInfo.userName) : userInfo.userName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userName != null ? userName.hashCode() : 0;
        result = 31 * result + (tenantDomain != null ? tenantDomain.hashCode() : 0);
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        return result;
    }
}