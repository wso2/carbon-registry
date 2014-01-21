package org.wso2.carbon.registry.indexing.indexer;

import org.apache.solr.common.SolrException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

public class PlainTextIndexer implements Indexer {

	public IndexDocument getIndexedDocument(File2Index fileData) throws SolrException,
            RegistryException {
		return new IndexDocument(fileData.path, RegistryUtils.decodeBytes(fileData.data), null);
	}

}
