/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.handlers;

import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;

/**
 * This class handles application/vnd.wso2.version-container media type. It validates collections
 * put into this collection and also updates symlinks directly inside it when the latest version
 * property is changed.
 */
public class VersionContainerMediaTypeHandler extends Handler {

    public void putChild(RequestContext requestContext) throws RegistryException {
        String path = requestContext.getResourcePath().getPath();
        Resource resource = requestContext.getResource();
        if (resource instanceof Collection) {
            String parentPath = resource.getParentPath();
            String newVersionNumber = RegistryUtils.getResourceName(path);
            if (!CommonConstants.VERSIONED_COLLECTION_MEDIA_TYPE.equals(resource.getMediaType())) {
                throw new RegistryException("Only Collections of type " +
                        CommonConstants.VERSIONED_COLLECTION_MEDIA_TYPE + " can be put into the " +
                        "collection: " + parentPath);
            }
            if (!newVersionNumber.matches(CommonConstants.SERVICE_VERSION_REGEX)) {
                throw new RegistryException("Version number should be in the format :" +
                        "<major no>.<minor no>.<patch no>");
            }
            Registry registry = requestContext.getRegistry();
            Resource parent = registry.get(parentPath);
            parent.setProperty(org.wso2.carbon.registry.common.CommonConstants.
                    LATEST_VERSION_PROP_NAME, newVersionNumber);
            registry.put(parentPath, parent);
        }
    }

    public void put(RequestContext requestContext) throws RegistryException {
        //update symlinks if the property has changed
        Registry registry = requestContext.getRegistry();
        ResourcePath containerPath = requestContext.getResourcePath();
        String latestVersionNumber = requestContext.getResource().getProperty(
                org.wso2.carbon.registry.common.CommonConstants.LATEST_VERSION_PROP_NAME);
        if (latestVersionNumber == null) {
            return;
            //throw new RegistryException("Property latest.version is missing in the Collection of " +
            //        "media-type application/vnd.wso2.version-container");
        }
        String latestVersionCollectionPath = containerPath + RegistryConstants.PATH_SEPARATOR +
                latestVersionNumber;
        if (registry.resourceExists(latestVersionCollectionPath)) {
            Collection latestVersionCollection = (Collection) registry.get(
                    latestVersionCollectionPath);
            for (String childPath : latestVersionCollection.getChildren()) {
                String childName = RegistryUtils.getResourceName(childPath);
                registry.createLink(containerPath + RegistryConstants.PATH_SEPARATOR + childName,
                        childPath);
            }
        } else {
            //add a temp property to skip the case off calling put() method in the above putChild()
            //  throw new RegistryException("Latest versioned collection not found at" +
            //          latestVersionCollectionPath);
        }
    }
}
