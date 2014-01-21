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

import com.ibm.wsdl.extensions.schema.SchemaImportImpl;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaExternal;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.registry.extensions.utils.WSDLUtil;
import org.wso2.carbon.registry.extensions.utils.WSDLValidationInfo;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.InputSource;

import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class SchemaProcessor {
    private Registry registry;
    private Registry systemRegistry;
    private List<String> processedSchemas;
    private List<String> visitedSchemas;
    private Map<String, SchemaInfo> schemas;
    private List<Association> associations;
    private String baseURI = null;
    private WSDLValidationInfo validationInfo;
    private static final String SCHEMA_VALIDATION_MESSAGE = "Schema Validation Message ";
    private static final String SCHEMA_STATUS = "Schema Validation";
    private boolean useOriginalSchema;

    private String resourceName = "";

    private static final Log log = LogFactory.getLog(SchemaProcessor.class);

    private int i;

    public SchemaProcessor(RequestContext requestContext, WSDLValidationInfo validationInfo) {
        this.registry = requestContext.getRegistry();
        try {
            this.systemRegistry = CommonUtil.getUnchrootedSystemRegistry(requestContext);
        } catch (RegistryException ignore) {
            this.systemRegistry = null;
        }
        i = 0;
        schemas = new LinkedHashMap<String, SchemaInfo> ();
        processedSchemas = new ArrayList<String>();
        visitedSchemas = new ArrayList<String>();
        associations = new ArrayList<Association>();
        this.validationInfo = validationInfo;
    }

    public SchemaProcessor(RequestContext requestContext, WSDLValidationInfo validationInfo, boolean useOriginalSchema) {
        this(requestContext,validationInfo);
        this.useOriginalSchema = useOriginalSchema;
    }

    /* Save the schema, schema imports, associations in the registry. Intended to used by the XSD Media-type handler
       only.
     */
    public String putSchemaToRegistry( RequestContext requestContext,
                                       String resourcePath,
                                       String commonLocation,
                                       boolean processIncludes,boolean disableSymlinkCreation) throws RegistryException {

        boolean isDefaultEnvironment = true;
        String currentSchemaLocation = null;
        String currentEnvironment = null;
        String masterVersion = null;

        List<String> dependeinciesList = new ArrayList<String>();

        resourceName = resourcePath.substring(resourcePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
        XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream((byte[])requestContext.getResource().getContent());
        InputSource inputSource = new InputSource(byteArrayInputStream);
        String url = "http://this.schema.needs/a/valid/source/url/to/proceed.xsd";

        if(requestContext.getSourceURL() != null) {
            url = requestContext.getSourceURL();
        }
        XmlSchema xmlSchema;
        try {
            xmlSchemaCollection.setBaseUri(url);
            xmlSchema = xmlSchemaCollection.read(inputSource, null);
            xmlSchema.setSourceURI(url);
            evaluateSchemasRecursively(xmlSchema, null, false, true);
        } catch (RuntimeException re) {
            String msg = "Could not read the XML Schema Definition file. ";
            if (re.getCause() instanceof org.apache.ws.commons.schema.XmlSchemaException) {
                msg += re.getCause().getMessage();
                log.error(msg, re);
                throw new RegistryException(msg);
            }

            throw new RegistryException(msg, re);
        }

        String path = null; // should depend on the central location / relative location flag
        if (!resourcePath.equals(RegistryConstants.PATH_SEPARATOR + resourceName)
                & !resourcePath.contains(commonLocation) & (registry.resourceExists(resourcePath))) {

            if (currentSchemaLocation == null) {
                currentEnvironment = resourcePath.substring(0, resourcePath.indexOf(CommonUtil.
                        derivePathFragmentFromNamespace(xmlSchema.getTargetNamespace()).replace("//", "/")));
                currentSchemaLocation = currentEnvironment.substring(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH.length() + 1);
            }
            if (masterVersion == null) {
                String namespaceSegment = CommonUtil.
                        derivePathFragmentFromNamespace(xmlSchema.getTargetNamespace()).replace("//", "/");
                String suffix = resourcePath.substring(resourcePath.indexOf(namespaceSegment)
                        + namespaceSegment.length());
                masterVersion = suffix.substring(0, suffix.indexOf(RegistryConstants.PATH_SEPARATOR));
            }
            isDefaultEnvironment = false;

            Association[] associations = registry.getAssociations(resourcePath, CommonConstants.DEPENDS);
            for (Association association : associations) {
                if (association.getSourcePath().equals(resourcePath)) {
                    dependeinciesList.add(association.getDestinationPath());
                }
            }
            dependeinciesList.add(resourcePath);

            //            updateSchemaPaths(currentEnvironment,masterVersion,dependeinciesList);

            //            updateSchemaInternalsAndAssociations();
            String symlinkLocation = RegistryUtils.getAbsolutePath(requestContext.getRegistryContext(),
                    requestContext.getResource().getProperty(RegistryConstants.SYMLINK_PROPERTY_NAME));

            Resource metaResource = requestContext.getResource();
            path = saveSchemasToRegistry(requestContext, currentEnvironment, symlinkLocation,
                                         metaResource, masterVersion, dependeinciesList,disableSymlinkCreation);
            persistAssociations(path);

        } else {
            updateSchemaPaths(commonLocation);

            updateSchemaInternalsAndAssociations();
            String symlinkLocation = RegistryUtils.getAbsolutePath(requestContext.getRegistryContext(),
                    requestContext.getResource().getProperty(RegistryConstants.SYMLINK_PROPERTY_NAME));

            Resource metaResource = requestContext.getResource();
            path = saveSchemaToRegistry(requestContext, resourcePath, symlinkLocation, metaResource,disableSymlinkCreation);
            persistAssociations(path);
        }
        return path;
    }


    /* Save the schema, schema imports, associations in the registry. Intended to used by the XSD Media-type handler
       only. 
     */
    public String importSchemaToRegistry(RequestContext requestContext,
                                       String resourcePath,
                                       String commonLocation,
                                       boolean processIncludes,boolean disableSymlinkCreation) throws RegistryException {
        resourceName = resourcePath.substring(resourcePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
        String url = requestContext.getSourceURL();
        XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
        xmlSchemaCollection.setBaseUri(url);
        baseURI = url;
        InputSource inputSource = new InputSource(url);

        try {
            // Here we assue schema is correct. Schema validation is beyond our scope, so we don't
            // bother with a ValidationEventHandler.
            XmlSchema xmlSchema = xmlSchemaCollection.read(inputSource, null);
            evaluateSchemasRecursively(xmlSchema, null, false, true);
        } catch (RuntimeException re) {
            String msg = "Could not read the XML Schema Definition file. ";
            if (re.getCause() instanceof org.apache.ws.commons.schema.XmlSchemaException) {
                msg += re.getCause().getMessage();
                log.error(msg, re);
                throw new RegistryException(msg);
            }
            throw new RegistryException(msg, re);
        }
        updateSchemaPaths(commonLocation);

        updateSchemaInternalsAndAssociations();

        String symlinkLocation = requestContext.getResource().getProperty(RegistryConstants.SYMLINK_PROPERTY_NAME);

        Resource metaResource = requestContext.getResource();
        String path = saveSchemaToRegistry(requestContext, resourcePath, symlinkLocation, metaResource,disableSymlinkCreation); // should depend on the central location / relative location flag
        persistAssociations(path);
        return path;
    }


    public void evaluateSchemas(
            Types types,
            String wsdlDocumentBaseURI,
            boolean evaluateImports,
            ArrayList<String> dependencies) throws RegistryException {
        baseURI = wsdlDocumentBaseURI;
        /* evaluating schemas found under wsdl:types tag in a wsdl */
        if (types != null) {
            List extensibleElements = types.getExtensibilityElements();
            Schema schema;
            Object extensionObject;
            XmlSchema xmlSchema;
            XmlSchemaCollection xmlSchemaCollection;
            wsdlDocumentBaseURI = wsdlDocumentBaseURI.substring(0, wsdlDocumentBaseURI.lastIndexOf("/") + 1);
            for (Object extensibleElement : extensibleElements) {
                extensionObject = extensibleElement;
                if (extensionObject instanceof Schema) {
                    schema = (Schema)extensionObject;
                    if (schema.getImports().size() > 0) {
                        SchemaImportImpl schemaImport =
                                (SchemaImportImpl) ((Vector) schema.getImports().values()
                                        .toArray()[0]).firstElement();
                        if (schemaImport.getReferencedSchema() != null) {
                            // already added imported xsd
                            CommonUtil.addImportedArtifact(
                                    new File(schemaImport.getReferencedSchema().getDocumentBaseURI()).toString());
                        }
                    }
                    xmlSchemaCollection = new XmlSchemaCollection();
                    /* setting base URI in the collection to load relative schemas */
                    xmlSchemaCollection.setBaseUri(wsdlDocumentBaseURI);
                    xmlSchema = xmlSchemaCollection.read(schema.getElement());
                    evaluateSchemasRecursively(xmlSchema, dependencies, true, false);
                }
            }
        }
    }

    private void evaluateSchemasRecursively(
            XmlSchema xmlSchema,
            ArrayList<String> dependencies,
            boolean isWSDLInlineSchema, boolean isMasterSchema) throws RegistryException {
        // first process the imports and includes
        XmlSchemaObjectCollection includes = xmlSchema.getIncludes();
        SchemaInfo schemaInfo = new SchemaInfo();
        schemaInfo.setMasterSchema(isMasterSchema);
        // set this as an visited schema to stop infinite traversal
        visitedSchemas.add(xmlSchema.getSourceURI());
        if (includes != null) {
            Object externalComponent;
            XmlSchemaExternal xmlSchemaExternal;
            XmlSchema innerSchema;
            for (Iterator iter = includes.getIterator(); iter.hasNext();) {
                externalComponent = iter.next();
                if (externalComponent instanceof XmlSchemaExternal) {
                    xmlSchemaExternal = (XmlSchemaExternal)externalComponent;
                    innerSchema = xmlSchemaExternal.getSchema();
                    if (innerSchema != null) {
                        String sourceURI = innerSchema.getSourceURI();
                        if (isWSDLInlineSchema) {
                            dependencies.add(sourceURI);
                        }
                        else {
                            schemaInfo.getSchemaDependencies().add(sourceURI);
                        }

                        if (!visitedSchemas.contains(sourceURI)) {
                            evaluateSchemasRecursively(
                                    innerSchema,
                                    null,   /* passing null is safe since we are passing isWSDLSchema = false */
                                    false, false); /* ignore inline schema and proceed with included ones */
                        }
                    }
                }
            }
        }

        if (!isWSDLInlineSchema) {
            // after processing includes and imports save the xml schema
            String sourceURI = xmlSchema.getSourceURI();
            String fileNameToSave;
            if (isMasterSchema) {
                fileNameToSave = extractResourceFromURL(resourceName, ".xsd");
            } else {
                fileNameToSave = extractResourceFromURL(sourceURI.substring(sourceURI.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1), ".xsd");
            }

            fileNameToSave = fileNameToSave.replace("?xsd=", ".");
            String originalName = fileNameToSave;
            while (processedSchemas.contains(fileNameToSave)) {
                fileNameToSave = fileNameToSave.substring(0, fileNameToSave.indexOf(".")) + ++i + ".xsd";
            }

            // If this was the master schema, and we already have a schema by that name, simply swap
            // proposed names. But in the process, validate whether the schema that already exists
            // on the list is not a master schema, and thereby avoid any recursion.
            if (schemaInfo.isMasterSchema() && !originalName.equals(fileNameToSave)) {
                for (SchemaInfo schema : schemas.values()) {
                    if (schema.getProposedResourceName().equals(originalName)) {
                        if (!schema.isMasterSchema()) {
                            schema.setProposedResourceName(fileNameToSave);
                            fileNameToSave = originalName;
                        }
                        break;
                    }
                }
            }
            // add this entry to the processed schema map
            processedSchemas.add(fileNameToSave);
            //schemaInfo.setProposedRegistryURL(fileNameToSave);
            schemaInfo.setProposedResourceName(fileNameToSave);
            schemaInfo.setSchema((xmlSchema));
            schemaInfo.setOriginalURL(sourceURI);
            schemas.put(getAbsoluteSchemaURL(sourceURI), schemaInfo);
        }
    }

    public String getSchemaRegistryPath(String parentRegistryPath, String sourceURL) throws RegistryException {
        SchemaInfo schemaInfo = schemas.get(getAbsoluteSchemaURL(sourceURL));
        if (schemaInfo != null) {
            //return WSDLUtil.getLocationPrefix(parentRegistryPath) + schemaInfo.getProposedRegistryURL();
            return WSDLUtil.computeRelativePathWithVersion(parentRegistryPath, schemaInfo.getProposedRegistryURL(), systemRegistry);
        }
        return null;
    }

    public String getSchemaAssociationPath(String sourceURL) {
        SchemaInfo schemaInfo = schemas.get(sourceURL);
        if (schemaInfo != null) {
            String proposedRegistryURL = schemaInfo.getProposedRegistryURL();
            return proposedRegistryURL.replaceAll("\\.\\./", "");
        }
        return null;
    }

    /**
     * For each schema found in schemas, change corresponding schemaInfo's ProposedRegistryURL based on
     * commonSchemaLocation and mangled targetNamespace
     * @param commonSchemaLocation the location to store schemas
     * @throws RegistryException if the operation failed.
     */
    private void updateSchemaPaths(String commonSchemaLocation) throws RegistryException {
        /* i.e. ROOT/commonSchemaLocation */
        if (!systemRegistry.resourceExists(commonSchemaLocation)) {
            systemRegistry.put(commonSchemaLocation, systemRegistry.newCollection());
        }
        for (SchemaInfo schemaInfo: schemas.values()) {
            XmlSchema schema = schemaInfo.getSchema();
            String targetNamespace = schema.getTargetNamespace();
            if ((targetNamespace == null) || ("".equals(targetNamespace))) {
                targetNamespace = "unqualified";
            }
            String schemaLocation = (commonSchemaLocation +
                    CommonUtil.derivePathFragmentFromNamespace(targetNamespace)).replace("//", "/");
            schemaLocation += schemaInfo.getProposedResourceName();
            schemaInfo.setProposedRegistryURL(schemaLocation);
        }
    }
    private void updateSchemaPaths(String commonSchemaLocation,String version,List dependencies) throws RegistryException {
        /* i.e. ROOT/commonSchemaLocation */
        if (!systemRegistry.resourceExists(commonSchemaLocation)) {
            systemRegistry.put(commonSchemaLocation, systemRegistry.newCollection());
        }
        outerLoop:
        for (SchemaInfo schemaInfo: schemas.values()) {
            XmlSchema schema = schemaInfo.getSchema();
            String targetNamespace = schema.getTargetNamespace();
            if ((targetNamespace == null) || ("".equals(targetNamespace))) {
                targetNamespace = "unqualified";
            }
            
            String schemaLocation = (commonSchemaLocation +
                    CommonUtil.derivePathFragmentFromNamespace(targetNamespace)).replace("//", "/");
            String regex =  schemaLocation + "[\\d].[\\d].[\\d]" + RegistryConstants.PATH_SEPARATOR
                    + schemaInfo.getProposedResourceName();

            for (Object dependency : dependencies) {
                String path = dependency.toString();
                if(path.matches(regex)){
                    schemaLocation = path;
                    schemaInfo.setProposedRegistryURL(schemaLocation);
                    continue outerLoop;
                }
            }
            schemaLocation = schemaLocation + version + RegistryConstants.PATH_SEPARATOR
                    + schemaInfo.getProposedResourceName();
            schemaInfo.setProposedRegistryURL(schemaLocation);



        }
    }

    /**
     * Update the schema's internal import location according to the new registry URLs.
     * Furthermore, fill the associations arraylist according to the detectored associations.
     */
    private void updateSchemaInternalsAndAssociations() throws RegistryException {
        for (SchemaInfo schemaInfo: schemas.values()) {
            XmlSchema schema = schemaInfo.getSchema();
            XmlSchemaObjectCollection includes = schema.getIncludes();
            if (includes != null) {
                for (Iterator iter = includes.getIterator(); iter.hasNext();) {
                    Object externalComponent = iter.next();
                    if (externalComponent instanceof XmlSchemaExternal) {
                        XmlSchemaExternal xmlSchemaExternal = (XmlSchemaExternal)externalComponent;
                        XmlSchema schema1 = xmlSchemaExternal.getSchema();
                        if (schema1 != null) {
                            String sourceURI = getAbsoluteSchemaURL(schema1.getSourceURI());
                            if (schemas.containsKey(sourceURI)) {
                                SchemaInfo info = schemas.get(sourceURI);
                                String relativeSchemaPath = WSDLUtil.computeRelativePathWithVersion(schemaInfo.getProposedRegistryURL(), info.getProposedRegistryURL(), registry);
                                xmlSchemaExternal.setSchemaLocation(relativeSchemaPath);
                            }
                        }
                    }
                }
            }

            // creating associations
            for(String associatedTo : schemaInfo.getSchemaDependencies()) {
                SchemaInfo schemaInfoAssociated = schemas.get(associatedTo);
                if (schemaInfoAssociated != null) {
                    associations.add(new Association(schemaInfo.getProposedRegistryURL(),
                            schemaInfoAssociated.getProposedRegistryURL(),
                            CommonConstants.DEPENDS));
                    associations.add(new Association(schemaInfoAssociated.getProposedRegistryURL(),
                            schemaInfo.getProposedRegistryURL(),
                            CommonConstants.USED_BY));
                }
            }
        }
    }

    public String saveSchemasToRegistry(RequestContext requestContext, String commonSchemaLocation,
                                        String symlinkLocation, Resource metaResource,boolean disableSymlinkCreation)
            throws RegistryException {
        updateSchemaPaths(commonSchemaLocation);
        updateSchemaInternalsAndAssociations();
        String path = saveSchemaToRegistry(requestContext, null, symlinkLocation, metaResource,disableSymlinkCreation);
        persistAssociations(path);
        return path;
    }
    public String saveSchemasToRegistry(RequestContext requestContext, String commonSchemaLocation,
                                        String symlinkLocation, Resource metaResource,String version,List dependencies,boolean disableSymlinkCreation)
            throws RegistryException {
        updateSchemaPaths(commonSchemaLocation,version,dependencies);
        updateSchemaInternalsAndAssociations();
        String path = saveSchemaToRegistry(requestContext, null, symlinkLocation, metaResource,disableSymlinkCreation);
        persistAssociations(path);
        return path;
    }

    @SuppressWarnings("unchecked")
    private String saveSchemaToRegistry(RequestContext requestContext, String resourcePath,
                                        String symlinkLocation, Resource metaResource,boolean disableSymlinkCreation)
            throws RegistryException {
        String path = resourcePath;
        for (SchemaInfo schemaInfo: schemas.values()) {
            XmlSchema schema = schemaInfo.getSchema();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            schema.write(byteArrayOutputStream);
            byte[] xsdContent = byteArrayOutputStream.toByteArray();

            String schemaPath = schemaInfo.getProposedRegistryURL();
            Resource xsdResource;
            if (metaResource != null && registry.resourceExists(schemaPath)) {
                xsdResource = registry.get(schemaPath);
            } else {
                xsdResource = new ResourceImpl();
                if (metaResource != null) {
                    Properties properties = metaResource.getProperties();
                    if (properties != null) {
                        List<String> linkProperties = Arrays.asList(
                                RegistryConstants.REGISTRY_LINK,
                                RegistryConstants.REGISTRY_USER,
                                RegistryConstants.REGISTRY_MOUNT,
                                RegistryConstants.REGISTRY_AUTHOR,
                                RegistryConstants.REGISTRY_MOUNT_POINT,
                                RegistryConstants.REGISTRY_TARGET_POINT,
                                RegistryConstants.REGISTRY_ACTUAL_PATH,
                                RegistryConstants.REGISTRY_REAL_PATH);
                        for (Map.Entry<Object, Object> e : properties.entrySet()) {
                            String key = (String) e.getKey();
                            if (!linkProperties.contains(key)) {
                                xsdResource.setProperty(key, (List<String>) e.getValue());
                            }
                        }
                    }
                }
            }
            xsdResource.setMediaType("application/x-xsd+xml");

            if (this.useOriginalSchema) {
                try {
                    xsdResource.setContent(CarbonUtils.getBytesFromFile(new File(new URI(schemaInfo.getOriginalURL()))));
                } catch (CarbonException e) {
                    String errMsg = "Trying to store original schema in registry failed while " +
                                    "generating the content from original schema.";
                    log.error(errMsg, e);
                    throw new RegistryException(errMsg, e);
                } catch (URISyntaxException e) {
                    String errMsg = "Trying to store original schema in registry failed due to error " +
                                    "occurred in file url:" + schemaInfo.getOriginalURL();
                    log.error(errMsg, e);
                    throw new RegistryException(errMsg, e);
                }
            } else {
                xsdResource.setContent(xsdContent);
            }

            if(metaResource != null){
                xsdResource.setDescription(metaResource.getDescription());
            }
            String targetNamespace = schema.getTargetNamespace();
            xsdResource.setProperty("targetNamespace", targetNamespace);


            if (schemaInfo.isMasterSchema() && validationInfo != null) {

                ArrayList<String> messages = validationInfo.getValidationMessages();
                if (messages.size() > 0) {
                    xsdResource.setProperty(SCHEMA_STATUS, WSDLUtils.INVALID);
                } else {
                    xsdResource.setProperty(SCHEMA_STATUS, WSDLUtils.VALID);
                }
                int i = 1;
                for (String message : messages) {
                    if (message == null) {
                        continue;
                    }
                    if (message.length() > 1000) {
                        message = message.substring(0, 997) + "...";
                    }
                    xsdResource.setProperty(SCHEMA_VALIDATION_MESSAGE + i, message);
                    i++;
                }
            }
            if(schemaInfo.isMasterSchema()){
//                persisting the UUID for master schema
                xsdResource.setUUID(metaResource.getUUID());

//                if there is a change in the path, we delete the old resource
                if(path != null && !schemaPath.equals(path) && registry.resourceExists(path)){
                    Resource oldResource = registry.get(path);
                    if(oldResource.getProperty("registry.resource.symlink.path") != null){
                        registry.delete(oldResource.getProperty("registry.resource.symlink.path"));
                    }
                    registry.delete(path);
                }
            }

            boolean newSchemaUpload = !registry.resourceExists(schemaPath);
            saveToRepositorySafely(requestContext, schemaInfo.getOriginalURL(), schemaPath,
                    xsdResource);
            if (symlinkLocation == null && resourcePath != null &&
                    !(resourcePath.equals("/") || resourcePath.equals(schemaPath)
                            || resourcePath.equals(""))) {
                symlinkLocation = RegistryUtils.getParentPath(resourcePath);
            }

            if (schemaInfo.isMasterSchema() && symlinkLocation != null) {
                if (registry.resourceExists(symlinkLocation)) {
                    Resource resource = registry.get(symlinkLocation);
                    if (resource != null) {
                        String isLink = resource.getProperty("registry.link");
                        String mountPoint = resource.getProperty("registry.mountpoint");
                        String targetPoint = resource.getProperty("registry.targetpoint");
                        String actualPath = resource.getProperty("registry.actualpath");
                        if (isLink != null && mountPoint != null && targetPoint != null) {
    //                        symlinkLocation = symlinkLocation.replace(mountPoint, targetPoint);
                            symlinkLocation = actualPath + RegistryConstants.PATH_SEPARATOR;
                        }
                    }
                }
                // 1. New resource: resourcePath = /foo, schemaPath = /ns/name.xsd, symlinkPath = /foo, resourceExist = false, resourceIsSymLink = false, createSymlink = true. DoWork = true
                // 2. New resource, existing symlink: resourcePath = /foo, xsdPath = /ns/name.xsd, symlinkPath = /foo, resourceExist = false, resourceIsSymLink = true, createSymlink = false
                // 3. Edit from symlink: resourcePath = /foo, schemaPath = /ns/name.xsd, symlinkPath = /foo, resourceExist = true, resourceIsSymLink = true,  createSymlink = false,
                // 4. Edit from resource: resourcePath = /ns/name.xsd, schemaPath = /ns/name.xsd, symlinkPath = /ns/name.xsd, resourceExist = true, resourceIsSymLink = false, createSymlink = false,
                // 5. Edit from resource, change ns: resourcePath = /ns/name.xsd, schemaPath = /ns2/name.xsd, symlinkPath = /ns/name.xsd, resourceExist = true, resourceIsSymLink = false, createSymlink = false, deleteResource = true. DoWork = true
                // 6. Edit from symlink, change ns: resourcePath = /ns/name.xsd, schemaPath = /ns2/name.xsd, symlinkPath = /ns/name.xsd, resourceExist = true, resourceIsSymLink = true, createSymlink = delete and add, deleteResource = true. DoWork = true
                if (!symlinkLocation.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    symlinkLocation = symlinkLocation + RegistryConstants.PATH_SEPARATOR;
                }
                /*
                String symLinkTargetNs = "";
                if(!symlinkLocation.startsWith("/_system/governance")) {
                    if (targetNamespace.startsWith("http://")) {
                        symLinkTargetNs = targetNamespace.replaceAll("http://", "").replaceAll("\\.", "_") + "_";
                    } else if (targetNamespace.startsWith("https://")) {
                        symLinkTargetNs = targetNamespace.replaceAll("https://", "").replaceAll("\\.", "_") + "_";
                    }
                }
                String symlinkPath = symlinkLocation + symLinkTargetNs +resourceName;
                                */
                String symlinkPath = symlinkLocation + extractResourceFromURL(resourceName,".xsd");
                int j = 0;
            if(!disableSymlinkCreation) {
                while (true) {
                    if (!registry.resourceExists(symlinkPath)) {
                        if (CommonUtil.isSymbolicLinkMapExisting()) {
                            CommonUtil.addToSymbolicLinkMap(symlinkPath, schemaPath);
                        } else {
                            systemRegistry.createLink(symlinkPath, schemaPath);
                        }
                        break;
                    } else if (newSchemaUpload) {
                        if (registry.get(symlinkPath).getProperty(RegistryConstants.REGISTRY_LINK) != null) {
                            //only symlinks can be exist in the root
                            String actualPath = registry.get(symlinkPath).getProperty(RegistryConstants.REGISTRY_ACTUAL_PATH);
                            if (schemaPath.equals(actualPath)) {
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                    j++;
                    symlinkPath = RegistryConstants.ROOT_PATH + extractResourceFromURL(resourceName,"-"+j+".xsd");
                }
            }
            }

            if (schemaInfo.isMasterSchema()) {
                path = schemaPath;
            }
        }
        return path;
    }

    /**
     * Save associations to the registry if they do not exist.
     * Execution time could be improved if registry provides a better way to check existing associations.
     *
     * @throws RegistryException
     */
    private void persistAssociations(String schemaPath) throws RegistryException {
        // until registry provides a functionality to check existing associations, this method will consume a LOT of time
        for (Association association: associations) {
            boolean isAssociationExist = false;
            Association[] existingAssociations = registry.getAllAssociations(association.getSourcePath());
            if (existingAssociations != null) {
                for (Association currentAssociation: existingAssociations) {
                    if (currentAssociation.getDestinationPath().equals(association.getDestinationPath()) &&
                            currentAssociation.getAssociationType().equals(association.getAssociationType())) {
                        isAssociationExist = true;
                        break;
                    }
                }
            }
            if (!isAssociationExist) {
                registry.addAssociation(association.getSourcePath(),
                        association.getDestinationPath(),
                        association.getAssociationType());
            }
        }
        // this code was added to fix CARBON-11188
        if( schemaPath!= null && associations.isEmpty()) {
            Association[] dependencies = registry.getAssociations(schemaPath, CommonConstants.DEPENDS);
            for(Association dependency : dependencies) {
                if(dependency.getSourcePath().equals(schemaPath)) {
                    registry.removeAssociation(dependency.getSourcePath(), dependency.getDestinationPath(), CommonConstants.DEPENDS);
                    registry.removeAssociation(dependency.getDestinationPath(), dependency.getSourcePath(), CommonConstants.USED_BY);
                }
            }
        }
    }

    /**
     * Saves the resource iff the resource is not already existing in the repository
     * @param path: resource path
     * @param resource: resource object
     * @throws RegistryException
     */
    private void saveToRepositorySafely(RequestContext context, String url, String path,
                                        Resource resource) throws RegistryException {

        String schemaId = resource.getUUID();

        if (schemaId == null) {
            // generate a service id
            schemaId = UUID.randomUUID().toString();
            resource.setUUID(schemaId);
        }
//        if (systemRegistry != null) {
//            CommonUtil.addGovernanceArtifactEntryWithAbsoluteValues(systemRegistry, schemaId, path);
//        }
        
        if (!registry.resourceExists(path)) {
            addSchemaToRegistry(context, path, url, resource, registry);
        } else {
            log.debug("A Resource already exists at given location. Overwriting resource content.");
            addSchemaToRegistry(context, path, url, resource, registry);
        }

//        if (!(resource instanceof Collection) &&
//           ((ResourceImpl) resource).isVersionableChange()) {
//            registry.createVersion(path);
//        }
        String relativeArtifactPath = RegistryUtils.getRelativePath(registry.getRegistryContext(), path);
        // adn then get the relative path to the GOVERNANCE_BASE_PATH
        relativeArtifactPath = RegistryUtils.getRelativePathToOriginal(relativeArtifactPath,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
        ((ResourceImpl)resource).setPath(relativeArtifactPath);
    }

    /**
     * Method that gets called instructing a schema to be added the registry.
     *
     * @param context  the request context for this request.
     * @param path     the path to add the resource to.
     * @param url      the path from which the resource was imported from.
     * @param resource the resource to be added.
     * @param registry the registry instance to use.
     *
     * @throws RegistryException if the operation failed.
     */
    protected void addSchemaToRegistry(RequestContext context, String path, String url,
                                       Resource resource, Registry registry) throws RegistryException {
        registry.put(path, resource);
    }

    private String extractResourceFromURL(String wsdlURL, String suffix) {
        String resourceName = wsdlURL;
        if (wsdlURL.indexOf("?") > 0) {
            resourceName = wsdlURL.substring(0, wsdlURL.indexOf("?")) + suffix;
        } else if (wsdlURL.indexOf(".") > 0) {
            resourceName = wsdlURL.substring(0, wsdlURL.lastIndexOf(".")) + suffix;
        } else if (!wsdlURL.endsWith(suffix)) {
            resourceName = wsdlURL + suffix;
        }
        return resourceName;
    }

    private String getAbsoluteSchemaURL(String schemaLocation) throws RegistryException {
         if (schemaLocation != null && baseURI != null) {
             try {
                 URI uri = new URI(baseURI);
                 URI absoluteURI = uri.resolve(schemaLocation);
                 return absoluteURI.toString();
             } catch (URISyntaxException e) {
                 throw new RegistryException(e.getMessage(), e);
             }
         }
        return schemaLocation;
    }


    
}
