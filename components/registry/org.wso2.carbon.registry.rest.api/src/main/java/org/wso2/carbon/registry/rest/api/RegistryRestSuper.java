/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.rest.api;

import java.util.List;

import javax.ws.rs.core.PathSegment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

public class RegistryRestSuper {

    private UserRegistry userRegistry = null;
    private int pageSize = 10;
    private int begin = 0;
    private int end = 0;
    private String tenantID = null;
    private Log log = LogFactory.getLog(RegistryRestSuper.class);

    /**
     * This method creates the registry instance belongs to the particular user
     * to isolate from other users among the tenant.
     *
     * @param username username of the authorized enduser belongs to the access token
     * @param tenantID tenantID of the authorized enduser belongs to the access token
     * @return user registry instance
     */
    protected Registry getUserRegistry(String username, int tenantID) {
        Registry userRegistry = null;
        RegistryService registryService = (RegistryService) PrivilegedCarbonContext.getThreadLocalCarbonContext().
                getOSGiService(RegistryService.class);
        try {
            userRegistry = registryService.getUserRegistry(username, tenantID);
        } catch (RegistryException e) {
            log.error("unable to create user registry", e);
        }
        return userRegistry;
    }

    /**
     * This method calculates the string literal of the requested path of the
     * resource.
     *
     * @param path list of path segment (eg: <_system,governance,sample.xml>
     * @return concatenated string representation of the path variable
     */
    protected String getResourcePath(List<PathSegment> path) {
        if (path == null || !(path.size() > 0)) {
            return null;
        }
        String resourcePath = "";
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(resourcePath);
        for (PathSegment pathSegment : path) {
            strBuilder.append("/");
            strBuilder.append(pathSegment);
        }
        resourcePath = strBuilder.toString();
        if (resourcePath.length() == 0) {
            resourcePath = "/";
        }
        return resourcePath;
    }

    protected UserRegistry getUserRegistry() {
        return userRegistry;
    }

    protected void setUserRegistry(UserRegistry userRegistry) {
        this.userRegistry = userRegistry;
    }

    protected int getPageSize() {
        return pageSize;
    }

    protected void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    protected int getBegin() {
        return begin;
    }

    protected void setBegin(int begin) {
        this.begin = begin;
    }

    protected int getEnd() {
        return end;
    }

    protected void setEnd(int end) {
        this.end = end;
    }

    protected String getTenantID() {
        try {
            int x = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            tenantID = String.valueOf(x);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return tenantID;
    }
}
