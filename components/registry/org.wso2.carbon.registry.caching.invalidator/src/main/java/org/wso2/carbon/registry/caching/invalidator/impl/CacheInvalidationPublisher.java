/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.caching.invalidator.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.caching.impl.CacheInvalidator;
import org.wso2.carbon.registry.caching.invalidator.connection.CacheInvalidationException;
import org.wso2.carbon.registry.caching.invalidator.connection.InvalidationConnectionFactory;
import org.wso2.carbon.registry.caching.invalidator.internal.CacheInvalidationDataHolder;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Global cache invalidation publisher implements org.wso2.carbon.caching.impl.CacheInvalidator interface
 */
public class CacheInvalidationPublisher implements CacheInvalidator {
    private static final Log log = LogFactory.getLog(CacheInvalidationPublisher.class);

    @Override
    public void invalidateCache(int tenantId, String cacheManagerName, String cacheName, Serializable cacheKey) {
        log.debug("Global cache invalidation: initializing the connection");
        if (CacheInvalidationDataHolder.getConnection() == null) {
            ConfigurationManager.init();
        }
        //Converting data to json string
        GlobalCacheInvalidationEvent event = new GlobalCacheInvalidationEvent();
        event.setTenantId(tenantId);
        event.setCacheManagerName(cacheManagerName);
        event.setCacheName(cacheName);
        event.setCacheKey(cacheKey);
        String uuid = UUIDGenerator.generateUUID();
        event.setUuid(uuid);
        byte data[];
        try {
            log.debug("Global cache invalidation: converting serializable object to byte stream.");
             data = serialize(event);
            log.debug("Global cache invalidation: converting data to byte stream complete.");
        } catch (IOException e) {
            log.error("Global cache invalidation: Error while converting data to byte stream", e);
            return;
        }

        if (CacheInvalidationDataHolder.getConnection() != null) {
            CacheInvalidationDataHolder.getConnection().publish(data);
        } else {
            try {
                InvalidationConnectionFactory.createMessageBrokerConnection();
                CacheInvalidationDataHolder.getConnection().publish(data);
            } catch (CacheInvalidationException e) {
                log.error("Error while publishing data, connection couldn't establish", e);
            }
        }
    }

    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(obj);
        return byteArrayOutputStream.toByteArray();
    }
}
