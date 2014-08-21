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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.Util;
import org.wso2.carbon.registry.metadata.provider.MetadataProvider;
import org.wso2.carbon.registry.metadata.version.Version;

import java.util.HashMap;
import java.util.Map;

public class BaseMetadataManager implements MetadataManager {

    private String mediaType;
    private MetadataProvider provider;
    private Registry registry;
    private String rootStoragePath;
    private static final Log log = LogFactory.getLog(BaseMetadataManager.class);

    public BaseMetadataManager(String mediaType) throws RegistryException {
        this.mediaType = mediaType;
        this.provider = Util.getProvider(mediaType);
//        rootStoragePath = StringBuilder(Constants.BASE_STORAGE_PATH).append(mediaType.split("/")[0].replaceAll(".","/")).append("/").append(mediaType.split("/"))
    }

    @Override
    public Base newInstance(String name) {
        return provider.createNewInstance(name);
    }

    @Override
    public void delete(String uuid) throws RegistryException  {
        String path = getMetadataPath(uuid);
        if(registry.resourceExists(path)) {
             registry.delete(path);
         } else {
            log.error("Metadata instance " + uuid + " does not exists at path " + path);
        }
    }

    @Override
    public void add(Base metadata) throws RegistryException {
//TODO Index
        Resource resource = provider.buildResource(metadata);
        putResource(generateMetadataStoragePath(metadata.getUUID()),resource);
    }

    @Override
    public void update(Base metadata) throws RegistryException {
//TODO Index
        provider.updateResource(metadata,getResource(metadata.getUUID()));
    }

    @Override
    public Base[] getAllMetadata() {
//        Indexer result
        return new Base[0];
    }

    @Override
    public Base[] findMetadata(Map<String, String> criteria) {
//        indexer result
        return new Base[0];
    }

    @Override
    public Base getMetadata(String uuid) throws RegistryException {
        return provider.get(getResource(uuid));
    }

    @Override
    public void add(Version version) {

    }

    @Override
    public void update(Version version) {

    }

    @Override
    public Version getMetadataVersion(String uuid) {
        return null;
    }

    @Override
    public Version[] findMetadataVersions(Map<String, String> criteria) {
        return new Version[0];
    }

    private String generateMetadataStoragePath(String uuid){
        return rootStoragePath + "/" + uuid;
    }

    private Resource getResource(String uuid) throws RegistryException {
        String path = getMetadataPath(uuid);
        if(path == null){
            return null;
        }
        if(registry.resourceExists(path)) {
            return registry.get(path);
        } else {
           log.error("Metadata instance " + uuid + " does not exists at path "+path);
           return null;
        }
    }

    private String getMetadataPath(String uuid)
            throws RegistryException {

        try {
            String sql = "SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE WHERE REG_UUID = ?";

            String[] result;
            Map<String, String> parameter = new HashMap<String, String>();
            parameter.put("1", uuid);
            parameter.put("query", sql);
            result = registry.executeQuery(null, parameter).getChildren();

            if (result != null && result.length == 1) {
                return result[0];
            }
            return null;
        } catch (RegistryException e) {
            String msg = "Error in getting the path from the registry. Execute query failed with message : "
                    + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    private void putResource(String path,Resource resource) throws RegistryException {
        boolean succeeded = false;
        try {
            registry.beginTransaction();
            if(registry.resourceExists(path)){
                throw new RegistryException("Metadata instance " + resource.getUUID() + " already exists at "+path);
            }
            registry.put(path, resource);
            succeeded = true;
        }
        catch (RegistryException e) {
            throw new RegistryException("Failed to persist the resource");
        } finally {
            if (succeeded) {
                try {
                    registry.commitTransaction();
                } catch (RegistryException e) {
                    log.error("Error in commiting transaction for the meta data instance "+resource.getUUID(), e);
                }
            } else {
                try {
                    registry.rollbackTransaction();
                } catch (RegistryException e) {
                    log.error("Error in rollbacking transaction for the meta data instance "+resource.getUUID(), e);

                }
            }
        }
    }
}
