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
package org.wso2.carbon.registry.metadata.models.endpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.VersionBase;
import org.wso2.carbon.registry.metadata.exception.MetadataException;
import org.wso2.carbon.registry.metadata.models.version.EndpointVersionV1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HTTPEndpointV1 extends Base {

//  Type specific attributes goes here
    public static final String KEY_URL = "url";

    //  Variables defined for the internal implementation
    private static final Log log = LogFactory.getLog(HTTPEndpointV1.class);
    private static final String mediaType = "vnd.wso2.endpoint/http+xml;version=1";

    public HTTPEndpointV1(Registry registry, String name) throws MetadataException {
        super(mediaType, name, registry);
    }

    public HTTPEndpointV1(Registry registry, String name, VersionBase version) throws MetadataException {
        super(mediaType, name, registry,version);
    }

    public HTTPEndpointV1(Registry registry, String name, String uuid, Map<String, List<String>> propertyBag, Map<String, List<String>> attributeMap) throws MetadataException {
        super(mediaType,name, uuid, propertyBag,attributeMap, registry);
    }

    public EndpointVersionV1 newVersion(String key) throws MetadataException {
        EndpointVersionV1 v = new EndpointVersionV1(registry, key);
        v.setBaseUUID(uuid);
        v.setBaseName(name);
        return v;
    }

    public void setUrl(String url) {
        setAttribute(KEY_URL,url);
    }

    public String getUrl() {
       return getSingleValuedAttribute(KEY_URL);
    }

    public static void add(Registry registry, Base metadata) throws MetadataException {
        if (((HTTPEndpointV1) metadata).baseVersion == null) {
            add(registry, metadata,
                    generateMetadataStoragePath(metadata.getName(), metadata.getRootStoragePath()));
        } else {
            add(registry, metadata,
                    generateMetadataStoragePath(metadata.getName(), metadata.getRootStoragePath()));
            EndpointVersionV1.add(registry, ((HTTPEndpointV1) metadata).baseVersion);
        }
    }

    public static void update(Registry registry, Base metadata) throws MetadataException {
        update(registry, metadata,
                generateMetadataStoragePath(metadata.getName(), metadata.getRootStoragePath()));
    }

    /**
     * @return all meta data instances and their children that denotes from this particular media type
     */
    public static HTTPEndpointV1[] getAll(Registry registry) throws MetadataException {
        List<Base> list = getAll(registry, mediaType);
        return list.toArray(new HTTPEndpointV1[list.size()]);
    }

    /**
     * Search all meta data instances of this particular type with the given search attributes
     *
     * @param criteria Key value map that has search attributes
     * @return
     */
    public static HTTPEndpointV1[] find(Registry registry, Map<String, String> criteria) throws MetadataException {
        List<Base> list = find(registry, criteria, mediaType);
        return list.toArray(new HTTPEndpointV1[list.size()]);
    }

    /**
     * Returns the meta data instance that can be identified by the given UUID
     *
     * @param uuid - UUID of the metadata insatnce
     * @return meta data from the UUID
     */
    public static HTTPEndpointV1 get(Registry registry, String uuid) throws MetadataException {
        return (HTTPEndpointV1) get(registry, uuid, mediaType);
    }

    private void setAttribute(String key,String val){
        List<String> value = new ArrayList<String>();
        value.add(val);
        attributeMap.put(key,value);
    }

    private String getSingleValuedAttribute(String key){
        List<String> value = attributeMap.get(key);
        return value != null ? value.get(0) : null;
    }

}
