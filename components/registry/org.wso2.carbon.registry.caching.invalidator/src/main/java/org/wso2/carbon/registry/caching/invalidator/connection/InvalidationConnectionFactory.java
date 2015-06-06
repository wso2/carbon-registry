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
import org.wso2.carbon.registry.caching.invalidator.internal.CacheInvalidationDataHolder;

import java.util.Properties;

public class InvalidationConnectionFactory {
    private static final Log log = LogFactory.getLog(InvalidationConnectionFactory.class);

    public static void createMessageBrokerConnection() throws CacheInvalidationException {
        Properties properties = ConfigurationManager.getCacheConfiguration();
        if (properties.containsKey("class.CacheInvalidationClass")) {
            InvalidNotification connection = (InvalidNotification) getObject(properties.getProperty("class.CacheInvalidationClass"));
            if (connection != null) {
                connection.createConnection(properties);
                CacheInvalidationDataHolder.setConnection(connection);
            } else {
                log.warn("Error while initializing message, Global cache invalidation will not work");
            }
        }
    }

    private static Object getObject(String className) throws CacheInvalidationException {
        try {
            Class factoryClass = Class.forName(className);
            return factoryClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new CacheInvalidationException("Class " + className + " not found ", e);
        } catch (IllegalAccessException e) {
            throw new CacheInvalidationException("Class not be accessed ", e);
        } catch (InstantiationException e) {
            throw new CacheInvalidationException("Class not be instantiated ", e);
        }
    }
}
