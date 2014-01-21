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

package org.wso2.carbon.registry.search.services.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.config.StaticConfiguration;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.MediaTypesUtils;

import java.sql.Timestamp;
import java.util.*;

/**
 * Represents an advanced query on normal resources. Handles all details of
 * defining, executing and parameter processing of such queries.
 */
public class AdvancedResourceQuery {

	private static Log log = LogFactory.getLog(AdvancedResourceQuery.class);

	/**
	 * List of parameters for the query. NOTE THAT THIS LIST SHOULD ALWAYS BE
	 * PROCESSED IN THE ORDER THEY ARE LISTED BELOW.
	 */
	private String resourceName;
	private String authorName;
	private String updaterName;
	private String commentWords;

	private String associationType;
	private String associationDest;

	private String propertyName;
	private String leftPropertyValue;
	private String rightPropertyValue;
	private String content;
	private Map<String, String> customSearchValues;
	private String mediaType;

	// members to be used for the negation logic
	private boolean authorNameNegate;
	private boolean updaterNameNegate;
	private boolean createdRangeNegate;
	private boolean updatedRangeNegate;
	private boolean mediaTypeNegate;

	private String leftOp;
	private String rightOp;

	private boolean propertyRange;
	// End

	private Set<String> tags;

	private String queryPath;

	private long createdAfter = Long.MIN_VALUE;

	private long createdBefore = Long.MIN_VALUE;

	private long updatedAfter = Long.MIN_VALUE;

	private long updatedBefore = Long.MIN_VALUE;

	public Resource execute(Registry configSystemRegistry, Registry registry)
			throws RegistryException {

		// find if query is already defined
		// if defined, execute
		// else, create new query and execute

		// if the registry is in read-only mode, and the query is not available
		// on the registry,
		// it will be passed in as a parameter.
        String[] resourcePaths = new String[0];
        String[] collectionPaths = new String[0];

        String resourceQuery = generateSQLForResources();
        if (resourceQuery != null) {
            resourcePaths = executeResourceQuery(registry,
                    resourceQuery);
        }

        String collectionQuery = generateSQLForCollections();
        if (collectionQuery != null) {
            collectionPaths = executeCollectionQuery(registry,
                    collectionQuery);
        }

        Set<String> totalPathsArr = new HashSet<String>();

		totalPathsArr.addAll(Arrays.asList(resourcePaths));
        totalPathsArr.addAll(Arrays.asList(collectionPaths));


		String[] totalPaths = totalPathsArr.toArray(new String[totalPathsArr
				.size()]);

		Collection c = registry.newCollection();
		c.setContent(totalPaths);

		return c;
	}

	private String[] executeResourceQuery(Registry registry, String query)
			throws RegistryException {
		
		List<Object> params = new ArrayList<Object>();
		if (resourceName != null && resourceName.length() != 0) {
			params.add(resourceName);
		}

		if (authorName != null && authorName.length() != 0) {
			params.add("%" + authorName + "%");
		}

		if (updaterName != null && updaterName.length() != 0) {
			params.add("%" + updaterName + "%");
		}

		if (createdAfter > Long.MIN_VALUE) {
			params.add(new Timestamp(createdAfter));
		}

		if (createdBefore > Long.MIN_VALUE) {
			params.add(new Timestamp(createdBefore));
		}

		if (updatedAfter > Long.MIN_VALUE) {
			params.add(new Timestamp(updatedAfter));
		}

		if (updatedBefore > Long.MIN_VALUE) {
			params.add(new Timestamp(updatedBefore));
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

		if (leftPropertyValue != null) {
			params.add(leftPropertyValue);
		}

		if (rightPropertyValue != null) {
			params.add(rightPropertyValue);
		}

		if (mediaType != null) {
			params.add(mediaType);
		}

		if (customSearchValues.size() > 0) {

            for (String s : customSearchValues.keySet()) {
                if (!customSearchValues.get(s).trim().equals("")) {
                    params.add(customSearchValues.get(s));
                }
            }
		}

		/*
		 * if(associationType != null){ params.add(associationType); }
		 */
		/*
		 * if(associationDest != null){
		 * params.add(associationDest.toLowerCase()); }
		 */
		Map<String,Object> paramMap = new HashMap<String, Object>();
		for (int i = 0; i < params.size(); i++) {
			Object value = params.get(i);
			paramMap.put(Integer.toString(i + 1), value);
		}
//		if (content != null) {
//			paramMap.put("content", content);
//		}
		Resource r;
		if (query != null && query.length() != 0) {
			paramMap.put("query", query);

			r = registry.executeQuery(null, paramMap);
		} else {
			r = registry.executeQuery(queryPath, paramMap);
		}
		
		return (String[]) r.getContent();
	}

	private String[] executeCollectionQuery(Registry registry, String query)
			throws RegistryException {
		List<Object> params = new ArrayList<Object>();
		if (resourceName != null && resourceName.length() != 0) {
			params.add("%/" + resourceName);
//			params.add("%/" + resourceName + "%");
		}

		if (authorName != null && authorName.length() != 0) {
			params.add("%" + authorName + "%");
		}

		if (updaterName != null && updaterName.length() != 0) {
			params.add("%" + updaterName + "%");
		}

		if (createdAfter > Long.MIN_VALUE) {
			params.add(new Timestamp(createdAfter));
		}

		if (createdBefore > Long.MIN_VALUE) {
			params.add(new Timestamp(createdBefore));
		}

		if (updatedAfter > Long.MIN_VALUE) {
			params.add(new Timestamp(updatedAfter));
		}

		if (updatedBefore > Long.MIN_VALUE) {
			params.add(new Timestamp(updatedBefore));
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

		if (leftPropertyValue != null) {
			params.add(leftPropertyValue);
		}

		if (rightPropertyValue != null) {
			params.add(rightPropertyValue);
		}

		if (mediaType != null) {
			params.add(mediaType);
		}
		if (customSearchValues.size() > 0) {

            for (String s : customSearchValues.keySet()) {
                if (!customSearchValues.get(s).trim().equals("")) {
                    params.add(customSearchValues.get(s));
                }
            }
		}

		if (associationType != null) {
			params.add(associationType);
		}
		if (associationDest != null) {
			params.add("%/" + associationDest.toLowerCase());
			params.add("%/" + associationDest.toLowerCase() + "/%");
		}
		Map<String,Object> paramMap = new HashMap<String, Object>();
		for (int i = 0; i < params.size(); i++) {
			Object value = params.get(i);
			paramMap.put(Integer.toString(i + 1), value);
		}
//		if (content != null) {
//			paramMap.put("content", content);
//		}
		Resource r;
		if (query != null && query.length() != 0) {
			paramMap.put("query", query);
			r = registry.executeQuery(null, paramMap);
		} else {
			r = registry.executeQuery(queryPath, paramMap);
		}
		return (String[]) r.getContent();
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		if ("".equals(resourceName))
			resourceName = null;
		if (resourceName != null) {
			resourceName = resourceName.toLowerCase();
		}
		this.resourceName = resourceName;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		if ("".equals(authorName))
			authorName = null;
		if (authorName != null) {
			authorName = authorName.toLowerCase();
		}
		this.authorName = authorName;
	}

	public String getUpdaterName() {
		return updaterName;
	}

	public void setUpdaterName(String updaterName) {
		if ("".equals(updaterName))
			updaterName = null;
		if (updaterName != null) {
			updaterName = updaterName.toLowerCase();
		}
		this.updaterName = updaterName;
	}

	public Date getCreatedAfter() {
		return (createdAfter == Long.MIN_VALUE) ? null : new Date(createdAfter);
	}

	public void setCreatedAfter(Date createdAfter) {
		this.createdAfter = (createdAfter == null) ? Long.MIN_VALUE
				: createdAfter.getTime();
	}

	public Date getCreatedBefore() {
		return (createdBefore == Long.MIN_VALUE) ? null : new Date(
				createdBefore);
	}

	public void setCreatedBefore(Date createdBefore) {
		this.createdBefore = (createdBefore == null) ? Long.MIN_VALUE
				: createdBefore.getTime();
	}

	public Date getUpdatedAfter() {
		return (updatedAfter == Long.MIN_VALUE) ? null : new Date(updatedAfter);
	}

	public void setUpdatedAfter(Date updatedAfter) {
		this.updatedAfter = (updatedAfter == null) ? Long.MIN_VALUE
				: updatedAfter.getTime();
	}

	public Date getUpdatedBefore() {
		return (updatedBefore == Long.MIN_VALUE) ? null : new Date(
				updatedBefore);
	}

	public void setUpdatedBefore(Date updatedBefore) {
		this.updatedBefore = (updatedBefore == null) ? Long.MIN_VALUE
				: updatedBefore.getTime();
	}

	public void setTags(String tags) {
		this.tags = parseTags(tags);
	}

	public void setPropertyName(String propertyName) {
		if ("".equals(propertyName)) {
			this.propertyName = null;
		} else {
			this.propertyName = propertyName.toLowerCase();
		}
	}

	public void setLeftPropertyValue(String leftPropertyValue) {
		if ("".equals(leftPropertyValue))
			leftPropertyValue = null;
		this.leftPropertyValue = leftPropertyValue;
	}

	public void setRightPropertyValue(String rightPropertyValue) {
		if ("".equals(rightPropertyValue))
			rightPropertyValue = null;
		this.rightPropertyValue = rightPropertyValue;
	}

	public void setMediaTypeNegate(String mediaNegate) {
		this.mediaTypeNegate = !mediaNegate.equals("");
	}

	public String getCommentWords() {
		return commentWords;
	}

	public void setCommentWords(String commentWords) {
		if ("".equals(commentWords))
			commentWords = null;
		this.commentWords = commentWords;
	}

	public String getAssociationType() {
		return associationType;
	}

	public void setAssociationType(String associationType) {
		if ("".equals(associationType))
			associationType = null;
		this.associationType = associationType;
	}

	public String getAssociationDest() {
		return associationDest;
	}

	public void setAssociationDest(String associationDest) {
		if ("".equals(associationDest))
			associationDest = null;
		this.associationDest = associationDest;
	}

//	public String getContent() {
//		return this.content;
//	}
//
	public void setContent(String content) {
		this.content = content;
	}

	public Map<String, String> getCustomSearchValues() {
		return customSearchValues;
	}

	public void setCustomSearchValues(Map<String, String> customSearchValues) {
		this.customSearchValues = customSearchValues;
	}

	public String getMediaType() {

		return mediaType;
	}

	public void setMediaType(String mediaType) {
		if ("".equals(mediaType))
			mediaType = null;
		this.mediaType = MediaTypesUtils
				.getMimeTypeFromHumanReadableMediaType(mediaType);
	}

	public void setAuthorNameNegate(String authNegate) {
		this.authorNameNegate = !authNegate.equals("");
	}

	public void setUpdaterNameNegate(String updNegate) {
		this.updaterNameNegate = !updNegate.equals("");
	}

	public void setCreatedRangeNegate(String createdRange) {
		this.createdRangeNegate = !createdRange.equals("");
	}

	public void setUpdatedRangeNegate(String updatedRange) {
		this.updatedRangeNegate = !updatedRange.equals("");
	}

	public void setLeftOp(String leftOp) {
        if (leftPropertyValue != null) {
            if (leftOp.equals("gt")) {
                this.leftOp = ">";
            } else if (leftOp.equals("ge")){
                this.leftOp = ">=";
            }
        }
	}

	public void setRightOp(String rightOp) {
        if (rightPropertyValue != null) {
            if (rightOp.equals("lt")) {
                this.rightOp = "<";
            } else if (rightOp.equals("le")) {
                this.rightOp = "<=";
            } else if (rightOp.equals("eq")){
                this.rightOp = "=";
            }
            if(leftPropertyValue != null)
                propertyRange = true;
        }
	}

	private boolean queryExists(Registry configSystemRegistry, String queryPath)
			throws RegistryException {
		UserRegistry registry = (UserRegistry) configSystemRegistry;
		return registry.resourceExists(queryPath);
	}

	private void defineQueries(Registry configSystemRegistry, String queryPath,
			String queryContent) throws RegistryException {
		UserRegistry registry = (UserRegistry) configSystemRegistry;

		Resource q1 = registry.newResource();
		q1.setContent(queryContent);
		q1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
		q1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
				RegistryConstants.RESOURCES_RESULT_TYPE);
		registry.put(queryPath, q1);

	}

	public Set<String> parseTags(String tags) {
		Set<String> result = new HashSet<String>();

		String[] parts = tags.split("\\,");
		for (String part1 : parts) {
			String part = part1.trim();

			if (!"".equals(part)) {
				result.add(part.toLowerCase());
			}
		}

		return result;
	}

	// get sql queries to search resources

	private String generateSQLForResources() {
		ArrayList<String> tables = new ArrayList<String>();
		ArrayList<String> conditions = new ArrayList<String>();

		if (resourceName != null && resourceName.length() != 0) {
			conditions.add("lower(R.REG_NAME) LIKE ?");
		}

		if (authorName != null && authorName.length() != 0) {
			if (authorNameNegate)
				conditions.add("lower(R.REG_CREATOR) NOT LIKE ?");
			else
				conditions.add("lower(R.REG_CREATOR) LIKE ?");
		}

		if (updaterName != null && updaterName.length() != 0) {
			if (updaterNameNegate)
				conditions.add("lower(R.REG_LAST_UPDATOR) NOT LIKE ?");
			else
				conditions.add("lower(R.REG_LAST_UPDATOR) LIKE ?");
		}

		if (createdAfter > Long.MIN_VALUE) {
			if (createdRangeNegate) {
                if(!(createdBefore > Long.MIN_VALUE))
                    conditions.add("R.REG_CREATED_TIME < ?");
            } else {
				conditions.add("R.REG_CREATED_TIME > ?");
            }
		}

		if (createdBefore > Long.MIN_VALUE) {
			if (createdRangeNegate){
                if(!(createdAfter > Long.MIN_VALUE))
                    conditions.add("R.REG_CREATED_TIME > ?");
                else
                    conditions.add("R.REG_CREATED_TIME < ? OR R.REG_CREATED_TIME > ?");
            } else {
				conditions.add("R.REG_CREATED_TIME < ?");
            }
		}

		if (updatedAfter > Long.MIN_VALUE) {
            if (updatedRangeNegate) {
                if(!(updatedBefore > Long.MIN_VALUE))
                    conditions.add("R.REG_LAST_UPDATED_TIME < ?");
            } else {
                conditions.add("R.REG_LAST_UPDATED_TIME > ?");
            }
		}

		if (updatedBefore > Long.MIN_VALUE) {
            if (updatedRangeNegate){
                if(!(updatedAfter > Long.MIN_VALUE))
                    conditions.add("R.REG_LAST_UPDATED_TIME > ?");
                else
                    conditions.add("R.REG_LAST_UPDATED_TIME < ? OR R.REG_LAST_UPDATED_TIME > ?");
            } else {
                conditions.add("R.REG_LAST_UPDATED_TIME < ?");
            }
		}

		if (commentWords != null && commentWords.length() != 0) {
			tables.add(", REG_COMMENT C");
			tables.add(", REG_RESOURCE_COMMENT RC");
			if (StaticConfiguration.isVersioningComments()) {
				conditions
						.add("R.REG_VERSION=RC.REG_VERSION AND RC.REG_COMMENT_ID=C.REG_ID AND "
								+ "C.REG_COMMENT_TEXT LIKE ?");
			} else {
				conditions.add("R.REG_PATH_ID=RC.REG_PATH_ID AND "
						+ "((R.REG_NAME = RC.REG_RESOURCE_NAME)) AND "
						+ "RC.REG_COMMENT_ID=C.REG_ID AND "
						+ "C.REG_COMMENT_TEXT LIKE ?");
			}
		}

		if (tags != null && !tags.isEmpty()) {
			tables.add(", REG_TAG T");
			tables.add(", REG_RESOURCE_TAG RT");

			StringBuilder tagClause = new StringBuilder();
			if (StaticConfiguration.isVersioningTags()) {
				tagClause.append("R.REG_VERSION=RT.REG_VERSION AND "
						+ "RT.REG_TAG_ID=T.REG_ID ");
			} else {
				tagClause.append("R.REG_PATH_ID=RT.REG_PATH_ID AND "
						+ "((R.REG_NAME = RT.REG_RESOURCE_NAME)) AND "
						+ "RT.REG_TAG_ID=T.REG_ID ");
			}

			Iterator<String> i = tags.iterator();
			int count = 0;
			while (i.hasNext()) {
				count++;
				if (count == 1) {
					tagClause.append(" AND (lower(T.REG_TAG_NAME) LIKE ?");
				} else {
					tagClause.append(" OR lower(T.REG_TAG_NAME) LIKE ?");
				}
				i.next();
			}
			if (tags.size() > 0) {
				tagClause.append(")");
			}
			conditions.add(tagClause.toString());
		}
		boolean bool = false;
		if (customSearchValues.size() > 0) {
			for (Map.Entry<String, String> e : customSearchValues.entrySet()) {
				if (!e.getValue().trim().equals("")) {
					bool = true;
				}
			}
		}
        if (bool || (rightPropertyValue != null && rightOp.equals("=")) || propertyName != null) {
            tables.add(", REG_PROPERTY PP");
            tables.add(", REG_RESOURCE_PROPERTY RP");
            // StringBuffer propertyClause = new StringBuffer();
            if (StaticConfiguration.isVersioningProperties()) {
                conditions.add("R.REG_VERSION=RP.REG_VERSION AND "
                        + "RP.REG_PROPERTY_ID=PP.REG_ID");
            } else {
                conditions.add("R.REG_PATH_ID=RP.REG_PATH_ID AND "
                        + "((R.REG_NAME = RP.REG_RESOURCE_NAME)) AND "
                        + "RP.REG_PROPERTY_ID=PP.REG_ID");
            }

        }

        if (propertyName != null) {
            conditions.add(" lower(PP.REG_NAME) LIKE ? ");

            if (propertyRange) {
                conditions.add(" PP.REG_VALUE " + leftOp
                        + " ? AND PP.REG_VALUE " + rightOp + " ? ");

            } else if(rightPropertyValue != null){

                if(rightOp.equals("=")) {
                    conditions.add(" PP.REG_VALUE LIKE ? ");
                }else{
                    conditions.add(" PP.REG_VALUE " + rightOp + " ? ");
                }

            } else if(leftPropertyValue != null){
                conditions.add(" PP.REG_VALUE " + leftOp + " ? ");
            }
        } else if(rightPropertyValue != null && rightOp.equals("=")){
            conditions.add(" PP.REG_VALUE LIKE ? ");
        }

		if (mediaType != null) {
			if (mediaTypeNegate)
				conditions.add("R.REG_MEDIA_TYPE NOT LIKE ?");
			else
				conditions.add("R.REG_MEDIA_TYPE LIKE ?");
		}

		if (customSearchValues.size() > 0) {
			StringBuilder propertyClause = new StringBuilder();
			boolean firstTime = true;

			for (Map.Entry<String, String> e : customSearchValues.entrySet()) {
				if (!e.getValue().trim().equals("")) {
					if (firstTime) {
						propertyClause.append(" lower(PP.REG_NAME) LIKE \'")
								.append(e.getKey().toLowerCase()).append("\'")
								.append(" AND PP.REG_VALUE LIKE ?");
						firstTime = false;
					} else {
						propertyClause
								.append(" AND lower(PP.REG_NAME) LIKE \'")
								.append(e.getKey().toLowerCase()).append("\'")
								.append(" AND PP.REG_VALUE LIKE ?");
					}
				}
			}
			if (!(propertyClause.toString().equals(""))) {
				conditions.add(propertyClause.toString());
			}

		}

		/*
		 * if(associationType != null){ tables.add(", REG_ASSOCIATION RA");
		 * tables.add(", REG_PATH RPA");
		 * conditions.add(" R.REG_PATH_ID=RPA.REG_PATH_ID"); conditions.add(
		 * " (RA.REG_SOURCEPATH = (RPA.REG_PATH_VALUE || R.REG_NAME)) ");
		 * conditions.add(" RA.REG_ASSOCIATION_TYPE LIKE ?");
		 * 
		 * }
		 */
		/*
		 * if(associationDest != null){ if (associationType == null) {
		 * tables.add(", REG_ASSOCIATION RA"); tables.add(", REG_PATH RPA");
		 * conditions
		 * .add(" (RA.REG_SOURCEPATH = (RPA.REG_PATH_VALUE || R.REG_NAME))  ");
		 * } tables.add(", REG_PATH RPA2"); tables.add(", REG_RESOURCE R2");
		 * conditions.add(" R2.REG_PATH_ID=RPA2.REG_PATH_ID"); conditions.add(
		 * " (RA.REG_TARGETPATH = (RPA2.REG_PATH_VALUE || R2.REG_NAME)) ");
		 * conditions.add("lower(R2.REG_NAME) LIKE ?");
		 * 
		 * }
		 */

//        At this time we check whether the conditions are empty
//        This means that no fields have been filled. So there should be no results
        if(conditions.isEmpty()){
            return  null;
        }
//

		StringBuilder query = new StringBuilder();
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

		if (resourceName != null && resourceName.length() != 0) {
			tables.add(", REG_PATH P");
			conditions
					.add(" lower(P.REG_PATH_VALUE) LIKE ? " +
//                            "AND lower(P.REG_PATH_VALUE) NOT LIKE ? " +
                            "AND P.REG_PATH_ID=R.REG_PATH_ID");
		}

		if (authorName != null && authorName.length() != 0) {
			if (authorNameNegate)
				conditions.add("lower(R.REG_CREATOR) NOT LIKE ?");
			else
				conditions.add("lower(R.REG_CREATOR) LIKE ?");
		}

		if (updaterName != null && updaterName.length() != 0) {
			if (updaterNameNegate)
				conditions.add("lower(R.REG_LAST_UPDATOR) NOT LIKE ?");
			else
				conditions.add("lower(R.REG_LAST_UPDATOR) LIKE ?");
		}

        if (createdAfter > Long.MIN_VALUE) {
            if (createdRangeNegate) {
                if(!(createdBefore > Long.MIN_VALUE))
                    conditions.add("R.REG_CREATED_TIME < ?");
            } else {
                conditions.add("R.REG_CREATED_TIME > ?");
            }
        }

        if (createdBefore > Long.MIN_VALUE) {
            if (createdRangeNegate){
                if(!(createdAfter > Long.MIN_VALUE))
                    conditions.add("R.REG_CREATED_TIME > ?");
                else
                    conditions.add("R.REG_CREATED_TIME < ? OR R.REG_CREATED_TIME > ?");
            } else {
                conditions.add("R.REG_CREATED_TIME < ?");
            }
        }

        if (updatedAfter > Long.MIN_VALUE) {
            if (updatedRangeNegate) {
                if(!(updatedBefore > Long.MIN_VALUE))
                    conditions.add("R.REG_LAST_UPDATED_TIME < ?");
            } else {
                conditions.add("R.REG_LAST_UPDATED_TIME > ?");
            }
        }

        if (updatedBefore > Long.MIN_VALUE) {
            if (updatedRangeNegate){
                if(!(updatedAfter > Long.MIN_VALUE))
                    conditions.add("R.REG_LAST_UPDATED_TIME > ?");
                else
                    conditions.add("R.REG_LAST_UPDATED_TIME < ? OR R.REG_LAST_UPDATED_TIME > ?");
            } else {
                conditions.add("R.REG_LAST_UPDATED_TIME < ?");
            }
        }
		if (commentWords != null && commentWords.length() != 0) {
			tables.add(", REG_COMMENT C");
			tables.add(", REG_RESOURCE_COMMENT RC");
			if (StaticConfiguration.isVersioningComments()) {
				conditions
						.add("R.REG_VERSION=RC.REG_VERSION AND RC.REG_COMMENT_ID=C.REG_ID AND "
								+ "C.REG_COMMENT_TEXT LIKE ?");
			} else {
				conditions
						.add("R.REG_PATH_ID=RC.REG_PATH_ID AND "
								+ "R.REG_NAME IS NULL AND RC.REG_RESOURCE_NAME IS NULL AND "
								+ "RC.REG_COMMENT_ID=C.REG_ID AND "
								+ "C.REG_COMMENT_TEXT LIKE ?");
			}
		}

		if (tags != null && !tags.isEmpty()) {
			tables.add(", REG_TAG T");
			tables.add(", REG_RESOURCE_TAG RT");

			StringBuilder tagClause = new StringBuilder();
			if (StaticConfiguration.isVersioningTags()) {
				tagClause.append("R.REG_VERSION=RT.REG_VERSION AND "
						+ "RT.REG_TAG_ID=T.REG_ID ");
			} else {
				tagClause
						.append("R.REG_PATH_ID=RT.REG_PATH_ID AND "
								+ "R.REG_NAME IS NULL AND RT.REG_RESOURCE_NAME IS NULL AND "
								+ "RT.REG_TAG_ID=T.REG_ID ");
			}

			Iterator<String> i = tags.iterator();
			int count = 0;
			while (i.hasNext()) {
				count++;
				if (count == 1) {
					tagClause.append(" AND (lower(T.REG_TAG_NAME) LIKE ?");
				} else {
					tagClause.append(" OR lower(T.REG_TAG_NAME) LIKE ?");
				}
				i.next();
			}
			if (tags.size() > 0) {
				tagClause.append(")");
			}
			conditions.add(tagClause.toString());
		}
		boolean bool = false;

		if (customSearchValues.size() > 0) {

			for (Map.Entry<String, String> e : customSearchValues.entrySet()) {
				if (!e.getValue().trim().equals("")) {
					bool = true;
				}
			}
		}
        if (bool || (rightPropertyValue != null && rightOp.equals("=")) || propertyName != null) {
            tables.add(", REG_PROPERTY PP");
            tables.add(", REG_RESOURCE_PROPERTY RP");
            if (StaticConfiguration.isVersioningProperties()) {
                conditions.add("R.REG_VERSION=RP.REG_VERSION AND "
                        + "RP.REG_PROPERTY_ID=PP.REG_ID");
            } else {
                conditions
                        .add("R.REG_PATH_ID=RP.REG_PATH_ID AND "
                                + "R.REG_NAME IS NULL AND RP.REG_RESOURCE_NAME IS NULL AND "
                                + "RP.REG_PROPERTY_ID=PP.REG_ID");
            }
        }

        if (propertyName != null) {
            conditions.add(" lower(PP.REG_NAME) LIKE ? ");

            if (propertyRange) {
                conditions.add(" PP.REG_VALUE " + leftOp
                        + " ? AND PP.REG_VALUE " + rightOp + " ? ");

            } else if(leftPropertyValue != null){

                if(leftOp.equals("=")) {
                    conditions.add(" PP.REG_VALUE LIKE ? ");
                }else{
                    conditions.add(" PP.REG_VALUE " + leftOp + " ? ");
                }

            } else if(rightPropertyValue != null){
                conditions.add(" PP.REG_VALUE " + rightOp + " ? ");
            }
        } else if(rightPropertyValue != null && rightOp.equals("=")){
            conditions.add(" PP.REG_VALUE LIKE ? ");
        }

		if (mediaType != null) {
			if (mediaTypeNegate)
				conditions.add("R.REG_MEDIA_TYPE NOT LIKE ?");
			else
				conditions.add("R.REG_MEDIA_TYPE LIKE ?");
		}
		if (customSearchValues.size() > 0) {
			StringBuilder propertyClause = new StringBuilder();
			boolean firstTime = true;

			for (Map.Entry<String, String> e : customSearchValues.entrySet()) {
				if (!e.getValue().trim().equals("")) {
					if (firstTime) {
						propertyClause.append(" lower(PP.REG_NAME) LIKE \'")
								.append(e.getKey().toLowerCase()).append("\'")
								.append(" AND PP.REG_VALUE LIKE ?");
						firstTime = false;
					} else {
						propertyClause
								.append(" AND lower(PP.REG_NAME) LIKE \'")
								.append(e.getKey().toLowerCase()).append("\'")
								.append(" AND PP.REG_VALUE LIKE ?");
					}
				}

			}
			if (!(propertyClause.toString().equals(""))) {
				conditions.add(propertyClause.toString());
			}

		}

		if (associationType != null) {
			tables.add(", REG_ASSOCIATION RA");
			if (resourceName == null) {
				tables.add(", REG_PATH P");
			}
			conditions.add(" R.REG_PATH_ID=P.REG_PATH_ID");
			conditions.add(" RA.REG_SOURCEPATH = P.REG_PATH_VALUE");
			conditions.add(" RA.REG_ASSOCIATION_TYPE LIKE ?");

		}
		if (associationDest != null) {

			if (associationType == null) {
				tables.add(", REG_ASSOCIATION RA");
				if (resourceName == null) {
					tables.add(", REG_PATH P");
				}

			}
			tables.add(", REG_PATH P2");
			conditions.add(" R.REG_PATH_ID=P.REG_PATH_ID");
			conditions.add(" RA.REG_TARGETPATH = P2.REG_PATH_VALUE ");
			conditions.add(" lower(P2.REG_PATH_VALUE) LIKE ?");
			conditions.add(" lower(P2.REG_PATH_VALUE) NOT LIKE ?");
			conditions.add(" RA.REG_SOURCEPATH = P.REG_PATH_VALUE");

		}

		if (conditions.isEmpty()) {
			return null;
		}
        conditions.add("R.REG_NAME IS NULL");

		StringBuilder query = new StringBuilder();
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
