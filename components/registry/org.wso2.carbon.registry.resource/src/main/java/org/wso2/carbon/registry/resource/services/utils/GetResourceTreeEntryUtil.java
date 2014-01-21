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
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.beans.ResourceTreeEntryBean;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GetResourceTreeEntryUtil {

    private static final Log log = LogFactory.getLog(GetResourceTreeEntryUtil.class);

    public static ResourceTreeEntryBean getResourceTreeEntry(String resourcePath,
                                                             UserRegistry registry) throws Exception {

        ResourceTreeEntryBean bean = new ResourceTreeEntryBean();

        try {
            Resource resource = null;
            try {
                if (!registry.resourceExists(resourcePath)) {
                    return null;
                }
                resource = registry.get(resourcePath);
                if (resource == null) {
                    throw new Exception();
                } else if (resource.getProperty("registry.absent") != null) {
                    throw new Exception();
                }
                
            } catch(Exception e) {
                if (log.isDebugEnabled()) {
                    // We need to log this as an error, only if we are in debug mode. If not, this is
                    // not required to be known.
                    log.debug("[ERROR] An exception occured: ", e);
                }
                resource = null;
            } finally {
                if (resource == null) {
                    return null;                    
                }
            }
            if (resource.getProperty(RegistryConstants.REGISTRY_LINK) != null) {
                if (resource.getProperty(RegistryConstants.REGISTRY_REAL_PATH) != null) {
                    bean.setSymlink("remotelink");
                } else {
                    bean.setSymlink("symlink");
                }
            } else {
                bean.setSymlink(null);
            }
            if (resource instanceof Collection) {
                bean.setCollection(true);
                // just to filter out the dangling symlinks
                String[] children = ((Collection)resource).getChildren();
                List<String> filteredChilds = new ArrayList<String>();
                for (String child: children) {
                    if (registry.resourceExists(child)) {
                        filteredChilds.add(child);
                    }
                }
                String[] filteredChildArr = filteredChilds.toArray(new String[filteredChilds.size()]);
                bean.setChildren(filteredChildArr);
            } else {
                bean.setCollection(false);
            }
            resource.discard();

        } catch (RegistryException e) {

            String msg = "Failed to get the resource information of resource " + resourcePath +
                    " for constructing the resource tree entry. " +
                    ((e.getCause() instanceof SQLException) ?
                    "" : e.getMessage());
            log.error(msg, e);
            throw e;
        }

        return bean;
    }
}
