/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.metadata.manager;

import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.BaseV1;
import org.wso2.carbon.registry.metadata.version.Version;

import java.util.Map;

public interface MetadataManager {

    /**
     * Creates a new meta data object with the given name
     * @param name Human readable name of the meta data instance
     * @return the plain meta data instance with the given name.
     */
    public Base newInstance(String name);

    /**
     * Deletes the meta data instance that represents from the given UUID
     * @param uuid  UUID of the instance
     */
    public void delete(String uuid);

    /**
     * Persisting the mata data instance details to the repository
     * @param metadata - meta data instance
     */
    public void add(Base metadata);

    /**
     * Update the meta data info with the given meta data instance
     * @param metadata - meta data insatnce
     */
    public void update(Base metadata);

    /**
     *
     * @return all meta data instances and their children that denotes from this particular media type
     */
    public Base[] getAllMetadata();

    /**
     *  Search all meta data instances of this particular type with the given search attributes
     * @param criteria Key value map that has search attributes
     * @return
     */
    public Base[] findMetadata(Map<String,String> criteria);

    /**
     * Returns the meta data instance that can be identified by the given UUID
     * @param uuid - UUID of the metadata insatnce
     * @return meta data from the UUID
     */
    public Base getMetadata(String uuid);

    /**
     * Persisting the mata data version instance details to the repository
     * @param version - meta data instance
     */
    public void add(Version version);

    /**
     * Update the meta data version info with the given meta data instance
     * @param version - meta data version insatnce
     */
    public void update(Version version);

    /**
     * Returns the meta data version instance that can be identified by the given UUID
     * @param uuid - UUID of the metadata insatnce
     * @return meta data version from the UUID
     */
    public Version getMetadataVersion(String uuid);

    /**
     *  Search all meta data version instances of this particular type with the given search attributes
     * @param criteria Key value map that has search attributes
     * @return
     */
    public Version[] findMetadataVersions(Map<String,String> criteria);
}
