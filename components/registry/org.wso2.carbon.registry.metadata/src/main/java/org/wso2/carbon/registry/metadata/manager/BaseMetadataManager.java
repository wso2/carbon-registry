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

import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.manager.util.Util;
import org.wso2.carbon.registry.metadata.provider.MetadataProvider;
import org.wso2.carbon.registry.metadata.version.Version;

import java.util.Map;

public class BaseMetadataManager implements MetadataManager {

    private String mediaType;
    private MetadataProvider provider;

    public BaseMetadataManager(String mediaType) {
        this.mediaType = mediaType;
        this.provider = Util.getProvider(mediaType);
    }

    @Override
    public Base newInstance(String name) {
        return null;
    }

    @Override
    public void delete(String uuid) {

    }

    @Override
    public void add(Base metadata) {
         provider.insert(metadata);
    }

    @Override
    public void update(Base metadata) {
        provider.update(metadata);
    }

    @Override
    public Base[] getAllMetadata() {
        return new Base[0];
    }

    @Override
    public Base[] findMetadata(Map<String, String> criteria) {
        return new Base[0];
    }

    @Override
    public Base getMetadata(String uuid) {
        return provider.get(uuid);
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
}
