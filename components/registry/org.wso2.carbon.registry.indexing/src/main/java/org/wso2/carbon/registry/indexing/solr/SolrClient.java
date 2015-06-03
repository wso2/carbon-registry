/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.indexing.solr;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.pagination.PaginationUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.indexing.RegistryConfigLoader;
import org.wso2.carbon.registry.indexing.SolrConstants;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;

public class SolrClient {

    public static final Log log = LogFactory.getLog(SolrClient.class);

    private static volatile SolrClient instance;
    private org.apache.solr.client.solrj.SolrClient server;
    private Map<String, String> filePathMap = new HashMap<String, String>();
    // solr home directory path
    private static final String SOLR_HOME_FILE_PATH = CarbonUtils.getCarbonConfigDirPath() + File.separator + "solr";
    private File solrHome, confDir, langDir;
    private String solrCore = null;

    protected SolrClient() throws IOException {
        // Get the solr server url from the registry.xml
        RegistryConfigLoader configLoader = RegistryConfigLoader.getInstance();
        String solrServerUrl = configLoader.getSolrServerUrl();

        // Default solr core is set to registry-indexing
        solrCore = IndexingConstants.DEFAULT_SOLR_SERVER_CORE;
        if (log.isDebugEnabled()) {
            log.debug("Solr server core is set as: " + solrCore);
        }

        // Create the solr home path defined in SOLR_HOME_FILE_PATH : carbon_home/repository/conf/solr
        solrHome = new File(SOLR_HOME_FILE_PATH);
        if (!solrHome.exists() && !solrHome.mkdirs()) {
            throw new IOException("Solr home directory could not be created. path: " + solrHome);
        }

        // Create the configuration folder inside solr core : carbon_home/repository/conf/solr/<solrCore>/conf
        confDir = new File(solrHome, solrCore + File.separator + "conf");
        if (!confDir.exists() && !confDir.mkdirs()) {
            throw new IOException("Solr conf directory could not be created! Path: " + confDir);
        }

        // Create lang directory inside conf to store language specific stop words
        // commons-io --> file utils
        langDir = new File(confDir, "lang");
        if (!langDir.exists() && !langDir.mkdirs()) {
            throw new IOException("Solf lang directory could not be created! Path: " + langDir);
        }

        // Read the configuration file name and there destination path and stored in filePathMap
        readConfigurationFilePaths();
        // Read the content of the files in filePathMap and copy them into destination path
        copyConfigurationFiles();
        // Set the solr home path
        System.setProperty(SolrConstants.SOLR_HOME_SYSTEM_PROPERTY, solrHome.getPath());

        if (solrServerUrl != null && !solrServerUrl.isEmpty()) {
            this.server = new HttpSolrClient(solrServerUrl);
            log.info("Http Sorl server initiated at: " + solrServerUrl);
        } else {
            CoreContainer coreContainer = new CoreContainer(solrHome.getPath());
            coreContainer.load();
            this.server = new EmbeddedSolrServer(coreContainer, solrCore);
            log.info("Default Embedded Solr Server Initialized");
        }
    }

    public static SolrClient getInstance() throws IndexerException {
        if (instance == null) {
            synchronized (SolrClient.class) {
                if (instance == null) {
                    try {
                        instance = new SolrClient();
                    } catch (IOException e) {
                        log.error("Could not instantiate Solr client", e);
                        throw new IndexerException("Could not instantiate Solr client", e);
                    }
                }
            }
        }
        return instance;
    }

    /**
     * Reads sourceFilePath and destFilePath from solr_configuration_files.properties file
     * e.g: protwords.txt = home/core/conf
     * protword.txt is the resource file name
     * home/core/conf is destination file path, this will go to solr-home/<solr-core>/conf directory.
     * @throws IOException
     */
    private void readConfigurationFilePaths() throws IOException {
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = getClass().getClassLoader()
                    .getResourceAsStream(SolrConstants.SOLR_CONFIG_FILES_CONTAINER);
            Properties fileProperties = new Properties();
            fileProperties.load(resourceAsStream);

            for (Entry<Object, Object> entry : fileProperties.entrySet()) {
                if (entry.getValue() != null) {
                    String[] fileNames = entry.getValue().toString().split(",");
                    for (String fileName : fileNames) {
                        filePathMap.put(fileName, (String) entry.getKey());
                    }
                }
            }
        } finally {
            if (resourceAsStream != null) {
                resourceAsStream.close();
            }
        }
    }

    /**
     * Copy solr configuration files in resource folder to solr home folder.
     * @throws IOException
     */
    private void copyConfigurationFiles() throws IOException {
        for (Entry<String, String> entry : filePathMap.entrySet()) {
            String sourceFileName = entry.getKey();
            String fileDestinationPath = entry.getValue();
            File file;

            if (SolrConstants.SOLR_HOME.equals(fileDestinationPath)) {
                file = new File(solrHome, sourceFileName);
            } else if (SolrConstants.SOLR_CORE.equals(fileDestinationPath)) {
                file = new File(confDir.getParentFile(), sourceFileName);
            } else if (SolrConstants.SOLR_CONF_LANG.equals(fileDestinationPath)) {
                file = new File(langDir, sourceFileName);
            } else {
                file = new File(confDir, sourceFileName);
            }

            if (!file.exists()) {
                write2File(sourceFileName, file);
            }
        }
    }

    private void write2File(String sourceFileName, File dest) throws IOException {
        byte[] buf = new byte[1024];
        InputStream in = null;
        OutputStream out = null;
        try {
            in = getClass().getClassLoader().getResourceAsStream(sourceFileName);
            out = new FileOutputStream(dest);

            if (SolrConstants.CORE_PROPERTIES.equals(sourceFileName)) {
                Properties coreProperties = new Properties();
                coreProperties.load(in);
                coreProperties.setProperty("name", solrCore);
                coreProperties.store(out, null);
            } else {
                int read;
                while ((read = in.read(buf)) >= 0) {
                    out.write(buf, 0, read);
                }
            }

        } finally {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Method to generate the solr document id
     * @param tenantId tenant id
     * @param path resource path
     * @return generated document id
     */
    private String generateId(int tenantId, String path) {
        return path + IndexingConstants.FIELD_TENANT_ID + tenantId;
    }

    /**
     * Method dedicated for add IndexDocument to solr server
     * @param indexDoc IndexDocument
     * @throws SolrException
     */
    public void addDocument(IndexDocument indexDoc) throws SolrException {
        try {
            // Get resource path
            String path = indexDoc.getPath();
            // Get resource content
            String rawContent = indexDoc.getRawContent();
            // Get resource content as text
            String contentAsText = indexDoc.getContentAsText();
            // Get tenant id
            int tenantId = indexDoc.getTenantId();
            // Get the attribute fields in the IndexDocument
            Map<String, List<String>> fields = indexDoc.getFields();
            // To ease the debugging
            if (log.isDebugEnabled()) {
                log.debug("Indexing Document in resource path: " + path);
            }
            SolrInputDocument solrInputDocument = new SolrInputDocument();
            // Add field id
            addFieldID(tenantId, path, rawContent, solrInputDocument);
            // Add field raw content
            addRawContent(rawContent, solrInputDocument);
            // Add field tenant id
            addTenantId(tenantId, solrInputDocument);
            // Add field content as text
            addContentAsText(contentAsText, solrInputDocument);
            // Add advance search related dynamic fields
            addDynamicFields(fields, solrInputDocument);
            // Add solr input document to server
            server.add(solrInputDocument);
        } catch (SolrServerException e) {
            String message = "Error at indexing.";
            throw new SolrException(ErrorCode.SERVER_ERROR, message, e);
        } catch (IOException e) {
            String message = "Error at indexing.";
            throw new SolrException(ErrorCode.SERVER_ERROR, message, e);
        }
    }

    /**
     * Method for add dynamic fields of the resource
     * @param fields dynamic fields need to index
     * @param solrInputDocument Solr InputDocument
     */
    private void addDynamicFields(Map<String, List<String>> fields, SolrInputDocument solrInputDocument) {
        // Add advance search related dynamic fields.
        if (fields != null && fields.size() > 0) {
            String fieldKey;
            for (Map.Entry<String, List<String>> fieldList : fields.entrySet()) {
                // Add multivalued attributes.
                if (fieldList.getKey().equals(IndexingConstants.FIELD_PROPERTY_VALUES) || fieldList.getKey()
                        .equals(IndexingConstants.FIELD_ASSOCIATION_DESTINATIONS) || fieldList.getKey()
                        .equals(IndexingConstants.FIELD_ASSOCIATION_TYPES) || fieldList.getKey()
                        .equals(IndexingConstants.FIELD_COMMENTS) || fieldList.getKey()
                        .equals(IndexingConstants.FIELD_TAGS)) {
                    if (fieldList.getKey().equals(IndexingConstants.FIELD_PROPERTY_VALUES)) {
                        for (String value : fieldList.getValue()) {
                            String[] propertyValArray = value.split(",");
                            fieldKey = propertyValArray[0];
                            String [] propValues = Arrays.copyOfRange(propertyValArray, 1, propertyValArray.length);
                            addPropertyField(fieldKey, propValues, solrInputDocument);
                        }
                    } else {
                        fieldKey = fieldList.getKey() + SolrConstants.SOLR_MULTIVALUED_STRING_FIELD_KEY_SUFFIX;
                        for (String value : fieldList.getValue()) {
                            solrInputDocument.addField(fieldKey, value);
                        }
                    }

                } else {
                    // Add date fields
                    if (fieldList.getKey().equals(IndexingConstants.FIELD_CREATED_DATE) || fieldList.getKey()
                            .equals(IndexingConstants.FIELD_LAST_UPDATED_DATE)) {
                        fieldKey = fieldList.getKey() + SolrConstants.SOLR_DATE_FIELD_KEY_SUFFIX;
                        String date = toSolrDateFormat(fieldList.getValue().get(0), SolrConstants.REG_LOG_DATE_FORMAT);
                        if (date != null) {
                            // Add date attributes
                            solrInputDocument.addField(fieldKey, date);
                        }
                    } else {
                        String fieldKeyValue;
                        if (fieldList.getKey().equals(IndexingConstants.FIELD_RESOURCE_NAME)) {
                            fieldKeyValue = IndexingConstants.FIELD_RESOURCE_NAME;
                        } else {
                            fieldKeyValue = fieldList.getKey();
                        }
                        // Add single field String values
                        solrInputDocument
                                .addField(fieldKeyValue + SolrConstants.SOLR_STRING_FIELD_KEY_SUFFIX,
                                        fieldList.getValue().get(0));
                    }
                }
            }
        }
    }

    /**
     * Method to add property values
     * @param fieldKey property field key value
     * @param values property field value
     * @param solrInputDocument Solr InputDocument
     */
    private void addPropertyField(String fieldKey, String[] values, SolrInputDocument solrInputDocument) {
        int intValue;
        double doubleValue;
        // Check whether the value is an Int or decimal or string
        String valueType = getType(values[0]);
        for (String propValue : values) {
            switch (valueType) {
                case SolrConstants.TYPE_INT:
                    intValue = Integer.parseInt(propValue);
                    solrInputDocument.addField(fieldKey + SolrConstants.SOLR_MULTIVALUED_INT_FIELD_KEY_SUFFIX, intValue);
                    break;
                case SolrConstants.TYPE_DOUBLE:
                    doubleValue = Double.parseDouble(propValue);
                    solrInputDocument.addField(fieldKey + SolrConstants.SOLR_MULTIVALUED_DOUBLE_FIELD_KEY_SUFFIX, doubleValue);
                    break;
                case SolrConstants.TYPE_STRING:
                    solrInputDocument.addField(fieldKey + SolrConstants.SOLR_MULTIVALUED_STRING_FIELD_KEY_SUFFIX, propValue);
                    break;
            }
        }
    }

    /**
     * Method to identify the type of the given string. Match a number with optional '-' and decimal.
     * @param value String value
     * @return type of the value
     */
    private String getType(String value) {
        String type;
        Matcher intMatcher = SolrConstants.INT_PATTERN.matcher(value);
        Matcher doubleMatcher = SolrConstants.DOUBLE_PATTERN.matcher(value);
        if (intMatcher.matches()) {
            type = SolrConstants.TYPE_INT;
        } else if (doubleMatcher.matches()) {
            type = SolrConstants.TYPE_DOUBLE;
        } else {
            type = SolrConstants.TYPE_STRING;
        }
        return type;
    }

    /**
     * Method for add content of the resource
     * @param contentAsText content of the resource
     * @param solrInputDocument Solr InputDocument
     */
    private void addContentAsText(String contentAsText, SolrInputDocument solrInputDocument) {
        if (contentAsText != null && contentAsText.length() > 0) {
            solrInputDocument.addField(IndexingConstants.FIELD_CONTENT_ONLY, contentAsText);
        }
    }

    /**
     * Method for add tenant id of the resource
     * @param tenantId tenant id
     * @param solrInputDocument Solr InputDocument
     */
    private void addTenantId(int tenantId, SolrInputDocument solrInputDocument) {
        solrInputDocument.addField(IndexingConstants.FIELD_TENANT_ID, String.valueOf(tenantId));
    }

    /**
     * Method for add raw content of the resource
     * @param rawContent raw content
     * @param solrInputDocument Solr InputDocument
     */
    private void addRawContent(String rawContent, SolrInputDocument solrInputDocument) {
        if (rawContent != null && StringUtils.isNotEmpty(rawContent)) {
            solrInputDocument.addField(IndexingConstants.FIELD_TEXT, rawContent, 1.0f);
        }
    }

    /**
     * Method for add Solr document id
     * @param tenantId tenant id
     * @param path resource path
     * @param rawContent raw content
     * @param solrInputDocument solr InputDocument
     */
    private void addFieldID(int tenantId, String path, String rawContent, SolrInputDocument solrInputDocument) {
        // Generate the solr Document id
        String id = generateId(tenantId, path);
        if (id == null) {
            id = IndexingConstants.FIELD_ID + rawContent.hashCode();
        }
        solrInputDocument.addField(IndexingConstants.FIELD_ID, id, 1.0f);
    }

    /**
     * Method to get Solr generic date formatter
     * @param dateStr date value
     * @param currentFormat date format
     * @return solr date format
     */
    private String toSolrDateFormat(String dateStr, String currentFormat) {
        String solrDateFormatResult = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(currentFormat);
            Date date = sdf.parse(dateStr);
            sdf.applyPattern(SolrConstants.SOLR_DATE_FORMAT);
            solrDateFormatResult = sdf.format(date);
        } catch (ParseException e) {
            log.error("Error when passing date to create solr date format." + e);
        }
        return solrDateFormatResult;
    }

    public void indexDocument(AsyncIndexer.File2Index fileData, Indexer indexer) throws RegistryException {
        IndexDocument doc = indexer.getIndexedDocument(fileData);
        doc.setTenantId(fileData.tenantId);
        addDocument(doc);
    }

    public synchronized void deleteFromIndex(String path, int tenantId) throws SolrException {
        try {
            String id = generateId(tenantId, path);
            server.deleteById(id);
            if (log.isDebugEnabled()) {
                log.debug("Solr delete index path: " + path + " id: " + id);
            }
        } catch (SolrServerException e) {
            // Throw unchecked exception: SolrException, this will throw when there is an error in connection.
            throw new SolrException(ErrorCode.SERVER_ERROR, "Failure at deleting", e);
        } catch (IOException e) {
            // Throw unchecked exception: SolrException, this will throw when there is an error in connection.
            throw new SolrException(ErrorCode.SERVER_ERROR, "Failure at deleting", e);
        }
    }

    public SolrDocumentList query(String keywords, int tenantId) throws SolrException {
        return query(keywords, tenantId, Collections.<String, String>emptyMap());
    }

    public SolrDocumentList query(int tenantId, Map<String, String> fields) throws SolrException {
        return query("[* TO *]", tenantId, fields);
    }

    /**
     * Method to create the solr query for indexing.
     * @param keywords content search keyword.
     * @param tenantId tenant id.
     * @param fields Dynamic fields attribute list.
     * @return query response result.
     * @throws SolrException
     */
    public SolrDocumentList query(String keywords, int tenantId, Map<String, String> fields) throws SolrException {
        try {
            SolrQuery query;
            // Get the attribute value for content
            String contentAttribute = fields.get(IndexingConstants.FIELD_CONTENT);
            if (contentAttribute != null && StringUtils.isNotEmpty(contentAttribute)) {
                // Check for '&&' and replace with AND, Check for ' ' and replace with OR
                query = new SolrQuery(contentAttribute.replaceAll(" ", " OR ").replaceAll("&&", " AND "));
                fields.remove(IndexingConstants.FIELD_CONTENT);
            } else if (keywords.equals("[* TO *]")) {
                query = new SolrQuery("* TO *");
            } else {
                query = new SolrQuery(keywords);
            }

            // Set no of rows
            query.setRows(Integer.MAX_VALUE);
            // Solr does not allow to search with special characters ,therefore this fix allow
            // to contain "-" in super tenant id.
            if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                query.addFilterQuery(IndexingConstants.FIELD_TENANT_ID + ":" + "\\" + tenantId);
            } else {
                query.addFilterQuery(IndexingConstants.FIELD_TENANT_ID + ":" + tenantId);
            }
            if (fields.get(IndexingConstants.FIELD_MEDIA_TYPE) != null) {
                // This is for fixing  REGISTRY-1695, This is temporary solution until
                // the default security polices also stored in Governance registry.
                if (fields.get(IndexingConstants.FIELD_MEDIA_TYPE).equals(
                        RegistryConstants.POLICY_MEDIA_TYPE)) {
                    query.addFilterQuery(IndexingConstants.FIELD_ID + ":" +
                            SolrConstants.GOVERNANCE_REGISTRY_BASE_PATH + "*");

                }
            }
            // Add query filters
            addQueryFilters(fields, query);
            QueryResponse queryresponse;
            MessageContext messageContext = MessageContext.getCurrentMessageContext();
            if ((messageContext != null && PaginationUtils.isPaginationHeadersExist(messageContext))
                    || PaginationContext.getInstance() != null) {
                try {
                    PaginationContext paginationContext;
                    if (messageContext != null) {
                        paginationContext = PaginationUtils.initPaginationContext(messageContext);
                    } else {
                        paginationContext = PaginationContext.getInstance();
                    }
                    // TODO: Proper mechanism once authroizations are fixed - senaka
                    // query.setStart(paginationContext.getStart());
                    // query.setRows(paginationContext.getCount());

                    String sortBy = paginationContext.getSortBy();
                    if (sortBy.length() > 0) {
                        query.setSort(sortBy + "_s",
                                paginationContext.getSortOrder().equals("ASC") ?
                                        SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
                    }
                    queryresponse = server.query(query);
                    if (log.isDebugEnabled()) {
                        log.debug("Solr index queried query: " + query);
                    }
                    // TODO: Proper mechanism once authroizations are fixed - senaka
                    // PaginationUtils.setRowCount(messageContext,
                    // Long.toString(queryresponse.getResults().getNumFound()));
                } finally {
                    if (messageContext != null) {
                        PaginationContext.destroy();
                    }
                }
            } else {
                queryresponse = server.query(query);
                if (log.isDebugEnabled()) {
                    log.debug("Solr index queried query: " + query);
                }
            }
            return queryresponse.getResults();
        } catch (SolrServerException e) {
            String message = "Failure at query ";
            throw new SolrException(ErrorCode.SERVER_ERROR, message + keywords, e);
        }
    }

    public List<FacetField.Count> facetQuery(int tenantId, Map<String, String> fields) throws SolrException {
        String facetField = null;
        try {
            SolrQuery query = new SolrQuery("* TO *");
            // Set no of rows
            query.setRows(Integer.MAX_VALUE);
            // Solr does not allow to search with special characters ,therefore this fix allow
            // to contain "-" in super tenant id.
            if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                query.addFilterQuery(IndexingConstants.FIELD_TENANT_ID + ":" + "\\" + tenantId);
            } else {
                query.addFilterQuery(IndexingConstants.FIELD_TENANT_ID + ":" + tenantId);
            }
            if (fields.get(IndexingConstants.FIELD_MEDIA_TYPE) != null) {
                // This is for fixing  REGISTRY-1695, This is temporary solution until
                // the default security polices also stored in Governance registry.
                if (fields.get(IndexingConstants.FIELD_MEDIA_TYPE).equals(
                        RegistryConstants.POLICY_MEDIA_TYPE)) {
                    query.addFilterQuery(IndexingConstants.FIELD_ID + ":" +
                            SolrConstants.GOVERNANCE_REGISTRY_BASE_PATH + "*");

                }
            }
            // Add facet fields
            facetField = addFacetFields(fields, query);
            // Add query filters
            addQueryFilters(fields, query);
            if (log.isDebugEnabled()) {
                log.debug("Solr index faceted query: " + query);
            }

            QueryResponse queryresponse = server.query(query);
            return queryresponse.getFacetField(facetField).getValues();

        } catch (SolrServerException e) {
            String message = "Failure at query ";
            throw new SolrException(ErrorCode.SERVER_ERROR, message + facetField, e);
        }
    }

    private String addFacetFields(Map<String, String> fields, SolrQuery query) {
        //set the facet true to enable facet
        query.setFacet(true);
        String fieldName = fields.get(IndexingConstants.FACET_FIELD_NAME);
        String queryField = null;
        if (fieldName != null) {
            //set the field for the facet
            if (IndexingConstants.FIELD_TAGS.equals(fieldName) ||
                    IndexingConstants.FIELD_COMMENTS.equals(fieldName) ||
                    IndexingConstants.FIELD_ASSOCIATION_DESTINATIONS.equals(fieldName) ||
                    IndexingConstants.FIELD_ASSOCIATION_TYPES.equals(fieldName)) {
                queryField = fieldName + SolrConstants.SOLR_MULTIVALUED_STRING_FIELD_KEY_SUFFIX;
                query.addFacetField(queryField);
            } else {
                queryField = fieldName + SolrConstants.SOLR_STRING_FIELD_KEY_SUFFIX;
                query.addFacetField(queryField);
            }
            fields.remove(IndexingConstants.FACET_FIELD_NAME);
            //set the limit for the facet
            if (fields.get(IndexingConstants.FACET_LIMIT) != null) {
                query.setFacetLimit(Integer.parseInt(fields.get(IndexingConstants.FACET_LIMIT)));
                fields.remove(IndexingConstants.FACET_LIMIT);
            } else {
                query.setFacetLimit(IndexingConstants.FACET_LIMIT_DEFAULT);
            }
            //set the mincount for the facet
            if (fields.get(IndexingConstants.FACET_MIN_COUNT) != null) {
                query.setFacetMinCount(Integer.parseInt(fields.get(IndexingConstants.FACET_MIN_COUNT)));
                fields.remove(IndexingConstants.FACET_MIN_COUNT);
            } else {
                query.setFacetMinCount(IndexingConstants.FACET_MIN_COUNT_DEFAULT);
            }
            //set the sort value for facet: possible values : index or count
            if (fields.get(IndexingConstants.FACET_SORT) != null) {
                query.setFacetSort(fields.get(IndexingConstants.FACET_SORT));
                fields.remove(IndexingConstants.FACET_SORT);
            }
            // set the prefix value for facet
            if (fields.get(IndexingConstants.FACET_PREFIX) != null) {
                query.setFacetPrefix(fields.get(IndexingConstants.FACET_PREFIX));
                fields.remove(IndexingConstants.FACET_PREFIX);
            }
        }
        return queryField;
    }

    /**
     * Method to add filters to the solr query
     * @param fields dynamic fields
     * @param query solr query
     */
    private void addQueryFilters(Map<String, String> fields, SolrQuery query) {
        String fieldKeySuffix;
        if (fields.size() > 0) {
            String propertyName = "", leftPropertyValue = "", rightPropertyValue = "", leftOp = "", rightOp = "",
                    createdBefore = "", createdAfter = "", updatedBefore = "", updatedAfter = "", mediaType = "",
                    mediaTypeNegate = "", createdBy = "", createdByNegate = "", updatedBy = "", updatedByNegate = "",
                    createdRangeNegate = "", updatedRangeNegate = "", resourceName = "";
            for (Map.Entry<String, String> field : fields.entrySet()) {
                // Query for multivalued fields
                if (field.getValue() != null && StringUtils.isNotEmpty(field.getValue())) {
                    if (field.getKey().equals(IndexingConstants.FIELD_TAGS) || field.getKey()
                            .equals(IndexingConstants.FIELD_COMMENTS) || field.getKey()
                            .equals(IndexingConstants.FIELD_ASSOCIATION_DESTINATIONS) || field.getKey()
                            .equals(IndexingConstants.FIELD_ASSOCIATION_TYPES)) {
                        // Set the suffix value of the key
                        fieldKeySuffix = SolrConstants.SOLR_MULTIVALUED_STRING_FIELD_KEY_SUFFIX + ":*";
                        query.addFilterQuery(
                                field.getKey() + fieldKeySuffix + field.getValue() + "*");
                    } else if (IndexingConstants.FIELD_PROPERTY_NAME.equals(field.getKey())) {
                        // Get the value of property name
                        propertyName = field.getValue();
                    } else if (IndexingConstants.FIELD_LEFT_PROPERTY_VAL.equals(field.getKey())) {
                        // Get the value of left property value
                        leftPropertyValue = field.getValue();
                    } else if (IndexingConstants.FIELD_RIGHT_PROPERTY_VAL.equals(field.getKey())) {
                        // Get the value of right property value
                        rightPropertyValue = field.getValue();
                    } else if (IndexingConstants.FIELD_LEFT_OP.equals(field.getKey())) {
                        // Get the value of left operation
                        leftOp = field.getValue();
                    } else if (IndexingConstants.FIELD_RIGHT_OP.equals(field.getKey())) {
                        // Get the value of right operation
                        rightOp = field.getValue();
                    } else if (IndexingConstants.FIELD_CREATED_BEFORE.equals(field.getKey())) {
                        // Get the value of created before date
                        createdBefore = field.getValue();
                    } else if (IndexingConstants.FIELD_CREATED_AFTER.equals(field.getKey())) {
                        // Get the value of created after date
                        createdAfter = field.getValue();
                    } else if (IndexingConstants.FIELD_UPDATED_BEFORE.equals(field.getKey())) {
                        // Get the value of update before date
                        updatedBefore = field.getValue();
                    } else if (IndexingConstants.FIELD_UPDATED_AFTER.equals(field.getKey())) {
                        // Get the value of updated after date
                        updatedAfter = field.getValue();
                    } else if (IndexingConstants.FIELD_RESOURCE_NAME.equals(field.getKey())) {
                        // Set the suffix value of the key
                        resourceName = field.getValue();
                    } else if (IndexingConstants.FIELD_MEDIA_TYPE.equals(field.getKey())) {
                        // Get the value of resource mediaType
                        mediaType = field.getValue();
                    } else if (IndexingConstants.FIELD_MEDIA_TYPE_NEGATE.equals(field.getKey())) {
                        // Get the value of resource mediaType negation
                        mediaTypeNegate = field.getValue();
                    } else if (IndexingConstants.FIELD_CREATED_BY.equals(field.getKey())) {
                        // Get the value of resource author
                        createdBy = field.getValue();
                    } else if (IndexingConstants.FIELD_CREATED_BY_NEGATE.equals(field.getKey())) {
                        // Get the value of resource author negation
                        createdByNegate = field.getValue();
                    } else if (IndexingConstants.FIELD_LAST_UPDATED_BY.equals(field.getKey())) {
                        // Get the value of resource updater
                        updatedBy = field.getValue();
                    } else if (IndexingConstants.FIELD_UPDATE_BY_NEGATE.equals(field.getKey())) {
                        // Get the value of resource updater negation
                        updatedByNegate = field.getValue();
                    } else if (IndexingConstants.FIELD_CREATED_RANGE_NEGATE.equals(field.getKey())) {
                        // Get the value of created date range negate
                        createdRangeNegate = field.getValue();
                    } else if (IndexingConstants.FIELD_UPDATED_RANGE_NEGATE.equals(field.getKey())) {
                        // Get the value of updated date range negate
                        updatedRangeNegate = field.getValue();
                    } else {
                        // Set the suffix value of the key
                        fieldKeySuffix = SolrConstants.SOLR_STRING_FIELD_KEY_SUFFIX + ":";
                        query.addFilterQuery(field.getKey() + fieldKeySuffix + (field.getValue()));
                    }
                }
            }
            // Set query filter for mediaType
            if (StringUtils.isNotEmpty(mediaType)) {
                // Set the value of the key
                String fieldKey = IndexingConstants.FIELD_MEDIA_TYPE + SolrConstants.SOLR_STRING_FIELD_KEY_SUFFIX + ":";
                setQueryFilterSingleValue(query, fieldKey, mediaType, mediaTypeNegate);
            }
            // Set query filter for author
            if (StringUtils.isNotEmpty(createdBy)) {
                // Set the value of the key
                String fieldKey = IndexingConstants.FIELD_CREATED_BY + SolrConstants.SOLR_STRING_FIELD_KEY_SUFFIX + ":";
                String createdByValue = getWildcardSearchQueryValue(createdBy);
                setQueryFilterSingleValue(query, fieldKey, createdByValue, createdByNegate);
            }
            // Set query filter for updater
            if (StringUtils.isNotEmpty(updatedBy)) {
                // Set the value of the key
                String fieldKey =
                        IndexingConstants.FIELD_LAST_UPDATED_BY + SolrConstants.SOLR_STRING_FIELD_KEY_SUFFIX + ":";
                String updatedByValue = getWildcardSearchQueryValue(updatedBy);
                setQueryFilterSingleValue(query, fieldKey, updatedByValue, updatedByNegate);
            }
            // Set query filter for created date range
            setQueryFilterDateRange(query, createdAfter, createdBefore, createdRangeNegate,
                    IndexingConstants.FIELD_CREATED_DATE);
            // Set query filter for updated date range
            setQueryFilterDateRange(query, updatedAfter, updatedBefore, updatedRangeNegate,
                    IndexingConstants.FIELD_LAST_UPDATED_DATE);
            // Set query filter for property
            setQueryFilterProperty(query, propertyName, leftPropertyValue, leftOp, rightPropertyValue, rightOp);
            // Set query filter for resource name
            setQueryFilterResourceName(query, resourceName);
        }
    }

    /**
     * Method to add the query filter for single fields (resource author, resource last updater, resource media type)
     * @param fieldValue resource value
     * @param query solr query
     * @param fieldNegate resource negation
     */
    private void setQueryFilterSingleValue(SolrQuery query, String fieldKey, String fieldValue, String fieldNegate) {
        String fieldQuery;
        if (StringUtils.isNotEmpty(fieldNegate) && fieldNegate.equalsIgnoreCase(
                SolrConstants.NEGATE_VALUE_DEFAULT)) {
            fieldQuery = fieldKey + SolrConstants.SOLR_NEGATE_VALUE + fieldKey + fieldValue;
        } else {
            fieldQuery = fieldKey + fieldValue;
        }
        query.addFilterQuery(fieldQuery);
    }

    /**
     * Method to add the query filter for resource name
     * @param resourceName resource name
     * @param query solr query
     */
    private void setQueryFilterResourceName(SolrQuery query, String resourceName) {
        if (StringUtils.isNotEmpty(resourceName)) {
            String fieldKeySuffix = SolrConstants.SOLR_STRING_FIELD_KEY_SUFFIX + ":";
            String resourceNameValue = getWildcardSearchQueryValue(resourceName);
            query.addFilterQuery(IndexingConstants.FIELD_RESOURCE_NAME + fieldKeySuffix + resourceNameValue);
        }
    }

    /**
     * Method to add the query filter for resource property values
     * @param propertyName name of the property (property key)
     * @param leftPropertyValue left property value
     * @param leftOp left operation
     * @param rightPropertyValue right property value
     * @param rightOp right operation
     * @param query solr query
     */
    private void setQueryFilterProperty(SolrQuery query, String propertyName, String leftPropertyValue, String leftOp,
            String rightPropertyValue, String rightOp) {
        if (StringUtils.isNotEmpty(propertyName)) {

            if (leftPropertyValue != null && rightPropertyValue != null || rightOp
                    .equals(SolrConstants.OPERATION_EQUAL)) {
                String rightValueType, leftValueType;
                int rightIntValue = 0;
                double rightDoubleValue = 0;
                // No operation values only check the property name
                if (StringUtils.isEmpty(leftPropertyValue) && StringUtils.isEmpty(rightPropertyValue)) {
                    String fieldKeyInt = propertyName + SolrConstants.SOLR_MULTIVALUED_INT_FIELD_KEY_SUFFIX + ":";
                    String fieldKeyDouble = propertyName + SolrConstants.SOLR_MULTIVALUED_DOUBLE_FIELD_KEY_SUFFIX + ":";
                    String fieldKeyString = propertyName + SolrConstants.SOLR_MULTIVALUED_STRING_FIELD_KEY_SUFFIX + ":";
                    query.addFilterQuery(fieldKeyInt + "* | " + fieldKeyDouble + "* | " + fieldKeyString + "*");
                }
                // check foe equal operation
                if (rightOp.equals(SolrConstants.OPERATION_EQUAL) && StringUtils.isNotEmpty(rightPropertyValue)) {
                    setQueryFilterPropertyEqualOperation(query, propertyName, rightPropertyValue);
                } else {
                    rightValueType = getType(rightPropertyValue);
                    leftValueType = getType(leftPropertyValue);
                    if (rightValueType.equals(SolrConstants.TYPE_INT)) {
                        rightIntValue = Integer.parseInt(rightPropertyValue);
                        if (rightOp.equals(SolrConstants.OPERATION_LESS_THAN)) {
                            --rightIntValue;
                        }
                    } else if (rightValueType.equals(SolrConstants.TYPE_DOUBLE)) {
                        rightDoubleValue = Double.parseDouble(rightPropertyValue);
                        if (rightOp.equals(SolrConstants.OPERATION_LESS_THAN)) {
                            rightDoubleValue = rightDoubleValue - 0.1;
                        }
                    }
                    if (rightValueType.equals(SolrConstants.TYPE_INT) || leftValueType.equals(SolrConstants.TYPE_INT)) {
                        setQueryFilterForIntegerPropertyValues(query, propertyName, leftPropertyValue,
                                rightPropertyValue, rightIntValue,
                                leftOp, rightOp);
                    } else if (rightValueType.equals(SolrConstants.TYPE_DOUBLE) || leftValueType
                            .equals(SolrConstants.TYPE_DOUBLE)) {
                        setQueryFilterForDoublePropertyValues(query, propertyName, leftPropertyValue,
                                rightPropertyValue, rightDoubleValue,
                                leftOp, rightOp);
                    }
                }
            }
        }
    }

    /**
     * Method for set query filter for property search integer property values
     * @param leftPropertyValue left property value
     * @param rightPropertyValue right property value
     * @param rightDoubleValue right double value
     * @param leftOp left operation
     * @param rightOp right operation
     * @param query solr query
     * @param propertyName name of the property (property key)
     */
    private void setQueryFilterForDoublePropertyValues(SolrQuery query, String propertyName, String leftPropertyValue,
            String rightPropertyValue,
            double rightDoubleValue, String leftOp, String rightOp) {
        // Get the double values
        double leftDoubleValue = 0;
        if (StringUtils.isNotEmpty(leftPropertyValue)) {
            leftDoubleValue = Double.parseDouble(leftPropertyValue);
        }
        String fieldKey = propertyName + SolrConstants.SOLR_MULTIVALUED_DOUBLE_FIELD_KEY_SUFFIX + ":";
        if (leftOp.equals(SolrConstants.OPERATION_GREATER_THAN) || leftOp
                .equals(SolrConstants.OPERATION_GREATER_THAN_OR_EQUAL)
                || leftOp.equals(SolrConstants.OPERATION_NA)) {
            // If operation is greater than add .1
            if (leftOp.equals(SolrConstants.OPERATION_GREATER_THAN)) {
                leftDoubleValue = leftDoubleValue + 0.1;
            }

            if ((rightOp.equals(SolrConstants.OPERATION_NA) || StringUtils.isEmpty(rightPropertyValue)) && StringUtils
                    .isNotEmpty(leftPropertyValue)) {
                query.addFilterQuery(fieldKey + "[" + leftDoubleValue + " TO * ]");
            } else if (StringUtils.isNotEmpty(rightPropertyValue) && StringUtils
                    .isNotEmpty(leftPropertyValue)) {
                query.addFilterQuery(
                        fieldKey + "[" + leftDoubleValue + " TO " + rightDoubleValue + "]");
            } else if ((leftOp.equals(SolrConstants.OPERATION_NA) || StringUtils.isEmpty(leftPropertyValue))
                    && StringUtils
                    .isNotEmpty(rightPropertyValue)) {
                query.addFilterQuery(fieldKey + "[ * TO " + rightDoubleValue + "]");
            }
        }
    }

    /**
     * Method for set query filter for property search integer property values
     * @param leftPropertyValue left property value
     * @param rightPropertyValue right property value
     * @param rightIntValue right int value
     * @param leftOp left operation
     * @param rightOp right operation
     * @param query solr query
     * @param propertyName value for the property name
     */
    private void setQueryFilterForIntegerPropertyValues(SolrQuery query, String propertyName, String leftPropertyValue,
            String rightPropertyValue,
            int rightIntValue, String leftOp, String rightOp) {
        int leftIntValue = 0;
        // Get the integer values
        if (StringUtils.isNotEmpty(leftPropertyValue)) {
            leftIntValue = Integer.parseInt(leftPropertyValue);
        }
        String fieldKey = propertyName + SolrConstants.SOLR_MULTIVALUED_INT_FIELD_KEY_SUFFIX + ":";
        if (leftOp.equals(SolrConstants.OPERATION_GREATER_THAN) || leftOp
                .equals(SolrConstants.OPERATION_GREATER_THAN_OR_EQUAL)
                || leftOp.equals(SolrConstants.OPERATION_NA)) {

            // If operation is greater than add 1
            if (leftOp.equals(SolrConstants.OPERATION_GREATER_THAN)) {
                ++leftIntValue;
            }

            if ((rightOp.equals(SolrConstants.OPERATION_NA) || StringUtils.isEmpty(rightPropertyValue)) && StringUtils
                    .isNotEmpty(leftPropertyValue)) {
                query.addFilterQuery(fieldKey + "[" + leftIntValue + " TO * ]");
            } else if (StringUtils.isNotEmpty(rightPropertyValue) && StringUtils
                    .isNotEmpty(leftPropertyValue)) {
                query.addFilterQuery(fieldKey + "[" + leftIntValue + " TO " + rightIntValue + "]");
            } else if ((leftOp.equals(SolrConstants.OPERATION_NA) || StringUtils.isEmpty(leftPropertyValue))
                    && StringUtils
                    .isNotEmpty(
                            rightPropertyValue)) {
                query.addFilterQuery(fieldKey + "[ * TO " + rightIntValue + "]");
            }
        }
    }

    /**
     * Method to add query filter for Property search equal operation
     * @param rightPropertyValue right field property value
     * @param query solr query
     * @param propertyName value for the property name
     */
    private void setQueryFilterPropertyEqualOperation(SolrQuery query, String propertyName, String rightPropertyValue) {
        String valueType = getType(rightPropertyValue);
        String fieldKey;
        if (valueType.equals(SolrConstants.TYPE_INT)) {
            // Get the integer value
            int intValue = Integer.parseInt(rightPropertyValue);
            fieldKey = propertyName + SolrConstants.SOLR_MULTIVALUED_INT_FIELD_KEY_SUFFIX + ":";
            query.addFilterQuery(fieldKey + intValue);
        } else if (valueType.equals(SolrConstants.TYPE_DOUBLE)) {
            // Get the float value
            double doubleValue = Double.parseDouble(rightPropertyValue);
            fieldKey = propertyName + SolrConstants.SOLR_MULTIVALUED_DOUBLE_FIELD_KEY_SUFFIX + ":";
            query.addFilterQuery(fieldKey + doubleValue);
        } else if (valueType.equals(SolrConstants.TYPE_STRING)) {
            // Get the string value
            rightPropertyValue = getWildcardSearchQueryValue(rightPropertyValue);
            fieldKey = propertyName + SolrConstants.SOLR_MULTIVALUED_STRING_FIELD_KEY_SUFFIX + ":";
            query.addFilterQuery(fieldKey + rightPropertyValue);
        }
    }

    /**
     * Method to add the query filter for date ranges
     * @param dateAfter create after date
     * @param dateBefore create before date
     * @param dateRangeNegate negate value
     * @param query solr query
     */
    private void setQueryFilterDateRange(SolrQuery query, String dateAfter, String dateBefore, String dateRangeNegate,
            String fieldKeyName) {
        String dateRangeQuery;
        // Set the suffix value of the key
        String fieldKeySuffix = SolrConstants.SOLR_DATE_FIELD_KEY_SUFFIX + ":[";
        if (StringUtils.isNotEmpty(dateAfter) && StringUtils.isNotEmpty(dateBefore)) {
            if (StringUtils.isNotEmpty(dateRangeNegate) && dateRangeNegate.equalsIgnoreCase(
                    SolrConstants.NEGATE_VALUE_DEFAULT)) {
                dateRangeQuery =
                        "(NOT " + fieldKeyName + fieldKeySuffix + toSolrDateFormat(dateAfter,
                                SolrConstants.CALENDER_DATE_FORMAT)
                                + " TO " + toSolrDateFormat(dateBefore,
                                SolrConstants.CALENDER_DATE_FORMAT) + "])";
            } else {
                dateRangeQuery =
                        fieldKeyName + fieldKeySuffix + toSolrDateFormat(dateAfter,
                                SolrConstants.CALENDER_DATE_FORMAT) + " TO " + toSolrDateFormat(dateBefore,
                                SolrConstants.CALENDER_DATE_FORMAT) + "]";
            }
            query.addFilterQuery(dateRangeQuery);
        } else if (StringUtils.isNotEmpty(dateAfter)) {
            if (StringUtils.isNotEmpty(dateRangeNegate) && dateRangeNegate
                    .equalsIgnoreCase(
                            SolrConstants.NEGATE_VALUE_DEFAULT)) {
                dateRangeQuery = "(NOT " + fieldKeyName + fieldKeySuffix + toSolrDateFormat(dateAfter,
                        SolrConstants.CALENDER_DATE_FORMAT) + " TO NOW])";
            } else {
                dateRangeQuery =
                        fieldKeyName + fieldKeySuffix + toSolrDateFormat(dateAfter, SolrConstants.CALENDER_DATE_FORMAT)
                                + " TO NOW]";
            }
            query.addFilterQuery(dateRangeQuery);
        } else if (StringUtils.isNotEmpty(dateBefore)) {
            if (StringUtils.isNotEmpty(dateRangeNegate) && dateRangeNegate
                    .equalsIgnoreCase(
                            SolrConstants.NEGATE_VALUE_DEFAULT)) {
                dateRangeQuery =
                        fieldKeyName + fieldKeySuffix + toSolrDateFormat(dateBefore, SolrConstants.CALENDER_DATE_FORMAT)
                                + " TO NOW]";
            } else {
                dateRangeQuery =
                        "(NOT " + fieldKeyName + fieldKeySuffix + toSolrDateFormat(dateBefore,
                                SolrConstants.CALENDER_DATE_FORMAT)
                                + " TO NOW])";
            }
            query.addFilterQuery(dateRangeQuery);
        }
    }

    /**
     * Method to get the solr query value need for wildcard search
     * @param fieldValue property field value
     * @return result value
     */
    private String getWildcardSearchQueryValue(String fieldValue) {
        String result;
        char lastCharacter = fieldValue.charAt(fieldValue.length() - 1);
        char firstCharacter = fieldValue.charAt(0);
        if (lastCharacter == '%' && firstCharacter == '%') {
            result = "*" + fieldValue.substring(1, fieldValue.length() - 1) + "*";
        } else if (lastCharacter == '%') {
            result = fieldValue.substring(0, fieldValue.length() - 1) + "*";
        } else if (firstCharacter == '%') {
            result = "*" + fieldValue.substring(1, fieldValue.length());
        } else {
            result = fieldValue;
        }
        return result;
    }

    public void cleanAllDocuments() {
        try {
            QueryResponse results = server.query(new SolrQuery("ICWS"));
            SolrDocumentList resultsList = results.getResults();

            for (SolrDocument aResultsList : resultsList) {
                String id = (String) aResultsList.getFieldValue(
                        IndexingConstants.FIELD_ID);
                UpdateResponse deleteById = server.deleteById(id);
                if (log.isDebugEnabled()) {
                    log.debug("Deleted ID " + id + " Status " + deleteById.getStatus());
                }
            }
        } catch (SolrServerException e) {
            //throw unchecked exception: SolrException, this will throw when there is an error in connection.
            throw new SolrException(ErrorCode.SERVER_ERROR, e);
        } catch (IOException e) {
            //throw unchecked exception: SolrException, this will throw when there is an error in connection.
            throw new SolrException(ErrorCode.SERVER_ERROR, e);
        }
    }

}
