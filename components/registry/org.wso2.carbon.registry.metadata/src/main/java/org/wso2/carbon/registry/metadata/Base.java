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


import org.wso2.carbon.registry.metadata.exception.MetadataException;

public interface Base {

    /**
     * @return the UUID of the meta data instance
     */
    public String getUUID() throws MetadataException;

    /**
     * @return the human readable name that is given to the meta data instance
     */
    public String getName() throws MetadataException;

    /**
     * @return media type of the meta data instance that uniquely identifies the type of this instance
     */
    public String getMediaType() throws MetadataException;

    /**
     * @return media type of the version meta data instance that uniquely identifies the type of version.
     */
    public String getVersionMediaType() throws MetadataException;

    /**
     * This is the property bag
     *
     * @param key   - property name
     * @param value - property value(single valued property)
     */
    public void setProperty(String key, String value) throws MetadataException;

    /**
     * Removes the property from this instance
     *
     * @param key name of the property
     */
    public void removeProperty(String key) throws MetadataException;

    /**
     * Removes the property from this instance
     *
     * @param key name of the property
     * @return value of the property
     */
    public String getProperty(String key) throws MetadataException;

    /**
     * @return true if the instance is a version type.
     * false if the instance type is NOT a version type but any meta data super/sub type
     */
    public boolean isVersionType();

}
