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
package org.wso2.carbon.registry.metadata.version;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.metadata.AbstractBase;
import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.Constants;
import org.wso2.carbon.registry.metadata.Util;
import org.wso2.carbon.registry.metadata.provider.MetadataProvider;

import java.util.HashMap;
import java.util.Map;

public class HTTPServiceVersionV1 extends AbstractBase implements VersionV1 {

//    private String name;
    private String endpointUrl;
    protected static String mediaType = "vnd.wso2.version/service.http+xml;version=1";
    private static final Log log = LogFactory.getLog(HTTPServiceVersionV1.class);
    private static final String rootStoragePath = Constants.BASE_STORAGE_PATH + "version";
    private String baseUUID;
    private String baseName;
    private Map<String,String> attributeMap = new HashMap<String, String>();
    private static String ROOT_STORAGE_PATH = Constants.BASE_STORAGE_PATH
            + mediaType.split(";")[0].replaceAll("\\+",".").replaceAll("\\.","/")
            + "/v"
            + mediaType.split(";")[1].split("=")[1];


    public HTTPServiceVersionV1(Registry registry,String name) throws RegistryException {
        super(name,true,registry);
        this.name = name;
    }

    public HTTPServiceVersionV1(Registry registry,String name, String uuid,String baseName,String baseUUID,Map<String,String> propertyBag,Map<String,String> attributeMap) throws RegistryException {
        super(name,uuid,false,propertyBag,registry);
        this.attributeMap = attributeMap;
        this.baseName = baseName;
        this.baseUUID = baseUUID;
    }

    @Override
    public String getUUID() throws RegistryException {
        return uuid;
    }

    @Override
    public String getName() throws RegistryException {
        return this.name;
    }

    @Override
    public String getMediaType() throws RegistryException {
        return mediaType;
    }

    @Override
    public String getVersionMediaType() throws RegistryException {
        return null;
    }

    @Override
    public void setProperty(String key, String value) throws RegistryException {
       propertyBag.put(key,value);
    }

    @Override
    public void removeProperty(String key) throws RegistryException {
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

    public String getEndpointUrl() {
       return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }


    public static void add(Registry registry,Base metadata) throws RegistryException {
        add(registry,metadata,Util.getProvider(mediaType),generateMetadataStoragePath(
                ((HTTPServiceVersionV1)metadata).getBaseName()
                ,metadata.getName()
                , ROOT_STORAGE_PATH));

        Util.createAssociation(registry,((HTTPServiceVersionV1)metadata).getBaseUUID(), metadata.getUUID(), Constants.CHILD_VERSION);
        Util.createAssociation(registry,metadata.getUUID(),((HTTPServiceVersionV1)metadata).getBaseUUID(), Constants.VERSION_OF);
//      TODO add Index
    }

    public static void update(Registry registry,Base metadata) throws RegistryException {
        update(registry,metadata,Util.getProvider(mediaType), generateMetadataStoragePath(
                ((HTTPServiceVersionV1)metadata).getBaseName()
                ,metadata.getName()
                , ROOT_STORAGE_PATH));
//      TODO update index
    }

    public static void delete(Registry registry,String uuid) throws RegistryException {
        deleteResource(registry,uuid);
//      TODO remove index
    }
    /**
     *
     * @return all meta data instances and their children that denotes from this particular media type
     */
    public static HTTPServiceVersionV1[] getAll(Registry registry) throws RegistryException {
        return (HTTPServiceVersionV1[]) getAll(registry,Util.getProvider(mediaType));
    }

    /**
     *  Search all meta data instances of this particular type with the given search attributes
     * @param criteria Key value map that has search attributes
     * @return
     */
    public static HTTPServiceVersionV1[] find(Registry registry,Map<String,String> criteria) throws RegistryException {
        return (HTTPServiceVersionV1[]) find(registry,criteria,Util.getProvider(mediaType));
    }

    /**
     * Returns the meta data instance that can be identified by the given UUID
     * @param uuid - UUID of the metadata insatnce
     * @return meta data from the UUID
     */
    public static HTTPServiceVersionV1 get(Registry registry,String uuid) throws RegistryException {
        return (HTTPServiceVersionV1) get(registry, uuid, Util.getProvider(mediaType));
    }

    @Override
    public void setBaseUUID(String name) {
        this.baseUUID = name;
    }

    @Override
    public String getBaseUUID() {
        return baseUUID;
    }

    @Override
    public void setBaseName(String name) {
        this.baseName=name;
    }

    @Override
    public String getBaseName() {
        return baseName;
    }

    private static String generateMetadataStoragePath(String name,String version,String rootStoragePath) {
        return rootStoragePath + "/" + name + "/" + version;
    }
}
