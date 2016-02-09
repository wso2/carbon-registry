/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.registry.indexing.indexer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JSONIndexer implements Indexer {

    public static final Log log = LogFactory.getLog(JSONIndexer.class);

    @Override
    public IndexDocument getIndexedDocument(AsyncIndexer.File2Index fileData) throws SolrException, RegistryException {

        if (log.isDebugEnabled()) {
            log.debug("Registry JSON Indexer is running");
        }

        return getPreProcessedDocument(fileData);
    }

    private IndexDocument getPreProcessedDocument(AsyncIndexer.File2Index fileData) throws RegistryException {
        String jsonAsString = RegistryUtils.decodeBytes(fileData.data);

        IndexDocument indexDocument = new IndexDocument(fileData.path, jsonAsString,
                null);
        Map<String, List<String>> attributes = new HashMap<>();
        if (fileData.mediaType != null) {
            attributes.put(IndexingConstants.FIELD_MEDIA_TYPE, Arrays.asList(fileData.mediaType.toLowerCase()));
        }
        if (fileData.lcState != null) {
            attributes.put(IndexingConstants.FIELD_LC_STATE, Arrays.asList(fileData.lcState.toLowerCase()));
        }
        if (fileData.lcName != null) {
            attributes.put(IndexingConstants.FIELD_LC_NAME, Arrays.asList(fileData.lcName.toLowerCase()));
        }
        if (fileData.path != null) {
            attributes.put(IndexingConstants.FIELD_OVERVIEW_NAME, Arrays.asList(RegistryUtils.getResourceName(fileData.path).toLowerCase()));
        }
        indexDocument.setFields(attributes);
        return indexDocument;
    }
}


