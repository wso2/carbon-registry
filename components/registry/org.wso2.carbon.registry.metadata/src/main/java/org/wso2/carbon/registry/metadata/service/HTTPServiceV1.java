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
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.metadata.AbstractBase;
import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.Util;
import org.wso2.carbon.registry.metadata.lifecycle.StateMachineLifecycle;
import org.wso2.carbon.registry.metadata.provider.MetadataProvider;
import org.wso2.carbon.registry.metadata.version.HTTPServiceVersionV1;
import org.wso2.carbon.registry.metadata.version.VersionV1;

import java.util.Map;

public class HTTPServiceV1 extends AbstractBase implements ServiceV1 {

    private String owner;
    private static final Log log = LogFactory.getLog(HTTPServiceV1.class);
    private static String mediaType = "vnd.wso2.service/http+xml;version=1";
    private static MetadataProvider provider;

    public HTTPServiceV1(String name,VersionV1 version) throws RegistryException {
        super(name);
        this.provider = Util.getProvider(mediaType);
        version.setBaseName(name);
        HTTPServiceVersionV1.add(version);
    }

    @Override
    public HTTPServiceVersionV1 newVersion(String key) throws RegistryException {
        HTTPServiceVersionV1 v = new HTTPServiceVersionV1(key);
        v.setBaseName(name);
        return v;
    }

    @Override
    public HTTPServiceVersionV1[] getVersions() {
        return new HTTPServiceVersionV1[0];
    }

    @Override
    public HTTPServiceVersionV1 getVersion(int major, int minor, int patch) {
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
    public void setProperty(String key, String value) {
         propertyBag.put(key,value);
    }

    @Override
    public void removeProperty(String key) {
        propertyBag.remove(key);
    }

    @Override
    public void setOwner(String owner) {
       this.owner = owner;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    public static void add(Base metadata) throws RegistryException {
          add(metadata,provider);
//        TODO add Index

    }

    public static void update(Base metadata) throws RegistryException {
        update(metadata,provider);
//        TODO update index
    }

        public static void delete(String uuid) throws RegistryException {
           delete(uuid);
//        TODO remove index
    }
        /**
         *
         * @return all meta data instances and their children that denotes from this particular media type
         */
    public static HTTPServiceV1[] getAll() throws RegistryException {
        return (HTTPServiceV1[]) getAll(provider);
    }

    /**
     *  Search all meta data instances of this particular type with the given search attributes
     * @param criteria Key value map that has search attributes
     * @return
     */
    public static HTTPServiceV1[] find(Map<String,String> criteria) throws RegistryException {
        return (HTTPServiceV1[]) find(criteria,provider);
    }

    /**
     * Returns the meta data instance that can be identified by the given UUID
     * @param uuid - UUID of the metadata insatnce
     * @return meta data from the UUID
     */
    public static HTTPServiceV1 get(String uuid) throws RegistryException {
        return (HTTPServiceV1) get(uuid,provider);
    }


}
