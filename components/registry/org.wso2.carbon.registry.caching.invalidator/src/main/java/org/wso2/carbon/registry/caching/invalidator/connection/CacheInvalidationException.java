package org.wso2.carbon.registry.caching.invalidator.connection;

public class CacheInvalidationException extends Exception {
    public CacheInvalidationException(String message,  Throwable cause) {
        super(message,cause);
    }

    public CacheInvalidationException(String message) {
        super(message);
    }
}
