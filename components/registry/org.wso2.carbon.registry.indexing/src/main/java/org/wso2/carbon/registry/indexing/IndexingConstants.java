/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.indexing;

import org.wso2.carbon.registry.core.RegistryConstants;


public class IndexingConstants {

    public static final String LAST_ACCESS_TIME_LOCATION = RegistryConstants.LOCAL_REPOSITORY_BASE_PATH +
            RegistryConstants.REGISTRY_COMPONENT_PATH + "/indexing/lastaccesstime";

    public static final long STARTING_DELAY_IN_SECS_DEFAULT_VALUE = 10 * 60; //10 minutes
    public static final long INDEXING_FREQ_IN_SECS_DEFAULT_VALUE = 1 * 60; //1 minute

    public static final String FIELD_ID ="id";
    public static final String FIELD_TENANT_ID="tenantId";
    public static final String FIELD_TEXT="text";
    public static final String FIELD_COUNT_ONLY="contentOnly";
    public static final String FIELD_MEDIA_TYPE="mediaType";
    public static final String FIELD_LC_NAME="lcName";
    public static final String FIELD_LC_STATE="lcState";


}
