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

import org.wso2.carbon.registry.metadata.Base;

public interface MetadataProvider {

    /**
     * Persists the metadata information in to the repository layer
     * @param metadata  the particular metadata object that needs to insert
     */
    public void insert(Base metadata);

    /**
     * Update the meta data information
     * @param metadata  the particular metadata object that needs to update
     */
    public void update(Base metadata);

    /**
     * Obtain the metadata info from the repository and construct the Meta data instance
     * @param uuid - UUID that represents the meta data instance
     * @return
     */
    public Base get(String uuid);

}
