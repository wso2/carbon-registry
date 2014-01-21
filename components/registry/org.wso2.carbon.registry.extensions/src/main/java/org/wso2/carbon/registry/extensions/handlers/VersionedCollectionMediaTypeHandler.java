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

import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

/**
 * This class handles application/vnd.wso2.versioned-collection media type. When a resource is put
 * into a collection of this media type, this handler updates the symlinks of the parent folder if
 * parent folder considers this collections as its latest version.
 */
public class VersionedCollectionMediaTypeHandler extends Handler {

    public void putChild(RequestContext requestContext) throws RegistryException {
        Registry registry = requestContext.getRegistry();
        String thisCollectionPath = requestContext.getParentPath();
        String thisCollectionName = RegistryUtils.getResourceName(thisCollectionPath);
        String parentContainerPath = RegistryUtils.getParentPath(thisCollectionPath);
        Resource parentContainer = registry.get(parentContainerPath);

        if (thisCollectionName.equals(parentContainer.getProperty(
                CommonConstants.LATEST_VERSION_PROP_NAME))) {
            String resourcePath = requestContext.getResourcePath().getPath();
            String resourceName = RegistryUtils.getResourceName(resourcePath);
            registry.createLink(parentContainerPath + RegistryConstants.PATH_SEPARATOR +
                    resourceName, resourcePath);
        }
    }

}
