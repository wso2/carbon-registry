package org.wso2.carbon.registry.indexing.indexer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

public class MSPowerpointIndexer implements Indexer {

	public static final Log log = LogFactory.getLog(MSPowerpointIndexer.class);
	
	public IndexDocument getIndexedDocument(File2Index fileData)
			throws SolrException {
		try {
			POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(fileData.data));
			PowerPointExtractor extractor = new PowerPointExtractor(fs);
			String ppText = extractor.getText();

			return new IndexDocument(fileData.path, ppText, null);
		} catch (IOException e) {
			String msg = "Failed to write to the index";
			log.error(msg, e);
			throw new SolrException(ErrorCode.SERVER_ERROR, msg);
		}

	}

}
