package org.wso2.carbon.registry.indexing.indexer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.pdfbox.cos.COSDocument;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

public class PDFIndexer implements Indexer {
	
	public static final Log log = LogFactory.getLog(PDFIndexer.class); 

	public IndexDocument getIndexedDocument(File2Index fileData) throws SolrException {
        COSDocument cosDoc = null;
		try {
			PDFParser parser = new PDFParser(new ByteArrayInputStream(fileData.data));
			parser.parse();
			 cosDoc = parser.getDocument();

			PDFTextStripper stripper = new PDFTextStripper();
			String docText = stripper.getText(new PDDocument(cosDoc));


			return new IndexDocument(fileData.path, docText, null);
		} catch (IOException e) {
			String msg = "Failed to write to the index";
			log.error(msg, e);
			throw new SolrException(ErrorCode.SERVER_ERROR, msg);
		} finally {
            if (cosDoc != null) {
                try {
                    cosDoc.close();
                } catch (IOException e) {
                   log.error("Failed to close pdf doc stream ",e);
                }
            }
        }
    }

}
