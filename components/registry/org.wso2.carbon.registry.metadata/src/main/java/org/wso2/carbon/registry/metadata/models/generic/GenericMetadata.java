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
package org.wso2.carbon.registry.metadata.models.generic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.exception.MetadataException;
import org.wso2.carbon.registry.metadata.models.version.GenericVersionV1;
import org.wso2.carbon.registry.metadata.models.version.ServiceVersionV1;
import org.wso2.carbon.registry.metadata.VersionBase;

import java.util.List;
import java.util.Map;

public class GenericMetadata extends Base {


//  Variables defined for the internal implementation
    private static final Log log = LogFactory.getLog(GenericMetadata.class);

    private static final String mediaType = "vnd.wso2.generic/+xml;version=1";

    private GenericVersionV1 baseVersion = null;

    public GenericMetadata(Registry registry, String name, VersionBase version) throws MetadataException {
        super(mediaType, name, registry,version);
    }

    public GenericMetadata(Registry registry, String name, String uuid, Map<String, List<String>> propertyBag, Map<String, List<String>> attributeMap) throws MetadataException {
        super(mediaType,name, uuid, propertyBag,attributeMap, registry);
    }

    public GenericVersionV1 newVersion(String key) throws MetadataException {
        GenericVersionV1 v = new GenericVersionV1(registry, key);
        v.setBaseUUID(uuid);
        v.setBaseName(name);
        return v;
    }


    public static void add(Registry registry, Base metadata) throws MetadataException {
        if (((GenericMetadata) metadata).baseVersion == null) {
            add(registry, metadata,
                    generateMetadataStoragePath(metadata.getName(), metadata.getRootStoragePath()));
        } else {
            add(registry, metadata,
                    generateMetadataStoragePath(metadata.getName(), metadata.getRootStoragePath()));
            GenericVersionV1.add(registry, ((GenericMetadata) metadata).baseVersion);
        }
    }

    public static void update(Registry registry, Base metadata) throws MetadataException {
        update(registry, metadata,
                generateMetadataStoragePath(metadata.getName(), metadata.getRootStoragePath()));
    }

    /**
     * @return all meta data instances and their children that denotes from this particular media type
     */
    public static GenericMetadata[] getAll(Registry registry) throws MetadataException {
        List<Base> list = getAll(registry, mediaType);
        return list.toArray(new GenericMetadata[list.size()]);
    }

    /**
     * Search all meta data instances of this particular type with the given search attributes
     *
     * @param criteria Key value map that has search attributes
     * @return
     */
    public static GenericMetadata[] find(Registry registry, Map<String, String> criteria) throws MetadataException {
        List<Base> list = find(registry, criteria, mediaType);
        return list.toArray(new GenericMetadata[list.size()]);
    }

    /**
     * Returns the meta data instance that can be identified by the given UUID
     *
     * @param uuid - UUID of the metadata insatnce
     * @return meta data from the UUID
     */
    public static GenericMetadata get(Registry registry, String uuid) throws MetadataException {
        return (GenericMetadata) get(registry, uuid, mediaType);
    }

}
