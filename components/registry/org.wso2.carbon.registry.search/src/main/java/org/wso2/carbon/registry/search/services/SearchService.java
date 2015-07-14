/*
 * Copyright (c) 2006-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.search.services;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.admin.api.search.ISearchService;
import org.wso2.carbon.registry.common.AttributeSearchService;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.pagination.PaginationUtils;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.search.beans.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.beans.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.beans.MediaTypeValueList;
import org.wso2.carbon.registry.search.beans.SearchResultsBean;
import org.wso2.carbon.registry.search.internal.SearchDataHolder;
import org.wso2.carbon.registry.search.services.utils.AdvancedSearchFilterActions;
import org.wso2.carbon.registry.search.services.utils.CustomSearchParameterPopulator;
import org.wso2.carbon.registry.search.services.utils.SearchResultsBeanPopulator;
import org.wso2.carbon.registry.search.services.utils.SearchUtils;

import java.util.HashMap;
import java.util.Map;

/**
 *  This class is dedicated for resource search
 */
public class SearchService extends RegistryAbstractAdmin implements
        ISearchService<SearchResultsBean, AdvancedSearchResultsBean, CustomSearchParameterBean, MediaTypeValueList> {

    private boolean allEmpty = true;
    private static final String SEARCH_ATTRIBUTES_ALL_EMPTY_MESSAGE = "At least one field must be filled";
    private static final String SEARCH_ATTRIBUTES_CONTAINS_ILLEGAL_CHARACTER_MESSAGE = " contains illegal characters";

    /* (non-Javadoc)
     * @see org.wso2.carbon.registry.search.services.ISearchService#getSearchResults(java.lang.String, java.lang.String)
	 */
    public SearchResultsBean getSearchResults(String searchType, String criteria) throws RegistryException {
        RegistryUtils.recordStatistics(searchType, criteria);
        UserRegistry registry = (UserRegistry) getRootRegistry();
        return SearchResultsBeanPopulator.populate(registry, searchType, criteria);
    }

/*    public AdvancedSearchResultsBean getAdvancedSearchResults(String resourceName, String authorName, String updaterName,
                                                              String createdAfter, String createdBefore, String updatedAfter,
                                                              String updatedBefore, String tags, String commentWords,
                                                              String propertyName, String propertyValue, String content)
            throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        Registry configSystemRegistry = getConfigSystemRegistry();
        return AdvancedSearchResultsBeanPopulator.populate(configSystemRegistry, registry, resourceName,
                                                           authorName, updaterName, createdAfter, createdBefore, updatedAfter, updatedBefore, tags,
                                                           commentWords, propertyName, propertyValue, content);
    }*/

    /**
     * Method to get Advance search result bean
     * @param parameters CustomSearchParameterBean
     * @return AdvancedSearchResultsBean
     * @throws RegistryException
     */
    public AdvancedSearchResultsBean getAdvancedSearchResults(CustomSearchParameterBean parameters)
            throws RegistryException {
        RegistryUtils.recordStatistics(parameters);
        AdvancedSearchResultsBean advancedSearchResultsBean;
        UserRegistry registry = (UserRegistry) getRootRegistry();
        AttributeSearchService attributeSearchService = SearchDataHolder.getInstance().getAttributeSearchService();
        // Get advance search parameter values
        String[][] searchParameterValues = parameters.getParameterValues();
        ResourceData[] advanceSearchResourceData = new ResourceData[0];
        // Map to store advance search attributes values
        Map<String, String> advanceSearchAttributes;

        // Validating the values sent
        String validationErrorMessage = getValidationErrorMessage(searchParameterValues);
        if (validationErrorMessage != null && StringUtils.isNotEmpty(validationErrorMessage)) {
            return SearchUtils.getEmptyResultBeanWithErrorMsg(validationErrorMessage);
        }
        // No attribute has provide for search
        if (allEmpty) {
            return SearchUtils.getEmptyResultBeanWithErrorMsg(SEARCH_ATTRIBUTES_ALL_EMPTY_MESSAGE);
        }
        // Add search parameter values to advanceSearchAttributes Map
        advanceSearchAttributes = getAdvanceSearchValueMap(searchParameterValues);

        // Get Advance Search resource data
        if (attributeSearchService != null && advanceSearchAttributes.size() > 0) {
            if (!(advanceSearchAttributes.size() == 2 && advanceSearchAttributes.containsKey("leftOp")
                    && advanceSearchAttributes.get("leftOp").equals("na") && advanceSearchAttributes
                    .containsKey("rightOp")
                    && advanceSearchAttributes.get("rightOp").equals("na"))) {
                advanceSearchAttributes.put(IndexingConstants.ADVANCE_SEARCH, "true");
                advanceSearchResourceData = attributeSearchService.search(registry, advanceSearchAttributes);
            }
        }
        advancedSearchResultsBean = new AdvancedSearchResultsBean();
        if (advanceSearchResourceData != null && advanceSearchResourceData.length > 0) {

            advancedSearchResultsBean.setResourceDataList(advanceSearchResourceData);
            if (!isEmptyResourceDataList(advancedSearchResultsBean)) {
                return getPaginatedResult(advancedSearchResultsBean);
            }

        }
        advancedSearchResultsBean.setResourceDataList(new ResourceData[0]);
        return advancedSearchResultsBean;
    }

    /**
     * Method to check whether the result set is empty
     * @param resultsBean AdvancedSearchResultsBean
     * @return boolean value of result empty or not
     */
    private boolean isEmptyResourceDataList(AdvancedSearchResultsBean resultsBean) {
        boolean resultEmpty = true;
        if ((resultsBean.getResourceDataList() != null && resultsBean.getResourceDataList().length > 0)) {
            for (ResourceData data : resultsBean.getResourceDataList()) {
                if (data != null) {
                    resultEmpty = false;
                    break;
                }
            }
        }
        return resultEmpty;
    }

    /**
     * Method to get the advance search attribute validation error message
     * @param searchParameterValues search parameter values
     * @return validation error message
     */
    private String getValidationErrorMessage(String[][] searchParameterValues) {
        String message = null;
        for (String[] tempParameterValue : searchParameterValues) {
            if (tempParameterValue[1] != null & tempParameterValue[1].trim().length() > 0) {
                allEmpty = false;
                // Validating all the dates
                if (tempParameterValue[0].equals("createdAfter") || tempParameterValue[0].equals("createdBefore") ||
                        tempParameterValue[0].equals("updatedAfter") || tempParameterValue[0].equals("updatedBefore")) {
                    if (!SearchUtils.validateDateInput(tempParameterValue[1])) {
                        message = tempParameterValue[0] + SEARCH_ATTRIBUTES_CONTAINS_ILLEGAL_CHARACTER_MESSAGE;
                    }
                } else if (tempParameterValue[0].equals("author")) { // Validating media type
                    if (SearchUtils.validatePathInput(tempParameterValue[1])) {
                        message = tempParameterValue[0] + SEARCH_ATTRIBUTES_CONTAINS_ILLEGAL_CHARACTER_MESSAGE;
                    }
                } else if (tempParameterValue[0].equals("mediaType")) { // Validating media type
                    if (SearchUtils.validateMediaTypeInput(tempParameterValue[1])) {
                        message = tempParameterValue[0] + SEARCH_ATTRIBUTES_CONTAINS_ILLEGAL_CHARACTER_MESSAGE;
                    }
                } else if (tempParameterValue[0].equals("content")) { // Validating content
                    if (SearchUtils.validateContentInput(tempParameterValue[1])) {
                        message = tempParameterValue[0] + SEARCH_ATTRIBUTES_CONTAINS_ILLEGAL_CHARACTER_MESSAGE;
                    }
                } else if (tempParameterValue[0].equals("tags")) { // Validating tags
                    boolean containsTag = false;
                    for (String str : tempParameterValue[1].split(",")) {
                        if (str.trim().length() > 0) {
                            containsTag = true;
                            break;
                        }
                    }
                    if (!containsTag) {
                        message = tempParameterValue[0] + SEARCH_ATTRIBUTES_CONTAINS_ILLEGAL_CHARACTER_MESSAGE;
                    }
                    if (SearchUtils.validateTagsInput(tempParameterValue[1])) {
                        message = tempParameterValue[0] + SEARCH_ATTRIBUTES_CONTAINS_ILLEGAL_CHARACTER_MESSAGE;
                    }
                } else {
                    if (SearchUtils.validatePathInput(tempParameterValue[1])) {
                        message = tempParameterValue[0] + SEARCH_ATTRIBUTES_CONTAINS_ILLEGAL_CHARACTER_MESSAGE;
                    }
                }
            }
        }
        return message;
    }

    /**
     * Method to get the advance search attribute Map
     * @param searchParameterValues search parameter array
     * @return attribute map
     */
    private Map<String, String> getAdvanceSearchValueMap(String[][] searchParameterValues) {
        // Map to store advance search attributes values
        Map<String, String> advanceSearchAttributes = new HashMap<String, String>();

        for (String[] tempParameterValue : searchParameterValues) {
            if (tempParameterValue[0].equals("content") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_CONTENT, tempParameterValue[1]);
            } else if (tempParameterValue[0].equals("mediaType") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_MEDIA_TYPE, tempParameterValue[1]);
            } else if (tempParameterValue[0].equals("resourcePath") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                if (!(tempParameterValue[1].length() == 1 && tempParameterValue[1].charAt(0) == '%')) {
                    advanceSearchAttributes.put(IndexingConstants.FIELD_RESOURCE_NAME, tempParameterValue[1]);
                }
            } else if (tempParameterValue[0].equals("author") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                if (!(tempParameterValue[1].length() == 1 && tempParameterValue[1].charAt(0) == '%')) {
                    advanceSearchAttributes.put(IndexingConstants.FIELD_CREATED_BY, tempParameterValue[1]);
                }
            } else if (tempParameterValue[0].equals("updater") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                if (!(tempParameterValue[1].length() == 1 && tempParameterValue[1].charAt(0) == '%')) {
                    advanceSearchAttributes.put(IndexingConstants.FIELD_LAST_UPDATED_BY, tempParameterValue[1]);
                }
            } else if (tempParameterValue[0].equals("createdAfter") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_CREATED_AFTER, tempParameterValue[1]);
            } else if (tempParameterValue[0].equals("createdBefore") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_CREATED_BEFORE, tempParameterValue[1]);
            } else if (tempParameterValue[0].equals("updatedAfter") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_UPDATED_AFTER, tempParameterValue[1]);
            } else if (tempParameterValue[0].equals("updatedBefore") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_UPDATED_BEFORE, tempParameterValue[1]);
            } else if (tempParameterValue[0].equals("tags") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_TAGS, tempParameterValue[1]);
            } else if (tempParameterValue[0].equals("commentWords") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_COMMENTS, tempParameterValue[1]);
            } else if (tempParameterValue[0].equals("associationType") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_ASSOCIATION_TYPES, tempParameterValue[1]);
            } else if (tempParameterValue[0].equals("associationDest") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_ASSOCIATION_DESTINATIONS, tempParameterValue[1]);
            } else if (tempParameterValue[0].equals("authorNameNegate") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_CREATED_BY_NEGATE, tempParameterValue[1]);
            } else if (tempParameterValue[0].equals("updaterNameNegate") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_UPDATE_BY_NEGATE, tempParameterValue[1]);
            } else if (tempParameterValue[0].equals("createdRangeNegate") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_CREATED_RANGE_NEGATE, tempParameterValue[1]);
            } else if (tempParameterValue[0].equals("updatedRangeNegate") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_UPDATED_RANGE_NEGATE, tempParameterValue[1]);
            } else if (tempParameterValue[0].equals("mediaTypeNegate") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_MEDIA_TYPE_NEGATE, tempParameterValue[1]);
            } else if (tempParameterValue[0].equals("propertyName") && tempParameterValue[1] != null && !StringUtils
                    .isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_PROPERTY_NAME, tempParameterValue[1]);
            } else if (tempParameterValue[0].equals("leftPropertyValue") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                if (!(tempParameterValue[1].length() == 1 && tempParameterValue[1].charAt(0) == '%')) {
                    advanceSearchAttributes.put(IndexingConstants.FIELD_LEFT_PROPERTY_VAL, tempParameterValue[1]);
                }
            } else if (tempParameterValue[0].equals("rightPropertyValue") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                if (!(tempParameterValue[1].length() == 1 && tempParameterValue[1].charAt(0) == '%')) {
                    advanceSearchAttributes.put(IndexingConstants.FIELD_RIGHT_PROPERTY_VAL, tempParameterValue[1]);
                }
            } else if (tempParameterValue[0].equals("leftOp") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_LEFT_OP, tempParameterValue[1]);
            } else if (tempParameterValue[0].equals("rightOp") && tempParameterValue[1] != null &&
                    !StringUtils.isEmpty(tempParameterValue[1])) {
                advanceSearchAttributes.put(IndexingConstants.FIELD_RIGHT_OP, tempParameterValue[1]);
            }
        }
        return advanceSearchAttributes;
    }

    /**
     * Method to get the paginated result for advance search
     * @param advancedSearchResultsBean AdvancedSearchResultsBean
     * @return paginated result
     */
    private AdvancedSearchResultsBean getPaginatedResult(AdvancedSearchResultsBean advancedSearchResultsBean) {
        ResourceData[] paginatedResult;
        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        if (messageContext != null && PaginationUtils.isPaginationHeadersExist(messageContext)
                && advancedSearchResultsBean.getResourceDataList() != null) {

            int rowCount = advancedSearchResultsBean.getResourceDataList().length;
            try {
                PaginationUtils.setRowCount(messageContext, Integer.toString(rowCount));
                PaginationContext paginationContext = PaginationUtils.initPaginationContext(messageContext);

                int start = paginationContext.getStart();
                int count = paginationContext.getCount();

                int startIndex;
                if (start == 1) {
                    startIndex = 0;
                } else {
                    startIndex = start;
                }
                if (rowCount < start + count) {
                    paginatedResult = new ResourceData[rowCount - startIndex];
                    System.arraycopy(advancedSearchResultsBean.getResourceDataList(), startIndex, paginatedResult, 0,
                            (rowCount - startIndex));
                } else {
                    paginatedResult = new ResourceData[count];
                    System.arraycopy(advancedSearchResultsBean.getResourceDataList(), startIndex, paginatedResult, 0,
                            count);
                }
                advancedSearchResultsBean.setResourceDataList(paginatedResult);
                return advancedSearchResultsBean;

            } finally {
                PaginationContext.destroy();
            }
        } else {
            return advancedSearchResultsBean;
        }
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.registry.search.services.ISearchService#getMediaTypeSearch(java.lang.String)
	 */
    public MediaTypeValueList getMediaTypeSearch(String mediaType) throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        return CustomSearchParameterPopulator.getMediaTypeParameterValues(registry, mediaType);
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.registry.search.services.ISearchService#saveAdvancedSearchFilter(org.wso2.carbon.registry.search.beans.CustomSearchParameterBean, java.lang.String)
	 */
    public void saveAdvancedSearchFilter(CustomSearchParameterBean queryBean, String filterName) throws
            RegistryException {
        UserRegistry configUserRegistry = (UserRegistry) getConfigUserRegistry();
        AdvancedSearchFilterActions.saveAdvancedSearchQueryBean(configUserRegistry, queryBean, filterName);
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.registry.search.services.ISearchService#getAdvancedSearchFilter(java.lang.String)
	 */
    public CustomSearchParameterBean getAdvancedSearchFilter(String filterName) throws
            RegistryException {
        UserRegistry configUserRegistry = (UserRegistry) getConfigUserRegistry();
        return AdvancedSearchFilterActions.getAdvancedSearchQueryBean(configUserRegistry, filterName);
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.registry.search.services.ISearchService#getSavedFilters()
	 */
    public String[] getSavedFilters() throws RegistryException {
        UserRegistry configUserRegistry = (UserRegistry) getConfigUserRegistry();
        return AdvancedSearchFilterActions.getSavedFilterNames(configUserRegistry);
    }

    public void deleteFilter(String filterName) throws RegistryException {
        UserRegistry configUserRegistry = (UserRegistry) getConfigUserRegistry();
        configUserRegistry
                .delete(RegistryConstants.PATH_SEPARATOR + "users" + RegistryConstants.PATH_SEPARATOR + CarbonContext
                        .getThreadLocalCarbonContext().getUsername() + RegistryConstants.PATH_SEPARATOR
                        + "searchFilters" + RegistryConstants.PATH_SEPARATOR + filterName);
    }
}
