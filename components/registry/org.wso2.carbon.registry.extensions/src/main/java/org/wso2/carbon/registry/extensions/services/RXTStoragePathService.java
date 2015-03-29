/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.extensions.services;

public interface RXTStoragePathService {

    /**
     * This method returns the registry path expression of the given media type
     * @param mediaType media type of the RXT(Configurable artifact)
     * @return registry path expression
     */
    String getStoragePath(String mediaType);

    /**
     * This method stores the path expression of the given media type
     * @param mediaType  media type of the RXT(Configurable artifact)
     * @param storagePath registry path expression
     */
    void addStoragePath(String mediaType, String storagePath);

    /**
     * This method removes the path expression of the given media type
     * @param mediaType media type of the RXT(Configurable artifact)
     */
    void removeStoragePath(String mediaType);
}
