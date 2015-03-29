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
package org.wso2.carbon.registry.extensions.handlers.utils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.*;

public class EndpointUtils {
    private static final Log log = LogFactory.getLog(EndpointUtils.class);

    private static final String SOAP11_ENDPOINT_EXPR = "/wsdl:definitions/wsdl:service/wsdl:port/soap:address";
    private static final String SOAP12_ENDPOINT_EXPR = "/wsdl:definitions/wsdl:service/wsdl:port/soap12:address";
    private static final String HTTP_ENDPOINT_EXPR = "/wsdl:definitions/wsdl:service/wsdl:port/http:address";
    private static final String SERVICE_ENDPOINT_ENTRY_EXPR = "/s:metadata/s:endpoints/s:entry";
    private static final String SERVICE_ENDPOINT_EXPR = "/s:metadata/s:endpoints";
    private static final String SERVICE_ENDPOINTS_ELEMENT = "endpoints";
    private static final String SERVICE_ENDPOINTS_ENTRY_ELEMENT = "entry";
    private static final String LOCATION_ATTR = "location";

    private static final String SYNAPSE_NAMESPACE = "http://ws.apache.org/ns/synapse";
    private static final String SYNAPSE_NAMESPACE_PREFIX = "ns";
    private static final String SYNAPSE_ENDPOINT = "endpoint";
    private static final String SYNAPSE_ENDPOINT_NAME_ATTRIBUTE = "name";
    private static final String SYNAPSE_ENDPOINT_ADDRESS = "address";
    private static final String SYNAPSE_ENDPOINT_ADDRESS_URI_ATTRIBUTE = "uri";

    private static final String ENDPOINT_DEFAULT_LOCATION = "/trunk/endpoints/";
    private static String endpointLocation = ENDPOINT_DEFAULT_LOCATION;
    private static String endpointMediaType = CommonConstants.ENDPOINT_MEDIA_TYPE;

    public static void setEndpointLocation(String endpointLocation) {
        EndpointUtils.endpointLocation = endpointLocation;
    }

    public static String getEndpointLocation() {
        return endpointLocation;
    }

    public static void setEndpointMediaType(String mediaType) {
        endpointMediaType = mediaType;
    }

    public static String getEndpointMediaType() {
        return endpointMediaType;
    }

    public static void removeEndpointEntry(String oldWSDL, OMElement serviceElement, Registry registry)
            throws RegistryException {
        List<OMElement> serviceEndpointEntryElements;
        try {
            serviceEndpointEntryElements = evaluateXPathToElements(SERVICE_ENDPOINT_ENTRY_EXPR, serviceElement);
        } catch (Exception e) {
            String msg = "Error in evaluating xpath expressions to extract endpoints";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        if (serviceEndpointEntryElements == null || serviceEndpointEntryElements.size() == 0) {
            return;
        }
        for(OMElement endpointOmElement : serviceEndpointEntryElements){
            if(endpointOmElement!=null){
                endpointOmElement.detach();
            }
        }
    }
    public static void saveEndpointsFromWSDL(String wsdlPath, Resource wsdlResource,
                                      Registry registry, Registry systemRegistry)
            throws RegistryException {
        // building the wsdl element.
        byte[] wsdlContentBytes = (byte[])wsdlResource.getContent();
        if (wsdlContentBytes == null) {
            return;
        }
        OMElement wsdlElement;
        try {
            wsdlElement = buildOMElement(RegistryUtils.decodeBytes(wsdlContentBytes));
        } catch (Exception e) {
            String msg = "Error in building the wsdl element for path: " + wsdlPath + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        // saving soap11 endpoints
        List<OMElement> soap11Elements;
        try {
            soap11Elements =  evaluateXPathToElements(SOAP11_ENDPOINT_EXPR, wsdlElement);
        } catch (Exception e) {
            String msg = "Error in evaluating xpath expressions to extract endpoints, wsdl path: " + wsdlPath + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        for (OMElement soap11Element: soap11Elements) {
            String locationUrl = soap11Element.getAttributeValue(new QName(LOCATION_ATTR));
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(CommonConstants.SOAP11_ENDPOINT_ATTRIBUTE, "true");
            saveEndpoint(registry, locationUrl, wsdlPath, properties, systemRegistry);
        }

        // saving soap12 endpoints
        List<OMElement> soap12Elements;
        try {
            soap12Elements =  evaluateXPathToElements(SOAP12_ENDPOINT_EXPR, wsdlElement);
        } catch (Exception e) {
            String msg = "Error in evaluating xpath expressions to extract endpoints, wsdl path: " + wsdlPath + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        for (OMElement soap12Element: soap12Elements) {
            String locationUrl = soap12Element.getAttributeValue(new QName(LOCATION_ATTR));
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(CommonConstants.SOAP12_ENDPOINT_ATTRIBUTE, "true");
            saveEndpoint(registry, locationUrl, wsdlPath, properties, systemRegistry);
        }

        // saving http endpoints
        List<OMElement> httpElements;
        try {
            httpElements =  evaluateXPathToElements(HTTP_ENDPOINT_EXPR, wsdlElement);
        } catch (Exception e) {
            String msg = "Error in evaluating xpath expressions to extract endpoints, wsdl path: " + wsdlPath + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        for (OMElement httpElement: httpElements) {
            String locationUrl = httpElement.getAttributeValue(new QName(LOCATION_ATTR));
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(CommonConstants.HTTP_ENDPOINT_ATTRIBUTE, "true");
            saveEndpoint(registry, locationUrl, wsdlPath, properties, systemRegistry);
        }
    }
    public static void saveEndpointsFromWSDL(String wsdlPath, Resource wsdlResource,
                                      Registry registry, Registry systemRegistry,String environment
            ,List<String> dependencies,String version) throws RegistryException {
        // building the wsdl element.
        byte[] wsdlContentBytes = (byte[])wsdlResource.getContent();
        if (wsdlContentBytes == null) {
            return;
        }
        OMElement wsdlElement;
        try {
            wsdlElement = buildOMElement(RegistryUtils.decodeBytes(wsdlContentBytes));
        } catch (Exception e) {
            String msg = "Error in building the wsdl element for path: " + wsdlPath + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        // saving soap11 endpoints
        List<OMElement> soap11Elements;
        try {
            soap11Elements =  evaluateXPathToElements(SOAP11_ENDPOINT_EXPR, wsdlElement);
        } catch (Exception e) {
            String msg = "Error in evaluating xpath expressions to extract endpoints, wsdl path: " + wsdlPath + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        for (OMElement soap11Element: soap11Elements) {
            String locationUrl = soap11Element.getAttributeValue(new QName(LOCATION_ATTR));
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(CommonConstants.SOAP11_ENDPOINT_ATTRIBUTE, "true");
            saveEndpoint(registry, locationUrl, wsdlPath, properties, systemRegistry,environment,dependencies,version);
        }

        // saving soap12 endpoints
        List<OMElement> soap12Elements;
        try {
            soap12Elements =  evaluateXPathToElements(SOAP12_ENDPOINT_EXPR, wsdlElement);
        } catch (Exception e) {
            String msg = "Error in evaluating xpath expressions to extract endpoints, wsdl path: " + wsdlPath + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        for (OMElement soap12Element: soap12Elements) {
            String locationUrl = soap12Element.getAttributeValue(new QName(LOCATION_ATTR));
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(CommonConstants.SOAP12_ENDPOINT_ATTRIBUTE, "true");
            saveEndpoint(registry, locationUrl, wsdlPath, properties, systemRegistry,environment,dependencies,version);
        }

        // saving http endpoints
        List<OMElement> httpElements;
        try {
            httpElements =  evaluateXPathToElements(HTTP_ENDPOINT_EXPR, wsdlElement);
        } catch (Exception e) {
            String msg = "Error in evaluating xpath expressions to extract endpoints, wsdl path: " + wsdlPath + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        for (OMElement httpElement: httpElements) {
            String locationUrl = httpElement.getAttributeValue(new QName(LOCATION_ATTR));
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(CommonConstants.HTTP_ENDPOINT_ATTRIBUTE, "true");
            saveEndpoint(registry, locationUrl, wsdlPath, properties, systemRegistry,environment,dependencies,version);
        }
    }

    public static void saveEndpointsFromServices(String servicePath, OMElement serviceElement,
                                      Registry registry, Registry systemRegistry)
            throws RegistryException {

        // first iterate through soap11 endpoints
        // saving soap11 endpoints
        List<OMElement> serviceEndpointEntryElements;
        try {
            serviceEndpointEntryElements =  evaluateXPathToElements(SERVICE_ENDPOINT_ENTRY_EXPR, serviceElement);
        } catch (Exception e) {
            String msg = "Error in evaluating xpath expressions to extract endpoints, " +
                    "service path: " + servicePath + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        // and add the associations and before adding them first remove all the endpoint dependencies
        removeEndpointDependencies(servicePath, registry);

        // iterate through the new endpoints..
        for (OMElement endpointElement: serviceEndpointEntryElements) {
            Map<String, String> properties = new HashMap<String, String>();

            String entryText = endpointElement.getText();
            String entryKey = null;
            String entryVal;
            int colonIndex = entryText.indexOf(":");
            if (colonIndex < entryText.length()- 1) {
                entryKey = entryText.substring(0, colonIndex);
                entryText = entryText.substring(colonIndex + 1);
            }
            entryVal = entryText;

            if (!"".equals(entryKey)) {
                // here the key is the environment

                String endpointPath = RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                        org.wso2.carbon.registry.core.RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                endpointLocation) + deriveEndpointFromUrl(entryVal);

                String existingEnv = null;

                if (registry.resourceExists(endpointPath)) {
                    registry.get(endpointPath).removeProperty(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR);
                }
                existingEnv = entryKey;
                properties.put(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR, existingEnv);
            }

            // the entry value is the url
            saveEndpoint(registry, entryVal, servicePath, properties, systemRegistry);
        }

        // and we are getting the endpoints of all the attached wsdls.

        addAssociations(servicePath, registry);
    }
    public static void saveEndpointsFromServices(String servicePath, OMElement serviceElement,
                                      Registry registry, Registry systemRegistry,String environment)
            throws RegistryException {
        if (!CommonUtil.isAddingAssociationLockAvailable()) {
            return;
        }
        CommonUtil.acquireAddingAssociationLock();
        try {
            // first iterate through soap11 endpoints
            // saving soap11 endpoints
            List<OMElement> serviceEndpointEntryElements;
            try {
                serviceEndpointEntryElements =  evaluateXPathToElements(SERVICE_ENDPOINT_ENTRY_EXPR, serviceElement);
            } catch (Exception e) {
                String msg = "Error in evaluating xpath expressions to extract endpoints, " +
                        "service path: " + servicePath + ".";
                log.error(msg, e);
                throw new RegistryException(msg, e);
            }

            // and add the associations and before adding them first remove all the endpoint dependencies
            removeEndpointDependencies(servicePath, registry);

            // iterate through the new endpoints..
            for (OMElement endpointElement: serviceEndpointEntryElements) {
                Map<String, String> properties = new HashMap<String, String>();

                String entryText = endpointElement.getText();
                String entryKey = null;
                String entryVal;
                int colonIndex = entryText.indexOf(":");
                if (colonIndex < entryText.length()- 1) {
                    entryKey = entryText.substring(0, colonIndex);
                    entryText = entryText.substring(colonIndex + 1);
                }
                entryVal = entryText;

                if (!"".equals(entryKey)) {
                    // here the key is the environment

                    String endpointPath = RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                            org.wso2.carbon.registry.core.RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                    environment) + deriveEndpointFromUrl(entryVal);

                    String existingEnv = null;

                    if (registry.resourceExists(endpointPath)) {
                        registry.get(endpointPath).removeProperty(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR);
                    }
                    existingEnv = entryKey;
                    properties.put(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR, existingEnv);
                }

                // the entry value is the url
                saveEndpoint(registry, entryVal, servicePath, properties, systemRegistry,environment);
            }
        } finally {
            CommonUtil.releaseAddingAssociationLock();
        }

        // and we are getting the endpoints of all the attached wsdls.

        addAssociations(servicePath, registry);
    }

    private static void addAssociations(String servicePath, Registry registry) throws RegistryException {
        Association[] associations = registry.getAssociations(servicePath, CommonConstants.DEPENDS);
        for (Association association: associations) {
            String targetPath = association.getDestinationPath();
            if (registry.resourceExists(targetPath)) {
                Resource targetResource = registry.get(targetPath);
                if (CommonConstants.WSDL_MEDIA_TYPE.equals(targetResource.getMediaType())) {
                    // for the wsdl, we are getting all the endpoints
                    Association[] wsdlAssociations = registry.getAssociations(targetPath,
                            CommonConstants.DEPENDS);
                    for (Association wsdlAssociation: wsdlAssociations) {
                        String wsdlTargetPath = wsdlAssociation.getDestinationPath();
                        if (registry.resourceExists(wsdlTargetPath)) {
                            Resource wsdlTargetResource = registry.get(wsdlTargetPath);
                            if (CommonConstants.ENDPOINT_MEDIA_TYPE.equals(
                                    wsdlTargetResource.getMediaType())) {
                                // so it is the wsdl associated to endpoints,
                                // so we associate these endpoints to the services as well.
                                registry.addAssociation(servicePath, wsdlTargetPath,
                                        CommonConstants.DEPENDS);
                                registry.addAssociation(wsdlTargetPath, servicePath,
                                        CommonConstants.USED_BY);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void removeEndpointDependencies(String servicePath, Registry registry) throws RegistryException {
        // update lock check removed from for loop to prevent the database lock
            Association[] associations = registry.getAllAssociations(servicePath);
            for (Association association : associations) {
                String path = association.getDestinationPath();
                if (registry.resourceExists(path)) {
                    Resource endpointResource = registry.get(path);
                    if (CommonConstants.ENDPOINT_MEDIA_TYPE.equals(endpointResource.getMediaType())) {
                        registry.removeAssociation(servicePath, path, CommonConstants.DEPENDS);
                        registry.removeAssociation(path, servicePath, CommonConstants.USED_BY);
                    }
                }
            }
    }

    private static String[] wsdlPrefixes = {
            "wsdl", "http://schemas.xmlsoap.org/wsdl/",
            "wsdl2", "http://www.w3.org/ns/wsdl",
            "xsd", "http://www.w3.org/2001/XMLSchema",
            "soap", "http://schemas.xmlsoap.org/wsdl/soap/",
            "soap12", "http://schemas.xmlsoap.org/wsdl/soap12/",
            "http", "http://schemas.xmlsoap.org/wsdl/http/",
            "s", CommonConstants.SERVICE_ELEMENT_NAMESPACE,
    };

    private static List<OMElement> evaluateXPathToElements(String expression,
                                                           OMElement root) throws Exception {
        String[] nsPrefixes = wsdlPrefixes;
        AXIOMXPath xpathExpression = new AXIOMXPath(expression);

        for (int j = 0; j < nsPrefixes.length; j ++) {
            xpathExpression.addNamespace(nsPrefixes[j++], nsPrefixes[j]);
        }
        return (List<OMElement>)xpathExpression.selectNodes(root);
    }

    private static OMElement buildOMElement(String content) throws Exception {
        XMLStreamReader parser;
        try {
            parser = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(content));
        } catch (XMLStreamException e) {
            String msg = "Error in initializing the parser to build the OMElement.";
            throw new Exception(msg, e);
        }

        //create the builder
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        //get the root element (in this case the envelope)

        return builder.getDocumentElement();
    }

    private static void saveEndpoint(Registry registry, String url,
                                   String associatedPath, Map<String, String> properties,
                                   Registry systemRegistry,String environment) throws RegistryException {
        String urlToPath = deriveEndpointFromUrl(url);

        String endpointAbsoluteBasePath = RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                org.wso2.carbon.registry.core.RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                environment);
        if (!systemRegistry.resourceExists(endpointAbsoluteBasePath)) {
            systemRegistry.put(endpointAbsoluteBasePath, systemRegistry.newCollection());
        }
        String relativePath = environment + urlToPath;
        String endpointAbsolutePath = endpointAbsoluteBasePath + urlToPath;

        saveEndpointValues(registry, url, associatedPath, properties, systemRegistry, relativePath, endpointAbsolutePath);
    }
    private static void saveEndpoint(Registry registry, String url,
                                   String associatedPath, Map<String, String> properties,
                                   Registry systemRegistry,String environment,List<String> dependencies,String version) throws RegistryException {
        String urlToPath = deriveEndpointFromUrl(url);

        String endpointAbsoluteBasePath = RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                environment);
        if (!systemRegistry.resourceExists(endpointAbsoluteBasePath)) {
            systemRegistry.put(endpointAbsoluteBasePath, systemRegistry.newCollection());
        }

        String prefix = urlToPath.substring(0,urlToPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) +1 );
        String name = urlToPath.replace(prefix,"");

        String regex = endpointAbsoluteBasePath + prefix + "[\\d].[\\d].[\\d]" + RegistryConstants.PATH_SEPARATOR + name;

        for (String dependency : dependencies) {
            if(dependency.matches(regex)){
                String newRelativePath =  RegistryUtils.getRelativePathToOriginal(dependency,
                        org.wso2.carbon.registry.core.RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH );
                saveEndpointValues(registry, url, associatedPath, properties, systemRegistry, newRelativePath, dependency);
                return;
            }
        }
        String endpointAbsolutePath = environment + prefix + version + RegistryConstants.PATH_SEPARATOR + name;
        String relativePath = environment.substring(0,RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH.length())
                + prefix + version + RegistryConstants.PATH_SEPARATOR + name;

        saveEndpointValues(registry, url, associatedPath, properties, systemRegistry, relativePath, endpointAbsolutePath);
    }
    private static void saveEndpoint(Registry registry, String url,
                                   String associatedPath, Map<String, String> properties,
                                   Registry systemRegistry) throws RegistryException {
        String urlToPath = deriveEndpointFromUrl(url);

        String endpointAbsoluteBasePath = RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                org.wso2.carbon.registry.core.RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                endpointLocation);
        if (!systemRegistry.resourceExists(endpointAbsoluteBasePath)) {
            systemRegistry.put(endpointAbsoluteBasePath, systemRegistry.newCollection());
        }
        if(endpointLocation.endsWith(RegistryConstants.PATH_SEPARATOR)){
            if(urlToPath.startsWith(RegistryConstants.PATH_SEPARATOR)){
                urlToPath = urlToPath.replaceFirst(RegistryConstants.PATH_SEPARATOR,"");
            }
        }else{
            if(!urlToPath.startsWith(RegistryConstants.PATH_SEPARATOR)){
                urlToPath = RegistryConstants.PATH_SEPARATOR + urlToPath;
            }
        }
        String relativePath = endpointLocation + urlToPath;
        String endpointAbsolutePath = endpointAbsoluteBasePath + urlToPath;

        saveEndpointValues(registry, url, associatedPath, properties, systemRegistry, relativePath, endpointAbsolutePath);
    }

    private static void saveEndpointValues(Registry registry, String url, String associatedPath
            , Map<String, String> properties, Registry systemRegistry, String relativePath
            , String endpointAbsolutePath) throws RegistryException {
        Resource resource;
        String endpointId = null;
        if (registry.resourceExists(endpointAbsolutePath)) {
            resource = registry.get(endpointAbsolutePath);
            endpointId = resource.getUUID();
            String existingContent;
            String newContent = getEndpointContent(url, endpointAbsolutePath);
            if(resource.getContent() != null) {
                existingContent = new String((byte[])(resource.getContent()));
                if(!existingContent.equals(newContent)) {
                    resource.setContent(RegistryUtils.encodeString(newContent));
                }
            } else {
                resource.setContent(RegistryUtils.encodeString(newContent));
            }
        }else {
            resource = registry.newResource();
            resource.setContent(RegistryUtils.encodeString(getEndpointContent(url, endpointAbsolutePath)));
        }
        boolean endpointIdCreated = false;
        if (endpointId == null) {
            endpointIdCreated = true;
            endpointId = UUID.randomUUID().toString();
            resource.setUUID(endpointId);
        }

//        CommonUtil.addGovernanceArtifactEntryWithRelativeValues(
//                systemRegistry, endpointId, relativePath);

        boolean propertiesChanged = false;
        if (properties != null) {
            for (Map.Entry<String, String> e : properties.entrySet()) {
                propertiesChanged = true;
                resource.setProperty(e.getKey(), e.getValue());
            }
        }

        if (endpointIdCreated || propertiesChanged) {
            // this will be definitely false for a brand new resource
            resource.setMediaType(endpointMediaType);
            registry.put(endpointAbsolutePath, resource);
            // we need to create a version here.
        }

        registry.addAssociation(associatedPath, endpointAbsolutePath, CommonConstants.DEPENDS);
        registry.addAssociation(endpointAbsolutePath, associatedPath, CommonConstants.USED_BY);
    }

    public static void addEndpointToService(Registry registry,
                                            String servicePath,
                                            String endpointUrl,
                                            String endpointEnv) throws RegistryException {
        Resource serviceResource = registry.get(servicePath);
        byte[] serviceBytes = (byte[])serviceResource.getContent();
        String serviceContent = RegistryUtils.decodeBytes(serviceBytes);

        OMElement serviceElement;
        try {
            serviceElement = buildOMElement(serviceContent);
        } catch (Exception e) {
            String msg = "Failed building the service element. " + servicePath + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        OMElement serviceEndpointElement;
        OMNamespace namespace = OMAbstractFactory.getOMFactory().createOMNamespace(
                CommonConstants.SERVICE_ELEMENT_NAMESPACE, null);
        try {
            List<OMElement> endpointElements = evaluateXPathToElements(
                    SERVICE_ENDPOINT_EXPR, serviceElement);
            if (endpointElements.size() == 0) {
                // we need to create the element.
                serviceEndpointElement =
                        OMAbstractFactory.getOMFactory().createOMElement(
                                SERVICE_ENDPOINTS_ELEMENT, namespace);
                serviceElement.addChild(serviceEndpointElement);
            } else {
                serviceEndpointElement = endpointElements.get(0);
            }
        } catch (Exception e) {
            String msg = "Error in getting the endpoint element of the service. " +
                    "service path: " + servicePath + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        Iterator it = serviceEndpointElement.getChildElements();
        List<String> currentEndpoints = new ArrayList<String>();
        while(it.hasNext()){
            currentEndpoints.add(((OMElement) it.next()).getText());
        }
        if(!currentEndpoints.contains(endpointEnv + ":" + endpointUrl)){
            OMElement entryElement =
                    OMAbstractFactory.getOMFactory().createOMElement(
                            SERVICE_ENDPOINTS_ENTRY_ELEMENT, namespace);
            entryElement.setText(endpointEnv + ":" + endpointUrl);
            serviceEndpointElement.addChild(entryElement);

            // now we are saving it to the registry.
            String serviceElementStr = serviceElement.toString();
            serviceResource.setContent(RegistryUtils.encodeString(serviceElementStr));
            registry.put(servicePath, serviceResource);
        }
    }

    public static void removeEndpointFromService(Registry registry,
                                            String servicePath,
                                            String endpointUrl,
                                            String endpointEnv) throws RegistryException {
        Resource serviceResource = registry.get(servicePath);
        byte[] serviceBytes = (byte[])serviceResource.getContent();
        String serviceContent = RegistryUtils.decodeBytes(serviceBytes);

        OMElement serviceElement;
        try {
            serviceElement = buildOMElement(serviceContent);
        } catch (Exception e) {
            String msg = "Failed building the service element. " + servicePath + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        OMElement serviceEndpointElement;
        OMNamespace namespace = OMAbstractFactory.getOMFactory().createOMNamespace(
                CommonConstants.SERVICE_ELEMENT_NAMESPACE, null);
        try {
            List<OMElement> endpointElements = evaluateXPathToElements(
                    SERVICE_ENDPOINT_EXPR, serviceElement);
            if (endpointElements.size() == 0) {
                // we need to create the element.
                serviceEndpointElement =
                        OMAbstractFactory.getOMFactory().createOMElement(
                                SERVICE_ENDPOINTS_ELEMENT, namespace);
                serviceElement.addChild(serviceEndpointElement);
            } else {
                serviceEndpointElement = endpointElements.get(0);
            }
        } catch (Exception e) {
            String msg = "Error in getting the endpoint element of the service. " +
                    "service path: " + servicePath + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        Iterator it = serviceEndpointElement.getChildElements();
        while(it.hasNext()){
            OMElement next = (OMElement) it.next();
            if (next.getText().equals(endpointEnv + ":" + endpointUrl)) {
                next.detach();
                // now we are saving it to the registry.
                String serviceElementStr = serviceElement.toString();
                serviceResource.setContent(RegistryUtils.encodeString(serviceElementStr));
                registry.put(servicePath, serviceResource);
                break;
            }
        }
    }

    /**
     * Returns an endpoint path for the url without the starting '/'
     * @param url the endpoint url
     * @return the path
     */
    public static String deriveEndpointFromUrl(String url) {
        final String ENDPOINT_RESOURCE_PREFIX = "ep-";
        String tempURL = url;
        if (tempURL.startsWith("jms:/")) {
            tempURL = tempURL.split("[?]")[0];
        }
        String name = tempURL.split("/")[tempURL.split("/").length - 1].replace(".","-").
                replace("=", "-").replace("@", "-").replace("#", "-").replace("~", "-");
        String[] temp = url.split("[?]")[0].split("/");
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<temp.length-1; i++){
            sb.append(temp[i]).append("/");
        }
        String urlToPath = CommonUtil.derivePathFragmentFromNamespace(sb.toString());
        // excluding extra slashes.
        if (urlToPath.length() > 1) {
            urlToPath = urlToPath.substring(1, urlToPath.length() - 1);
        }
        urlToPath += "/" + ENDPOINT_RESOURCE_PREFIX +  name;
        return urlToPath;
    }

    /**
     * Create the endpoint content
     *
     * @param endpoint endpoint URI
     * @param path endpoint location in the registry
     * @return
     * @throws RegistryException
     */
    public static String getEndpointContent(String endpoint, String path) throws RegistryException {
        String p;
        if(path.startsWith(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)){
            p = new String("gov/" + path.substring((RegistryConstants.
                GOVERNANCE_REGISTRY_BASE_PATH + ENDPOINT_DEFAULT_LOCATION).length()));
        } else {
            p = new String("gov/" + path);
        }
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement ep = factory.createOMElement(new QName(SYNAPSE_NAMESPACE , SYNAPSE_ENDPOINT, SYNAPSE_NAMESPACE_PREFIX));
        ep.addAttribute(SYNAPSE_ENDPOINT_NAME_ATTRIBUTE, p, null);
        OMElement address = factory.createOMElement(new QName(SYNAPSE_NAMESPACE_PREFIX + ":" + SYNAPSE_ENDPOINT_ADDRESS));
        address.addAttribute(SYNAPSE_ENDPOINT_ADDRESS_URI_ATTRIBUTE, endpoint, null);
        ep.addChild(address);
        return ep.toString();
    }

    /**
     * Extract endpoint URL from content
     *
     * @param endpointContent endpoint content
     * @return
     * @throws RegistryException
     */
    public static String deriveEndpointFromContent(String endpointContent) throws RegistryException {
        try {
            OMElement endpointElement = AXIOMUtil.stringToOM(endpointContent);
            OMElement addressElement = endpointElement.getFirstChildWithName
                    (new QName(SYNAPSE_NAMESPACE, SYNAPSE_ENDPOINT_ADDRESS));
            return addressElement.getAttribute(new QName(SYNAPSE_ENDPOINT_ADDRESS_URI_ATTRIBUTE)).getAttributeValue();
        } catch (Exception e) {
            throw new RegistryException("Invalid endpoint content", e);
        }
    }



}
