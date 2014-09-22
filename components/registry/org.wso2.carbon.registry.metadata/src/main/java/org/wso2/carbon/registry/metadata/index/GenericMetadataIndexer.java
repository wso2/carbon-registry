/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.registry.metadata.index;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.indexer.XMLIndexer;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;
import org.wso2.carbon.registry.metadata.Constants;
import org.wso2.carbon.registry.metadata.provider.util.Util;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.Map;

/**
 * This is the registry indexer registered through registry.xml to index all metadata object attributes
 */
public class GenericMetadataIndexer extends XMLIndexer implements Indexer {

    public IndexDocument getIndexedDocument(AsyncIndexer.File2Index fileData) throws RegistryException {
        IndexDocument indexedDocument = super.getIndexedDocument(fileData);
        try {

            String xmlAsStr = RegistryUtils.decodeBytes(fileData.data);
            OMElement rootEl = AXIOMUtil.stringToOM(xmlAsStr);

            Map<String, List<String>> propBag = Util.getPropertyBag(rootEl);
            Map<String, List<String>> attributeMap = Util.getAttributeMap(rootEl.getFirstChildWithName(new QName(Constants.CONTENT_ATTRIBUTE_EL_ROOT_NAME)));

            Map<String, List<String>> fields = indexedDocument.getFields();
            setAttributesToLowerCase(fields);

            //Content artifact (policy, wsdl, schema ...etc) doesn't contains the attributes.
            if (fileData.mediaType.matches("vnd.wso2.(.)+\\+xml;version=(.)+") && (propBag.size() > 0 || attributeMap.size() > 0)) {
                setAttributesToLowerCase(attributeMap);
                setAttributesToLowerCase(propBag);
                fields.putAll(attributeMap);
                fields.putAll(propBag);
            }
            indexedDocument.setFields(fields);
        } catch (XMLStreamException e) {
            log.error("Unable to parse XML", e);
            throw new RegistryException(e.getMessage(),e);
        }

        return indexedDocument;

    }

    private void setAttributesToLowerCase(Map<String, List<String>> attributes) {
        for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
            List<String> list = entry.getValue();
            if (list == null) continue;
            for (int i = 0; i < list.size(); i++) {
                list.set(i, list.get(i) != null ? list.get(i).toLowerCase() : list.get(i));
            }
        }
    }


}
