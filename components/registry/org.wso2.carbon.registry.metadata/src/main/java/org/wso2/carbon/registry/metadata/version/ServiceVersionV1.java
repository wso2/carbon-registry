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
import org.wso2.carbon.registry.metadata.Constants;
import org.wso2.carbon.registry.metadata.exception.MetadataException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServiceVersionV1 extends VersionBase {

//  Type specific attributes goes here
    public static final String ENDPOINT_URL = "endpointUrl";


//  Variables defined for internal implementation purpose
    protected static String mediaType = "vnd.wso2.version/service+xml;version=1";
    private static final Log log = LogFactory.getLog(ServiceVersionV1.class);

    public ServiceVersionV1(Registry registry, String name) throws MetadataException {
        super(mediaType,name,registry);
        this.name = name;
    }

    public ServiceVersionV1(Registry registry, String name, String uuid, String baseName, String baseUUID, Map<String, List<String>> propertyBag, Map<String, List<String>> attributeMap) throws MetadataException {
        super(mediaType,name, uuid, baseName,baseUUID,propertyBag,attributeMap, registry);
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getMediaType() {
        return mediaType;
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

    public static void add(Registry registry, VersionBase metadata) throws MetadataException {
        add(registry, metadata, generateMetadataStoragePath(
                ((ServiceVersionV1) metadata).getBaseName()
                , metadata.getName()
                , metadata.getRootStoragePath()));

    }

    public static void update(Registry registry, VersionBase metadata) throws MetadataException {
        update(registry, metadata, generateMetadataStoragePath(
                ((ServiceVersionV1) metadata).getBaseName()
                , metadata.getName()
                , metadata.getRootStoragePath()));
    }


    /**
     * @return all meta data instances and their children that denotes from this particular media type
     */
    public static ServiceVersionV1[] getAll(Registry registry) throws MetadataException {
        List<VersionBase> list = getAll(registry, mediaType);
        return list.toArray(new ServiceVersionV1[list.size()]);
    }

    /**
     * Search all meta data instances of this particular type with the given search attributes
     *
     * @param criteria Key value map that has search attributes
     * @return
     */
    public static ServiceVersionV1[] find(Registry registry, Map<String, String> criteria) throws MetadataException {
        List<VersionBase> list = find(registry, criteria, mediaType);
        return list.toArray(new ServiceVersionV1[list.size()]);
    }

    /**
     * Returns the meta data instance that can be identified by the given UUID
     *
     * @param uuid - UUID of the metadata insatnce
     * @return meta data from the UUID
     */
    public static ServiceVersionV1 get(Registry registry, String uuid) throws MetadataException {
        return (ServiceVersionV1) get(registry, uuid, mediaType);
    }



    private static String generateMetadataStoragePath(String name, String version, String rootStoragePath) {
        return rootStoragePath + "/" + name + "/" + version;
    }
}
