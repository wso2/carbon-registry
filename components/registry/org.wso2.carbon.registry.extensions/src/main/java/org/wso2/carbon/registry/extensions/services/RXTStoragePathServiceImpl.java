/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.extensions.services;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.HashMap;
import java.util.Map;

public class RXTStoragePathServiceImpl extends AbstractAdmin implements RXTStoragePathService {

    private static Map<Integer, Map<String, String>> tenantMap = new HashMap<Integer, Map<String, String>>();

    /**
     * This method returns the registry path expression of the given media type
     * @param mediaType media type of the RXT(Configurable artifact)
     * @return registry path expression
     */
    public String getStoragePath(String mediaType) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantMap.get(tenantId) != null) {
            return tenantMap.get(tenantId).get(mediaType);
        }
        return null;
    }

    /**
     * This method removes the path expression of the given media type
     * @param mediaType media type of the RXT(Configurable artifact)
     */
    public void removeStoragePath(String mediaType) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantMap.get(tenantId) != null && tenantMap.get(tenantId).get(mediaType) != null) {
            tenantMap.get(tenantId).remove(mediaType);
        }
    }

    /**
     * This method stores the path expression of the given media type
     * @param mediaType  media type of the RXT(Configurable artifact)
     * @param storagePath registry path expression
     */
    public void addStoragePath(String mediaType, String storagePath) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        storagePath = getCompletePath(storagePath);
        if (tenantMap.get(tenantId) != null) {
            Map<String, String> pathMap = tenantMap.get(tenantId);
            pathMap.put(mediaType, storagePath);
            tenantMap.put(tenantId, pathMap);
        } else {
            Map<String, String> pathMap = new HashMap<String, String>();
            pathMap.put(mediaType, storagePath);
            tenantMap.put(tenantId, pathMap);
        }
    }

    private String getCompletePath(String storagePath) {
        String path = "/_system/governance";
        if (!storagePath.startsWith(path)) {
            storagePath = path + storagePath;
        }
        return storagePath;
    }
}
