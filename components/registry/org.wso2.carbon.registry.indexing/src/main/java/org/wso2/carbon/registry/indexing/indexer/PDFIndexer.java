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
