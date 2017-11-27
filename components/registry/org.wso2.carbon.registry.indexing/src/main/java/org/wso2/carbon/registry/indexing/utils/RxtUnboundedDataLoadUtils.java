/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.registry.indexing.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.MediaTypesUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.SolrConstants;
import org.wso2.carbon.registry.indexing.bean.RxtUnboundedEntryBean;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to load rxt data.
 */
public class RxtUnboundedDataLoadUtils {
    private static final Log log = LogFactory.getLog(RxtUnboundedDataLoadUtils.class);

    /**
     * This method is used to get rxt data.
     *
     * @param userRegistry userRegistry.
     * @return
     * @throws RegistryException
     */
    public static Map<String, List<String>> getRxtData(UserRegistry userRegistry) throws RegistryException {

        String[] paths = getRxtPathLists(userRegistry);
        Map<String, List<String>> RxtDetails = new ConcurrentHashMap<>();
        for (String path : paths) {
            String rxtContent = RegistryUtils.decodeBytes((byte[]) userRegistry.get(path).getContent());
            RxtUnboundedEntryBean rxtUnboundedEntryBean = getRxtUnboundedEntries(rxtContent);
            String mediaType = rxtUnboundedEntryBean.getMediaType();
            List<String> unboundedFields = rxtUnboundedEntryBean.getFields();

            if (mediaType != null && unboundedFields.size() > 0) {
                RxtDetails.put(rxtUnboundedEntryBean.getMediaType(), rxtUnboundedEntryBean.getFields());
            }
        }
        return RxtDetails;
    }

    /**
     * This method is used to get rxt path list.
     *
     * @param registry registry object.
     * @return
     * @throws RegistryException
     */
    private static String[] getRxtPathLists(Registry registry) throws RegistryException {
        String[] paths;
        try {
            paths = MediaTypesUtils.getResultPaths(registry, SolrConstants.RXT_MEDIA_TYPE);
            if (paths == null) {
                paths = new String[0];
            }
            return paths;
        } catch (RegistryException e) {
            throw new RegistryException("Error occurred while getting all rxt list", e);
        }
    }

    /**
     * This method is used to get unbounded field values in a rxt.
     *
     * @param rxtContent rxt configuration
     * @return list of unbounded filed values.
     * @throws RegistryException
     */
    public static RxtUnboundedEntryBean getRxtUnboundedEntries(String rxtContent)
            throws RegistryException {
        RxtUnboundedEntryBean rxtUnboundedEntryBean = new RxtUnboundedEntryBean();
        String mediaType;
        List<String> fields = new ArrayList<>();

        DocumentBuilderFactory factory = getSecuredDocumentBuilder();
        DocumentBuilder builder;
        Document doc;
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(rxtContent.getBytes())) {
            builder = factory.newDocumentBuilder();
            if (builder != null) {
                doc = builder.parse(byteArrayInputStream);
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                //Get media Type
                XPathExpression expToGetMediaType = xpath.compile(SolrConstants.RXT_ROOT_XPATH);
                Node rxtRootNode = (Node) expToGetMediaType.evaluate(doc, XPathConstants.NODE);
                mediaType = rxtRootNode.getAttributes().getNamedItem(SolrConstants.WORD_TYPE).getTextContent();
                rxtUnboundedEntryBean.setMediaType(mediaType);

                if (StringUtils.isNotEmpty(mediaType)) {
                    XPathExpression expr1 = xpath.compile(SolrConstants.UNBOUNDED_TABLE_XPATH);
                    NodeList n1 = (NodeList) expr1.evaluate(doc, XPathConstants.NODESET);
                    XPathExpression expr2 = xpath.compile(SolrConstants.UNBOUNDED_FIELD_XPATH);
                    NodeList n2 = (NodeList) expr2.evaluate(doc, XPathConstants.NODESET);

                    // Stop the iteration if no unbounded table entries were found in rxt configuration.
                    if (n1.getLength() != 0 || n2.getLength() != 0) {
                        // Add unbounded table values
                        for (int i = 0; i < n1.getLength(); i++) {
                            Node n = n1.item(i);
                            String tableName = n.getAttributes().getNamedItem(SolrConstants.WORD_NAME).getTextContent();
                            String expr2Text = SolrConstants.UNBOUNDED_TABLE_XPATH_PREFIX + tableName
                                    + SolrConstants.UNBOUNDED_TABLE_XPATH_SUFFIX;
                            XPathExpression expr3 = xpath.compile(expr2Text);
                            NodeList n3 = (NodeList) expr3.evaluate(doc, XPathConstants.NODESET);

                            if (n3.getLength() > 0) {
                                for (int j = 0; j < n3.getLength(); j++) {
                                    Node node = n3.item(j);
                                    fields.add(
                                            toCamelCase(tableName) + SolrConstants.UNDERSCORE + node.getTextContent());
                                }
                            }
                        }
                        // Add unbounded option test field values.
                        for (int k = 0; k < n2.getLength(); k++) {
                            Node node = n2.item(k);
                            String tableName = node.getParentNode().getAttributes().getNamedItem(
                                    SolrConstants.WORD_NAME).getTextContent();
                            if (tableName != null) {
                                fields.add(toCamelCase(tableName) + SolrConstants.UNDERSCORE_ENTRY);
                            }
                        }
                    }
                }
            }
            rxtUnboundedEntryBean.setFields(fields);
        } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException e) {
            throw new RegistryException("Failed to read rxt configuration and filter the unbounded fields", e);
        }
        return rxtUnboundedEntryBean;
    }


    private static String toCamelCase(String key) {
        if (StringUtils.isEmpty(key)) {
            return key;
        }
        String[] parts = key.split(" ");
        String camelCaseString = parts[0].toLowerCase();
        if (parts.length > 1) {
            for (int i = 1; i < parts.length; i++) {
                camelCaseString = camelCaseString + toProperCase(parts[i]);
            }
        }
        return camelCaseString;
    }

    private static String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase() +
                s.substring(1).toLowerCase();
    }

    private static DocumentBuilderFactory getSecuredDocumentBuilder() {
        final int ENTITY_EXPANSION_LIMIT = 0;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);

        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
            // Skip throwing the error as this exception doesn't break actual DocumentBuilderFactory creation
            log.error("Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or "
                    + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE, e);
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);
        return dbf;
    }
}
