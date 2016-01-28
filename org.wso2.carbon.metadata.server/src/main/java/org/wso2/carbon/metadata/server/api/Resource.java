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

package org.wso2.carbon.metadata.server.api;


import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This class is represent the resource that is stored on the metadata repository. Each resource will have some
 * metadata and content. Resources can also have properties, and media type.
 */
public class Resource {

    protected Resource(String uuid) {
        this.uuid = uuid;
    }

    /**
     * UUID to identify the resource.
     */
    private String uuid;

    /**
     * Unique path or user provided identifier of the resource within the metadata api.
     */
    private String key;

    /**
     * Content of the resource. Object and the type stored in this field depends on the resource
     * type. If the resource is a file with no special media type handling, this contains an array
     * of bytes (byte[]) containing the raw bytes of the file. If the resource is a collection, this
     * contains a String[] containing the UUID of child resources.
     */
    private Object content;

    /**
     * Media type of the resource. Each resource can have a media type associated with it. This can
     * be either a standard MIME media type or a custom media type defined by the users of the
     * metadata api. Media type is used to activate media type handlers defined in the registry. Thus,
     * by defining a media type for a resource and by registering a media type handler to handle
     * that media type, it is possible to apply special processing for resources.
     */
    private String mediaType;

    /**
     * Properties associated with the resource. A resource can contain zero or more properties,
     * where each property is a key and  value pair. Both key and the value should be strings.
     */
    protected Properties properties = new Properties();

    /**
     * User roles associated with the resource. A resource can contain one or more user roles,
     * who can access the resource. Both key and the value should be strings.
     */
    private Map<String, String> allowedRoles;

    /**
     * The Resource Unique ID, In the default implementation this returns the auto generate UUID.
     *
     * @return the resource unique id
     */
    public String getUUID() {
        return uuid;
    }

    /**
     * Method to set the resource unique id.
     *
     * @param uuid the resource unique id.
     */
    protected void setUUID(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Returning the of the resource, which is provided by user or end system
     *
     * @return the key of the resource.
     */
    public String getKey() {
        return key;
    }

    /**
     * Setting the key of the resource
     *
     * @param key the key of the resource.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Method to get the media type.
     *
     * @return the media type of resource.
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Method to set the media type.
     *
     * @param mediaType the media type.
     */
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Method to get the content of the resource. If the resource is a collection this will return
     * an array of string that represent the paths of its children, otherwise it returns an byte
     * array or a string from the default implementation.
     *
     * @return the content
     * @throws MetadataStoreException An Exception will be thrown when performing this operation on a collections.
     */
    public Object getContent() throws MetadataStoreException {
        return content;
    }

    /**
     * Set the content of the resource.
     *
     * @param content the resource.
     */
    public void setContent(Object content) {
        this.content = content;
    }

    /**
     * Returns all properties of the resource.
     *
     * @return All properties of the resource.
     */
    public Properties getProperties() {
        return properties;
    }


    /**
     * Returns the list of values for the given property name.
     *
     * @param key Key of the property.
     * @return List of values of the given property key.
     */
    public List<String> getPropertyValues(String key) {
        return (List<String>) properties.get(key);
    }

    /**
     * Set a property with single value.
     *
     * @param key    the property key.
     * @param values the property values.
     */
    public void setProperties(String key, String... values) {
        properties.put(key, values);

    }

    /**
     * Get the list of roles which are allowed to access the resource.
     *
     * @return List of Allowed Roles
     * @throws MetadataStoreException operation not supported
     */
    public Map<String, String> getAllowedRoles() throws MetadataStoreException {
        return allowedRoles;
    }

    /**
     * Set a allowed roles map to access the resource.
     *
     * @param allowedRoles Allowed Roles.
     */
    public void setAllowedRoles(Map<String, String> allowedRoles) {
        this.allowedRoles = allowedRoles;
    }


}
