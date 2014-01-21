/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.common;

public class CommonConstants {

    public static final String RESOURCE = "resource";
    public static final String COLLECTION = "collection";

    public static final String ERROR_CODE = "error.code";
    public static final String ERROR_MESSAGE = "error.message";
    
    public static final String ASSOCIATION_TYPE01 = "depends";

    public static final String SERVICE_ELEMENT_NAMESPACE = "http://www.wso2.org/governance/metadata";

    public static final String RETENTION_FROM_DATE_PROP_NAME = "registry.retention.fromDate";
    public static final String RETENTION_TO_DATE_PROP_NAME = "registry.retention.toDate";
    public static final String RETENTION_WRITE_LOCKED_PROP_NAME = "registry.retention.writeLocked";
    public static final String RETENTION_DELETE_LOCKED_PROP_NAME = "registry.retention.deleteLocked";
    public static final String RETENTION_USERNAME_PROP_NAME = "registry.retention.user.name";

    public static final String LATEST_VERSION_PROP_NAME = "latest.version";

    public static final String HANDLER_SKIP_PROP_NAME = "registry.handler.skip";

    public static ThreadLocal<Boolean> isExternalUDDIInvoke = new ThreadLocal<Boolean>() {
        protected Boolean initialValue() {
            return false;
        }
    };

}
