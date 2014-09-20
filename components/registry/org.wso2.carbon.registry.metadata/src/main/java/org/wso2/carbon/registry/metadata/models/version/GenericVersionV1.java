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
package org.wso2.carbon.registry.metadata.models.version;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.metadata.VersionBase;
import org.wso2.carbon.registry.metadata.exception.MetadataException;

import java.util.List;
import java.util.Map;

public class GenericVersionV1 extends VersionBase {

//  Variables defined for internal implementation purpose
    private static String mediaType = "vnd.wso2.version/generic+xml;version=1";
    private static final Log log = LogFactory.getLog(GenericVersionV1.class);

    public GenericVersionV1(Registry registry, String name) throws MetadataException {
        super(mediaType,name,registry);
        this.name = name;
    }

    public GenericVersionV1(Registry registry, String name, String uuid, String baseName, String baseUUID, Map<String, List<String>> propertyBag, Map<String, List<String>> attributeMap) throws MetadataException {
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

    public static void add(Registry registry, VersionBase metadata) throws MetadataException {
        add(registry, metadata, generateMetadataStoragePath(
                 metadata.getBaseName()
                , metadata.getName()
                , metadata.getRootStoragePath()));

    }

    public static void update(Registry registry, VersionBase metadata) throws MetadataException {
        update(registry, metadata, generateMetadataStoragePath(
                 metadata.getBaseName()
                , metadata.getName()
                , metadata.getRootStoragePath()));
    }


    /**
     * @return all meta data instances and their children that denotes from this particular media type
     */
    public static GenericVersionV1[] getAll(Registry registry) throws MetadataException {
        List<VersionBase> list = getAll(registry, mediaType);
        return list.toArray(new GenericVersionV1[list.size()]);
    }

    /**
     * Search all meta data instances of this particular type with the given search attributes
     *
     * @param criteria Key value map that has search attributes
     * @return
     */
    public static GenericVersionV1[] find(Registry registry, Map<String, String> criteria) throws MetadataException {
        List<VersionBase> list = find(registry, criteria, mediaType);
        return list.toArray(new GenericVersionV1[list.size()]);
    }

    /**
     * Returns the meta data instance that can be identified by the given UUID
     *
     * @param uuid - UUID of the metadata insatnce
     * @return meta data from the UUID
     */
    public static GenericVersionV1 get(Registry registry, String uuid) throws MetadataException {
        return (GenericVersionV1) get(registry, uuid, mediaType);
    }

}
