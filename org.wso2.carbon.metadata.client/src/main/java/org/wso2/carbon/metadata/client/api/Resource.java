/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.metadata.client.api;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This class represents the Resource objects in the client side.
 */
public class Resource {

    /**
     * UUID of the resource
     */
    private String uuid;

    /**
     * path of the resource stores as a Key object
     */
    private Key path;

    /**
     * Properties associated with the resource. A resource can contain zero or more properties,
     * where each property is a key and  value pair. Both key and the value should be strings.
     */
    private Properties properties = new Properties();

    /**
     * Media type of the resource. Each resource can have a media type associated with it. This can
     * be either a standard MIME media type or a custom media type defined by the users of the
     * client api.
     */
    private String mediaType;

    /**
     * Content of the resource. Object and the type stored in this field depends on the resource
     * type. If the resource is a file with no special media type handling, this contains an array
     * of bytes (byte[]) containing the raw bytes of the file. If the resource is a collection, this
     * contains a String[] containing the UUID of child resources.
     */
    private Object content;

    /**
     * stores whether the given resource is a collection
     */
    private boolean isCollection;

    /**
     * Constructor for Resource objects. This is used by the ResourceBuilder class.
     *
     * @param uuid         UUID of the resource
     * @param path         Path given by the user
     * @param properties   Properties to be set to the resource
     * @param mediaType    Media type of the resource
     * @param content      Content of the resource
     * @param isCollection true, if the resource is a collection
     */
    public Resource(String uuid, Key path, Properties properties, String mediaType, Object content,
            boolean isCollection) {
        this.uuid = uuid;
        this.path = path;
        this.properties = properties;
        this.mediaType = mediaType;
        this.content = content;
        this.isCollection = isCollection;
    }

    /**
     * Getter for the path of the resource
     *
     * @return Path of the resource
     */

    public Key getPath() {
        return path;
    }

    /**
     * Getter for UUID of the resource
     *
     * @return UUID of the resource
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Getter for the content of the resource
     *
     * @return content of the resource
     */
    public Object getContent() {
        return content;
    }

    /**
     * Getter for the properties bag of the resource
     *
     * @return Properties bag of the resource
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Get whether the resource is a collection or not
     *
     * @return Whether resource is a collection.
     */
    public boolean isCollection() {
        return isCollection;
    }

    /**
     * Getter for the media type of the resource
     *
     * @return Media type of the resource
     */
    public String getMediaType() {
        return mediaType;
    }

    //    /**
    //     * Add a resource ?
    //     * @param resource
    //     * @return
    //     */
    //    public String addResource(Resource resource) {
    //        return null;
    //    }

    /**
     * Get the resource given UUID
     *
     * @param uuid UUID of the resource that needs to retrieve
     * @return Resource object
     * @throws MetadataClientException throws if operation failed
     */
    public Resource getResource(String uuid) throws MetadataClientException {
        return null;
    }

    /**
     * Get the resource given path
     *
     * @param path Path of the resource that needs to retrieve
     * @return Resource object
     * @throws MetadataClientException throws if operation failed
     */
    public Resource getResource(Key path) throws MetadataClientException {
        return null;
    }

    /**
     * Remove a resource specified by UUID
     *
     * @param uuid UUID of the resource
     * @throws MetadataClientException throws if operation failed
     */
    public void removeResource(String uuid) throws MetadataClientException {

    }

    /**
     * Remove a resource specified by path
     *
     * @param path Path of the resource
     * @throws MetadataClientException throws if operation failed
     */
    public void removeResource(Key path) throws MetadataClientException {

    }

    /**
     * Get a list of properties of a resource specified by UUID
     *
     * @param uuid UUID of the resource
     * @return Map containing properties and their values
     * @throws MetadataClientException throws if operation failed
     */
    public Map<String, String> getResourceProperties(String uuid) throws MetadataClientException {
        return null;
    }

    /**
     * Get a list of properties of a resource specified by Path
     *
     * @param path Path of the resource
     * @return Map containing properties and their values
     * @throws MetadataClientException throws if operation failed
     */
    public Map<String, String> getResourceProperties(Key path) throws MetadataClientException {
        return null;
    }

    /**
     * Modify Resource specified by the UUID
     *
     * @param uuid UUID of the resource
     * @return UUID of the changed resource
     * @throws MetadataClientException throws if operation failed
     */
    public String modifyResource(String uuid) throws MetadataClientException {
        return null;
    }

    /**
     * Modify Resource specified by the path
     *
     * @param path path of the resource
     * @return UUID of the changed resource
     * @throws MetadataClientException throws if operation failed
     */
    public String modifyResource(Key path) throws MetadataClientException {
        return null;
    }

    /**
     * Whether a resource exists by given UUID
     *
     * @param uuid UUID of the resource
     * @return true or false based on the existence
     * @throws MetadataClientException throws if operation failed
     */
    public boolean exists(String uuid) throws MetadataClientException {
        return false;
    }

    /**
     * Whether a resource exists by given path
     *
     * @param path Path of the resource
     * @return true or false based on the existence
     * @throws MetadataClientException throws if operation failed
     */
    public boolean exists(Key path) throws MetadataClientException {
        return false;
    }

    /**
     * Retrieve a list of resources specified by number of attributes
     *
     * @param attributes Attributes to be checked
     * @return List of resources
     * @throws MetadataClientException throws if operation failed
     */
    public List<Resource> find(String... attributes) throws MetadataClientException {
        return null;
    }

}
