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
package org.wso2.carbon.metadata.server.cache;


import org.wso2.carbon.metadata.server.api.Collection;
import org.wso2.carbon.metadata.server.api.Key;
import org.wso2.carbon.metadata.server.api.MetadataStore;
import org.wso2.carbon.metadata.server.api.MetadataStoreException;
import org.wso2.carbon.metadata.server.api.Resource;

import java.util.HashMap;
import java.util.Map;

;

/**
 * CachebasedMetadataStore has wrapped from the MetadataStore interface to support caching
 */
public class CachebasedMetadataStore implements MetadataStore {

    private MetadataStore metadataStore;

    private Map<String, Resource> resourceCache = new HashMap<>();

    private Map<String, String> keyMap = new HashMap<>();

    public CachebasedMetadataStore(MetadataStore metadataStore) {
        this.metadataStore = metadataStore;
    }


    @Override
    public Resource newResource() throws MetadataStoreException {
        return metadataStore.newResource();
    }

    @Override
    public Collection newCollection() throws MetadataStoreException {
        return metadataStore.newCollection();
    }

    @Override
    public String put(Key key, Resource resource) throws MetadataStoreException {
        String uuid = metadataStore.put(key, resource);
        keyMap.remove(key.getKey());
        resourceCache.remove(uuid);
        return uuid;
    }

    @Override
    public String put(Key key) throws MetadataStoreException {
        return metadataStore.put(key);
    }

    @Override
    public void remove(String uuid) throws MetadataStoreException {
        metadataStore.remove(uuid);
        Resource resource = resourceCache.get(uuid);
        if (resource != null) {
            keyMap.remove(resource.getKey());
            resourceCache.remove(uuid);
        }
    }

    @Override
    public Resource get(Key key) throws MetadataStoreException {
        Resource resource;
        String uuid = keyMap.get(key.getKey());
        if (uuid != null && resourceCache.get(uuid) != null) {
            resource = resourceCache.get(keyMap.get(key.getKey()));
        } else {
            resource = metadataStore.get(key);
            resourceCache.put(uuid, resource);
            keyMap.put(key.getKey(), uuid);
        }
        return resource;
    }

    @Override
    public Resource get(String uuid) throws MetadataStoreException {
        Resource resource;
        if (resourceCache.get(uuid) != null) {
            resource = resourceCache.get(uuid);
        } else {
            resource = metadataStore.get(uuid);
            resourceCache.put(resource.getUUID(), resource);
            keyMap.put(resource.getKey(), resource.getUUID());
        }
        return resource;
    }

    @Override
    public String exists(Key key) throws MetadataStoreException {
        return metadataStore.exists(key);
    }

    @Override
    public String exists(String uuid) throws MetadataStoreException {
        return metadataStore.exists(uuid);
    }
}
