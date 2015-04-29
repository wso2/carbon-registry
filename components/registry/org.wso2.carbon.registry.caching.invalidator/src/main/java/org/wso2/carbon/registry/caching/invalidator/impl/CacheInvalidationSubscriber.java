/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.core.clustering.api.CoordinatedActivity;
import org.wso2.carbon.registry.caching.invalidator.connection.CacheInvalidationException;
import org.wso2.carbon.registry.caching.invalidator.connection.InvalidationConnectionFactory;
import org.wso2.carbon.registry.caching.invalidator.internal.CacheInvalidationDataHolder;

/**
 * Global cache invalidation subscriber implements org.wso2.carbon.core.clustering.api.CoordinatedActivity interface
 */
public class CacheInvalidationSubscriber implements CoordinatedActivity {
    private static final Log log = LogFactory.getLog(CacheInvalidationSubscriber.class);

    public CacheInvalidationSubscriber() {
        if (CacheInvalidationDataHolder.getConfigContext() != null &&
                CacheInvalidationDataHolder.getConfigContext().getAxisConfiguration().getClusteringAgent() != null) {
            boolean isCoordinator = CacheInvalidationDataHolder.getConfigContext().getAxisConfiguration()
                    .getClusteringAgent().isCoordinator();
            if (isCoordinator && !ConfigurationManager.isSubscribed()) {
                if (CacheInvalidationDataHolder.getConnection() != null) {
                    CacheInvalidationDataHolder.getConnection().subscribe();
                    ConfigurationManager.setSubscribed(true);
                } else {
                    try {
                        InvalidationConnectionFactory.createMessageBrokerConnection();
                        CacheInvalidationDataHolder.getConnection().subscribe();
                        ConfigurationManager.setSubscribed(true);
                    } catch (CacheInvalidationException e) {
                        log.error("Error while subscribing to the queue, connection couldn't establish", e);
                    }
                }
            }
        }
    }

    @Override
    public void execute() {
        if(ConfigurationManager.init() && CacheInvalidationDataHolder.getConfigContext() != null) {
            boolean isCoordinator = CacheInvalidationDataHolder.getConfigContext().getAxisConfiguration()
                    .getClusteringAgent().isCoordinator();
            if (isCoordinator && !ConfigurationManager.isSubscribed()) {
                if (CacheInvalidationDataHolder.getConnection() != null) {
                    CacheInvalidationDataHolder.getConnection().subscribe();
                } else {
                    try {
                        InvalidationConnectionFactory.createMessageBrokerConnection();
                        CacheInvalidationDataHolder.getConnection().subscribe();
                    } catch (CacheInvalidationException e) {
                        log.error("Error while subscribing to the queue, connection couldn't establish", e);
                        return;
                    }
                }
                ConfigurationManager.setSubscribed(true);
            }
            if (!isCoordinator && ConfigurationManager.isSubscribed()) {
                ConfigurationManager.setSubscribed(false);
            }
        }
    }

}
