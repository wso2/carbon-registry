package org.wso2.carbon.registry.extensions.handlers.utils;

import org.apache.axiom.om.*;
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
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.Repository;
import org.wso2.carbon.registry.core.jdbc.VersionRepository;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.registry.extensions.utils.WSDLValidationInfo;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class WADLProcessor {

    private static final Log log = LogFactory.getLog(WADLProcessor.class);
    private String wadlMediaType = "application/wadl+xml";
    private String xsdMediaType = "application/xsd+xml";
    private static String commonWADLLocation = "/wadls/";
    private static String commonSchemaLocation = "/schemas/";

    private Registry registry;
    private Repository repository;
    private VersionRepository versionRepository;
    private boolean createService = true;
    private List<String> importedSchemas = new LinkedList<String>();

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

        OMElement wadlElement;
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
        String namespaceSegment = CommonUtil.derivePathFragmentFromNamespace(
                wadlNamespace).replace("//", "/");
        String actualPath = getChrootedWadlLocation(requestContext.getRegistryContext()) +
                namespaceSegment + version  + "/" + wadlName;

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
                wadlElement.addChild(resolveImports(grammarsElement, wadlBaseUri, version));
            }
        } else {
            if (!skipValidation) {
                File tempFile = null;
                BufferedWriter bufferedWriter = null;
                try {
                    tempFile = File.createTempFile(wadlName, null);
                    bufferedWriter = new BufferedWriter(new FileWriter(tempFile));
                    bufferedWriter.write(wadlElement.toString());
                    bufferedWriter.flush();
                } catch (IOException e) {
                    String msg = "Error occurred while reading the WADL File";
                    log.error(msg, e);
                    throw new RegistryException(msg, e);
                } finally {
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e) {
                            String msg = "Error occurred while closing File writer";
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
                grammarsElement = resolveImports(grammarsElement, null, version);
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
	                                                                              RegistryUtils.getRelativePath(
			                                                                              requestContext
					                                                                              .getRegistryContext(),
			                                                                              actualPath));
	        String servicePath = RESTServiceUtils.addServiceToRegistry(requestContext, serviceElement);
	        registry.addAssociation(servicePath, actualPath, CommonConstants.DEPENDS);
	        registry.addAssociation(actualPath, servicePath, CommonConstants.USED_BY);
	        String endpointPath = createEndpointElement(requestContext, wadlElement, version);
	        if(endpointPath != null) {
		        registry.addAssociation(servicePath, endpointPath, CommonConstants.DEPENDS);
		        registry.addAssociation(endpointPath, servicePath, CommonConstants.USED_BY);
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
        String version = requestContext.getResource().getProperty(RegistryConstants.VERSION_PARAMETER_NAME);

        if(version == null){
            version = CommonConstants.WADL_VERSION_DEFAULT_VALUE;
            requestContext.getResource().setProperty(RegistryConstants.VERSION_PARAMETER_NAME, version);
        }

        String uri = requestContext.getSourceURL();
        if(!skipValidation) {
            validateWADL(uri);
        }

        Registry registry = requestContext.getRegistry();
        Resource resource = registry.newResource();
        if (resource.getUUID() == null) {
            resource.setUUID(UUID.randomUUID().toString());
        }
        resource.setMediaType(wadlMediaType);
        resource.setProperties(requestContext.getResource().getProperties());

        ByteArrayOutputStream outputStream;
        OMElement wadlElement;
        try {
            InputStream inputStream = new URL(uri).openStream();

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
        }

        String wadlNamespace = wadlElement.getNamespace().getNamespaceURI();
        String namespaceSegment = CommonUtil.
                derivePathFragmentFromNamespace(wadlNamespace).replace("//", "/");

        OMElement grammarsElement = wadlElement.
                getFirstChildWithName(new QName(wadlNamespace, "grammars"));
        String wadlBaseUri = uri.substring(0, uri.lastIndexOf("/") + 1);
        if(grammarsElement != null){
            grammarsElement.detach();
            wadlElement.addChild(resolveImports(grammarsElement, wadlBaseUri, version));
        }

        String actualPath;
        if(commonLocation != null){
            actualPath = commonLocation + namespaceSegment + version + "/" + wadlName;
        } else {
            actualPath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                    commonWADLLocation + namespaceSegment  + version + "/" + wadlName;
        }
        if (resource.getProperty(CommonConstants.SOURCE_PROPERTY) == null){
            resource.setProperty(CommonConstants.SOURCE_PROPERTY, CommonConstants.SOURCE_AUTO);
        }

        resource.setContent(wadlElement.toString());
        requestContext.setResourcePath(new ResourcePath(actualPath));
        registry.put(actualPath, resource);
        addImportAssociations(actualPath);
        if(createService){
	        OMElement serviceElement = RESTServiceUtils.createRestServiceArtifact(wadlElement, wadlName, version,
	                                                                              RegistryUtils.getRelativePath(
			                                                                              requestContext
					                                                                              .getRegistryContext(),
			                                                                              actualPath));
            String servicePath = RESTServiceUtils.addServiceToRegistry(requestContext, serviceElement);
	        addDependency(servicePath, actualPath);
			String endpointPath = createEndpointElement(requestContext, wadlElement, version);
	        if(endpointPath != null) {
		        addDependency(servicePath, endpointPath);
	        }
        }

        return actualPath;
    }

    private OMElement resolveImports(OMElement grammarsElement,
                                String wadlBaseUri, String wadlVersion) throws RegistryException {
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
                String schemaPath = saveSchema(importUrl, wadlVersion);
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

    private String saveSchema(String schemaUrl, String version) throws RegistryException {
        if(schemaUrl != null){
            RequestContext requestContext =
                    new RequestContext(registry, repository, versionRepository);
            Resource local = requestContext.getRegistry().newResource();
            local.setMediaType(xsdMediaType);
            local.setProperty(CommonConstants.SOURCE_PROPERTY, CommonConstants.SOURCE_AUTO);
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
	private String createEndpointElement(RequestContext requestContext, OMElement wadlElement, String version)
			throws RegistryException {
		OMNamespace wadlNamespace = wadlElement.getNamespace();
		String wadlNamespaceURI = wadlNamespace.getNamespaceURI();
		String wadlNamespacePrefix = wadlNamespace.getPrefix();
		OMElement resourcesElement =
				wadlElement.getFirstChildWithName(new QName(wadlNamespaceURI, "resources", wadlNamespacePrefix));
		if (resourcesElement != null) {
			String endpointUrl = resourcesElement.getAttributeValue(new QName("base"));
			if (endpointUrl != null) {
				String endpointPath = EndpointUtils.deriveEndpointFromUrl(endpointUrl);
				String endpointName = EndpointUtils.deriveEndpointNameFromUrl(endpointUrl);
				String endpointContent =
						EndpointUtils.getEndpointContentWithOverview(endpointUrl, endpointPath, endpointName, version);
				OMElement endpointElement;
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
            addDependency(path, schema);
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

    private void addDependency(String source, String target) throws RegistryException {
        registry.addAssociation(source, target, CommonConstants.DEPENDS);
        registry.addAssociation(target, source, CommonConstants.USED_BY);
    }
}
