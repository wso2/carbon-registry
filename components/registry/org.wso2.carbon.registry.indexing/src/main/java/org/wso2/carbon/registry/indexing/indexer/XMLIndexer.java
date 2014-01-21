package org.wso2.carbon.registry.indexing.indexer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLIndexer implements Indexer {

	public static final Log log = LogFactory.getLog(XMLIndexer.class);

	public IndexDocument getIndexedDocument(File2Index fileData) throws SolrException, RegistryException {
        // we register both the content as it is and only text content
        String xmlAsStr = RegistryUtils.decodeBytes(fileData.data);

        final StringBuffer contentOnly = new StringBuffer();
        ByteArrayInputStream inData = new ByteArrayInputStream(fileData.data);

        // this will handle text content
        DefaultHandler handler = new DefaultHandler() {
            public void characters(char ch[], int start, int length)
            throws SAXException {
                contentOnly.append(new String(ch, start, length)).append(" ");
            }
        };
//			SAXParserFactory factory = SAXParserFactory.newInstance();
//			SAXParser saxParser = factory.newSAXParser();
//			saxParser.parse(inData, handler);

        IndexDocument indexDocument = new IndexDocument(fileData.path, xmlAsStr,
                contentOnly.toString());
        Map<String, List<String>> attributes = new HashMap<String, List<String>>();
        if (fileData.mediaType != null) {
            attributes.put(IndexingConstants.FIELD_MEDIA_TYPE, Arrays.asList(fileData.mediaType));
        }
        if (fileData.lcState != null) {
            attributes.put(IndexingConstants.FIELD_LC_STATE, Arrays.asList(fileData.lcState));
        }
        if (fileData.lcName != null) {
            attributes.put(IndexingConstants.FIELD_LC_NAME, Arrays.asList(fileData.lcName));
        }
        indexDocument.setFields(attributes);
        return indexDocument;


    }

}
