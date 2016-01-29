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

package org.wso2.carbon.metadata.server.dao.jdbc;

import org.wso2.carbon.metadata.server.api.Collection;
import org.wso2.carbon.metadata.server.api.Key;
import org.wso2.carbon.metadata.server.api.MetadataStoreException;
import org.wso2.carbon.metadata.server.dao.MetadataDAO;
import org.wso2.carbon.metadata.server.impl.ResourceImpl;

/**
 * JDBC Metadata DAO
 */
public class JDBCMetadataDAO implements MetadataDAO {
    @Override public void add(ResourceImpl resourceImpl) throws MetadataStoreException {
        
    }

    @Override public void remove(String uuid) throws MetadataStoreException {

    }

    @Override public void remove(Key path) throws MetadataStoreException {

    }

    @Override public void update(String uuid) throws MetadataStoreException {

    }

    @Override public String[] getChildren(Collection collection) throws MetadataStoreException {
        return new String[0];
    }
}
