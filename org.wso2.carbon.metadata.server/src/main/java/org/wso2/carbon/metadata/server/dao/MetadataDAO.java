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
package org.wso2.carbon.metadata.server.dao;

import org.wso2.carbon.metadata.server.api.Collection;
import org.wso2.carbon.metadata.server.api.Key;
import org.wso2.carbon.metadata.server.api.MetadataStoreException;
import org.wso2.carbon.metadata.server.impl.ResourceImpl;

/**
 * Metadata DAO
 */
public interface MetadataDAO {
    /**
     * Method to add a resource or collection to a given path
     *
     * @param resourceImpl Implementation of the resource or collection that needs to be added
     * @throws MetadataStoreException throws if operation is failed
     */
    void add(ResourceImpl resourceImpl) throws MetadataStoreException;

    /**
     * Removes a resource or a collection from the table given the UUID
     *
     * @param uuid UUID of the resource that needs to be removed
     * @throws MetadataStoreException throws if the operation failed
     */
    void remove(String uuid) throws MetadataStoreException;

    /**
     * Removes a collection or a resource given the path
     *
     * @param path path of the resource to be removed
     * @throws MetadataStoreException
     */
    void remove(Key path) throws MetadataStoreException;

    /**
     * Update a resource given the UUID
     *
     * @param uuid UUID of the resource
     * @throws MetadataStoreException
     */
    void update(String uuid) throws MetadataStoreException;

    /**
     * get the list of paths of children of a collection
     *
     * @param collection the collection needs to be addressed
     * @return list of paths
     * @throws MetadataStoreException
     */
    String[] getChildren(Collection collection) throws MetadataStoreException;

}
