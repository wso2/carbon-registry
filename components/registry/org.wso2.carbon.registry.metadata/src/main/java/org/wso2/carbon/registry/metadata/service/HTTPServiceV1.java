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

import org.wso2.carbon.registry.metadata.Constants;
import org.wso2.carbon.registry.metadata.version.Version;

public class HTTPServiceV1 implements ServiceV1 {

    @Override
    public Version newVersion(String value) {
        return null;
    }

    @Override
    public Version[] getVersions() {
        return new Version[0];
    }

    @Override
    public Version getVersion(int major, int minor, int patch) {
        return null;
    }

    @Override
    public String getUUID() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getMediaType() {
        return null;
    }

    @Override
    public void setProperty(String key, String value) {

    }

    @Override
    public void removeProperty(String key) {

    }

    @Override
    public void setOwner(String owner) {

    }

    @Override
    public String getOwner() {
        return null;
    }


}
