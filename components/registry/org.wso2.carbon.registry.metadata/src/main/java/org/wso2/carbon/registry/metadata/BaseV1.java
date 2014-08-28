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
package org.wso2.carbon.registry.metadata;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.metadata.version.VersionV1;

public interface BaseV1 extends Base {

    /**
     * Creates a plain version instance
     * @param value - version value
     * @return
     */
    public VersionV1 newVersion(String value) throws RegistryException;

    /**
     *
     * @return all meta data version instances created from this instance
     */
    public VersionV1[] getVersions() throws RegistryException;

    /**
     * Returns the version that matches with given major,minor and patch values
     * @param major
     * @param minor
     * @param patch
     * @return
     */
    public VersionV1 getVersion (int major, int minor, int patch) throws RegistryException;

    /**
     *
     * @return Base version instance used to create this meta data type
     */
//    public VersionV1 getBaseVersion();

}
