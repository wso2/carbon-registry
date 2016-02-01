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

import java.util.Map;

/**
 * This class represents collections on the client side.
 */
public class Collection {

    /**
     * Add a new collection to the repository
     *
     * @return UUID of the collection
     * @throws MetadataClientException Throws if the operation failed
     */
    public String addCollection(String path) throws MetadataClientException {
        return null;
    }

    /**
     * Remove a collection from the repository given the path
     *
     * @param path path that the collection needs to be stored
     * @throws MetadataClientException throws if the operation fails
     */
    public void removeCollection(Key path) throws MetadataClientException {

    }

    /**
     * Remove a collection from the repository given the UUID of the collection
     *
     * @param UUID
     * @throws MetadataClientException
     */
    public void removeCollection(String UUID) throws MetadataClientException {

    }

    /**
     * Get all the members of a collection given path of the collection
     *
     * @param path path of the collection that the children need to be retrieved
     * @return Map containing UUID and Path of child resources
     * @throws MetadataClientException throws if the operation fails
     */
    public Map<String, String> getCollection(Key path) throws MetadataClientException {
        return null;
    }

    /**
     * Get all the members of a collection given UUID of the collection
     *
     * @param UUID UUID of the collection that the children need to be retrieved
     * @return Map containing UUID and Path of child resources
     * @throws MetadataClientException throws if the operation fails
     */
    public Map<String, String> getCollection(String UUID) throws MetadataClientException {
        return null;
    }

}
