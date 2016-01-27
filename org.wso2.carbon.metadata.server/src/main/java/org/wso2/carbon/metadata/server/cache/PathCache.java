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

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

/**
 * Path cache
 */
public class PathCache {

    private Cache<String, String> cache;

    private PathCache() {

    }

    public final PathCache getPathCache() {
        if (this.cache == null) {
            setCache(createCacheInstance());
        }
        return new PathCache();
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    private static Cache createCacheInstance() {
        Cache cache = null;
        try {
            CachingProvider provider = Caching.getCachingProvider();
            CacheManager manager = provider.getCacheManager(provider.getDefaultURI(),
                    Thread.currentThread().getContextClassLoader(), provider.getDefaultProperties());
            cache = manager.getCache("PathCache", String.class, String.class);
        } catch (CacheException e) {
            // ...
        }
        return cache;
    }
}
