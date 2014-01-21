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

package org.wso2.carbon.registry.common.ui;

import org.wso2.carbon.registry.common.CommonConstants;

public class UIConstants extends CommonConstants {

    public static final String UNKNOWN = "unknown";

    public static final String REQUESTED_PAGE = "requestedPage";
    
    public static final String CUSTOM_UI_MANAGER = "custom.ui.manager";
    public static final String CUSTOM_UI_BEAN = "customUIBean";
    public static final String CREATE_OPTIONS_BEAN = "createOptionsBean";
    public static final String RESOURCE_PATH = "resourcePath";
    public static final String RESOURCE_NAME = "resourceName";
    public static final String PARENT_PATH = "parentPath";
    public static final String MEDIA_TYPE = "mediaType";
    public static final String COMMAND = "command";
    public static final String VIEW_KEY = "view";

    public static final String RAW_VIEW = "raw";

    // beans ids used as session attribute names
    public static final String RESOURCE_METADATA_BEAN = "metadata";
    public static final String RESOURCE_CONTENT_BEAN = "contentBean";
    public static final String RESOURCE_PERMISSIONS_BEAN = "permissions";
    public static final String TAG_SECTION_BEAN = "tagsBean";
    public static final String COMMENT_SECTION_BEAN = "commentsBean";
    public static final String PROPERTIES_BEAN = "propertiesBean";
    public static final String LIFE_CYCLE_BEAN = "lifecycleBean";
    public static final String DEPENDENCIES_BEAN = "dependenciesBean";
    public static final String RATINGS_BEAN = "ratingsBean";
    public static final String COMMON_BEAN = "common";
    public static final String PEOPLE_BEAN = "peopleBean";
    public static final String USER_BEAN = "userBean";
    public static final String ACTIVITY_BEAN = "activityBean";
    public static final String SEARCH_RESULTS_BEAN = "searchResultsBean";
    public static final String ADVANCED_SEARCH_RESULTS_BEAN = "advancedSearchResultsBean";

    public static final String RESOURCE_BEAN = "resource";
    public static final String COLLECTION_BEAN = "collection";
    public static final String ASSOCIATION_TREE_BEAN = "associationTree";
    public static final String USER_MANAGEMENT_BEAN = "user_management";
    public static final String ADMIN_BEAN = "admin";
    public static final String ADVANCED_SEARCH_BEAN = "advanced_search";
    public static final String SEARCH_BEAN = "search";
    public static final String VERSIONS_BEAN = "versions.bean";
    public static final String ANNOUNCEMENTS_BEAN = "announcements.bean";
    public static final String AJAX_RATING_BEAN = "ajaxRating";
    public static final String AJAX_DESCRIPTION_STRING = "ajaxDesc";
    public static final String AJAX_COMMENTS_LIST = "ajaxComments";
    public static final String AJAX_ASSOCIATIONS_LIST = "ajaxAssociations";
    public static final String AJAX_LIFECYCLE_LIST = "ajaxLifecycle";
    public static final String AJAX_GIVEPAGE_LIST = "givePage";
    public static final String RESOURCE_TREE_BEAN="resourceTreeBean";
    public static final String ACTIVITY_PROPERTY_BEAN="activityPropertyBean";

    public static final String PROCESSED_PATH = "processed.path";
    public static final String RESOURCES_PATH = "resources";
    public static final String VERSIONS_PATH = "versions";
    public static final String WEB_PATH = "web";
    public static final String PEOPLE_PATH = "people";
    public static final String ACTIVITY_PATH = "activity";
    public static final String SEARCH_PATH = "search";
    public static final String VERSION_PATH = "versions";
    public static final String SYSTEM_PATH = "system";
    public static final String CUSTOM_PATH = "custom";
    public static final String PATH_ATTR = "path";
    public static final String USER_ATTR = "currentUser";
    public static final String IS_ADMIN_ATTR = "is.admin";
    public static final String IS_LOGGED_IN_ATTR = "is.logged.in";
    public static final String QUERY_ATTR = "regQuery";
    public static final String SHOW_SYSPROPS_ATTR = "show.system-properties";

    public static final String SECTION_ERROR_MESSAGE = "section.error.message";
    public static final String SECTION_STATUS_MESSAGE = "section.status.message";
    public static final String SUCCESS_MESSAGE="success.message";
    public static final String RESOURCES_JSP = "/admin/resources.jsp";
    public static final String USER_MANAGEMENT_JSP = "/admin/people.jsp";
    public static final String ASSOCIATION_TREE_JSP = "/admin/ajax/association-tree.jsp";
    public static final String ACTIVITY_JSP = "/admin/recent-activity.jsp";
    public static final String ACTIVITY_AJAX_JSP = "/admin/ajax/activity.jsp";
    public static final String ADMIN_JSP = "/admin/admin.jsp";
    public static final String ADVANCED_SEARCH_JSP = "/admin/advanced-search.jsp";
    public static final String ADVANCED_AJAX_SEARCH_JSP = "/admin/ajax/advanced-search-results.jsp";
    public static final String SEARCH_JSP = "/admin/search.jsp";
    public static final String USER_JSP = "/admin/user.jsp";
    public static final String VERSIONS_JSP = "/admin/versions.jsp";
    public static final String AJAX_RATING_JSP = "/admin/ajax_rating.jsp";
    public static final String RESOURCE_TREE_JSP="/admin/ajax/resource-tree.jsp";
    public static final String AJAX_PERMISSIONS_JSP = "/admin/permission-content.jsp";
    public static final String AJAX_DESCRIPTION_JSP = "/admin/ajax_desc.jsp";
    public static final String AJAX_PROPERTIES_JSP = "/admin/ajax/resource-properties.jsp";
    public static final String AJAX_USER_FRIENDLY_NAME_JSP = "/admin/ajax/user-friendly-name.jsp";
    public static final String AJAX_USER_PASSWORD_JSP = "/admin/ajax/user-password-edit.jsp";
    public static final String AJAX_COMMENTS_JSP = "/admin/ajax/comment-list.jsp";
    public static final String AJAX_ASSOCIATIONS_JSP = "/admin/ajax/association-list.jsp";
    public static final String AJAX_ENTRY_LIST_JSP = "/admin/ajax/entry-list.jsp";
    public static final String AJAX_LIFECYCLELIST_JSP = "/admin/ajax/lifecycle_list.jsp";
    public static final String RESOURCE_DETAILS_JSP = "/admin/resources_details.jsp";
    public static final String ERROR_JSP = "/admin/error.jsp";
    public static final String ABOUT_JSP = "/admin/about.jsp";
    public static final String CREATE_OPTIONS_JSP = "/admin/ajax/custom-create-options.jsp";
    public static final String AJAX_ERROR = "/admin/ajax/error.jsp";

    public static final String SEARCH_TYPE = "registry.searchType";
}
