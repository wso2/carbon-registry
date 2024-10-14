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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class MSPowerpointIndexer implements Indexer {

	public static final Log log = LogFactory.getLog(MSPowerpointIndexer.class);
	
	public IndexDocument getIndexedDocument(File2Index fileData)
			throws SolrException {
		String ppText = null;
		try {
			HSLFSlideShow slideShow = new HSLFSlideShow(new ByteArrayInputStream(fileData.data));
			SlideShowExtractor<?, ?> extractor = new SlideShowExtractor <>(slideShow);
			ppText = extractor.getText();
		} catch (OfficeXmlFileException e){
			try {
				XMLSlideShow slideShow = new XMLSlideShow(new ByteArrayInputStream(fileData.data));
				SlideShowExtractor<?, ?> extractor = new SlideShowExtractor <>(slideShow);
				ppText = extractor.getText();
			} catch (IOException e1) {
				String msg = "Failed to write to the index";
				log.error(msg, e);
				throw new SolrException(ErrorCode.SERVER_ERROR, msg);
			}
		} catch (IOException e) {
			String msg = "Failed to write to the index";
			log.error(msg, e);
			throw new SolrException(ErrorCode.SERVER_ERROR, msg);
		}
		return new IndexDocument(fileData.path, ppText, null);
	}

}
