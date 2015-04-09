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

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.indexer.MimeTypeConstants;



public class SolrClientTest {
	public static void main(String[] args) throws Exception {
		//SolrClient client = new SolrClient("http://documents.ozone.wso2.com:8080/solr");
		//SolrClient client = new SolrClient("https://127.0.0.1:9443/registry/resource/solr");
//		SolrClient client = new SolrClient("http://ec2-174-129-248-197.compute-1.amazonaws.com:8080/solr");
		SolrClient client = new SolrClient();
		File2Index file = new File2Index(RegistryUtils.encodeString("<a att=\"xml\">This is a test</a>"), MimeTypeConstants.XML, "/testpath", MultitenantConstants.SUPER_TENANT_ID, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
		//client.indexDocument(file, IndexingManager.getInstance().getIndexerForMediaType(MimeTypeConstants.XML));
		
		SolrDocumentList list = client.query("xml", MultitenantConstants.SUPER_TENANT_ID);
		SolrDocument document = list.iterator().next();
		System.out.println(document.getFieldValue("id"));
		
		
//		Protocol.registerProtocol("https", new Protocol("https",
//				new EasySSLProtocolSocketFactory(), 443));
		
//		RequestContext requestContext = new RequestContext(null,null,null);
//		requestContext.setResource(new ResourceImpl());
//		requestContext.setSourceURL(new File("/home/hemapani/Desktop/toPrint/31037.pdf").toURL().toString());
//		client.indexPDF(requestContext);
//		
//		requestContext.setSourceURL(new File("/home/hemapani/Desktop/Hasthi@wo2.ppt").toURL().toString());
//		client.indexMSPowerpoint(requestContext);
		
//		requestContext.setSourceURL(new File("/home/hemapani/Desktop/temp/test/3.doc").toURL().toString());
//		client.indexMSWord(requestContext);
//		
//		SolrDocumentList results = client.query("Luckham CEP");
//		for(int i =0; i < (int)results.getNumFound();i++){
//    		System.out.println(results.get(i).getFirstValue("id"));
//    	}
//		
//		System.out.println();
//		System.out.println(client.query("Srinath"));
//		System.out.println(client.query("EBay"));
		//client.cleanAllDocuments();
	   //client.deleteFromIndex("/WSO2/AllWSO2/hasthi-shws2009.pdf");
	   
	   
	   

//		try{
//		SolrClient solrClient = new SolrClient(registryContext.getIndexingServerLocation());
//    	
//    	SolrDocumentList results = solrClient.query(keywords);
//    	
//    	String [] paths = new String [(int)results.getNumFound()];
//    	org.wso2.carbon.registry.core.Collection collection = new CollectionImpl();
//    	for(int i =0; i < (int)results.getNumFound();i++){
//    		paths[i] = (String)results.get(i).getFirstValue("id");
//    	}
//    	 collection.setContent(paths);
//         return collection;
//        } catch (IOException e) {
//            String msg = "Failed to search content";
//            log.error(msg, e);
//            throw new RegistryException(msg, e);
//        } 

		
	}
/*		
	    SolrServer server = new CommonsHttpSolrServer("http://localhost:8080/solr");

//	    // http://localhost:8983/solr/spellCheckCompRH?q=epod&spellcheck=on&spellcheck.build=true
//	    ModifiableSolrParams params = new ModifiableSolrParams();
//	    params.set("qt", "/spellCheckCompRH");
//	    params.set("q", "epod");
//	    params.set("spellcheck", "on");
//	    params.set("spellcheck.build", "true");
//
//	    QueryResponse response = solr.query(params);
//	    System.out.println("response = " + response);
	    
	 // Now add something...
	    SolrInputDocument doc1 = new SolrInputDocument();
	    doc1.addField( "id", "id1", 1.0f );
	    doc1.addField( "name", "doc1", 1.0f );
	    doc1.addField( "price", 10 );

	    SolrInputDocument doc2 = new SolrInputDocument();
	    doc2.addField( "id", "id2", 1.0f );
	    doc2.addField( "name", "doc2", 1.0f );
	    doc2.addField( "price", 20 );
	    
	    Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
	    docs.add( doc1 );
	    docs.add( doc2 );
	    
	    // Add the documents
	    server.add( docs );
	    server.commit();
	    
	    SolrQuery query = new SolrQuery();
	    query.setQuery( "*:*" );
	    query.addSortField( "price", SolrQuery.ORDER.asc );
	    QueryResponse rsp = server.query( query );
	    System.out.println(rsp);
	  }
*/
}
