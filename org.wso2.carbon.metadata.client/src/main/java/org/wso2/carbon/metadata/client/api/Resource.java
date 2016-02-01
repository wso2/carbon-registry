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
     * @param uuid
     * @param path
     * @param properties
     * @param mediaType
     * @param content
     * @param isCollection
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

    public Key getPath() {
        return path;
    }

    public String getUuid() {
        return uuid;
    }
}
