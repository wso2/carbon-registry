/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
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
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.indexing.RegistryConfigLoader;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class SolrClient {

    public static final Log log = LogFactory.getLog(SolrClient.class);

    private static volatile SolrClient instance;
    private org.apache.solr.client.solrj.SolrClient server;
    
    private Map<String,String> filePathMap = new HashMap<String, String>();
	
	// solr home directory path
    private static final String SOLR_HOME_FILE_PATH = CarbonUtils.getCarbonConfigDirPath() +
	                                                  File.separator + "solr";

    //properties file name which contains all solr filenames and relative paths
    private static final String SOLR_CONFIG_FILES_CONTAINER = "solr_configuration_files.properties";
    //solr core properties filename
    private static final String CORE_PROPERTIES = "core.properties";
    //constant to identify the file path. file maps to this value should go under home directory
    private static final String SOLR_HOME = "home/";
    //constant to identify the file path. file maps to this value should go under home/core directory
    private static final String SOLR_CORE = "home/core/";
    //constant to identify the file path. file maps to this value should go under home/core/conf/lang directory
    private static final String SOLR_CONF_LANG = "home/core/conf/lang";
    //constant to set the solr system property
    private static final String SOLR_HOME_SYSTEM_PROPERTY = "solr.solr.home";
    //constant for governance path to solr queries
    private static final String GOVERNANCE_REGISTRY_BASE_PATH =  "\\/_system\\/governance\\/";

    private File solrHome, confDir, langDir;
	
    private String solrCore = null;

    protected SolrClient() throws IOException{
    	//get the solr server url from the registry.xml
    	RegistryConfigLoader configLoader = RegistryConfigLoader.getInstance();
    	String solrServerUrl = configLoader.getSolrServerUrl();

        /**
         * default solr core is set to registry-indexing
         */
        solrCore = IndexingConstants.DEFAULT_SOLR_SERVER_CORE;
        if(log.isDebugEnabled()) {
            log.debug("Solr server core is set as: " + solrCore);
        }

    	//create the solr home path defined in SOLR_HOME_FILE_PATH : carbon_home/repository/conf/solr
    	solrHome = new File(SOLR_HOME_FILE_PATH);
        if (!solrHome.exists() && !solrHome.mkdirs()) {
            throw new IOException("Solr home directory could not be created. path: " + solrHome);
        }

        //create the configuration folder inside solr core : carbon_home/repository/conf/solr/<solrCore>/conf
    	confDir = new File(solrHome, solrCore + File.separator + "conf");
    	if (!confDir.exists() && !confDir.mkdirs()) {
            throw new IOException("Solr conf directory could not be created! Path: " + confDir);
    	}
	 	
    	//create lang directory inside conf to store language specific stopwords
    	//commons-io --> file utils
    	langDir = new File(confDir, "lang");
    	if (!langDir.exists() && !langDir.mkdirs()) {
            throw new IOException("Solf lang directory could not be created! Path: " + langDir);
        }
		
    	//read the configuration file name and there dest path and stored in filePathMap 
    	readConfigurationFilePaths();
    	//read the content of the files in filePathMap and copy them into destination path.
    	copyConfigurationFiles();
    	//set the solr home path
    	System.setProperty(SOLR_HOME_SYSTEM_PROPERTY, solrHome.getPath());

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
     * reads sourceFilePath and destFilePath from solr_configuration_files.properties file
     * e.g: protwords.txt = home/core/conf
     * protword.txt is the resource file name
     * home/core/conf is destination file path, this will go to solr-home/<solr-core>/conf directory.
     * @throws IOException
     */
    private void readConfigurationFilePaths() throws IOException {
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = getClass().getClassLoader()
                    .getResourceAsStream(SOLR_CONFIG_FILES_CONTAINER);
            Properties fileProperties = new Properties();
            fileProperties.load(resourceAsStream);

            for (Entry<Object, Object> entry : fileProperties.entrySet()) {
                if(entry.getValue() != null){
	                String [] fileNames = entry.getValue().toString().split(",");
	                for(String fileName : fileNames){
	                    filePathMap.put(fileName, (String) entry.getKey());
	                }
                }
            }
        } finally {
            if(resourceAsStream != null) {
                resourceAsStream.close();
            }
        }
    }
	
    /**
     * copy solr configuration files in resource folder to solr home folder.
     * @throws IOException
     */
    private void copyConfigurationFiles() throws IOException {
        for (Entry<String, String> entry : filePathMap.entrySet()) {
            String sourceFileName = entry.getKey();
            String fileDestinationPath = entry.getValue();
            File file;

            if (SOLR_HOME.equals(fileDestinationPath)) {
                file = new File(solrHome, sourceFileName);
            } else if (SOLR_CORE.equals(fileDestinationPath)) {
                file = new File(confDir.getParentFile(), sourceFileName);
            } else if (SOLR_CONF_LANG.equals(fileDestinationPath)) {
                file = new File(langDir, sourceFileName);
            } else {
                file = new File(confDir, sourceFileName);
            }

            if (!file.exists()) {
                write2File(sourceFileName, file);
            }
        }
    }
    
    private void write2File(String sourceFileName, File dest) throws IOException{
        byte[] buf = new byte[1024];
        InputStream in = null;
        OutputStream out = null;
        try {
            in = getClass().getClassLoader().getResourceAsStream(sourceFileName);
            out = new FileOutputStream(dest);

            if (CORE_PROPERTIES.equals(sourceFileName)) {
                Properties coreProperties = new Properties();
                coreProperties.load(in);
                coreProperties.setProperty("name", solrCore);
                coreProperties.store(out, null);
            }
            else {
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
    
    private String generateId(int tenantId, String path) {
        return path + IndexingConstants.FIELD_TENANT_ID + tenantId;
    }

    private void addDocument(IndexDocument indexDoc)
            throws SolrException {
        try {
            String path = indexDoc.getPath();
            String rawContent = indexDoc.getRawContent();
            String contentAsText = indexDoc.getContentAsText();
            int tenantId = indexDoc.getTenantId();
            Map<String,List<String>> fields = indexDoc.getFields();

            if (log.isDebugEnabled()) {
                log.debug("Indexing Document in resource path: " + path);
            }
            String id = generateId(tenantId, path);
            if (id == null) {
                id = "id" + rawContent.hashCode();
            }
            SolrInputDocument document = new SolrInputDocument();
            document.addField(IndexingConstants.FIELD_ID, id, 1.0f);
            document.addField(IndexingConstants.FIELD_TEXT, rawContent, 1.0f);
            document.addField(IndexingConstants.FIELD_TENANT_ID, String.valueOf(tenantId));

            if (contentAsText != null) {
                document.addField(IndexingConstants.FIELD_COUNT_ONLY, contentAsText);
            }
            
            if (fields!=null && fields.size() > 0) {
                for (Map.Entry<String, List<String>> e : fields.entrySet()) {
                    // The field is dynamic so we need to follow the solr schema.
                    String key = e.getKey() + "_s";
                    if (e.getValue().size() == 1) {
                        document.addField(key, e.getValue().get(0));
                    } else if (e.getValue().size() > 1) {
                        StringBuilder builder = new StringBuilder();
                        for (String s : e.getValue()) {
                            builder.append(s).append(",");
                        }
                        document.addField(key, builder.substring(0, builder.length() - 1));
                    }
                }
            }
            server.add(document);

        } catch (SolrServerException e) {
            //throw unchecked exception: SolrException, this will throw when there is an error in connection.
            throw new SolrException(ErrorCode.SERVER_ERROR, "Error at indexing", e);
        } catch (IOException e) {
            //throw unchecked exception: SolrException, this will throw when there is an error in connection.
            throw new SolrException(ErrorCode.SERVER_ERROR, "Error at indexing", e);
        }
    }

    public void indexDocument(File2Index fileData, Indexer indexer) throws RegistryException {
        IndexDocument doc = indexer.getIndexedDocument(fileData);
        doc.setTenantId(fileData.tenantId);
        addDocument(doc);
    }

    public synchronized void deleteFromIndex(String path, int tenantId)
    		throws SolrException {
        try {
            String id = generateId(tenantId, path);
            server.deleteById(id);
            if(log.isDebugEnabled()) {
                log.debug("Solr delete index path: " + path + " id: " + id);
            }
        } catch (SolrServerException e) {
            //throw unchecked exception: SolrException, this will throw when there is an error in connection.
            throw new SolrException(ErrorCode.SERVER_ERROR, "Failure at deleting", e);
        } catch (IOException e) {
            //throw unchecked exception: SolrException, this will throw when there is an error in connection.
            throw new SolrException(ErrorCode.SERVER_ERROR, "Failure at deleting", e);
        }
    }

    public SolrDocumentList query(String keywords, int tenantId) throws SolrException{
        return query(keywords, tenantId, Collections.<String, String>emptyMap());
    }

    public SolrDocumentList query(int tenantId, Map<String, String> fields) throws SolrException{
        return query("[* TO *]", tenantId, fields);
    }

    public SolrDocumentList query(String keywords, int tenantId, Map<String, String> fields) throws SolrException{
        try {
            SolrQuery query = new SolrQuery(keywords);
            query.setRows(Integer.MAX_VALUE);
            //Solr does not allow to search with special characters ,
            //Therefore this fix allow to contain "-" in super tenant id.
            if(tenantId== MultitenantConstants.SUPER_TENANT_ID){
                query.addFilterQuery(IndexingConstants.FIELD_TENANT_ID + ":" + "\\" + tenantId);
            }else {
                query.addFilterQuery(IndexingConstants.FIELD_TENANT_ID + ":" + tenantId);
            }
            //This is for fixing  REGISTRY-1695, This is temporary solution until
            //the default security polices also stored in Governance registry.
            if (fields.size() > 0 && fields.get(IndexingConstants.FIELD_MEDIA_TYPE).equals(
                    RegistryConstants.POLICY_MEDIA_TYPE)) {
                query.addFilterQuery(IndexingConstants.FIELD_ID + ":" +
                        GOVERNANCE_REGISTRY_BASE_PATH + "*");
            }
            if (fields.size() > 0) {
                for (Map.Entry<String, String> e : fields.entrySet()) {
                    //This is the fix REGISTRY-1970 before all the special characters where escaped sing
                    // 'SolrQueryParser.escape()' but because of that wildcard functionallity did not work poperly
                    // hence only ecaping ':' and ' ';
                    query.addFilterQuery(e.getKey() + "_s:" + e.getValue().replaceAll(":","\\\\\\:").replaceAll(" ",
                            "\\\\\\ "));
                }
            }
            QueryResponse queryresponse;
            MessageContext messageContext = MessageContext.getCurrentMessageContext();
            if ((messageContext != null && PaginationUtils
					.isPaginationHeadersExist(messageContext))
					|| PaginationContext.getInstance() != null) {
                try {
                    PaginationContext paginationContext;
                    if (messageContext != null) {
                        paginationContext = PaginationUtils.initPaginationContext(messageContext);
                    } else {
                        paginationContext = PaginationContext.getInstance();
                    }
// TODO: Proper mechanism once authroizations are fixed - senaka
//                    query.setStart(paginationContext.getStart());
//                    query.setRows(paginationContext.getCount());
                    String sortBy = paginationContext.getSortBy();
                    if (sortBy.length() > 0) {
                        query.setSort(sortBy + "_s", paginationContext.getSortOrder().equals("ASC") ?
                                SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
                    }
                    queryresponse = server.query(query);
                    if(log.isDebugEnabled()) {
                        log.debug("Solr index queried query: " + query);
                    }
// TODO: Proper mechanism once authroizations are fixed - senaka
//                    PaginationUtils.setRowCount(messageContext,
//                            Long.toString(queryresponse.getResults().getNumFound()));
                } finally {
                    if(messageContext!=null){
                        PaginationContext.destroy();
                    }
                }
            } else {
                queryresponse = server.query(query);
                if(log.isDebugEnabled()) {
                    log.debug("Solr index queried query: " + query);
                }
            }

            return queryresponse.getResults();
        } catch (SolrServerException e) {
            //throw unchecked exception: SolrException, this will throw when there is an invalid search query or error in connection.
            throw new SolrException(ErrorCode.SERVER_ERROR, "Failure at query " + keywords, e);
        }
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
