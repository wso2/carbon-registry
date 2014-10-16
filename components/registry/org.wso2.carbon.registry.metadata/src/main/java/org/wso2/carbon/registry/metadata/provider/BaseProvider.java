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
package org.wso2.carbon.registry.metadata.provider;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.exception.MetadataException;

public interface BaseProvider {

    public String getVersionMediaType();

    public String getMediaType();

    /**
     * Persists the metadata information in to the repository layer
     *
     * @param metadata the particular metadata object that needs to insert
     * @return Resource with the new content
     */
    public Resource buildResource(Base metadata, Resource resource) throws MetadataException;

    /**
     * Update the meta data information
     *
     * @param newMetadata the particular metadata object that needs to update
     * @return Resource with the updated content
     */
    public Resource updateResource(Base newMetadata, Resource resource) throws MetadataException;

    /**
     * Construct the Meta data instance
     *
     * @param resource - Resource instance that has metadata information stored in.
     * @return Base type metadata instance constructed from the resource
     */
    public Base get(Resource resource, Registry registry) throws MetadataException;

}
