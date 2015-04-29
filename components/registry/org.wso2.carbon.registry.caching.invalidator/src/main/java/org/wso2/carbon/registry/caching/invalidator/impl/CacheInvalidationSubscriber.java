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

/*    private void subscribe() {
        log.debug("Global cache invalidation: initializing the subscription");
        try {
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, ConfigurationManager.getInitialContextFactory());
            props.put(Context.PROVIDER_URL, ConfigurationManager.getProviderUrl());
            props.put(Context.SECURITY_PRINCIPAL, ConfigurationManager.getSecurityPrincipal());
            props.put(Context.SECURITY_CREDENTIALS, ConfigurationManager.getSecurityCredentials());
            props.put("topic.MyTopic", ConfigurationManager.getTopicName());
            InitialContext jndi = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndi.lookup("ConnectionFactory");
            Destination destination = (Destination)jndi.lookup("MyTopic");
            Connection connection = connectionFactory.createConnection(ConfigurationManager.getSecurityPrincipal(), ConfigurationManager.getSecurityCredentials());
            Session subSession = connection.createSession(false, TopicSession.AUTO_ACKNOWLEDGE);

            MessageConsumer messageConsumer = subSession.createConsumer(destination);
            messageConsumer.setMessageListener(this);
            connection.start();
            log.info("Global cache invalidation is online");
        } catch (JMSException e) {
            log.error("Global cache invalidation: Error message broker initialization", e);
        } catch (NamingException e) {
            log.error("Global cache invalidation: Error message broker initialization", e);
        }
    }*/

/*    private Object deserialize(byte[] bytes) throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return objectInputStream.readObject();
    }*/

/*    @Override
    public void onMessage(Message message) {
        BytesMessage bytesMessage = (BytesMessage)message;
        byte[] data;
        try {
            data = new byte[(int)bytesMessage.getBodyLength()];
            for (int i = 0; i < (int) bytesMessage.getBodyLength(); i++) {
                data[i] = bytesMessage.readByte();
            }
            log.debug("Cache invalidation message received: " + new String(data));
        } catch (JMSException jmsException) {
            log.error("Error while reading the received message" , jmsException);
            return;
        }

        boolean isCoordinator = false;
        if(CacheInvalidationDataHolder.getConfigContext() != null) {
            isCoordinator = CacheInvalidationDataHolder.getConfigContext().getAxisConfiguration()
                    .getClusteringAgent().isCoordinator();
        }
        if(isCoordinator) {
            PrivilegedCarbonContext.startTenantFlow();
            try {
                log.debug("Global cache invalidation: deserializing data to object");
                GlobalCacheInvalidationEvent event = (GlobalCacheInvalidationEvent) deserialize(data);
                log.debug("Global cache invalidation: deserializing complete");
                if (!ConfigurationManager.getSentMsgBuffer().contains(event.getUuid().trim())) { // Ignore own messages
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(event.getTenantId(), true);
                    CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(event.getCacheManagerName());
                    if (cacheManager != null) {
                        if (cacheManager.getCache(event.getCacheName()) != null) {
                            cacheManager.getCache(event.getCacheName()).remove(event.getCacheKey());
                            log.debug("Global cache invalidated: " + event.getCacheKey());
                        } else {
                            log.error("Global cache invalidation: error cache is null");
                        }
                    } else {
                        log.error("Global cache invalidation: error cache manager is null");
                    }
                } else {
                    // To resolve future performance issues
                    ConfigurationManager.getSentMsgBuffer().remove(event.getUuid().trim());
                    log.debug("Global cache invalidation: own message ignored");
                }
            } catch (Exception e) {
                log.error("Global cache invalidation: error local cache update", e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }*/
}
