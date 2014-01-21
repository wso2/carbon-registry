/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.common.ui.utils;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TreeNodeBuilderUtil {
    private static String[] wsdlPrefixes = {
            "wsdl", "http://schemas.xmlsoap.org/wsdl/",
            "wsdl2", "http://www.w3.org/ns/wsdl",
            "xsd", "http://www.w3.org/2001/XMLSchema",
            "soap", "http://schemas.xmlsoap.org/wsdl/soap/",
            "soap12", "http://schemas.xmlsoap.org/wsdl/soap12/",
            "http", "http://schemas.xmlsoap.org/wsdl/http/",
    };

    // here the target path is always a absolute path and both should be non-collections
    public static String calculateAbsolutePath(String wsdlPath, String targetPath) {
        // then only it is considered as relative.
        if (targetPath.startsWith("/") &&
                targetPath.startsWith("http://") && targetPath.startsWith("https://")) {
            return targetPath;
        }
        if (targetPath.startsWith("..") || targetPath.startsWith("./")) {
            if(targetPath.startsWith("./")){
                targetPath = targetPath.replaceFirst("./","/");
            }
            String wsdlCollection = RegistryUtils.getParentPath(wsdlPath);
            String targetCollection = RegistryUtils.getParentPath(targetPath);

            String[] targetPathParts = targetCollection.split("/");
            String[] currentPathParts = wsdlCollection.split("/");

            int countGoUpwardParts = 0;
            for (int i = 0; i < targetPathParts.length; i ++) {
                if (!targetPathParts[i].equals("..")) {
                    break;
                }
                countGoUpwardParts++;
            }
            if (currentPathParts.length < countGoUpwardParts) {
                return null;
            }
            StringBuffer absolutePath = new StringBuffer();
            for (int i = 0; i < currentPathParts.length - countGoUpwardParts; i ++) {
                absolutePath.append(currentPathParts[i]).append("/");
            }

            for (int i = countGoUpwardParts; i < targetPathParts.length; i ++) {
                absolutePath.append(targetPathParts[i]).append("/");
            }
            targetPath = absolutePath.toString() + RegistryUtils.getResourceName(targetPath);
            return targetPath.replaceAll("//","/");
        }
        return targetPath;
    }

    public static String convertQNameToString(String name, String namespaceURI) {
        if (namespaceURI == null) {
            return name;
        }
        return name + " [" + namespaceURI + "]";
    }

    public static String getNamespaceURI(String name, OMElement currentElement) {
        String prefix = getPrefix(name);
        OMNamespace namespace = null;
        if (prefix != null) {
            namespace = currentElement.findNamespace(null, prefix);
        } else {
            // if the prefix is null we can't assume the namespace is null
            namespace = currentElement.getDefaultNamespace();
        }
        if (namespace == null) {
            return null;
        }
        return namespace.getNamespaceURI();
    }

    public static String getLocalName(String name) {
        int colonIndex = name.indexOf(":");
        if (colonIndex == -1) {
            return name;
        }
        if (colonIndex == name.length() -1 ) {
            return "";
        }
        return name.substring(colonIndex + 1);
    }

    public static String getPrefix(String name) {
        int colonIndex = name.indexOf(":");
        if (colonIndex == -1) {
            return null;
        }
        return name.substring(0, colonIndex);
    }

    public static String generateKeyName(String type, String name) {
        return (name == null || name.equals(""))? type: type + "<em>: " + name + "</em>";
    }

    public static List<OMElement> evaluateXPathToElements(String expression,
                                                           OMElement root) throws Exception {
        String[] nsPrefixes = wsdlPrefixes;
        AXIOMXPath xpathExpression = new AXIOMXPath(expression);

        for (int j = 0; j < nsPrefixes.length; j ++) {
            xpathExpression.addNamespace(nsPrefixes[j++], nsPrefixes[j]);
        }
        List<OMElement> omElements = (List<OMElement>) xpathExpression.selectNodes(root);
        if (omElements == null) {
            return Collections.emptyList();
        }
        return omElements;
    }

    public static List<OMAttribute> evaluateXPathToAttributes(String expression,
                                                              OMElement root) throws Exception {
        String[] nsPrefixes = wsdlPrefixes;
        AXIOMXPath xpathExpression = new AXIOMXPath(expression);

        for (int j = 0; j < nsPrefixes.length; j ++) {
            xpathExpression.addNamespace(nsPrefixes[j++], nsPrefixes[j]);
        }
        List<OMAttribute> omElements = (List<OMAttribute>) xpathExpression.selectNodes(root);
        if (omElements == null) {
            return Collections.emptyList();
        }
        return omElements;
    }

    public static OMElement evaluateXPathToElement(String expression,
                                                           OMElement root) throws Exception {
        List<OMElement> nodes = evaluateXPathToElements(expression, root);
        if (nodes == null || nodes.size() == 0) {
            return null;
        }
        return nodes.get(0);
    }

    public static List<String> evaluateXPathToValues(String expression,
                                                      OMElement root) throws Exception {
        String[] nsPrefixes = wsdlPrefixes;
        AXIOMXPath xpathExpression = new AXIOMXPath(expression);

        for (int j = 0; j < nsPrefixes.length; j ++) {
            xpathExpression.addNamespace(nsPrefixes[j++], nsPrefixes[j]);
        }
        List nodeList = xpathExpression.selectNodes(root);
        List<String> values = new ArrayList<String>();
        if (nodeList != null) {

            for (Object nodeObj: nodeList) {
                String value = ((OMAttribute)nodeObj).getAttributeValue();
                values.add(value);
            }
        }
        return values;
    }

    public static String evaluateXPathToValue(String expression,
                                                      OMElement root) throws Exception {
        List<String> values = evaluateXPathToValues(expression, root);
        if (values == null || values.size() == 0) {
            return null;
        }
        return values.get(0);
    }
}
