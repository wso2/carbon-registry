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
package org.wso2.carbon.registry.indexing;

import java.util.regex.Pattern;

/**
 *  Class to store registry Solr indexing and searching related constants
 */
public final class SolrConstants {

    // Make the constructor private, since it is a utility class
    private SolrConstants(){}
    // Constant for operation not applicable// Constant for equal operation
    public static final String OPERATION_EQUAL = "eq";
    // Constant for less than operation
    public static final String OPERATION_LESS_THAN = "lt";
    // Constant for greater than operation
    public static final String OPERATION_GREATER_THAN = "gt";
    // Constant for greater than or equal operation
    public static final String OPERATION_GREATER_THAN_OR_EQUAL = "ge";
    // Constant for operation not applicable
    public static final String OPERATION_NA = "na";
    // Constant for default negation value
    public static final String NEGATE_VALUE_DEFAULT = "on";
    // Constant for default negation value query
    public static final String SOLR_NEGATE_VALUE = "[* TO *] -";
    // Properties file name which contains all solr file names and relative paths
    public static final String SOLR_CONFIG_FILES_CONTAINER = "solr_configuration_files.properties";
    // Solr core properties filename
    public static final String CORE_PROPERTIES = "core.properties";
    // Constant to identify the file path. file maps to this value should go under home directory
    public static final String SOLR_HOME = "home/";
    // Constant to identify the file path. file maps to this value should go under home/core directory
    public static final String SOLR_CORE = "home/core/";
    // Constant to identify the file path. file maps to this value should go under home/core/conf/lang directory
    public static final String SOLR_CONF_LANG = "home/core/conf/lang";
    // Constant to set the solr system property
    public static final String SOLR_HOME_SYSTEM_PROPERTY = "solr.solr.home";
    // Constant to identify solr standalone mode which is the HttpSolrServer
    public static final String SOLR_STANDALONE_MODE = "standalone";
    // Constant to identify solr embedded mode
    public static final String SOLR_EMBEDDED_MODE = "embedded";
    // Constant for solr date format
    public static final String SOLR_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    // Constant for registry log file date format
    public static final String REG_LOG_DATE_FORMAT = "EEE MMM d HH:mm:ss z yyyy";
    // Constant for calender date format
    public static final String CALENDER_DATE_FORMAT = "MM/dd/yyyy";
    // Constant for solr string field key suffix
    public static final String SOLR_STRING_FIELD_KEY_SUFFIX = "_s";
    // Constant for solr date field key suffix
    public static final String SOLR_DATE_FIELD_KEY_SUFFIX = "_dt";
    // Constant for solr multivalued string field key suffix
    public static final String SOLR_INT_FIELD_KEY_SUFFIX = "_i";
    // Constant for solr multivalued string field key suffix
    public static final String SOLR_DOUBLE_FIELD_KEY_SUFFIX = "_d";
    // Constant for solr multivalued string field key suffix
    public static final String SOLR_MULTIVALUED_STRING_FIELD_KEY_SUFFIX = "_ss";
    //Constant for solr multivalued int field key suffix
    public static final String SOLR_MULTIVALUED_INT_FIELD_KEY_SUFFIX = "_is";
    //Constant for solr multivalued double field key suffix
    public static final String SOLR_MULTIVALUED_DOUBLE_FIELD_KEY_SUFFIX = "_ds";
    // Constant for String type
    public static final String TYPE_STRING = "string";
    // Constant for Integer type
    public static final String TYPE_INT = "int";
    // Constant for Float type
    public static final String TYPE_DOUBLE = "double";
    // Constant for regex int
    public static final Pattern INT_PATTERN = Pattern.compile("-?\\d+");
    // Constant for regex double
    public static final Pattern DOUBLE_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
    //constant for governance path to solr queries
    public static final String GOVERNANCE_REGISTRY_BASE_PATH =  "\\/_system\\/governance\\/";

}
