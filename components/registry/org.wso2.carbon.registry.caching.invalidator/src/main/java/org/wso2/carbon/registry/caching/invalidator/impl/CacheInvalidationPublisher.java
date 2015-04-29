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

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TopicSession;
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
        // Setup the pub/sub connection, session
        // Send the msg (byte stream)
/*        Connection connection = null;
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

            connection = connectionFactory.createConnection(ConfigurationManager.getSecurityPrincipal(),
                    ConfigurationManager.getSecurityCredentials());
            connection.start();
            Session pubSession = connection.createSession(false, TopicSession.AUTO_ACKNOWLEDGE);
            MessageProducer publisher = pubSession.createProducer(destination);
            BytesMessage bytesMessage = pubSession.createBytesMessage();
            bytesMessage.writeBytes(data);
            publisher.send(bytesMessage);

        } catch (JMSException e) {
            log.error("Global cache invalidation: Error message broker initialization", e);
        } catch (NamingException e) {
            log.error("Global cache invalidation: Error message broker initialization", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    log.error("Global cache invalidation: error close publish connection", e);
                }
            }
        }*/
    }

    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(obj);
        return byteArrayOutputStream.toByteArray();
    }
}
