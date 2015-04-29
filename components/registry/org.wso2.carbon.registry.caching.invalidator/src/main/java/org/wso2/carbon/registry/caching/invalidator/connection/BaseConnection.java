package org.wso2.carbon.registry.caching.invalidator.connection;

import java.util.Properties;

public interface BaseConnection {

    public void createConnection(Properties config);

    public void closeConnection();

    public void publish(Object message);

    public void subscribe();
}
