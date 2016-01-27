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

package org.wso2.carbon.metadata.server.impl;

import org.wso2.carbon.metadata.server.api.Collection;
import org.wso2.carbon.metadata.server.api.Key;
import org.wso2.carbon.metadata.server.api.MetadataStore;
import org.wso2.carbon.metadata.server.api.MetadataStoreException;
import org.wso2.carbon.metadata.server.api.Resource;
import org.wso2.carbon.metadata.server.dao.MetadataDAO;
import org.wso2.carbon.metadata.server.dao.jdbc.JDBCMetadataDAO;

import java.util.List;

/**
 * This is the actual implementation of the MetadataStore
 */
public class MetadataStoreImpl implements MetadataStore {

    private MetadataDAO metadataDAO;

    public MetadataStoreImpl() {
        setMetadataDAO(new JDBCMetadataDAO());
    }

    public MetadataDAO getMetadataDAO() {
        return metadataDAO;
    }

    public void setMetadataDAO(MetadataDAO metadataDAO) {
        this.metadataDAO = metadataDAO;
    }

    @Override
    public Resource newResource() throws MetadataStoreException {
        this.metadataDAO = null;
        return null;
    }

    @Override
    public Collection newCollection() throws MetadataStoreException {
        return null;
    }

    @Override
    public String put(Key key, Resource resource) throws MetadataStoreException {
        return null;
    }

    @Override
    public String put(Key key) throws MetadataStoreException {
        return null;
    }

    @Override
    public void remove(String uuid) throws MetadataStoreException {

    }

    @Override
    public Resource get(Key key) throws MetadataStoreException {
        return null;
    }

    @Override
    public Resource get(String uuid) throws MetadataStoreException {
        return null;
    }

    @Override
    public String exists(Key key) throws MetadataStoreException {
        return null;
    }

    @Override
    public String exists(String uuid) throws MetadataStoreException {
        return null;
    }

    @Override
    public List<Resource> find(String... queryParam) throws MetadataStoreException {
        return null;
    }
}
