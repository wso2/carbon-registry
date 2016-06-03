/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

/**
 * This handler implementation customizes the delete process of a service with versioning in the repository.
 * 
 */
public class DeleteHierarchyHandler extends Handler {

    @Override
    public void delete(RequestContext requestContext) throws RegistryException {

        if(!RecursiveDeleteHandler.isDeleteLockAvailable()){
            return;
        }
        RecursiveDeleteHandler.acquireDeleteLock();
        try {
            Registry registry =  requestContext.getRegistry();
            String parentPath =  requestContext.getResource().getParentPath();

//        First we are going to delete the actual service resource
            registry.delete(requestContext.getResource().getPath());
            deleteRecursively(parentPath,registry);
//        Now we check whether there are any children of that parent collection.
//        We do this recursively and delete all the parent collections until there is a parent collection with children.

            requestContext.setProcessingComplete(true);
        } finally {
            RecursiveDeleteHandler.releaseDeleteLock();
        }

    }

    private void deleteRecursively(String path,Registry registry) throws RegistryException {
        Resource currentResource = registry.get(path);

        if((currentResource instanceof Collection) && ((Collection)currentResource).getChildCount() == 0 ){
            registry.delete(path);
            deleteRecursively(currentResource.getParentPath(),registry);
        }

    }
}