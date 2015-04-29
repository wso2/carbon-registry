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
            BaseConnection connection = (BaseConnection) getObject(properties.getProperty("class.CacheInvalidationClass"));
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
