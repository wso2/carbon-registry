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
package org.wso2.carbon.registry.resource.download;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.resource.beans.ContentDownloadBean;

/**
 * An interface to be implemented to extend the resource download functionality
 */

public interface DownloadManagerService {
    /**
     *
     * @param path  path of resource which should be downloaded
     * @param registry  Registry implementation.
     * @return   the ContentDownloadBean which has the downloadable content stream
     */
     public ContentDownloadBean getDownloadContent(String path, Registry registry) throws Exception;

}
