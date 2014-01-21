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

package org.wso2.carbon.registry.search.services.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.common.utils.UserUtil;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.carbon.registry.search.beans.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.beans.CustomSearchParameterBean;

import java.util.*;
import java.util.regex.Pattern;

public class AdvancedSearchResultsBeanPopulator {

    public static final Log log = LogFactory.getLog(AdvancedSearchResultsBeanPopulator.class);


    public static AdvancedSearchResultsBean populate(Registry configSystemRegistry, UserRegistry registry,
                                                     CustomSearchParameterBean propertyNameValues) {

        AdvancedSearchResultsBean advancedSearchResultsBean = new AdvancedSearchResultsBean();

        try {

            String[] childPaths =
                    getQueryResult(configSystemRegistry, registry, propertyNameValues.getParameterValues());

            String[][] parameterValues = propertyNameValues.getParameterValues();
            String resourcePathPattern = null;

            for (String[] parameterValue : parameterValues) {
                if(parameterValue[0].equals("resourcePath") && parameterValue[1] != null &&
                        parameterValue[1].length() > 0){
                    resourcePathPattern = "^(" + parameterValue[1].replace("%",".*").replace("$","\\$") + ")$";
                    break;
                }
            }

            boolean onlyAssociation = true;
            String associationType = null;
            String associationDestination = null;

            for (String[] parameterValue : parameterValues) {
                if(!parameterValue[0].equals("content") &&
                        !parameterValue[0].equals("associationType") && !parameterValue[0].equals("associationDest") &&
                        !parameterValue[0].equals("leftOp") && !parameterValue[0].equals("rightOp")
                        && parameterValue[1] != null && parameterValue[1].length() > 0){
                    onlyAssociation = false;
                }
                if(parameterValue[0].equals("associationType")
                        && parameterValue[1] != null && parameterValue[1].length() > 0){
                    associationType = parameterValue[1];
                }
                if(parameterValue[0].equals("associationDest")
                        && parameterValue[1] != null && parameterValue[1].length() > 0){
                    associationDestination = parameterValue[1];
                }
            }

            if(onlyAssociation){
                for (String[] parameterValue : parameterValues) {
                    if(parameterValue[0].equals("resourcePath")){
                        parameterValue[1] = "%";
                        break;
                    }
                }
                childPaths = getQueryResult(configSystemRegistry,registry,parameterValues);
            }

            List<ResourceData> resourceDataList = new ArrayList<ResourceData>();

            for (String childPath : childPaths) {
                if(resourcePathPattern != null && !Pattern.compile(resourcePathPattern).matcher(childPath.substring(childPath.lastIndexOf(
                        RegistryConstants.PATH_SEPARATOR) + 1)).find()){
                    continue;
                }

                boolean doContinue = true;
                boolean doContinueDest = true;

                if (associationDestination != null) {
                    Association[] destinationAssociations = registry
                            .getAllAssociations(childPath);
                    for (Association association : destinationAssociations) {
                        if (association.getDestinationPath().contains(
                                associationDestination))
                        {
//                                && association.getDestinationPath().equals(
//                                childPath)) {
                            doContinueDest = false;
                            break;
                        }
                    }
                }

                if (associationType != null) {
                    Association[] typeAssociations = registry.getAssociations(
                            childPath, associationType);
                    for (Association association : typeAssociations) {
                        if (association.getSourcePath().equals(childPath)) {
                            doContinue =false;
                            break;
                        }
                    }
                }

                if((associationType != null && doContinue) || (associationDestination != null && doContinueDest)){
                    continue;
                }
                try {
                    Resource child = registry.get(childPath);

                    if ("true".equals(child.getProperty("registry.absent"))) {
                        continue;
                    }
                    ResourceData resourceData = new ResourceData();
                    resourceData.setResourcePath(childPath);

                    if (childPath != null) {
                        if (RegistryConstants.ROOT_PATH.equals(childPath)) {
                            resourceData.setName("root");
                        } else {
                            String[] parts = childPath.split(RegistryConstants.PATH_SEPARATOR);
                            resourceData.setName(parts[parts.length - 1]);
                        }
                    }

                    String actualPath = child.getProperty("registry.actualpath");
                    if (actualPath != null && registry.resourceExists(actualPath)) {
                        child = registry.get(actualPath);
                    }
                    resourceData.setResourceType(child instanceof Collection ?
                            "collection" : "resource");
                    resourceData.setAuthorUserName(child.getAuthorUserName());
                    resourceData.setDescription(child.getDescription());
                    resourceData.setAverageRating(registry.getAverageRating(child.getPath()));
                    Calendar createdDateTime = Calendar.getInstance();
                    createdDateTime.setTime(child.getCreatedTime());
                    resourceData.setCreatedOn(createdDateTime);
                    CommonUtil.populateAverageStars(resourceData);

                    String user = child.getProperty("registry.user");

                    if (registry.getUserName().equals(user)) {
                        resourceData.setPutAllowed(true);
                        resourceData.setDeleteAllowed(true);
                        resourceData.setGetAllowed(true);
                    } else {
                        resourceData.setPutAllowed(
                                UserUtil.isPutAllowed(registry.getUserName(), childPath, registry));
                        resourceData.setDeleteAllowed(
                                UserUtil.isDeleteAllowed(registry.getUserName(), childPath, registry));
                        resourceData.setGetAllowed(
                                UserUtil.isGetAllowed(registry.getUserName(), childPath, registry));
                    }

                    child.discard();

                    resourceDataList.add(resourceData);

                } catch (AuthorizationFailedException e) {
                    // do not show unauthorized resource in search results.
                }

            }

            SearchUtils.sortResourceDataList(resourceDataList);
            advancedSearchResultsBean.setResourceDataList(resourceDataList.toArray(new ResourceData[resourceDataList.size()]));

        } catch (RegistryException e) {

            String msg = "Failed to get advanced search results. " + e.getMessage();
            advancedSearchResultsBean.setErrorMessage(msg);
        }
        catch (Exception e) {
            log.error("An error occurred while obtaining search results", e);
        }

        return advancedSearchResultsBean;
    }

    private static String[] getQueryResult(Registry configSystemRegistry, UserRegistry registry,
                                           String[][] propertyNameValues) throws Exception {

        AdvancedResourceQuery query = new AdvancedResourceQuery();

        Map<String, String> customValues = new HashMap<String, String>();

        for (String[] propertyNameValue : propertyNameValues) {
            if (propertyNameValue[0].equals("resourcePath")) {
                query.setResourceName(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("author")) {
                query.setAuthorName(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("updater")) {
                query.setUpdaterName(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("createdAfter")) {
                query.setCreatedAfter(CommonUtil.computeDate(propertyNameValue[1]));
            } else if (propertyNameValue[0].equals("createdBefore")) {
                query.setCreatedBefore(addOneDay(CommonUtil.computeDate(propertyNameValue[1])));
            } else if (propertyNameValue[0].equals("updatedAfter")) {
                query.setUpdatedAfter(CommonUtil.computeDate(propertyNameValue[1]));
            } else if (propertyNameValue[0].equals("updatedBefore")) {
                query.setUpdatedBefore(addOneDay(CommonUtil.computeDate(propertyNameValue[1])));
            } else if (propertyNameValue[0].equals("commentWords")) {
                query.setCommentWords(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("associationType")) {
                query.setAssociationType(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("associationDest")) {
                query.setAssociationDest(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("tags")) {
                query.setTags(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("propertyName")) {
                query.setPropertyName(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("leftPropertyValue")) {
                query.setLeftPropertyValue(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("rightPropertyValue")) {
                query.setRightPropertyValue(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("propertyValue")) {
                query.setRightPropertyValue(propertyNameValue[1]);
                query.setRightOp("eq");
            } else if (propertyNameValue[0].equals("authorNameNegate")) {
                query.setAuthorNameNegate(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("updaterNameNegate")) {
                query.setUpdaterNameNegate(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("createdRangeNegate")) {
                query.setCreatedRangeNegate(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("updatedRangeNegate")) {
                query.setUpdatedRangeNegate(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("leftOp")) {
                query.setLeftOp(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("rightOp")) {
                query.setRightOp(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("content")) {
                query.setContent(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("mediaType")) {
                query.setMediaType(propertyNameValue[1]);
            } else if (propertyNameValue[0].equals("mediaTypeNegate")) {
                query.setMediaTypeNegate(propertyNameValue[1]);
            } else {

                customValues.put(propertyNameValue[0], propertyNameValue[1]);
            }
        }        

        boolean first = true, noCustomSearch = true;
        Set<String> s = new HashSet<String>();

        for (Map.Entry<String, String> entry : customValues.entrySet()) {
            if (!entry.getValue().equals("")) {
                Map<String, String> temp = new HashMap<String, String>();
                temp.put(entry.getKey(), entry.getValue());

                query.setCustomSearchValues(temp);
                Resource qResults = query.execute(configSystemRegistry, registry);

                if (((String[]) qResults.getContent()).length > 0) {
                    if (first) {
                        s.addAll(Arrays.asList((String[]) qResults.getContent()));
                        first = false;
                    } else {
                        s.retainAll(Arrays.asList((String[]) qResults.getContent()));
                    }
                } else {
                    s.clear();
                    return new String[0];
                }


                noCustomSearch = false;
            }
        }

        if (noCustomSearch) {
            query.setCustomSearchValues(customValues);
            Resource qResults = query.execute(configSystemRegistry, registry);

            return (String[]) qResults.getContent();
        }

        String[] ret = new String[s.size()];
        ret = s.toArray(ret);

        return ret;
    }

    private static Date addOneDay(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }
}
