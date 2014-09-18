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

package org.wso2.carbon.registry.metadata.provider.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.metadata.Constants;
import org.wso2.carbon.registry.metadata.exception.MetadataException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.*;

public class Util {

    private static final Log log = LogFactory.getLog(Util.class);

    public static OMElement getBaseContentElement() {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement root = factory.createOMElement(new QName(Constants.CONTENT_ROOT_NAME));
        OMElement properties = factory.createOMElement(new QName(Constants.CONTENT_PROPERTY_EL_ROOT_NAME));
        OMElement attributes = factory.createOMElement(new QName(Constants.CONTENT_ATTRIBUTE_EL_ROOT_NAME));
        root.addChild(properties);
        root.addChild(attributes);
        return root;
    }

    public static OMElement getAttributeRoot() {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        return factory.createOMElement(new QName(Constants.CONTENT_ATTRIBUTE_EL_ROOT_NAME));
    }

    public static OMElement getPropertyRoot() {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        return factory.createOMElement(new QName(Constants.CONTENT_PROPERTY_EL_ROOT_NAME));
    }

    public static OMElement getContentRoot() {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        return factory.createOMElement(new QName(Constants.CONTENT_ROOT_NAME));
    }

    public static OMElement buildOMElement(byte[] content) throws MetadataException {
        XMLStreamReader parser;
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_COALESCING, new Boolean(true));
            parser = factory.createXMLStreamReader(new StringReader(
                    RegistryUtils.decodeBytes(content)));
        } catch (Exception e) {
            String msg = "Error in initializing the parser to build the OMElement.";
            log.error(msg, e);
            throw new MetadataException("", e);
        }

        //create the builder
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        //get the root element (in this case the envelope)

        return builder.getDocumentElement();
    }


    public static Map<String, List<String>> getPropertyBag(OMElement root) {
        Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
        OMElement properties = root.getFirstChildWithName(new QName(Constants.CONTENT_PROPERTY_EL_ROOT_NAME));
        if(properties != null) {
            Iterator itr = properties.getChildren();
            while (itr.hasNext()) {
                OMElement el = (OMElement) itr.next();
                String key = el.getLocalName();
                String value = el.getText();
                List<String> list = new ArrayList<String>();
                list.add(value);
                resultMap.put(key, list);
            }
        }
        return resultMap;
    }


    public static Map<String, List<String>> getAttributeMap(OMElement attributes) {
        Map<String, List<String>> attributeMap = new HashMap<String, List<String>>();
        if(attributes != null) {
            Iterator itr = attributes.getChildren();
            while (itr.hasNext()) {
                OMElement el = (OMElement) itr.next();
                String key = el.getLocalName();
                String value = el.getText();
                List<String> valList = new ArrayList<String>();
                valList.add(value);
                attributeMap.put(key, valList);
            }
        }
        return attributeMap;
    }



}
