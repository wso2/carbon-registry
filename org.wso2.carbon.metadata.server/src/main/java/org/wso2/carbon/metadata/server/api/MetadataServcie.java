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

/**
 * This represents the most fundamental API for the Metadata service. This is typically what you want if
 * you're a Java programmer wanting to simply store and manage Resources.  This interface can be
 * used to perform all basic metadata operations, and can also be used to implement a simple
 * metadata implementation.
 */
public interface MetadataServcie {

    /**
     * Creates a new resource.
     *
     * @return the created resource.
     * @throws MetadataStoreException if the operation failed.
     */
    Resource newResource() throws MetadataStoreException;

    /**
     * Creates a new collection.
     *
     * @return the created collection.
     * @throws MetadataStoreException if the operation failed.
     */
    Collection newCollection() throws MetadataStoreException;

    /**
     * Adds or updates resources in the metadata repository. If there is no resource at the given path,
     * resource is added. If a resource already exist at the given path, it will be replaced with
     * the new resource.
     *
     * @param key      the path which we'd like to use for the new resource.
     * @param resource Resource instance for the new resource
     * @return the UUID of the Resource
     * @throws MetadataStoreException is thrown depending on the implementation.
     */

    String put(Key key, Resource resource) throws MetadataStoreException;

    /**
     * Adds new collection resources in the metadata repository.
     *
     * @param key the Key(path) which is use for the new collection.
     * @return the UUID of the collection
     * @throws MetadataStoreException is thrown depending on the implementation.
     */
    String put(Key key) throws MetadataStoreException;

    /**
     * Deletes the resource with the given UUID. If the UUID refers to a collection, all child
     * resources of the collection will also be deleted.
     *
     * @param uuid UUID of the resource or collection to be deleted.
     * @throws MetadataStoreException is thrown depending on the implementation.
     */
    void remove(String uuid) throws MetadataStoreException;

    /**
     * Returns the resource at the given path.
     *
     * @param key Key of the resource. e.g. /project1/server/deployment.xml
     * @return Resource instance
     * @throws MetadataStoreException is thrown if the resource is not in the registry
     */
    Resource get(Key key) throws MetadataStoreException;

    /**
     * Returns the resource with given UUID.
     *
     * @param uuid UUID of the resource. e.g. /project1/server/deployment.xml
     * @return Resource instance
     * @throws MetadataStoreException is thrown if the resource is not in the registry
     */
    Resource get(String uuid) throws MetadataStoreException;

    /**
     * Check whether a resource exists at the given path
     *
     * @param key Key(path) of the resource to be checked
     * @return true if a resource exists at the given path, false otherwise.
     * @throws MetadataStoreException if an error occurs
     */
    String exists(Key key) throws MetadataStoreException;

    /**
     * Check whether a resource exists at the given UUID
     *
     * @param uuid UUID of the resource to be checked
     * @return true if a resource exists at the given path, false otherwise.
     * @throws MetadataStoreException if an error occurs
     */
    String exists(String uuid) throws MetadataStoreException;

}
