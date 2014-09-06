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
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.metadata.AbstractBase;
import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.Constants;
import org.wso2.carbon.registry.metadata.Util;
import org.wso2.carbon.registry.metadata.exception.MetadataException;
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
    private static String ROOT_STORAGE_PATH = Constants.BASE_STORAGE_PATH
            + mediaType.split(";")[0].replaceAll("\\+", ".").replaceAll("\\.", "/")
            + "/v"
            + mediaType.split(";")[1].split("=")[1];

    private Map<String, List<String>> attributeMap = new HashMap<String, List<String>>();
    private HTTPServiceVersionV1 baseVersion = null;

    public HTTPServiceV1(Registry registry, String name, VersionV1 version) throws MetadataException {
        super(name, false, registry);
        version.setBaseUUID(uuid);
        version.setBaseName(name);
        baseVersion = (HTTPServiceVersionV1) version;
    }

    public HTTPServiceV1(Registry registry, String name, String uuid, Map<String, List<String>> propertyBag, Map<String, List<String>> attributeMap) throws MetadataException {
        super(name, uuid, false, propertyBag, registry);
        this.attributeMap = attributeMap;
    }

    @Override
    public HTTPServiceVersionV1 newVersion(String key) throws MetadataException {
        HTTPServiceVersionV1 v = new HTTPServiceVersionV1(registry, key);
        v.setBaseUUID(uuid);
        v.setBaseName(name);
        return v;
    }

    @Override
    public HTTPServiceVersionV1[] getVersions() throws MetadataException {
        ArrayList<Base> list = getAllVersions(uuid, versionMediaType);
        HTTPServiceVersionV1[] arr = new HTTPServiceVersionV1[list.size()];
        arr = list.toArray(arr);
        return arr;
    }

    @Override
    public HTTPServiceVersionV1 getVersion(int major, int minor, int patch) throws MetadataException {
        //        TODO return index search result
        String version = String.valueOf(major) + "." + String.valueOf(minor) + "." + String.valueOf(patch);
        for (Base v : getAllVersions(uuid, versionMediaType)) {
            HTTPServiceVersionV1 http = (HTTPServiceVersionV1) v;
            if (version.equals(http.getName())) {
                return http;
            }

        }
        return null;
    }

    private VersionV1 getBaseVersion() {
        return baseVersion;
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
        return versionMediaType;
    }

    @Override
    public boolean isVersionType() {
        return isVersionType;
    }

    @Override
    public void setOwner(String owner) {
        if (attributeMap.get(OWNER) == null) {
            List<String> value = new ArrayList<String>();
            value.add(owner);
            attributeMap.put(OWNER, value);
        } else {
            attributeMap.get(OWNER).add(owner);
        }
    }

    @Override
    public String getOwner() {
        List<String> value = attributeMap.get(OWNER);
        return value != null ? value.get(0) : null;
    }

    public static void add(Registry registry, Base metadata) throws MetadataException {
        if (((HTTPServiceV1) metadata).getBaseVersion() == null) {
            add(registry, metadata, Util.getProvider(mediaType),
                    generateMetadataStoragePath(metadata.getName(), ROOT_STORAGE_PATH));
        } else {
            add(registry, metadata, Util.getProvider(mediaType),
                    generateMetadataStoragePath(metadata.getName(), ROOT_STORAGE_PATH));
            HTTPServiceVersionV1.add(registry, ((HTTPServiceV1) metadata).getBaseVersion());
        }
    }

    public static void update(Registry registry, Base metadata) throws MetadataException {
        update(registry, metadata, Util.getProvider(mediaType),
                generateMetadataStoragePath(metadata.getName(), ROOT_STORAGE_PATH));
    }

    public static void delete(Registry registry, String uuid) throws MetadataException {
        deleteResource(registry, uuid);
//        TODO Need to remove the associations for this UUID from the association table
//        TODO remove index
    }

    /**
     * @return all meta data instances and their children that denotes from this particular media type
     */
    public static HTTPServiceV1[] getAll(Registry registry) throws MetadataException {
        List<Base> list = getAll(registry, Util.getProvider(mediaType), mediaType);
        return list.toArray(new HTTPServiceV1[list.size()]);
    }

    /**
     * Search all meta data instances of this particular type with the given search attributes
     *
     * @param criteria Key value map that has search attributes
     * @return
     */
    public static HTTPServiceV1[] find(Registry registry, Map<String, String> criteria) throws MetadataException {
        List<Base> list = find(registry, criteria, Util.getProvider(mediaType), mediaType);
        return list.toArray(new HTTPServiceV1[list.size()]);
    }

    /**
     * Returns the meta data instance that can be identified by the given UUID
     *
     * @param uuid - UUID of the metadata insatnce
     * @return meta data from the UUID
     */
    public static HTTPServiceV1 get(Registry registry, String uuid) throws MetadataException {
        return (HTTPServiceV1) get(registry, uuid, Util.getProvider(mediaType));
    }

    public Map<String, List<String>> getAttributeMap() {
        return attributeMap;
    }

    public void setAttributeMap(Map<String, List<String>> attributeMap) {
        this.attributeMap = attributeMap;
    }

    private static String generateMetadataStoragePath(String name, String rootStoragePath) {
        return rootStoragePath + "/" + name;
    }
}
