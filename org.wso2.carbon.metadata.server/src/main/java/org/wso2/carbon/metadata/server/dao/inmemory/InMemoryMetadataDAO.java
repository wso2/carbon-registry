package org.wso2.carbon.metadata.server.dao.inmemory;

import org.wso2.carbon.metadata.server.api.Collection;
import org.wso2.carbon.metadata.server.api.Key;
import org.wso2.carbon.metadata.server.api.MetadataStoreException;
import org.wso2.carbon.metadata.server.dao.MetadataDAO;
import org.wso2.carbon.metadata.server.impl.ResourceImpl;

/**
 * In Memory Metadata DAO
 */
public class InMemoryMetadataDAO implements MetadataDAO {
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
