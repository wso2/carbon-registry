/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.metadata.server.dao.inmemory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metadata.server.api.Key;
import org.wso2.carbon.metadata.server.api.MetadataStoreException;
import org.wso2.carbon.metadata.server.api.Resource;
import org.wso2.carbon.metadata.server.dao.MetadataDAO;
import org.wso2.carbon.metadata.server.impl.CollectionImpl;
import org.wso2.carbon.metadata.server.impl.ResourceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.ws.http.HTTPException;

/**
 * In Memory Metadata DAO
 */
public class InMemoryMetadataDAO implements MetadataDAO {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryMetadataDAO.class);
    //stores resources against the path
    private static HashMap<String, Resource> inMemoryStore;
    //stores UUID against path
    private static HashMap<String, String> uuidStore;

    static {
        inMemoryStore = new HashMap<>();
        uuidStore = new HashMap<>();
    }

    @Override
    public void add(Resource resource) throws MetadataStoreException {
        String resourcePath = resource.getKey();
        if (exists(resourcePath)) {
            logger.error("Resource exists already");
            throw new MetadataStoreException("Resource exists already in inMemoryMetaDataStore");
        }
        //if not in the path map, then create entry
        if (resource instanceof CollectionImpl) {
            CollectionImpl collectionImpl = (CollectionImpl) resource;
            inMemoryStore.put(collectionImpl.getKey(), collectionImpl);
            uuidStore.put(collectionImpl.getUUID(), collectionImpl.getKey());
        } else {
            inMemoryStore.put(resource.getKey(), resource);
            uuidStore.put(resource.getUUID(), resource.getKey());
        }
        logger.debug("Resource added successfully");
    }

    /**
     * Check whether a given resource exists in inMemoryStore
     *
     * @param path path of the resource to be checked
     * @return true, if resource exists
     */
    public boolean exists(String path) {
        Resource retrievedResource = inMemoryStore.get(path);
        if (retrievedResource != null) {
            return true;
        }
        return false;
    }

    //removeByUUID() and removeByKey()? But atomic operations.
    //One remove method accepts a resource? But have to create.
    //One method accepts string and isUUID or isKey

    @Override
    public void removeByUUID(String uuid) throws Exception {
        String path = uuidStore.get(uuid);
        if (path != null) {
            uuidStore.remove(uuid);
            inMemoryStore.remove(path);
            logger.debug("Collection removed successfully");
        } else {
            throw new HTTPException(404);
        }
    }

    @Override
    public void removeByKey(Key path) throws HTTPException {
        Resource retrievedResource = inMemoryStore.get(path.getKey());
        if (retrievedResource != null) {
            inMemoryStore.remove(path.getKey());
            String uuid = retrievedResource.getUUID();
            uuidStore.remove(uuid);
            logger.debug("resource removed successfully");
        } else {
            throw new HTTPException(404);
        }
    }

    @Override
    public void update(Resource resource) throws MetadataStoreException {
        String path = resource.getKey();
        //remove old object and add new object or attach new property bag? implementing 1st option
        Key key = new Key();
        key.setKey(path);
        removeByKey(key);
        add(resource);
    }

    @Override
    public Resource get(Key path) throws MetadataStoreException {
        Resource resource = inMemoryStore.get(path.getKey());
        if (resource != null) {
            return resource;
        } else {
            throw new HTTPException(404);
        }
    }

    @Override
    public ArrayList<String> getChildrenPaths(CollectionImpl collectionImpl) throws MetadataStoreException {
        String collectionKey = collectionImpl.getKey();
        ArrayList<String> childrenList = new ArrayList<>();
        Iterator iterator = inMemoryStore.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Resource> resourceEntry = (Map.Entry) iterator.next();
            Resource resource = resourceEntry.getValue();
            String parentPath = "";
            if (resource instanceof ResourceImpl) {
                ResourceImpl childResourceImpl = (ResourceImpl) resource;
                parentPath = childResourceImpl.getParentPath();
            } else if (resource instanceof CollectionImpl) {
                CollectionImpl childCollectionImpl = (CollectionImpl) resource;
                parentPath = childCollectionImpl.getParentPath();
            }
            if (parentPath.equals(collectionKey)) {
                childrenList.add(resource.getKey());
            }
        }
        return childrenList;
    }
}
