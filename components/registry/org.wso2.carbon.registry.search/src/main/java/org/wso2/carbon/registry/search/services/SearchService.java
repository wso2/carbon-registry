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

package org.wso2.carbon.registry.search.services;

import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.admin.api.search.ISearchService;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.pagination.PaginationUtils;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.service.ContentSearchService;
import org.wso2.carbon.registry.search.Utils;
import org.wso2.carbon.registry.search.beans.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.beans.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.beans.MediaTypeValueList;
import org.wso2.carbon.registry.search.beans.SearchResultsBean;
import org.wso2.carbon.registry.search.services.utils.*;

import java.util.*;


public class SearchService extends RegistryAbstractAdmin implements ISearchService<SearchResultsBean, AdvancedSearchResultsBean, CustomSearchParameterBean, MediaTypeValueList> {

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

    //newly added method
    //this method is to get the custom search results

    /* (non-Javadoc)
	 * @see org.wso2.carbon.registry.search.services.ISearchService#getAdvancedSearchResults(org.wso2.carbon.registry.search.beans.CustomSearchParameterBean)
	 */
    public AdvancedSearchResultsBean getAdvancedSearchResults(CustomSearchParameterBean parameters) throws RegistryException {    	
        RegistryUtils.recordStatistics(parameters);
        AdvancedSearchResultsBean metaDataSearchResultsBean;
        UserRegistry registry = (UserRegistry) getRootRegistry();
        Registry configSystemRegistry = getConfigSystemRegistry();
        ContentSearchService contentSearchService = Utils.getContentSearchService();
        ResourceData[] contentSearchResourceData;
        String[][] tempParameterValues = parameters.getParameterValues();

//        Doing a validation of all the values sent
        boolean allEmpty = true;
        for (String[] tempParameterValue : tempParameterValues) {
            if(tempParameterValue[1] != null & tempParameterValue[1].trim().length() > 0){
                allEmpty = false;
//                Validating all the dates
                if (tempParameterValue[0].equals("createdAfter") || tempParameterValue[0].equals("createdBefore") ||
                        tempParameterValue[0].equals("updatedAfter") || tempParameterValue[0].equals("updatedBefore")){
                    if(!SearchUtils.validateDateInput(tempParameterValue[1])){
                        String message = tempParameterValue[0] + " contains illegal characters";
                        return SearchUtils.getEmptyResultBeanWithErrorMsg(message);
                    }
                }

                else if(tempParameterValue[0].equals("mediaType")){
                    if (SearchUtils.validateMediaTypeInput(tempParameterValue[1])) {
                        String message = tempParameterValue[0] + " contains illegal characters";
                        return SearchUtils.getEmptyResultBeanWithErrorMsg(message);
                    }
                }
                else if(tempParameterValue[0].equals("content")){
                    if (SearchUtils.validateContentInput(tempParameterValue[1])) {
                        String message = tempParameterValue[0] + " contains illegal characters";
                        return SearchUtils.getEmptyResultBeanWithErrorMsg(message);
                    }
                }
                else if(tempParameterValue[0].equals("tags")){
                    boolean containsTag = false;
                    for (String str : tempParameterValue[1].split(",")) {
                        if (str.trim().length() > 0) {
                            containsTag = true;
                            break;
                        }
                    }
                    if (!containsTag) {
                        String message = tempParameterValue[0] + " contains illegal characters";
                        return SearchUtils.getEmptyResultBeanWithErrorMsg(message);
                    }
                    if (SearchUtils.validateTagsInput(tempParameterValue[1])) {
                        String message = tempParameterValue[0] + " contains illegal characters";
                        return SearchUtils.getEmptyResultBeanWithErrorMsg(message);
                    }
                }
                else{
                    if(SearchUtils.validatePathInput(tempParameterValue[1])){
                        String message = tempParameterValue[0] + " contains illegal characters";
                        return SearchUtils.getEmptyResultBeanWithErrorMsg(message);
                    }
                }
            }
        }

        if (allEmpty) {
            return SearchUtils.getEmptyResultBeanWithErrorMsg("At least one field must be filled");
        }

        boolean onlyContent = true;
        for (String[] tempParameterValue : tempParameterValues) {
            if(!tempParameterValue[0].equals("content") && !tempParameterValue[0].equals("leftOp") &&
                    !tempParameterValue[0].equals("rightOp") && tempParameterValue[1] != null &&
                    tempParameterValue[1].length() > 0){
                onlyContent = false;
                break;
            }
        }

        for (String[] tempParameterValue : tempParameterValues) {
            if (tempParameterValue[0].equals("content") && tempParameterValue[1] != null &&
                    tempParameterValue[1].length() > 0) {
                try {
                    contentSearchResourceData = contentSearchService.search(registry, tempParameterValue[1]);
                } catch (RegistryException e) {
                    metaDataSearchResultsBean = new AdvancedSearchResultsBean();
                    metaDataSearchResultsBean.setErrorMessage(e.getMessage());
                    return getPaginatedResult(metaDataSearchResultsBean);
                }

//                If there are no resource paths returned from content, then there is no point of searching for more
                if (contentSearchResourceData != null && contentSearchResourceData.length > 0) {
//                    Map<String, ResourceData> resourceDataMap = new HashMap<String, ResourceData>();
                    Map<String,ResourceData> aggregatedMap = new HashMap<String, ResourceData>();

                    for (ResourceData resourceData : contentSearchResourceData) {
                        aggregatedMap.put(resourceData.getResourcePath(), resourceData);
                    }

                    metaDataSearchResultsBean = AdvancedSearchResultsBeanPopulator.populate(configSystemRegistry,
                            registry, parameters);

                    if (metaDataSearchResultsBean != null) {
                        ResourceData[] metaDataResourceData = metaDataSearchResultsBean.getResourceDataList();
                        if (metaDataResourceData != null && metaDataResourceData.length > 0) {

                            List<String> invalidKeys = new ArrayList<String>();
                            for (String key : aggregatedMap.keySet()) {
                                boolean keyFound = false;
                                for (ResourceData resourceData : metaDataResourceData) {
                                    if(resourceData.getResourcePath().equals(key)){
                                        keyFound = true;
                                        break;
                                    }
                                }
                                if(!keyFound){
                                    invalidKeys.add(key);
                                }
                            }
                            for (String invalidKey : invalidKeys) {
                                aggregatedMap.remove(invalidKey);
                            }
                        }else if(!onlyContent) {
                            aggregatedMap.clear();
                        }
                    }

                    ArrayList<ResourceData> sortedList = new ArrayList<ResourceData>(aggregatedMap.values());
                    SearchUtils.sortResourceDataList(sortedList);

                    metaDataSearchResultsBean = new AdvancedSearchResultsBean();
                    metaDataSearchResultsBean.setResourceDataList(sortedList.toArray(new ResourceData[sortedList.size()]));
                    return getPaginatedResult(metaDataSearchResultsBean);
                }else {
                    metaDataSearchResultsBean = new AdvancedSearchResultsBean();
                    metaDataSearchResultsBean.setResourceDataList(contentSearchResourceData);
                    return getPaginatedResult(metaDataSearchResultsBean);
                }
            }
        }
        return getPaginatedResult(
                AdvancedSearchResultsBeanPopulator.populate(configSystemRegistry, registry, parameters));
    }

    private AdvancedSearchResultsBean getPaginatedResult( AdvancedSearchResultsBean advancedSearchResultsBean){
        ResourceData[] paginatedResult;
        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        if (messageContext != null && PaginationUtils.isPaginationHeadersExist(messageContext) && advancedSearchResultsBean.getResourceDataList() != null) {

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
                    System.arraycopy(advancedSearchResultsBean.getResourceDataList(), startIndex, paginatedResult, 0, (rowCount - startIndex));
                } else {
                    paginatedResult = new ResourceData[count];
                    System.arraycopy(advancedSearchResultsBean.getResourceDataList(), startIndex, paginatedResult, 0, count);
                }
                advancedSearchResultsBean.setResourceDataList(paginatedResult);
                return advancedSearchResultsBean;

            } finally {
                PaginationContext.destroy();
            }
        }else {
            return advancedSearchResultsBean;
        }
    }

    /* (non-Javadoc)
	 * @see org.wso2.carbon.registry.search.services.ISearchService#getMediaTypeSearch(java.lang.String)
	 */
    public MediaTypeValueList getMediaTypeSearch(String mediaType) throws RegistryException{
        UserRegistry registry = (UserRegistry) getRootRegistry();
        return CustomSearchParameterPopulator.getMediaTypeParameterValues(registry, mediaType);
    }

    /* (non-Javadoc)
	 * @see org.wso2.carbon.registry.search.services.ISearchService#saveAdvancedSearchFilter(org.wso2.carbon.registry.search.beans.CustomSearchParameterBean, java.lang.String)
	 */
    public void saveAdvancedSearchFilter(CustomSearchParameterBean queryBean, String filterName) throws
                                                                                               RegistryException {
        UserRegistry configUserRegistry = (UserRegistry)getConfigUserRegistry();
        AdvancedSearchFilterActions.saveAdvancedSearchQueryBean(configUserRegistry, queryBean, filterName);
    }

    /* (non-Javadoc)
	 * @see org.wso2.carbon.registry.search.services.ISearchService#getAdvancedSearchFilter(java.lang.String)
	 */
    public CustomSearchParameterBean getAdvancedSearchFilter(String filterName) throws
                                                                              RegistryException {
        UserRegistry configUserRegistry = (UserRegistry)getConfigUserRegistry();
        return AdvancedSearchFilterActions.getAdvancedSearchQueryBean(configUserRegistry, filterName);
    }

    /* (non-Javadoc)
	 * @see org.wso2.carbon.registry.search.services.ISearchService#getSavedFilters()
	 */
    public String[] getSavedFilters() throws RegistryException {
        UserRegistry configUserRegistry = (UserRegistry)getConfigUserRegistry();
        return AdvancedSearchFilterActions.getSavedFilterNames(configUserRegistry);
    }

    public void deleteFilter(String filterName) throws RegistryException {
        UserRegistry configUserRegistry = (UserRegistry) getConfigUserRegistry();
        configUserRegistry.delete(RegistryConstants.PATH_SEPARATOR+"users"+RegistryConstants.PATH_SEPARATOR+CarbonContext.getThreadLocalCarbonContext().getUsername()+ RegistryConstants.PATH_SEPARATOR +"searchFilters" +RegistryConstants.PATH_SEPARATOR+filterName);
    }
}
