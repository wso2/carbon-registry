/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.extensions.handlers;

import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

/**
 * This handler is use to delete the resource collections recursively.
 * This will delete resources individually, not collectively which help
 * registry indexer to remove the resources from its index.
 *
 */
public class RecursiveDeleteHandler extends Handler {

    @Override
    public void delete(RequestContext requestContext) throws RegistryException {
        Resource resource = requestContext.getResource();
        if (resource instanceof Collection) {
            if(!isDeleteLockAvailable()){
                return;
            }
            acquireDeleteLock();
            try {
                deleteRecursively(requestContext.getRegistry(), resource);
                requestContext.setProcessingComplete(true);
            } finally {
                releaseDeleteLock();
            }
        }
    }

    private void deleteRecursively (Registry registry, Resource resource) throws RegistryException {
        if (resource instanceof Collection) {
            for(String childResource : ((Collection) resource).getChildren()){
                deleteRecursively(registry, registry.get(childResource));
            }
        }
        registry.delete(resource.getPath());
    }

    /**
     * This lock is shared with DeleteHierarchyHandler in
     * order to prevent delete same resource multiple times.
     */
    private static ThreadLocal<Boolean> deleteInProgress = new ThreadLocal<Boolean>() {
         protected Boolean initialValue() {
             return false;
         }
     };

     public static boolean isDeleteLockAvailable() {
         return !deleteInProgress.get();
     }

     public static void acquireDeleteLock() {
         deleteInProgress.set(true);
     }

     public static void releaseDeleteLock() {
         deleteInProgress.set(false);
     }
}
