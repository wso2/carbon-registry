package org.wso2.carbon.metadata.client.api;

import java.util.Properties;

/**
 * Builder class for creating resources.
 */
public class ResourceBuilder {
    private String uuid;
    private Key path;
    private Properties properties;
    private String mediaType;
    private Object content;
    private boolean isCollection;

    public ResourceBuilder setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public ResourceBuilder setPath(Key path) {
        this.path = path;
        return this;
    }

    public ResourceBuilder setProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    public ResourceBuilder setMediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public ResourceBuilder setContent(Object content) {
        this.content = content;
        return this;
    }

    public ResourceBuilder setIsCollection(boolean isCollection) {
        this.isCollection = isCollection;
        return this;
    }

    public Resource createResource() {
        return new Resource(uuid, path, properties, mediaType, content, isCollection);
    }
}