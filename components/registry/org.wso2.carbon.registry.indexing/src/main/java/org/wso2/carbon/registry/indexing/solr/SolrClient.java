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
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.search.SolrQueryParser;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.pagination.PaginationUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SolrClient {

    public static final Log log = LogFactory.getLog(SolrClient.class);

    private static volatile SolrClient instance;
    private SolrServer server;

    protected SolrClient() throws IOException, ParserConfigurationException, SAXException {
        File solrHome = new File(CarbonUtils.getCarbonHome() + File.separator + "repository" +
                File.separator + "conf", "solr");
        if (!solrHome.exists() && !solrHome.mkdirs()) {
            throw new IOException("Solr Home Directory could not be created. Path: " + solrHome);
        }
        File confDir = new File(solrHome, "conf");
        if (!confDir.exists() && !confDir.mkdirs()) {
            throw new IOException("Solf conf directory could not be created! Path: " + confDir);
        }

        String[] filePaths = new String[]{"elevate.xml", "protwords.txt", "schema.xml",
                "scripts.conf", "solrconfig.xml", "spellings.txt", "stopwords.txt", "synonyms.txt"};

        for (String path:filePaths) {
            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(path);
            if(resourceAsStream == null){
                throw new SolrException(ErrorCode.NOT_FOUND, "Can not find resource "+ path + " from the classpath");
            }
            File file = new File(confDir, path);
            if (!file.exists()) {
                write2File(resourceAsStream, file);
            }
        }

        System.setProperty("solr.solr.home", solrHome.getPath());
        CoreContainer.Initializer initializer = new CoreContainer.Initializer();
        CoreContainer coreContainer = initializer.initialize();
        this.server = new EmbeddedSolrServer(coreContainer, "");
    }

    public static SolrClient getInstance() throws IndexerException {
        if (instance == null) {
            synchronized (SolrClient.class) {
                try {
                    instance = new SolrClient();
                } catch (Exception e) {
                    log.error("Could not instantiate Solr client", e);
                    throw new IndexerException("Could not instantiate Solr client", e);
                }
            }
        }
        return instance;
    }

    private void write2File(InputStream in, File file) throws IOException{
        byte[] buf = new byte[1024];
        FileOutputStream out = new FileOutputStream(file);
        try {
            int read;

            while ((read = in.read(buf)) >= 0) {
                out.write(buf, 0, read);
            }
        } finally {
            out.close();
            in.close();
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
            UpdateResponse response = server.commit();

            if (log.isDebugEnabled()) {
                log.debug("Indexed document "+id + " with "+ response.getStatus());
            }
        } catch (SolrServerException e) {
            throw new SolrException(ErrorCode.SERVER_ERROR, "Error at indexing", e);
        } catch (IOException e) {
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
            /*if (id == null) {
                   throw new SolrException(ErrorCode.BAD_REQUEST, "ID not found");
               }*/
            server.deleteById(id);
            server.commit();
            if (log.isDebugEnabled()) {
                log.debug("Delete the document "+ id);
            }
        } catch (SolrServerException e) {
            throw new SolrException(ErrorCode.SERVER_ERROR,"Failure at deleting", e);
        } catch (IOException e) {
            throw new SolrException(ErrorCode.SERVER_ERROR,"Failure at deleting", e);
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
                query.addFilterQuery(IndexingConstants.FIELD_TENANT_ID + ":" + "\\"+tenantId);
            }else {
                query.addFilterQuery(IndexingConstants.FIELD_TENANT_ID + ":" + tenantId);
            }
            //This is for fixing  REGISTRY-1695, This is temporary solution until
            //the default security polices also stored in Governance registry.
            if (fields.size() > 0 && fields.get(IndexingConstants.FIELD_MEDIA_TYPE).equals(
                    RegistryConstants.POLICY_MEDIA_TYPE)) {
                query.addFilterQuery(IndexingConstants.FIELD_ID + ":" +
                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + "*");
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
            if ((messageContext != null && PaginationUtils.isPaginationHeadersExist(messageContext)) || PaginationContext.getInstance() != null) {
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
                        query.setSortField(sortBy + "_s", paginationContext.getSortOrder().equals("ASC") ?
                                SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
                    }
                    queryresponse = server.query(query);
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
            }

            return queryresponse.getResults();
        } catch (SolrServerException e) {
            throw new SolrException(ErrorCode.SERVER_ERROR, "Failure at query "+ keywords, e);
        }
    }

    public void cleanAllDocuments(){
        try {
            QueryResponse results = server.query(new SolrQuery("ICWS"));
            SolrDocumentList resultsList = results.getResults();

            for(int i =0; i < resultsList.size(); i++) {
                String id = (String)resultsList.get(i).getFieldValue(IndexingConstants.FIELD_ID);
                UpdateResponse deleteById = server.deleteById(id);
                server.commit();
                log.debug("Deleted ID "+ id + " Status " + deleteById.getStatus());
            }
        } catch (SolrServerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
