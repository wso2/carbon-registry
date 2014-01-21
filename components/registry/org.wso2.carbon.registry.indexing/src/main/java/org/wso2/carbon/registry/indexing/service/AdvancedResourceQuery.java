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

package org.wso2.carbon.registry.indexing.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.StaticConfiguration;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

/**
 * Represents an advanced query on normal resources. Handles all details of defining, executing and
 * parameter processing of such queries.
 */
@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class AdvancedResourceQuery {

    private static Log log = LogFactory.getLog(AdvancedResourceQuery.class);

    /**
     * List of parameters for the query. NOTE THAT THIS LIST SHOULD ALWAYS BE PROCESSED IN THE ORDER
     * THEY ARE LISTED BELOW.
     */
    private String resourceName;
    private String authorName;
    private String updaterName;
    private Date createdAfter;
    private Date createdBefore;
    private Date updatedAfter;
    private Date updatedBefore;
    private String commentWords;
    private String propertyName;
    private String propertyValue;
    private String content;

    private Set<String> tags;
    private String queryPath;

    public Resource execute(Registry registry) throws RegistryException {

        // find if query is already defined
        // if defined, execute
        // else, create new query and execute

        String queryPath = computeQueryPathPrefix();

        if (queryPath.indexOf("1") == -1 && queryPath.indexOf("T") == -1) {
            String msg = "No parameters are specified for the query.";
            log.error(msg);
            throw new RegistryException(msg);
        }
//these modifications are done so the queries are not stored in the registry
//        String resourceQueryPath = queryPath + "r";
//        if (!queryExists(resourceQueryPath)) {
            String queryResourceContent = generateSQLForResources();
//            if (queryResourceContent != null) {
//                defineQueries(resourceQueryPath, queryResourceContent);
//            }
//        }
//        String[] resourcePaths = executeResourceQuery(registry, resourceQueryPath);
        String[] resourcePaths = executeResourceQuery(registry, queryResourceContent);

        String[] collectionPaths;
//        String collectionQueryPath = queryPath + "c";
        String queryCollectionContent = generateSQLForCollections();
//        if (queryCollectionContent != null) {
//            defineQueries(collectionQueryPath, queryCollectionContent);
            collectionPaths = executeCollectionQuery(registry, queryCollectionContent);
//        }

        ArrayList<String> totalPathsArr = new ArrayList<String>();
        HashMap<String, Integer> resourceKeyHash = new HashMap<String, Integer>();

        for (int i = 0;i < resourcePaths.length; i ++) {
            totalPathsArr.add(resourcePaths[i]);
            resourceKeyHash.put(resourcePaths[i], Integer.valueOf(1));
        }
        
        for (int i = 0;i < collectionPaths.length; i ++) {
            if (resourceKeyHash.containsKey(collectionPaths[i])) {
                // is a resource
                continue;
            }
            totalPathsArr.add(collectionPaths[i]);
        }
        

        String[] totalPaths = totalPathsArr.toArray(new String[totalPathsArr.size()]);

        Collection c = registry.newCollection();
        c.setContent(totalPaths);

        return c;
    }

    private String[] executeResourceQuery(Registry registry, String query) throws RegistryException {
        List<Object> params = new ArrayList<Object>();
        if (resourceName != null && resourceName.length() != 0) {
            params.add(resourceName);
        }

        if (authorName != null && authorName.length() != 0) {
            params.add(authorName);
        }

        if (updaterName != null && updaterName.length() != 0) {
            params.add(updaterName);
        }

        if (createdAfter != null) {
            params.add(new Timestamp(createdAfter.getTime()));
        }

        if (createdBefore != null) {
            params.add(new Timestamp(createdBefore.getTime()));
        }

        if (updatedAfter != null) {
            params.add(new Timestamp(updatedAfter.getTime()));
        }

        if (updatedBefore != null) {
            params.add(new Timestamp(updatedBefore.getTime()));
        }

        if (commentWords != null && commentWords.length() != 0) {
            params.add("%" + commentWords + "%");
        }

        if (tags != null && !tags.isEmpty()) {
            params.addAll(tags);
        }

        if (propertyName != null) {
            params.add(propertyName);
        }

        if (propertyValue != null) {
            params.add(propertyValue);
        }

        Map paramMap = new HashMap();
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            paramMap.put(Integer.toString(i + 1), value);
        }
        if (content != null){
            paramMap.put("content",content);
        }
        paramMap.put("query", query);

        Resource r = registry.executeQuery(null, paramMap);
        return (String[])r.getContent();
    }

    private String[] executeCollectionQuery(Registry registry, String query) throws RegistryException {
        List<Object> params = new ArrayList<Object>();
        if (resourceName != null && resourceName.length() != 0) {
            params.add("%/" + resourceName);
        }

        if (authorName != null && authorName.length() != 0) {
            params.add(authorName);
        }

        if (updaterName != null && updaterName.length() != 0) {
            params.add(updaterName);
        }

        if (createdAfter != null) {
            params.add(new Timestamp(createdAfter.getTime()));
        }

        if (createdBefore != null) {
            params.add(new Timestamp(createdBefore.getTime()));
        }

        if (updatedAfter != null) {
            params.add(new Timestamp(updatedAfter.getTime()));
        }

        if (updatedBefore != null) {
            params.add(new Timestamp(updatedBefore.getTime()));
        }


        if (commentWords != null && commentWords.length() != 0) {
            params.add("%" + commentWords + "%");
        }

        if (tags != null && !tags.isEmpty()) {
            params.addAll(tags);
        }

        if (propertyName != null) {
            params.add(propertyName);
        }

        if (propertyValue != null) {
            params.add(propertyValue);
        }

        Map paramMap = new HashMap();
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            paramMap.put(Integer.toString(i + 1), value);
        }
        if (content != null){
            paramMap.put("content",content);
        }
        paramMap.put("query", query);
        Resource r = registry.executeQuery(null, paramMap);
        return (String[])r.getContent();
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        if ("".equals(resourceName)) resourceName = null;
        this.resourceName = resourceName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        if ("".equals(authorName)) authorName = null;
        this.authorName = authorName;
    }

    public String getUpdaterName() {
        return updaterName;
    }

    public void setUpdaterName(String updaterName) {
        if ("".equals(updaterName)) updaterName = null;
        this.updaterName = updaterName;
    }

    public Date getCreatedAfter() {
        return createdAfter;
    }

    public void setCreatedAfter(Date createdAfter) {
        this.createdAfter = createdAfter;
    }

    public Date getCreatedBefore() {
        return createdBefore;
    }

    public void setCreatedBefore(Date createdBefore) {
        this.createdBefore = createdBefore;
    }

    public Date getUpdatedAfter() {
        return updatedAfter;
    }

    public void setUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
    }

    public Date getUpdatedBefore() {
        return updatedBefore;
    }

    public void setUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
    }

    public void setTags(String tags) {
        this.tags = parseTags(tags);
    }

    public void setPropertyName(String propertyName) {
        if ("".equals(propertyName)) propertyName = null;
        this.propertyName = propertyName;
    }

    public void setPropertyValue(String propertyValue) {
        if ("".equals(propertyValue)) propertyValue = null;
        this.propertyValue = propertyValue;
    }

    public String getCommentWords() {
        return commentWords;
    }

    public void setCommentWords(String commentWords) {
        if ("".equals(commentWords)) commentWords = null;
        this.commentWords = commentWords;
    }

    public String getContent(){
        return this.content;
    }

    public void setContent(String content){
        this.content = content;
    }

    private boolean queryExists(String queryPath) throws RegistryException {
        UserRegistry registry = (UserRegistry) Utils.getRegistry();
        return registry.resourceExists(queryPath);
    }

    private void defineQueries(String queryPath, String queryContent) throws RegistryException {
        UserRegistry registry =
                (UserRegistry) Utils.getRegistry();

        Resource q1 = registry.newResource();
        q1.setContent(queryContent);
        q1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
        q1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                RegistryConstants.RESOURCES_RESULT_TYPE);
        registry.put(queryPath, q1);

    }

    private String computeQueryPathPrefix() {

        if (queryPath == null) {

            StringBuffer buf = new StringBuffer("/system/queries/advanced");

            buf.append(resourceName != null ? "1" : "0");
            buf.append(authorName != null ? "1" : "0");
            buf.append(updaterName != null ? "1" : "0");
            buf.append(createdAfter != null ? "1" : "0");
            buf.append(createdBefore != null ? "1" : "0");
            buf.append(updatedAfter != null ? "1" : "0");
            buf.append(updatedBefore != null ? "1" : "0");
            buf.append(commentWords != null ? "1" : "0");
            buf.append(propertyName != null ? "1" : "0");
            buf.append(propertyValue != null ? "1" : "0");

            if (tags != null) {
                buf.append("T");
                buf.append(tags.size());
            }

            queryPath = buf.toString();
        }

        return queryPath;
    }

    public Set<String> parseTags(String tags) {
        Set<String> result = new HashSet<String>();

        String[] parts = tags.split(",");
        for (String part1 : parts) {
            String part = part1.trim();

            if (!"".equals(part)) {
                result.add(part);
            }
        }

        return result;
    }


    // get sql queries to search resources
    private String generateSQLForResources() {
        ArrayList<String> tables = new ArrayList<String>();
        ArrayList<String> conditions = new ArrayList<String>();

        if (resourceName != null && resourceName.length() != 0) {
            conditions.add("R.REG_NAME LIKE ?");
        }

        if (authorName != null && authorName.length() != 0) {
            conditions.add("R.REG_CREATOR LIKE ?");
        }

        if (updaterName != null && updaterName.length() != 0) {
            conditions.add("R.REG_LAST_UPDATOR LIKE ?");
        }

        if (createdAfter != null) {
			conditions.add("R.REG_CREATED_TIME > ?");
		}

		if (createdBefore != null) {
			conditions.add("R.REG_CREATED_TIME < ?");
		}

		if (updatedAfter != null) {
			conditions.add("R.REG_LAST_UPDATED_TIME > ?");
		}

		if (updatedBefore != null) {
			conditions.add("R.REG_LAST_UPDATED_TIME < ?");
		}

        if (commentWords != null && commentWords.length() != 0) {
            tables.add(", REG_COMMENT C");
            tables.add(", REG_RESOURCE_COMMENT RC");
            if(StaticConfiguration.isVersioningComments()) {
                conditions.add("R.REG_VERSION=RC.REG_VERSION AND RC.REG_COMMENT_ID=C.REG_ID AND " +
                        "C.REG_COMMENT_TEXT LIKE ?");
            }
            else {
                conditions.add("R.REG_PATH_ID=RC.REG_PATH_ID AND " +
                        "((R.REG_NAME = RC.REG_RESOURCE_NAME)) AND " +
                        "RC.REG_COMMENT_ID=C.REG_ID AND " +
                        "C.REG_COMMENT_TEXT LIKE ?");
            }
        }

        if (tags != null && !tags.isEmpty()) {
            tables.add(", REG_TAG T");
            tables.add(", REG_RESOURCE_TAG RT");

            StringBuffer tagClause = new StringBuffer();
            if(StaticConfiguration.isVersioningTags()) {
                tagClause.append("R.REG_VERSION=RT.REG_VERSION AND " +
                            "RT.REG_TAG_ID=T.REG_ID ");
            }
            else {
                tagClause.append("R.REG_PATH_ID=RT.REG_PATH_ID AND " +
                            "((R.REG_NAME = RT.REG_RESOURCE_NAME)) AND " +
                            "RT.REG_TAG_ID=T.REG_ID ");
            }

            Iterator<String> i = tags.iterator();
            int count = 0;
            while (i.hasNext()) {
            	count++;
            	if(count == 1){
                    tagClause.append(" AND lower(T.REG_TAG_NAME)=lower(?)");            		
            	}else{
                    tagClause.append(" OR lower(T.REG_TAG_NAME)=lower(?)");            		
            	}
                i.next();
            }
            //tagClause.append(")");
            conditions.add(tagClause.toString());
        }

        if (propertyValue != null || propertyName != null) {
            tables.add(", REG_PROPERTY PP");
            tables.add(", REG_RESOURCE_PROPERTY RP");
            StringBuffer propertyClause = new StringBuffer();
            if(StaticConfiguration.isVersioningProperties()) {
                propertyClause.append("R.REG_VERSION=RP.REG_VERSION AND " +
                            "RP.REG_PROPERTY_ID=PP.REG_ID");
            }
            else {
                propertyClause.append("R.REG_PATH_ID=RP.REG_PATH_ID AND " +
                            "((R.REG_NAME = RP.REG_RESOURCE_NAME)) AND " +
                            "RP.REG_PROPERTY_ID=PP.REG_ID");
            }
            if (propertyName != null) {
                propertyClause.append(" AND lower(PP.REG_NAME)=lower(?)");
            }
            if (propertyValue != null) {
                propertyClause.append(" AND PP.REG_VALUE LIKE ?");
            }
            conditions.add(propertyClause.toString());
        }

        StringBuffer query = new StringBuffer();
        query.append("SELECT R.REG_PATH_ID, R.REG_NAME FROM REG_RESOURCE R");
        for (String table : tables) {
            query.append(table);
        }
        boolean first = true;
        for (String condition : conditions) {
            if (first) {
                query.append(" WHERE ");
                first = false;
            } else {
                query.append(" AND ");
            }
            query.append(condition);
        }
        return query.toString();
    }

    // sql specific to collections
    private String generateSQLForCollections() {
        ArrayList<String> tables = new ArrayList<String>();
        ArrayList<String> conditions = new ArrayList<String>();

        conditions.add("R.REG_NAME IS NULL");
        if (resourceName != null && resourceName.length() != 0) {
            tables.add(", REG_PATH P");
            conditions.add(" P.REG_PATH_VALUE LIKE ? AND P.REG_PATH_ID=R.REG_PATH_ID");
        }

        if (authorName != null && authorName.length() != 0) {
            conditions.add("R.REG_CREATOR LIKE ?");
        }

        if (updaterName != null && updaterName.length() != 0) {
            conditions.add("R.REG_LAST_UPDATOR LIKE ?");
        }

        if (createdAfter != null) {
            conditions.add("R.REG_CREATED_TIME > ?");
        }

        if (createdBefore != null) {
            conditions.add("R.REG_CREATED_TIME < ?");
        }

        if (updatedAfter != null) {
            conditions.add("R.REG_LAST_UPDATED_TIME > ?");
        }

        if (updatedBefore != null) {
            conditions.add("R.REG_LAST_UPDATED_TIME < ?");
        }

        if (commentWords != null && commentWords.length() != 0) {
            tables.add(", REG_COMMENT C");
            tables.add(", REG_RESOURCE_COMMENT RC");
            if(StaticConfiguration.isVersioningComments()) {
                conditions.add("R.REG_VERSION=RC.REG_VERSION AND RC.REG_COMMENT_ID=C.REG_ID AND " +
                        "C.REG_COMMENT_TEXT LIKE ?");
            }
            else {
                conditions.add("R.REG_PATH_ID=RC.REG_PATH_ID AND " +
                        "R.REG_NAME IS NULL AND RC.REG_RESOURCE_NAME IS NULL AND " +
                        "RC.REG_COMMENT_ID=C.REG_ID AND " +
                        "C.REG_COMMENT_TEXT LIKE ?");
            }
        }

        if (tags != null && !tags.isEmpty()) {
            tables.add(", REG_TAG T");
            tables.add(", REG_RESOURCE_TAG RT");

            StringBuffer tagClause = new StringBuffer();
            if(StaticConfiguration.isVersioningTags()) {
                tagClause.append("R.REG_VERSION=RT.REG_VERSION AND " +
                            "RT.REG_TAG_ID=T.REG_ID ");
            }
            else {
                tagClause.append("R.REG_PATH_ID=RT.REG_PATH_ID AND " +
                            "R.REG_NAME IS NULL AND RT.REG_RESOURCE_NAME IS NULL AND " +
                            "RT.REG_TAG_ID=T.REG_ID ");
            }

            Iterator<String> i = tags.iterator();
            int count = 0;
            while (i.hasNext()) {
            	count++;
            	if(count == 1){
            		tagClause.append(" AND lower(T.REG_TAG_NAME)=lower(?)");
            	}else{
                    tagClause.append(" OR lower(T.REG_TAG_NAME)=lower(?)");            		
            	}
                i.next();
            }
            //tagClause.append(")");
            conditions.add(tagClause.toString());
        }

        if (propertyValue != null || propertyName != null) {
            tables.add(", REG_PROPERTY PP");
            tables.add(", REG_RESOURCE_PROPERTY RP");
            StringBuffer propertyClause = new StringBuffer();
            if(StaticConfiguration.isVersioningProperties()) {
                propertyClause.append("R.REG_VERSION=RP.REG_VERSION AND " +
                            "RP.REG_PROPERTY_ID=PP.REG_ID");
            }
            else {
                propertyClause.append("R.REG_PATH_ID=RP.REG_PATH_ID AND " +
                            "R.REG_NAME IS NULL AND RP.REG_RESOURCE_NAME IS NULL AND " +
                            "RP.REG_PROPERTY_ID=PP.REG_ID");
            }
            if (propertyName != null) {
                propertyClause.append(" AND lower(PP.REG_NAME)=lower(?)");
            }
            if (propertyValue != null) {
                propertyClause.append(" AND PP.REG_VALUE LIKE ?");
            }
            conditions.add(propertyClause.toString());
        }

        if (conditions.size() == 0) {
            return null;
        }

        StringBuffer query = new StringBuffer();
        query.append("SELECT R.REG_PATH_ID, R.REG_NAME FROM REG_RESOURCE R");
        for (String table : tables) {
            query.append(table);
        }
        boolean first = true;
        for (String condition : conditions) {
            if (first) {
                query.append(" WHERE ");
                first = false;
            } else {
                query.append(" AND ");
            }
            query.append(condition);
        }
        return query.toString();
    }
}

