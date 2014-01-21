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

package org.wso2.carbon.registry.indexing.service;

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.common.utils.CommonUtil;

import java.util.Calendar;

public class AdvancedSearchResultsBeanPopulator {

    public static AdvancedSearchResultsBean populate(UserRegistry registry, String resourceName, String authorName, String updaterName,
                                                     String createdAfter, String createdBefore, String updatedAfter,
                                                     String updatedBefore, String tags, String commentWords,
                                                     String propertyName, String propertyValue, String content) {

        AdvancedSearchResultsBean advancedSearchResultsBean = new AdvancedSearchResultsBean();

        try {

            AdvancedResourceQuery query = new AdvancedResourceQuery();
            query.setResourceName(resourceName);
            query.setAuthorName(authorName);
            query.setUpdaterName(updaterName);
            query.setCreatedAfter(CommonUtil.computeDate(createdAfter));
            query.setCreatedBefore(CommonUtil.computeDate(createdBefore));
            query.setUpdatedAfter(CommonUtil.computeDate(updatedAfter));
            query.setUpdatedBefore(CommonUtil.computeDate(updatedBefore));
            query.setCommentWords(commentWords);
            query.setTags(tags);
            query.setPropertyName(propertyName);
            query.setPropertyValue(propertyValue);
            query.setContent(content);

            Resource qResults = query.execute(registry);

            String[] childPaths = (String[]) qResults.getContent();
            //Temporary fix for CARBON-4562.
            //We should have a search result iterator for advance search.
            //This fix will show only first 25 records
            int resultSize = childPaths.length;
            if(resultSize > 40){
            	resultSize = 40;
            }
            
            ResourceData [] resourceDataList = new ResourceData [resultSize];
            for (int i=0; i<resultSize; i++) {

                ResourceData resourceData = new ResourceData();
                resourceData.setResourcePath(childPaths[i]);

                if (childPaths[i] != null) {
                    if (RegistryConstants.ROOT_PATH.equals(childPaths[i])) {
                        resourceData.setName("root");
                    } else {
                        String[] parts = childPaths[i].split(RegistryConstants.PATH_SEPARATOR);
                        resourceData.setName(parts[parts.length - 1]);
                    }
                }

                try {
                    Resource child = registry.get(childPaths[i]);

                    resourceData.setResourceType(child instanceof Collection ?
                            "collection" : "resource");
                    resourceData.setAuthorUserName(child.getAuthorUserName());
                    resourceData.setDescription(child.getDescription());
                    resourceData.setAverageRating(registry.getAverageRating(child.getPath()));
                    Calendar createdDateTime = Calendar.getInstance();
                    createdDateTime.setTime(child.getCreatedTime());
                    resourceData.setCreatedOn(createdDateTime);
                    CommonUtil.populateAverageStars(resourceData);

                    child.discard();

                    resourceDataList [i] = resourceData;

                } catch (AuthorizationFailedException e) {
                    // do not show unauthorized resource in search results.
                }

            }

            advancedSearchResultsBean.setResourceDataList(resourceDataList);

        } catch (RegistryException e) {

            String msg = "Failed to get advanced search results. " + e.getMessage();
            advancedSearchResultsBean.setErrorMessage(msg);
        }

        return advancedSearchResultsBean;
    }
}
