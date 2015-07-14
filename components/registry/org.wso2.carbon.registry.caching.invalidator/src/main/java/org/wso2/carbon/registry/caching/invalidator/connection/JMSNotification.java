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
package org.wso2.carbon.registry.caching.invalidator.connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.caching.invalidator.impl.ConfigurationManager;
import org.wso2.carbon.registry.caching.invalidator.impl.GlobalCacheInvalidationEvent;
import org.wso2.carbon.registry.caching.invalidator.internal.CacheInvalidationDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Properties;

public class JMSNotification implements InvalidNotification, MessageListener{

    // Setup the pub/sub connection, session
    // Send the msg (byte stream)
    private static Connection connection = null;

    private static Destination destination = null;

    private static final Log log = LogFactory.getLog(JMSNotification.class);
    @Override
    public void createConnection(Properties config) {
        try {
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, config.getProperty("initialContextFactory"));
            props.put(Context.PROVIDER_URL, config.getProperty("providerUrl"));
            props.put(Context.SECURITY_PRINCIPAL, config.getProperty("securityPrincipal"));
            props.put(Context.SECURITY_CREDENTIALS, config.getProperty("securityCredentials"));
            props.put("topic.cacheInvalidateTopic", config.getProperty("cacheInvalidateTopic"));
            InitialContext jndi = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndi.lookup("ConnectionFactory");
            destination = (Destination)jndi.lookup("cacheInvalidateTopic");

            connection = connectionFactory.createConnection(config.getProperty("securityPrincipal"),
                    config.getProperty("securityCredentials"));
            connection.start();
        } catch (NamingException | JMSException e) {
            log.error("Global cache invalidation: Error message broker initialization", e);
        }
    }

    @Override
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                log.error("Global cache invalidation: Error in closing connection", e);
            }
        }
    }


    @Override
    public void publish(Object message) {
        Session pubSession = null;
        try {
            if (connection != null) {
                pubSession = connection.createSession(false, TopicSession.AUTO_ACKNOWLEDGE);
                MessageProducer publisher = pubSession.createProducer(destination);
                BytesMessage bytesMessage = pubSession.createBytesMessage();
                bytesMessage.writeBytes((byte[]) message);
                publisher.send(bytesMessage);
            }
        } catch (JMSException e) {
            log.error("Global cache invalidation: Error in publishing the message", e);
        } finally {
            if (pubSession != null) {
                try {
                    pubSession.close();
                } catch (JMSException e) {
                    log.error("Global cache invalidation: Error in publishing the message", e);                }
            }
        }
    }

    @Override
    public void subscribe() {
        try {
            Session subSession = connection.createSession(false, TopicSession.AUTO_ACKNOWLEDGE);
            MessageConsumer messageConsumer = subSession.createConsumer(destination);
            messageConsumer.setMessageListener(this);
            connection.start();
            log.info("Global cache invalidation is online");
        } catch (JMSException e) {
            log.error("Global cache invalidation: Error in subscribing to topic", e);
        }
    }

    @Override
    public void onMessage(Message message) {
        BytesMessage bytesMessage = (BytesMessage) message;
        byte[] data;
        try {
            data = new byte[(int) bytesMessage.getBodyLength()];
            for (int i = 0; i < (int) bytesMessage.getBodyLength(); i++) {
                data[i] = bytesMessage.readByte();
            }
            log.debug("Cache invalidation message received: " + new String(data));
        } catch (JMSException jmsException) {
            log.error("Error while reading the received message", jmsException);
            return;
        }

        boolean isCoordinator = false;
        if (CacheInvalidationDataHolder.getConfigContext() != null) {
            isCoordinator = CacheInvalidationDataHolder.getConfigContext().getAxisConfiguration()
                    .getClusteringAgent().isCoordinator();
        }
        if (isCoordinator) {
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
    }

    private Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return objectInputStream.readObject();
    }
}
