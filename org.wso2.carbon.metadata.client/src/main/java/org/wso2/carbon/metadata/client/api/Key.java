package org.wso2.carbon.metadata.client.api;

/**
 * Represents a path of a collection or a resource
 */
public class Key {

    //user provided key of the resource
    private String key;

    /**
     * Get the path of the resource
     * @return Path value
     */
    public String getKey() {
        return key;
    }

    /**
     * Set the path for the resource
     * @param key Path value
     */
    public void setKey(String key) {
        this.key = key;
    }
}
