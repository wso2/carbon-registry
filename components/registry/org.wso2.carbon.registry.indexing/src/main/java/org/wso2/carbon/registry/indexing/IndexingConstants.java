/*
 *  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 *  Class to store registry indexing related constants
 */
public final class IndexingConstants {

    // Make the constructor private, since it is a utility class
    private IndexingConstants() {
    }

    // Default last access time location path is set as default when nothing specified in registry.xml
    public static final String LAST_ACCESS_TIME_LOCATION = RegistryConstants.LOCAL_REPOSITORY_BASE_PATH +
            RegistryConstants.REGISTRY_COMPONENT_PATH + "/indexing/lastaccesstime";

    // Default value for Solr Server core is set to the embedded solr. This going to be used when nothing specify as solr url in registry.xml
    public static final String DEFAULT_SOLR_SERVER_CORE = "registry-indexing";

    // Default starting delay and indexing frequency when nothing specified in registry.xml
    public static final long STARTING_DELAY_IN_SECS_DEFAULT_VALUE = 10 * 60; //10 minutes
    public static final long INDEXING_FREQ_IN_SECS_DEFAULT_VALUE = 60; //1 minute

    // Fields are set for indexing document as default
    public static final String FIELD_ID = "id";
    public static final String FIELD_TENANT_ID = "tenantId";
    public static final String FIELD_TEXT = "text";
    public static final String FIELD_CONTENT_ONLY = "contentOnly";
    public static final String FIELD_MEDIA_TYPE = "mediaType";
    public static final String FIELD_LC_NAME = "lcName";
    public static final String FIELD_LC_STATE = "lcState";

    // New fields are added for indexing
    public static final String FIELD_TAGS = "tags";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_CREATED_DATE = "createdDate";
    public static final String FIELD_LAST_UPDATED_DATE = "lastUpdatedDate";
    public static final String FIELD_COMMENTS = "commentWords";
    public static final String FIELD_ASSOCIATION_TYPES = "associationType";
    public static final String FIELD_ASSOCIATION_DESTINATIONS = "associationDest";
    public static final String FIELD_LAST_UPDATED_BY = "updater";
    public static final String FIELD_CREATED_BY = "author";
    public static final String FIELD_RESOURCE_NAME = "resourceName";
    public static final String FIELD_PROPERTY_VALUES = "propertyValues";
    public static final String ADVANCE_SEARCH = "AdvanceSearch";

    // New fields need for index search
    public static final String FIELD_CREATED_AFTER = "createdAfter";
    public static final String FIELD_CREATED_BEFORE = "createdBefore";
    public static final String FIELD_UPDATED_AFTER = "updatedAfter";
    public static final String FIELD_UPDATED_BEFORE = "updatedBefore";
    public static final String FIELD_CREATED_BY_NEGATE = "authorNameNegate";
    public static final String FIELD_UPDATE_BY_NEGATE = "updaterNameNegate";
    public static final String FIELD_CREATED_RANGE_NEGATE = "createdRangeNegate";
    public static final String FIELD_UPDATED_RANGE_NEGATE = "updatedRangeNegate";
    public static final String FIELD_MEDIA_TYPE_NEGATE = "mediaTypeNegate";
    public static final String FIELD_PROPERTY_NAME = "propertyName";
    public static final String FIELD_LEFT_PROPERTY_VAL = "leftPropertyValue";
    public static final String FIELD_RIGHT_PROPERTY_VAL = "rightPropertyValue";
    public static final String FIELD_RIGHT_OP = "rightOp";
    public static final String FIELD_LEFT_OP = "leftOp";

    //Constants for facet search
    public static final String FACET_FIELD_NAME = "facet.field";
    public static final String FACET_LIMIT = "facet.limit";
    public static final int FACET_LIMIT_DEFAULT = -1;
    public static final String FACET_MIN_COUNT =  "facet.mincount";
    public static final int FACET_MIN_COUNT_DEFAULT = 1;
    public static final String FACET_SORT = "facet.sort";
    public static final String FACET_PREFIX = "facet.prefix";

}
