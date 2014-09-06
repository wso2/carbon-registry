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
import org.wso2.carbon.registry.metadata.AbstractBase;
import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.Constants;
import org.wso2.carbon.registry.metadata.Util;
import org.wso2.carbon.registry.metadata.exception.MetadataException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPServiceVersionV1 extends AbstractBase implements VersionV1 {

//  Type specific attributes goes here
    private final String ENDPOINT_URL = "endpointUrl";


//  Variables defined for internal implementation purpose
    protected static String mediaType = "vnd.wso2.version/service.http+xml;version=1";
    private static final Log log = LogFactory.getLog(HTTPServiceVersionV1.class);
    private String baseUUID;
    private String baseName;
    private static String ROOT_STORAGE_PATH = Constants.BASE_STORAGE_PATH
            + mediaType.split(";")[0].replaceAll("\\+", ".").replaceAll("\\.", "/")
            + "/v"
            + mediaType.split(";")[1].split("=")[1];
    private Map<String, List<String>> attributeMap = new HashMap<String, List<String>>();


    public HTTPServiceVersionV1(Registry registry, String name) throws MetadataException {
        super(name, true, registry);
        this.name = name;
    }

    public HTTPServiceVersionV1(Registry registry, String name, String uuid, String baseName, String baseUUID, Map<String, List<String>> propertyBag, Map<String, List<String>> attributeMap) throws MetadataException {
        super(name, uuid, false, propertyBag, registry);
        this.attributeMap = attributeMap;
        this.baseName = baseName;
        this.baseUUID = baseUUID;
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
    public String getVersionMediaType() {
        return null;
    }

    @Override
    public boolean isVersionType() {
        return isVersionType;
    }

    public String getEndpointUrl() {
        List<String> value = attributeMap.get(ENDPOINT_URL);
        return value != null ? value.get(0) : null;
    }

    public void setEndpointUrl(String endpointUrl) {
        List<String> value = attributeMap.get(ENDPOINT_URL);
        if (value == null) {
            List<String> valList = new ArrayList<String>();
            valList.add(endpointUrl);
            attributeMap.put(ENDPOINT_URL, valList);
        } else {
            attributeMap.get(ENDPOINT_URL).add(endpointUrl);
        }
    }

    public static void add(Registry registry, Base metadata) throws MetadataException {
        add(registry, metadata, Util.getProvider(mediaType), generateMetadataStoragePath(
                ((HTTPServiceVersionV1) metadata).getBaseName()
                , metadata.getName()
                , ROOT_STORAGE_PATH));

        Util.createAssociation(registry, ((HTTPServiceVersionV1) metadata).getBaseUUID(), metadata.getUUID(), Constants.CHILD_VERSION);
        Util.createAssociation(registry, metadata.getUUID(), ((HTTPServiceVersionV1) metadata).getBaseUUID(), Constants.VERSION_OF);
    }

    public static void update(Registry registry, Base metadata) throws MetadataException {
        update(registry, metadata, Util.getProvider(mediaType), generateMetadataStoragePath(
                ((HTTPServiceVersionV1) metadata).getBaseName()
                , metadata.getName()
                , ROOT_STORAGE_PATH));
    }

    public static void delete(Registry registry, String uuid) throws MetadataException {
        deleteResource(registry, uuid);
    }

    /**
     * @return all meta data instances and their children that denotes from this particular media type
     */
    public static HTTPServiceVersionV1[] getAll(Registry registry) throws MetadataException {
        List<Base> list = getAll(registry, Util.getProvider(mediaType), mediaType);
        return list.toArray(new HTTPServiceVersionV1[list.size()]);
    }

    /**
     * Search all meta data instances of this particular type with the given search attributes
     *
     * @param criteria Key value map that has search attributes
     * @return
     */
    public static HTTPServiceVersionV1[] find(Registry registry, Map<String, String> criteria) throws MetadataException {
        List<Base> list = find(registry, criteria, Util.getProvider(mediaType), mediaType);
        return list.toArray(new HTTPServiceVersionV1[list.size()]);
    }

    /**
     * Returns the meta data instance that can be identified by the given UUID
     *
     * @param uuid - UUID of the metadata insatnce
     * @return meta data from the UUID
     */
    public static HTTPServiceVersionV1 get(Registry registry, String uuid) throws MetadataException {
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
        this.baseName = name;
    }

    @Override
    public String getBaseName() {
        return baseName;
    }

    private static String generateMetadataStoragePath(String name, String version, String rootStoragePath) {
        return rootStoragePath + "/" + name + "/" + version;
    }
}
