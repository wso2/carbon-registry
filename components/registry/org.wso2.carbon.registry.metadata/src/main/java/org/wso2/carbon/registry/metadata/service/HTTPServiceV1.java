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
package org.wso2.carbon.registry.metadata.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.api.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.metadata.AbstractBase;
import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.Constants;
import org.wso2.carbon.registry.metadata.Util;
import org.wso2.carbon.registry.metadata.lifecycle.StateMachineLifecycle;
import org.wso2.carbon.registry.metadata.provider.MetadataProvider;
import org.wso2.carbon.registry.metadata.version.HTTPServiceVersionV1;
import org.wso2.carbon.registry.metadata.version.VersionV1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPServiceV1 extends AbstractBase implements ServiceV1 {

    // Service attributes defines here
    private final String OWNER = "owner";

    private static final Log log = LogFactory.getLog(HTTPServiceV1.class);
    private static String mediaType = "vnd.wso2.service/http+xml;version=1";
    private static String versionMediaType = "vnd.wso2.version/service.http+xml;version=1";
    private static final String rootStoragePath = Constants.BASE_STORAGE_PATH + "service/http"; //TODO construct this
    private Map<String,String> attributeMap = new HashMap<String, String>();

    public HTTPServiceV1(Registry registry,String name,VersionV1 version) throws RegistryException {
        super(name,false,registry);
        version.setBaseUUID(uuid);
        HTTPServiceVersionV1.add(registry,version);
    }

    public HTTPServiceV1(Registry registry,String name, String uuid, Map<String,String> propertyBag,Map<String,String> attributeMap) throws RegistryException {
        super(name,uuid,false,propertyBag,registry);
        this.attributeMap = attributeMap;
    }

    @Override
    public HTTPServiceVersionV1 newVersion(String key) throws RegistryException {
        HTTPServiceVersionV1 v = new HTTPServiceVersionV1(key,registry);
        v.setBaseUUID(uuid);
        return v;
    }

    @Override
    public HTTPServiceVersionV1[] getVersions() throws RegistryException {
        return (HTTPServiceVersionV1[]) getAllVersions(uuid,versionMediaType);
    }

    @Override
    public HTTPServiceVersionV1 getVersion(int major, int minor, int patch) throws RegistryException {
        //        TODO return index search result
        String version = String.valueOf(major) + "." + String.valueOf(minor) + "." + String.valueOf(patch);
        for(Base v:getAllVersions(uuid,versionMediaType)) {
            HTTPServiceVersionV1 http = (HTTPServiceVersionV1) v;
            if(version.equals(http.getName())){
                return http;
            }

        }
        return null;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMediaType() {
        return mediaType;
    }

    @Override
    public String getVersionMediaType() throws RegistryException {
        return versionMediaType;
    }

    @Override
    public void setProperty(String key, String value) {
         propertyBag.put(key,value);
    }

    @Override
    public void removeProperty(String key) {
        propertyBag.remove(key);
    }

    @Override
    public String getProperty(String key) throws RegistryException {
        return propertyBag.get(key);
    }

    @Override
    public boolean isVersionType() {
        return isVersionType;
    }

    @Override
    public void setOwner(String owner) {
         attributeMap.put(OWNER,owner);
    }

    @Override
    public String getOwner() {
        return attributeMap.get(OWNER);
    }

    public static void add(Registry registry,Base metadata) throws RegistryException {
          add(registry,metadata,Util.getProvider(mediaType));

//        TODO add Index
    }

    public static void update(Registry registry,Base metadata) throws RegistryException {
        update(registry,metadata,Util.getProvider(mediaType));
//        TODO update index
    }

    public static void delete(Registry registry,String uuid) throws RegistryException {
            deleteResource(registry,uuid);
//        TODO Need to remove the associations for this UUID from the association table
//        TODO remove index
    }
        /**
         *
         * @return all meta data instances and their children that denotes from this particular media type
         */
    public static HTTPServiceV1[] getAll(Registry registry) throws RegistryException {
        return (HTTPServiceV1[]) getAll(registry,Util.getProvider(mediaType));
    }

    /**
     *  Search all meta data instances of this particular type with the given search attributes
     * @param criteria Key value map that has search attributes
     * @return
     */
    public static HTTPServiceV1[] find(Registry registry,Map<String,String> criteria) throws RegistryException {
        return (HTTPServiceV1[]) find(registry,criteria,Util.getProvider(mediaType));
    }

    /**
     * Returns the meta data instance that can be identified by the given UUID
     * @param uuid - UUID of the metadata insatnce
     * @return meta data from the UUID
     */
    public static HTTPServiceV1 get(Registry registry,String uuid) throws RegistryException {
        return (HTTPServiceV1) get(registry,uuid,Util.getProvider(mediaType));
    }

    public Map<String, String> getAttributeMap() {
        return attributeMap;
    }

    public void setAttributeMap(Map<String, String> attributeMap) {
        this.attributeMap = attributeMap;
    }
}
