/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.jvnet.ws.wadl.ast.InvalidWADLException;
import org.jvnet.ws.wadl.ast.WadlAstBuilder;
import org.jvnet.ws.wadl.util.MessageListener;
import org.w3c.dom.Element;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.Repository;
import org.wso2.carbon.registry.core.jdbc.VersionRepository;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.services.Utils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.registry.extensions.utils.WSDLValidationInfo;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class WADLProcessor {

    private static final Log log = LogFactory.getLog(WADLProcessor.class);
    private static final String WADL_EXTENSION = ".wadl";
    private String wadlMediaType = "application/wadl+xml";
    private String xsdMediaType = "application/xsd+xml";
    private static String commonWADLLocation = "/wadls/";
    private static String commonSchemaLocation = "/schemas/";

    private Registry registry;
    private Repository repository;
    private VersionRepository versionRepository;
    private boolean createService = true;
    private List<String> importedSchemas = new LinkedList<String>();
    private OMElement wadlElement;

    public WADLProcessor(RequestContext requestContext) {
        registry = requestContext.getRegistry();
        repository = requestContext.getRepository();
        versionRepository = requestContext.getVersionRepository();
    }

    public boolean getCreateService() {
        return createService;
    }

    public void setCreateService(boolean createService) {
        this.createService = createService;
    }

    public static String getCommonSchemaLocation() {
        return commonSchemaLocation;
    }

    public static void setCommonSchemaLocation(String commonSchemaLocation) {
        WADLProcessor.commonSchemaLocation = commonSchemaLocation;
    }

    public static String getCommonWADLLocation() {
        return commonWADLLocation;
    }

    public static void setCommonWADLLocation(String commonWADLLocation) {
        WADLProcessor.commonWADLLocation = commonWADLLocation;
    }

    public String addWadlToRegistry(RequestContext requestContext, Resource resource,
                                    String resourcePath,boolean skipValidation)
            throws RegistryException {
        String wadlName = RegistryUtils.getResourceName(resourcePath);
        String version = requestContext.getResource().getProperty(RegistryConstants.VERSION_PARAMETER_NAME);

        if (version == null){
            version = CommonConstants.WADL_VERSION_DEFAULT_VALUE;
            requestContext.getResource().setProperty(RegistryConstants.VERSION_PARAMETER_NAME, version);
        }

        String wadlContent;
        Object resourceContent = resource.getContent();
        if (resourceContent instanceof String) {
            wadlContent = (String) resourceContent;
        } else {
            wadlContent = new String((byte[]) resourceContent);
        }

        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().
                    createXMLStreamReader(new StringReader(wadlContent));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            wadlElement = builder.getDocumentElement();
        } catch (XMLStreamException e) {
            //This exception is unexpected because the WADL already validated
            String msg = "Unexpected error occured " +
                    "while reading the WADL at " + resourcePath + ".";
            log.error(msg);
            throw new RegistryException(msg, e);
        }

        String wadlNamespace = wadlElement.getNamespace().getNamespaceURI();
        String actualPath = getWadlLocation(requestContext,wadlElement,wadlName,version);

        OMElement grammarsElement = wadlElement.
                getFirstChildWithName(new QName(wadlNamespace, "grammars"));

        if (StringUtils.isNotBlank(requestContext.getSourceURL())) {
            String uri = requestContext.getSourceURL();
            if (!skipValidation) {
                validateWADL(uri);
            }

            if (resource.getUUID() == null) {
                resource.setUUID(UUID.randomUUID().toString());
            }

            String wadlBaseUri = uri.substring(0, uri.lastIndexOf("/") + 1);
            if (grammarsElement != null) {
                //This is to avoid evaluating the grammars import when building AST
                grammarsElement.detach();
                wadlElement.addChild(resolveImports(grammarsElement, wadlBaseUri, version, requestContext.getResource().getProperties()));
            }
        } else {
            if (!skipValidation) {
                File tempFile = null;
                BufferedWriter bufferedWriter = null;
                FileWriter fileWriter = null;
                try {
                    tempFile = File.createTempFile(wadlName, null);
                    fileWriter = new FileWriter(tempFile);
                    bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write(wadlElement.toString());
                    bufferedWriter.flush();
                } catch (IOException e) {
                    String msg = "Error occurred while reading the WADL "+ wadlName +" file";
                    log.error(msg, e);
                    throw new RegistryException(msg, e);
                } finally {
                    if (fileWriter != null){
                        try {
                            fileWriter.close();
                        } catch (IOException e) {
                            String msg = "Error occurred while closing "+ wadlName +" file writer";
                            log.warn(msg, e);
                        }
                    }
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e) {
                            String msg = "Error occurred while closing WADL "+ wadlName +" file writer";
                            log.warn(msg, e);
                        }
                    }
                }
                validateWADL(tempFile.toURI().toString());
                try {
                    delete(tempFile);
                } catch (IOException e) {
                    String msg = "An error occurred while deleting the temporary files from local file system.";
                    log.warn(msg, e);
                    throw new RegistryException(msg, e);
                }
            }

            if (grammarsElement != null) {
                grammarsElement = resolveImports(grammarsElement, null, version, requestContext.getResource().getProperties());
                wadlElement.addChild(grammarsElement);
            }
        }

        requestContext.setResourcePath(new ResourcePath(actualPath));
        if (resource.getProperty(CommonConstants.SOURCE_PROPERTY) == null){
            resource.setProperty(CommonConstants.SOURCE_PROPERTY, CommonConstants.SOURCE_AUTO);
        }
        registry.put(actualPath, resource);
        addImportAssociations(actualPath);
        if(getCreateService()){
            OMElement serviceElement = RESTServiceUtils.createRestServiceArtifact(wadlElement, wadlName, version,
                    RegistryUtils.getRelativePath(requestContext.getRegistryContext(), actualPath));
            String servicePath = RESTServiceUtils.addServiceToRegistry(requestContext, serviceElement);
	        registry.addAssociation(servicePath, actualPath, CommonConstants.DEPENDS);
	        registry.addAssociation(actualPath, servicePath, CommonConstants.USED_BY);
	        String endpointPath = saveEndpointElement(requestContext, servicePath, version);
            if (StringUtils.isNotBlank(endpointPath)) {
                CommonUtil.addDependency(registry, actualPath, endpointPath);
            }
        }

        return resource.getPath();
    }

    /**
     * This method try to delete the temporary file,
     * If it fails it will just log a warning msg.
     *
     * @param file
     * @throws IOException
     */
    private void delete(File file) throws IOException {
        if (file != null && file.exists() && !file.delete()) {
            log.warn("Failed to delete file/directory at path: " + file.getAbsolutePath());
        }
    }

    public String importWADLToRegistry(RequestContext requestContext, String commonLocation, boolean skipValidation)
            throws RegistryException {

        ResourcePath resourcePath = requestContext.getResourcePath();
        String wadlName = RegistryUtils.getResourceName(resourcePath.getPath());

        if(!wadlName.endsWith(WADL_EXTENSION)) {
            wadlName += WADL_EXTENSION;
        }

        String version = requestContext.getResource().getProperty(RegistryConstants.VERSION_PARAMETER_NAME);

        if (version == null) {
            version = CommonConstants.WADL_VERSION_DEFAULT_VALUE;
            requestContext.getResource().setProperty(RegistryConstants.VERSION_PARAMETER_NAME, version);
        }

        String uri = requestContext.getSourceURL();
        if (!skipValidation) {
            validateWADL(uri);
        }

        Registry registry = requestContext.getRegistry();
        Resource resource = registry.newResource();
        if (resource.getUUID() == null) {
            resource.setUUID(UUID.randomUUID().toString());
        }
        resource.setMediaType(wadlMediaType);
        resource.setProperties(requestContext.getResource().getProperties());

        ByteArrayOutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            inputStream = new URL(uri).openStream();

            outputStream = new ByteArrayOutputStream();
            int nextChar;
            while ((nextChar = inputStream.read()) != -1) {
                outputStream.write(nextChar);
            }
            outputStream.flush();
            wadlElement = AXIOMUtil.stringToOM(new String(outputStream.toByteArray()));
            // to validate XML
            wadlElement.toString();
        } catch (Exception e) {
            //This exception is unexpected because the WADL already validated
            throw new RegistryException("Unexpected error occured " +
                    "while reading the WADL at" + uri, e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    String msg = "Error while closing  outputStream";
                    log.warn(msg);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    String msg = "Error while closing  inputStream";
                    log.warn(msg);
                }
            }
        }

        String wadlNamespace = wadlElement.getNamespace().getNamespaceURI();

        OMElement grammarsElement = wadlElement.
                getFirstChildWithName(new QName(wadlNamespace, "grammars"));
        String wadlBaseUri = uri.substring(0, uri.lastIndexOf("/") + 1);
        if (grammarsElement != null) {
            grammarsElement.detach();
            wadlElement.addChild(resolveImports(grammarsElement, wadlBaseUri, version,
                    requestContext.getResource().getProperties()));
        }

        String actualPath;
        //        if(commonLocation != null){
        //            actualPath = commonLocation + namespaceSegment + version + "/" + wadlName;
        //        } else {
        //            actualPath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
        //                    commonWADLLocation + namespaceSegment  + version + "/" + wadlName;
        actualPath = getWadlLocation(requestContext, wadlElement, wadlName, version);
        //        }
        if (resource.getProperty(CommonConstants.SOURCE_PROPERTY) == null) {
            resource.setProperty(CommonConstants.SOURCE_PROPERTY, CommonConstants.SOURCE_AUTO);
        }

        resource.setContent(wadlElement.toString());
        requestContext.setResourcePath(new ResourcePath(actualPath));
        registry.put(actualPath, resource);
        addImportAssociations(actualPath);
        if (createService) {
            OMElement serviceElement = RESTServiceUtils.createRestServiceArtifact(wadlElement, wadlName, version,
                    RegistryUtils.getRelativePath(requestContext.getRegistryContext(), actualPath));
            String servicePath = RESTServiceUtils.addServiceToRegistry(requestContext, serviceElement);
            CommonUtil.addDependency(registry, servicePath, actualPath);
            String endpointPath = saveEndpointElement(requestContext, servicePath, version);
            if (StringUtils.isNotBlank(endpointPath)) {
                CommonUtil.addDependency(registry, actualPath, endpointPath);
            }
        }

        return actualPath;
    }

    /**
     * Save endpoint element to the registry.
     *
     * @param requestContext        information about the current request.
     * @param servicePath           service path.
     * @param version               service version.
     * @throws RegistryException    If fails to save the endpoint.
     * @return                      Endpoint path
     */
    public String saveEndpointElement(RequestContext requestContext, String servicePath, String version)
            throws RegistryException {
        String endpointPath = createEndpointElement(requestContext, wadlElement, version, servicePath);
        if (StringUtils.isNotBlank(endpointPath)) {
            CommonUtil.addDependency(registry, servicePath, endpointPath);
            return endpointPath;
        }
        return null;
    }

    private OMElement resolveImports(OMElement grammarsElement,
                                String wadlBaseUri, String wadlVersion, Properties props) throws RegistryException {
        String wadlNamespace = grammarsElement.getNamespace().getNamespaceURI();
        Iterator<OMElement> grammarElements = grammarsElement.
                getChildrenWithName(new QName(wadlNamespace, "include"));
        while (grammarElements.hasNext()){
            OMElement childElement = grammarElements.next();
            OMAttribute refAttr = childElement.getAttribute(new QName("href"));
            String importUrl = refAttr.getAttributeValue();
            if(importUrl.endsWith(".xsd")) {
                if(!importUrl.startsWith("http")){
                    if (registry.resourceExists(importUrl)) {
                        continue;
                    } else {
                        if (wadlBaseUri != null) {
                            importUrl = wadlBaseUri + importUrl;
                        }
                    }
                }
                String schemaPath = saveSchema(importUrl, wadlVersion, props);
                importedSchemas.add(schemaPath);
                refAttr.setAttributeValue(schemaPath);
                childElement.addAttribute(refAttr);
            }
        }
        return grammarsElement;
    }

    private void validateWADL(String uri) throws RegistryException {
        WadlAstBuilder builder = new WadlAstBuilder(
                new WadlAstBuilder.SchemaCallback() {

                    public void processSchema(InputSource is) {
                        try {

                        } finally {
                            if (is != null && is.getByteStream() != null) {
                                try {

                                    is.getByteStream().close();
                                } catch (IOException e) {
                                    String msg = "Error while closing  InputSource";
                                    log.warn(msg);
                                }
                            }
                        }
                    }

                    public void processSchema(String uri, Element node) {
                    }
                },
                new MessageListener() {

                    public void warning(String message, Throwable throwable) {
                    }

                    public void info(String message) {
                    }

                    public void error(String message, Throwable throwable) {
                    }
                });

        try {
            builder.buildAst(new URI(uri));
        } catch (ConnectException e) {
            String msg = "Invalid WADL uri found " + uri;
            throw new RegistryException(msg, e);
        } catch (InvalidWADLException e){
            String msg = "Invalid WADL definition found";
            throw new RegistryException(msg, e);
        } catch (FileNotFoundException e) {
            String msg = "WADL not found";
            throw new RegistryException(msg, e);
        } catch (Exception e) {
            String msg = "Unexpected error occured while adding WADL from " + uri;
            throw new RegistryException(msg, e);
        }
    }

    private String saveSchema(String schemaUrl, String version, Properties props) throws RegistryException {
        if(schemaUrl != null){
            RequestContext requestContext =
                    new RequestContext(registry, repository, versionRepository);
            Resource local = requestContext.getRegistry().newResource();
            local.setMediaType(xsdMediaType);
            local.setProperty(CommonConstants.SOURCE_PROPERTY, CommonConstants.SOURCE_AUTO);
            local.setProperties(props);
            requestContext.setSourceURL(schemaUrl);
            requestContext.setResource(local);

            String xsdName = schemaUrl;
            if (xsdName.lastIndexOf("/") != -1) {
                xsdName = xsdName.substring(xsdName.lastIndexOf("/"));
            } else {
                xsdName = "/" + xsdName;
            }
            String path = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + xsdName;
            requestContext.setResourcePath(new ResourcePath(path));
            WSDLValidationInfo validationInfo;
            try {
                validationInfo = SchemaValidator.validate(new XMLInputSource(null, schemaUrl, null));
            } catch (Exception e) {
                throw new RegistryException("Exception occured while validating the schema" , e);
            }

            SchemaProcessor schemaProcessor = new SchemaProcessor(requestContext, validationInfo);
            try {
                return schemaProcessor.importSchemaToRegistry(requestContext, path,
                        getChrootedSchemaLocation(requestContext.getRegistryContext()), true, true);
            } catch (RegistryException e) {
                throw new RegistryException("Failed to import the schema" , e);
            }

        }
        return null;
    }

	/**
	 * Creates endpoint element for REST service
	 *
	 * @param requestContext        information about current request.
	 * @param wadlElement           wadl document.
	 * @param version               wadl version.
	 * @return                      Endpoint Path.
	 * @throws RegistryException    If fails to create endpoint element.
	 */
    private String createEndpointElement(RequestContext requestContext, OMElement wadlElement, String version,
            String servicePath) throws RegistryException {
        OMNamespace wadlNamespace = wadlElement.getNamespace();
        String wadlNamespaceURI = wadlNamespace.getNamespaceURI();
        String wadlNamespacePrefix = wadlNamespace.getPrefix();
        OMElement resourcesElement = wadlElement
                .getFirstChildWithName(new QName(wadlNamespaceURI, "resources", wadlNamespacePrefix));
        if (resourcesElement != null) {
            String endpointUrl = resourcesElement.getAttributeValue(new QName("base"));
            if (!StringUtils.isBlank(endpointUrl)) {
                String endpointPath = EndpointUtils.deriveEndpointFromUrl(endpointUrl);
                String endpointName = EndpointUtils.deriveEndpointNameWithNamespaceFromUrl(endpointUrl);
                String endpointContent = EndpointUtils
                        .getEndpointContentWithOverview(endpointUrl, endpointPath, endpointName, version);
                OMElement endpointElement;
                EndpointUtils.addEndpointToService(requestContext.getRegistry(), servicePath, endpointUrl, "");
                try {
                    endpointElement = AXIOMUtil.stringToOM(endpointContent);
                } catch (XMLStreamException e) {
                    throw new RegistryException("Error in creating the endpoint element. ", e);
                }

                return RESTServiceUtils.addEndpointToRegistry(requestContext, endpointElement, endpointPath);
            } else {
                log.warn("Base path does not exist. endpoint creation may fail. ");
            }
        } else {
            log.warn("Resources element is null. ");
        }
        return null;
    }

    private void addImportAssociations(String path) throws RegistryException {
        for (String schema : importedSchemas) {
            CommonUtil.addDependency(registry, path, schema);
        }
    }

    private String getChrootedWadlLocation(RegistryContext registryContext) {
        return RegistryUtils.getAbsolutePath(registryContext,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + getCommonWADLLocation());
    }

    private String getChrootedSchemaLocation(RegistryContext registryContext) {
        return RegistryUtils.getAbsolutePath(registryContext,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + getCommonSchemaLocation());
    }

    private String getWadlLocation(RequestContext context, OMElement wadlElement, String wadlName,
                                   String version) {
        if (Utils.getRxtService() != null) {
            String pathExpression = Utils.getRxtService().getStoragePath(wadlMediaType);
            pathExpression = CommonUtil.replaceExpressionOfPath(pathExpression, "name", wadlName);
            pathExpression = CommonUtil.getPathFromPathExpression(pathExpression,
                                                                  context.getResource().getProperties(), null);
            String namespace = CommonUtil.derivePathFragmentFromNamespace(
                    wadlElement.getNamespace().getNamespaceURI()).replace("//", "/");
            namespace = namespace.replace(".", "/");
            pathExpression = CommonUtil.replaceExpressionOfPath(pathExpression, "namespace", namespace);
            pathExpression = pathExpression.replace("//", "/");
            pathExpression = CommonUtil.replaceExpressionOfPath(pathExpression, "version", version);
            String wadlPath = RegistryUtils.getAbsolutePath(context.getRegistryContext(), pathExpression.replace("//", "/"));
            /**
             * Fix for the REGISTRY-3052 : validation is to check the whether this invoked by ZIPWSDLMediaTypeHandler
             * Setting the registry and absolute paths to current session to avoid incorrect resource path entry in REG_LOG table
             */
            if (CurrentSession.getLocalPathMap() != null && !Boolean.valueOf(CurrentSession.getLocalPathMap().get(CommonConstants.ARCHIEVE_UPLOAD))) {
                wadlPath = CommonUtil.getRegistryPath(context.getRegistry().getRegistryContext(), wadlPath);
                CurrentSession.getLocalPathMap().remove(context.getResourcePath().getCompletePath());
                if (log.isDebugEnabled()) {
                    log.debug("Saving current session local paths, key: " + wadlPath + " | value: " + pathExpression);
                }
                CurrentSession.getLocalPathMap().put(wadlPath, pathExpression);
            }
            return wadlPath;
        } else {
            String wadlNamespace = wadlElement.getNamespace().getNamespaceURI();
            String namespaceSegment = CommonUtil.derivePathFragmentFromNamespace(
                    wadlNamespace).replace("//", "/");
            String actualPath = getChrootedWadlLocation(context.getRegistryContext()) +
                                namespaceSegment + version  + "/" + wadlName;
            return actualPath;
        }

    }
}
