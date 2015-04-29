/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.utils;

import org.wso2.carbon.registry.core.RegistryConstants;

public class CommonConstants {

    public static final String GOVERNANCE_ARTIFACT_INDEX_PATH =
            "/repository/components/org.wso2.carbon.governance/artifacts";

    /*
     This is the path which save the rxt config files.
     */
    public static final String RXT_CONFIGS_PATH = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
            RegistryConstants.GOVERNANCE_COMPONENT_PATH +
            RegistryConstants.PATH_SEPARATOR + "types";

    //    public static final String ARTIFACT_ID_PROP_KEY = "registry.artifactId";

    public static final String SERVICE_NAME_ATTRIBUTE = "overview_name";
    public static final String SERVICE_NAMESPACE_ATTRIBUTE = "overview_namespace";
    public static final String SERVICE_WSDL_ATTRIBUTE = "interface_wsdlURL";
    public static final String SERVICE_OWNERS_ATTRIBUTE = "contacts_entry";
    public static final String SERVICE_CONSUMERS_ATTRIBUTE = "consumers_entry";
    public static final String PROCESS_BPEL_ATTRIBUTE = "definition_bpelURL";
    public static final String SLA_BPEL_ATTRIBUTE = "definition_bpelURL";

    public static final String SERVICE_ELEMENT_ROOT = "metadata";
    public static final String SERVICE_ELEMENT_NAMESPACE =
            org.wso2.carbon.registry.common.CommonConstants.SERVICE_ELEMENT_NAMESPACE;

    public static final String PEOPLE_ELEMENT_ROOT = "peopleMetaData";

    public static final String DEPENDS = "depends";
    public static final String USED_BY = "usedBy";

    public static final String OWNS = "depends";
    public static final String OWNED_BY = "ownedBy";

    public static final String CONSUMES = "depends";
    public static final String CONSUMED_BY = "consumedBy";

    public static final String ENDPOINT_MEDIA_TYPE = "application/vnd.wso2-endpoint+xml";
    public static final String WSDL_MEDIA_TYPE = "application/wsdl+xml";
    public static final String SCHEMA_MEDIA_TYPE = "application/xsd+xml";
    public static final String SWAGGER_MEDIA_TYPE = "application/swagger+json";
    public static final String REST_SERVICE_MEDIA_TYPE = "application/vnd.wso2-restservice+xml";
    public static final String SERVICE_MEDIA_TYPE = RegistryConstants.SERVICE_MEDIA_TYPE;
    public static final String SOAP_SERVICE_MEDIA_TYPE = "application/vnd.wso2-soap-service+xml";
    public static final String SERVICE_VERSION_COLLECTION_MEDIA_TYPE =
            "application/vnd.wso2-service-version.collection";
    public static final String SERVICE_MAJOR_VERSION_MEDIA_TYPE = "application/vnd.wso2-service-version.major";
    public static final String SERVICE_MINOR_VERSION_MEDIA_TYPE = "application/vnd.wso2-service-version.minor";
    public static final String SERVICE_PATCH_VERSION_MEDIA_TYPE = "application/vnd.wso2-service-version.patch";
    public static final String SMART_LIFECYCLE_LINK_MEDIA_TYPE = "application/vnd.wso2-smart-link";

    public static final String PROCESS_VERSION_COLLECTION_MEDIA_TYPE =
            "application/vnd.wso2-process-version.collection";
    public static final String PROCESS_MAJOR_VERSION_MEDIA_TYPE = "application/vnd.wso2-process-version.major";
    public static final String PROCESS_MINOR_VERSION_MEDIA_TYPE = "application/vnd.wso2-process-version.minor";
    public static final String PROCESS_PATCH_VERSION_MEDIA_TYPE = "application/vnd.wso2-process-version.patch";

    public static final String SLA_VERSION_COLLECTION_MEDIA_TYPE = "application/vnd.wso2-sla-version.collection";
    public static final String SLA_MAJOR_VERSION_MEDIA_TYPE = "application/vnd.wso2-sla-version.major";
    public static final String SLA_MINOR_VERSION_MEDIA_TYPE = "application/vnd.wso2-sla-version.minor";
    public static final String SLA_PATCH_VERSION_MEDIA_TYPE = "application/vnd.wso2-sla-version.patch";

    /*
    * Now version can be displayed dynamically (jira:REGISTRY-2211)
    * public static final String SERVICE_VERSION_REGEX = "^\\d+[.]\\d+[.]\\d+(-[a-zA-Z0-9]+)?$";
    * */
    public static final String SERVICE_VERSION_REGEX = "^(\\d+[.]*)+\\d+(-[a-zA-Z0-9]+)?$";

    public static final String SOAP11_ENDPOINT_ATTRIBUTE = "soap11";
    public static final String SOAP12_ENDPOINT_ATTRIBUTE = "soap12";

    public static final String HTTP_ENDPOINT_ATTRIBUTE = "http";

    public static final String ENDPOINT_ENVIRONMENT_ATTR = "environment";
    public static final String DOCUMENT_DESC = "documentComment";
    public static final String DOCUMENT_URL = "url";
    public static final String DOCUMENT_TYPE = "documentType";

    public static final int NO_OF_DOCUMENTS_ALLOWED = 3;

    public static final String ENABLE = "enable";
    public static final String UDDI_SYSTEM_PROPERTY = "uddi";

    public static final String SERVICE_VERSION_DEFAULT_VALUE = "1.0.0";

    public static final String WSDL_VERSION_DEFAULT_VALUE = "1.0.0";

    public static final String WADL_VERSION_DEFAULT_VALUE = "1.0.0";

    public static final String SCHEMA_VERSION_DEFAULT_VALUE = "1.0.0";

    public static final String POLICY_VERSION_DEFAULT_VALUE = "1.0.0";

    public static final String SWAGGER_DOC_VERSION_DEFAULT_VALUE = "1.0.0";

    public static final String ENDPOINT_VERSION_DEFAULT_VALUE = "1.0.0";

    public static final String VERSION_CONTAINER_MEDIA_TYPE = "application/vnd.wso2.version-container";

    public static final String VERSIONED_COLLECTION_MEDIA_TYPE = "application/vnd.wso2.versioned-collection";

    public static final String RXT_MEDIA_TYPE = "application/vnd.wso2.registry-ext-type+xml";

    public static final String REG_GAR_PATH_MAPPING = "/repository/carbonapps/gar_mapping/";
    public static final String REG_GAR_PATH_MAPPING_RESOURCE = "gar";
    public static final String REG_GAR_PATH_MAPPING_RESOURCE_ATTR_PATH = "path";
    public static final String REG_GAR_PATH_MAPPING_RESOURCE_TARGET = "target";

    public static final String LOCATION_TAG = "location";

    //Common Exception Messages
    public static final String RESOURCE_NOT_EXISTS = "Resource does not exist.";
    public static final String INVALID_CONTENT = "Resource content is not valid.";
    public static final String URL_TO_LOCAL_FILE =
            "The source URL must not be point to a file in the server's local file system. ";
    public static final String EMPTY_URL = "Source url is empty. Cannot read content.";

    // This constant will use to identify the property key of resources uploaded using URL
    public static final String SOURCEURL_PARAMETER_NAME="sourceURL";

    public static final String SOURCE_PROPERTY = org.wso2.carbon.registry.common.CommonConstants.SOURCE_PROPERTY;

    public static final String SOURCE_ADMIN_CONSOLE = org.wso2.carbon.registry.common.CommonConstants.SOURCE_ADMIN_CONSOLE;

    public static final String SOURCE_AUTO = org.wso2.carbon.registry.common.CommonConstants.SOURCE_AUTO;

    public static final String SOURCE_REMOTE = "remote";

}
