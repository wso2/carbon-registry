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
package org.wso2.carbon.registry.indexing.indexer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.indexing.IndexingManager;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;
import org.wso2.carbon.registry.indexing.solr.SolrClient;
import org.wso2.carbon.registry.indexing.utils.IndexingUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * IndexDocumentCreator class is responsible for create the IndexDocument for solr server.
 */
public class IndexDocumentCreator {

    // Instance of file2Index
    private final File2Index file2Index;
    // Instance of indexer
    private Indexer indexer;
    private boolean isMediaTypeSet = false;
    private String resourcePath = null;
    private UserRegistry registry;
    private Resource resource = null;
    private static final Log log = LogFactory.getLog(IndexDocumentCreator.class);
    // Indexing fields attribute Map
    private Map<String, List<String>> attributes = new HashMap<String, List<String>>();

    public IndexDocumentCreator(File2Index file2Index, Resource resource) {
        this.file2Index = file2Index;
        this.resource = resource;
        // Get the user registry
        this.registry = IndexingManager.getInstance().getRegistry(file2Index.tenantId);
        // Set the path of the resource
        resourcePath = file2Index.path;
    }

    /**
     *  Method to create the IndexDocument with all attributes need in advance search
     *  1. Get the IndexDocument defined for the mediaType
     *  2. Then update the document with attributes need for the advance search
     *  3. If mediaType is null create the IndexDocument with attributes need for advance search
     * @throws RegistryException
     */
    public void createIndexDocument() throws RegistryException, IndexerException {
        file2Index.lcName = resource.getProperty("registry.LC.name");
        file2Index.lcState = file2Index.lcName != null ? resource.getProperty("registry.lifecycle." + file2Index.lcName + ".state") : null;
        file2Index.mediaType = resource.getMediaType();
        // Check for resources that can get the byte content
        if (!(resource instanceof Collection)
                || IndexingManager.getInstance().getIndexerForMediaType(file2Index.mediaType) != null) {
            file2Index.data = IndexingUtils.getByteContent(resource, file2Index.sourceURL);
        }
        // Get the indexDocument
        IndexDocument indexDocument = getIndexDocument();
        // Get the index fields
        if (indexDocument.getFields() != null) {
            attributes = indexDocument.getFields();
        }

        // Set the resource name to attribute list
        addResourceName();
        // Set the author of the resource to the attribute list
        addAuthor();
        // Set the last updater of the resource to the attribute list
        addLastUpdateUser();
        // Set the created date of the resource to the attribute list
        addCreatedDate();
        // Set the last updated date of the resource to the attribute list
        addLastUpdatedDate();
        // Set the last mediaType of the resource to the attribute list
        addMediaType();
        if (!(resource instanceof Collection)) {
            // Set Comments of the resource to the attribute list
            addComments();
            // Set Tags of the resource to the attribute list
            addTags();
            // Set Association types and destinations of the resource to the attribute list
            addAssociations();
        }
        // Set Property names and values of the resource to the attribute list
        addPropertyData();
        // Set the attribute fields.
        indexDocument.setFields(attributes);
        // Set the tenant id.
        indexDocument.setTenantId(file2Index.tenantId);
        // Add the document to solr server.
        SolrClient.getInstance().addDocument(indexDocument);

    }

    /**
     * Method to set the resource Property names and values to IndexDocument attribute list.
     */
    private void addPropertyData() {
        // Get the property values of the resource
        Properties properties = resource.getProperties();
        // Get the property key set
        Set keySet = properties.keySet();
        List<String> propertyList = new ArrayList<String>();
        String propertyKey;
        if (keySet.size() > 0) {
            Object[] propertyKeys = keySet.toArray();
            List values;
            for (Object key : propertyKeys) {
                propertyKey = key.toString();
                values = (List) properties.get(key);
                String propertyValue = "";
                if (values != null) {
                    for (Object value : values) {
                        propertyValue += value + ",";
                    }
                } else {
                    propertyValue = ",";
                }
                propertyList.add(propertyKey + "," + propertyValue);
            }
        }
        if (propertyList.size() > 0) {
            attributes.put(IndexingConstants.FIELD_PROPERTY_VALUES, propertyList);
        }
    }

    /**
     *  Method to set the resource Association types and destinations to IndexDocument attribute list.
     */
    private void addAssociations() throws RegistryException {
        // Add resource association types and destinations
        Association[] associations;
        try {
            associations = registry.getAllAssociations(resourcePath);
        } catch (RegistryException e) {
            String message = "Error at IndexDocumentCreator when getting Registry Associations.";
            log.error(message, e);
            throw new RegistryException(message, e);
        }
        List<String> associationTypeList = new ArrayList<>();
        List<String> associationDestinationList = new ArrayList<>();
        if (associations != null && associations.length > 0) {
            for (Association association : associations) {
                associationTypeList.add(association.getAssociationType());
                associationDestinationList.add(association.getDestinationPath());
            }
            if (associationTypeList.size() > 0) {
                attributes.put(IndexingConstants.FIELD_ASSOCIATION_TYPES, associationTypeList);
            }
            if (associationDestinationList.size() > 0) {
                attributes.put(IndexingConstants.FIELD_ASSOCIATION_DESTINATIONS, associationDestinationList);
            }
        }
    }

    /**
     *  Method to set the resource Tags to IndexDocument attribute list.
     */
    private void addTags() throws RegistryException {
        // Add resource tags
        Tag[] tags;
        tags = registry.getTags(resourcePath);
        List<String> tagList = new ArrayList<>();
        if (tags != null && tags.length > 0) {
            for (Tag tag : tags) {
                tagList.add(tag.getTagName());
            }
            if (tagList.size() > 0) {
                attributes.put(IndexingConstants.FIELD_TAGS, tagList);
            }
        }
    }

    /**
     *  Method to set the resource comments to IndexDocument attribute list.
     */
    private void addComments() throws RegistryException {
        // Add resource comments
        Comment[] comments;
        try {
            comments = registry.getComments(resourcePath);
        } catch (RegistryException e) {
            String message = "Error at IndexDocumentCreator when getting Registry Comment.";
            log.error(message, e);
            throw new RegistryException(message, e);
        }
        List<String> commentList = new ArrayList<>();
        if (comments != null && comments.length > 0) {
            for (Comment comment : comments) {
                commentList.add(comment.getText());
            }
            if (commentList.size() > 0) {
                attributes.put(IndexingConstants.FIELD_COMMENTS, commentList);
            }
        }
    }

    /**
     *  Method to set the resource mediaType to IndexDocument attribute list.
     */
    private void addMediaType() {
        // Check whether the media type is added by the mediaType Indexer
        for (Map.Entry<String, List<String>> mediaTypeList : attributes.entrySet()) {
            if (mediaTypeList.getKey().equals(IndexingConstants.FIELD_MEDIA_TYPE)) {
                isMediaTypeSet = true;
                break;
            }
        }
        // Set the mediaType
        if (!isMediaTypeSet) {
            attributes.put(IndexingConstants.FIELD_MEDIA_TYPE, Arrays.asList(file2Index.mediaType));
        }
    }

    /**
     *  Method to set the resource last updated date to IndexDocument attribute list.
     */
    private void addLastUpdatedDate() {
        // Set the last updated date
        Date updatedDate = resource.getLastModified();
        if (updatedDate != null) {
            attributes.put(IndexingConstants.FIELD_LAST_UPDATED_DATE, Arrays.asList(updatedDate.toString()));
        }
    }

    /**
     *  Method to set the resource created date to IndexDocument attribute list.
     */
    private void addCreatedDate() {
        // Set the created date
        Date createdDate = resource.getCreatedTime();
        if (createdDate != null) {
            attributes.put(IndexingConstants.FIELD_CREATED_DATE, Arrays.asList(createdDate.toString()));
        }
    }

    /**
     *  Method to set the resource last updater to IndexDocument attribute list.
     */
    private void addLastUpdateUser() {
        // Set the last update user
        String lastUpdatedBy = resource.getLastUpdaterUserName();
        if (lastUpdatedBy != null && !StringUtils.isEmpty(lastUpdatedBy)) {
            attributes.put(IndexingConstants.FIELD_LAST_UPDATED_BY, Arrays.asList(lastUpdatedBy));
        }
    }

    /**
     *  Method to set the resource author to IndexDocument attribute list.
     */
    private void addAuthor() {
        // Set the author of the resource
        String createdBy = resource.getAuthorUserName();
        if (createdBy != null && !StringUtils.isEmpty(createdBy)) {
            attributes.put(IndexingConstants.FIELD_CREATED_BY, Arrays.asList(createdBy));
        }
    }

    /**
     *  Method to set the resource name to IndexDocument attribute list.
     */
    private void addResourceName() {
        // Set the resource name
        String resourceName = RegistryUtils.getResourceName(resourcePath);
        if (StringUtils.isNotEmpty(resourceName)) {
            attributes.put(IndexingConstants.FIELD_RESOURCE_NAME, Arrays.asList(resourceName));
        }
    }

    /**
     *  Method to get the IndexDocument written for the media type.
     * @return IndexDocument
     * @throws RegistryException
     */
    private IndexDocument getIndexDocument() throws RegistryException {
        IndexDocument indexDocument;
        // Get the Indexer if available
        if (file2Index.mediaType != null) {
            // Get the indexer define for the media type
            indexer = IndexingManager.getInstance().getIndexerForMediaType(file2Index.mediaType);
        }
        if (indexer != null) {
            try {
                // Get the index document from pre-defined Indexer.
                indexDocument = indexer.getIndexedDocument(file2Index);
            } catch (RegistryException e) {
                String message = "Error at IndexDocumentCreator when getting IndexDocument for mediaType.";
                log.error(message, e);
                throw new RegistryException(message, e);
            }
        } else {
            indexDocument = new IndexDocument();
        }
        // Set path in index document
        indexDocument.setPath(file2Index.path);
        return indexDocument;
    }

}
