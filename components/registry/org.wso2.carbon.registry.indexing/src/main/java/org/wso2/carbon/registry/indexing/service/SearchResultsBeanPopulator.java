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
import org.wso2.carbon.registry.core.TaggedResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.common.TagCount;
import org.wso2.carbon.registry.common.utils.CommonUtil;

import java.util.Map;
import java.util.Calendar;

public class SearchResultsBeanPopulator {
    public static SearchResultsBean populate(UserRegistry userRegistry, String searchType, String criteria) {

        SearchResultsBean searchResultsBean = new SearchResultsBean();

        try {
            if (searchType.equalsIgnoreCase("Tag")) {
                searchResultsBean.setResourceDataList(searchByTags(criteria, userRegistry));
            } else {
                searchResultsBean.setResourceDataList(searchByContent(criteria, userRegistry));
            }

        } catch (RegistryException e) {

            String msg = "Failed to generate search results. " + e.getMessage();
            searchResultsBean.setErrorMessage(msg);
        }

        return searchResultsBean;
    }

    private static ResourceData [] searchByTags(String tag, UserRegistry registry)
            throws RegistryException {

        ResourceData[] resourceDataList = new ResourceData[0];

        if (tag != null && tag.length() != 0) {
            TaggedResourcePath[] taggedPaths = registry.getResourcePathsWithTag(tag);
            resourceDataList = new ResourceData [taggedPaths.length];
            for (int i=0; i<taggedPaths.length; i++) {
                String resultPath = taggedPaths[i].getResourcePath();

                ResourceData resourceData = new ResourceData();
                resourceData.setResourcePath(resultPath);

                if (resultPath != null) {
                    if (resultPath.equals(RegistryConstants.ROOT_PATH)) {
                        resourceData.setName(RegistryConstants.ROOT_PATH);
                    } else {
                        String[] parts = resultPath.split(RegistryConstants.PATH_SEPARATOR);
                        resourceData.setName(parts[parts.length - 1]);
                    }
                }

                try {
                    Resource child = registry.get(resultPath);

                    resourceData.setResourceType(child instanceof Collection ?
                            "collection" : "resource");
                    resourceData.setAuthorUserName(child.getAuthorUserName());
                    resourceData.setDescription(child.getDescription());
                    resourceData.setAverageRating(registry.getAverageRating(child.getPath()));
                    Calendar createdDateTime = Calendar.getInstance();
                    createdDateTime.setTime(child.getCreatedTime());
                    resourceData.setCreatedOn(createdDateTime);

                    Map counts = taggedPaths[i].getTagCounts();
                    Object [] keySet = counts.keySet().toArray();
                    TagCount [] tagCounts = new TagCount [counts.size()];
                    for(int j=0; j<counts.size(); j++) {
                        TagCount tagCount = new TagCount();
                        tagCount.setKey((String)keySet[j]);
                        tagCount.setValue((Long)counts.get(keySet[j]));
                        tagCounts[j] = tagCount;
                    }
                    resourceData.setTagCounts(tagCounts);
                    
                    CommonUtil.populateAverageStars(resourceData);

                    child.discard();

                    resourceDataList[i] = resourceData;

                } catch (AuthorizationFailedException e) {
                    // do not show unauthorized resources in search results
                }
            }
        }
        return resourceDataList;
    }

         private static ResourceData [] searchByContent (String content, UserRegistry registry)
            throws RegistryException {

        ResourceData [] resourceDataList = new ResourceData[0];

        if(content != null && content.length() != 0){
            String [] paths;
            try {
                paths = (String [])registry.searchContent(content).getContent();
            } catch (Exception e) {
                return new ResourceData[0];
            }
            resourceDataList = new ResourceData [paths.length];
            for(int i=0; i<paths.length; i++){
                ResourceData resourceData = new ResourceData();
                resourceData.setResourcePath(paths[i]);

                if (paths[i] != null) {
                    if (paths[i].equals(RegistryConstants.ROOT_PATH)) {
                        resourceData.setName(RegistryConstants.ROOT_PATH);
                    } else {
                        String[] parts = paths[i].split(RegistryConstants.PATH_SEPARATOR);
                        resourceData.setName(parts[parts.length - 1]);
                    }
                }

                try {
                    Resource child = registry.get(paths[i]);

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

                    resourceDataList[i] = resourceData;

                } catch (AuthorizationFailedException e) {
                    // do not show unauthorized resources in search results
                }
            }
        }

        return resourceDataList;
    }
}
