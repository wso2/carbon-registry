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

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class WSDLFileProcessor {

    // remove this when it is not needed
    private int i;

    public static final String IMPORT_TAG = "import";
    public static final String INCLUDE_TAG = "include";

    /**
     * Buffer to hold associations until all the resources are added. We should add associations
     * only after both ends (resources) of the association is added to the registry. Otherwise, it
     * will cause an error as the registry tries to find the both end resources before setting the
     * association.
     */
    private List <Association> associationsBuffer = new ArrayList <Association> ();

    /**
     * saves a wsdl file including its imports and imported schemas.
     *
     * @param context the active request
     * @param location URL to WSDL document     *
     * @param registryBasePath base path of registry
     * @param processImports if true, we should pull imports in.  If not, we won't.
     * @param metadata a "template" resource containing the WSDL metadata
     * @return the actual path
     * @throws RegistryException
     */
    public String saveWSDLFileToRegistry(RequestContext context,
                                         String location,
                                         String registryBasePath,
                                         boolean processImports,
                                         Resource metadata)
            throws RegistryException {
        WSDLReader wsdlReader;
        String fileNameToSave;

        Registry registry = context.getRegistry();
        try {
            wsdlReader = WSDLFactory.newInstance().newWSDLReader();
        } catch (WSDLException e) {
            String msg = "Could not initiate the wsdl reader. Caused by: " + e.getMessage();
            throw new RegistryException(msg);
        }

        wsdlReader.setFeature("javax.wsdl.importDocuments", true);
        wsdlReader.setFeature("javax.wsdl.verbose", false);
        Definition wsdlDefinition;

        try {
            wsdlDefinition = wsdlReader.readWSDL(location);
            String baseUri = wsdlDefinition.getDocumentBaseURI();
            String wsdlFileName = baseUri.substring(baseUri.lastIndexOf("/") + 1);
            fileNameToSave = getFileNameToSave(wsdlFileName, ".wsdl");

        } catch (WSDLException e) {
            String msg = "Could not read the wsdl at location " + location + ". Caused by: " +
                         e.getMessage();
            throw new RegistryException(msg);
        }

        Map processedWSDLMap = new HashMap();
        calculateWSDLNamesAndChangeTypes(registry, wsdlDefinition, processedWSDLMap,
                                         new HashMap(), new HashSet(), registryBasePath,
                                         processImports);
        saveWSDLFileToRegistry(registry, wsdlDefinition, processedWSDLMap, new HashSet(),
                               registryBasePath, processImports, metadata , true);

        persistAssociations(registry, associationsBuffer);

        return fileNameToSave;
    }

    private String getFileNameToSave(String wsdlFileName, String suffix) {
        String fileNameToSave = wsdlFileName;
        if (wsdlFileName.indexOf(".") > 0) {
            fileNameToSave = wsdlFileName.substring(0, wsdlFileName.indexOf(".")) + suffix;
        } else if (wsdlFileName.indexOf("?") > 0) {
            fileNameToSave = wsdlFileName.substring(0, wsdlFileName.indexOf("?")) + suffix;
        } else if (!wsdlFileName.endsWith("wsdl")) {
            fileNameToSave = wsdlFileName + suffix;
        }
        return fileNameToSave;
    }

    static HashMap changed = new HashMap();
    static HashSet visited = new HashSet();

    /**
     * saves the given wsdl definition file with its imported wsdls and imported and included
     * schemas.
     *
     * @param wsdlDefinition     - wsdl file to save
     * @param processedWSDLMap   - map with original source URI vs new uris for wsdls
     * @param processedScheamMap - map with orignal source URI vs new uris for schemas
     */

    public void calculateWSDLNamesAndChangeTypes(Registry registry,
                                                 Definition wsdlDefinition,
                                                 Map processedWSDLMap,
                                                 Map processedScheamMap,
                                                 Set visitedWSDLs,
                                                 String registryBasePath,
                                                 boolean processImports) throws RegistryException {
        // first we have to process the imports and change the
        // schema locations suite for the registry

        if (processImports) {
            Iterator iter = wsdlDefinition.getImports().values().iterator();
            Vector values;
            Import wsdlImport;
            // add this to visited list to stop recursion
            visitedWSDLs.add(wsdlDefinition.getDocumentBaseURI());
            for (; iter.hasNext();) {
                values = (Vector)iter.next();
                for (Object value : values) {
                    wsdlImport = (Import)value;
                    // process the types recuresiveilt
                    Definition innerDefinition = wsdlImport.getDefinition();
                    if (!visitedWSDLs.contains(innerDefinition.getDocumentBaseURI())) {
                        // we have not process this wsdl file earlier
                        calculateWSDLNamesAndChangeTypes(registry,
                                innerDefinition, processedWSDLMap, processedScheamMap, visitedWSDLs,
                                registryBasePath, processImports);
                    }
                }
            }
        }

        // change the schema names
         String baseUri = wsdlDefinition.getDocumentBaseURI();
        String wsdlFileName = baseUri.substring(baseUri.lastIndexOf("/") + 1);
        String fileNameToSave = getFileNameToSave(wsdlFileName, ".wsdl");
        while (processedWSDLMap.containsValue(fileNameToSave)) {
            fileNameToSave = getFileNameToSave(wsdlFileName, ++i + ".wsdl");
        }
        Types types = wsdlDefinition.getTypes();
        if (types != null) {
            List extensibleElements = types.getExtensibilityElements();
            Schema schemaExtension;
            Object extensionObject;
            XmlSchema xmlSchema;
            XmlSchemaCollection xmlSchemaCollection;
            SchemaFileProcessor schemaFileProcessor;
            Map changedLocationMap;
            String basuri = wsdlDefinition.getDocumentBaseURI();
            basuri = basuri.substring(0, basuri.lastIndexOf("/") + 1);
            for (Object extensibleElement : extensibleElements) {
                extensionObject = extensibleElement;
                if (extensionObject instanceof Schema) {
                    // first get the schema object
                    schemaExtension = (Schema)extensionObject;
                    // create an xml schema object to be processed by SchemaFile procesor.
                    xmlSchemaCollection = new XmlSchemaCollection();
                    xmlSchemaCollection.setBaseUri(basuri);
                    xmlSchema = xmlSchemaCollection.read(schemaExtension.getElement());
                    schemaFileProcessor = new SchemaFileProcessor(registry);
                    changedLocationMap = new HashMap();
                    HashSet visitedSchemas = new HashSet();
                    schemaFileProcessor.calculateNewSchemaNames(
                            xmlSchema, processedScheamMap, visitedSchemas, true, processImports);
                    schemaFileProcessor.saveSchemaFileToRegistry(
                            xmlSchema, processedScheamMap, changedLocationMap, new HashSet(), true,
                            registryBasePath, processImports, null);
                    schemaFileProcessor.persistAssociations();

                    ArrayList xsdPaths = schemaFileProcessor.getSchemaPath();
                    for (Object xsdPath : xsdPaths) {
                        String xsd = (String)xsdPath;
                        String assocPath = "/" + fileNameToSave;
                        if (!"/".equals(registryBasePath)) {
                            assocPath = registryBasePath + assocPath;
                        }
                        associationsBuffer.add(new Association(assocPath, xsd,
                                CommonConstants.DEPENDS));
                        associationsBuffer.add(new Association(xsd, assocPath,
                                CommonConstants.USED_BY));
                    }

                    // update the current schema locations with the generated ones.
                    changeLocations(schemaExtension.getElement(), changedLocationMap);
                }
            }
        }

        // after processing the defintions save this to the registry
        // TODO: save this to the registry for the moment save this to the
        // folder and omit any exception occurs.

        // add this entry to the proccessed wsdl map
        processedWSDLMap.put(baseUri, fileNameToSave);

    }

    /**
     *
     * @param wsdlDefinition
     * @param processedWSDLMap
     * @param visitedWSDLs
     * @param registryBasePath
     * @param processImports
     * @param metadata
     * @param original  : To indicate whether the resource is the top most resource of imported one
     * @throws RegistryException
     */
    public void saveWSDLFileToRegistry(Registry registry,
                                       Definition wsdlDefinition,
                                       Map processedWSDLMap,
                                       Set visitedWSDLs,
                                       String registryBasePath,
                                       boolean processImports,
                                       Resource metadata ,
                                       boolean original) throws RegistryException {
        List associations = new ArrayList();
        if (processImports) {
            // first we have to process the imports and change the
            // schema locations suite for the registry
            Iterator iter = wsdlDefinition.getImports().values().iterator();
            Vector values;
            Import wsdlImport;
            // add this to visited list to stop recursion
            visitedWSDLs.add(wsdlDefinition.getDocumentBaseURI());
            for (; iter.hasNext();) {
                values = (Vector)iter.next();
                for (Object value : values) {
                    wsdlImport = (Import)value;
                    // process the types recuresiveilt
                    Definition innerDefinition = wsdlImport.getDefinition();
                    if (!visitedWSDLs.contains(innerDefinition.getDocumentBaseURI())) {
                        // we have not process this wsdl file earlier
                        saveWSDLFileToRegistry(registry, innerDefinition, processedWSDLMap,
                                               visitedWSDLs, registryBasePath, processImports,
                                               null ,false);
                    }
                    // set the import location according to the new location
                    wsdlImport.setLocationURI((String)processedWSDLMap.get(
                            innerDefinition.getDocumentBaseURI()));
                    // add this wsdl as an association
                    String innerImportedResourceName =
                            (String)processedWSDLMap.get(innerDefinition.getDocumentBaseURI());
                    String innerWsdlPath = getWSDLPath(registryBasePath, innerImportedResourceName);
                    associations.add(innerWsdlPath);
                }
            }
        }

        // after processing the defintions save this to the registry
        // save this to the registry
        String importedResourceName =
                (String)processedWSDLMap.get(wsdlDefinition.getDocumentBaseURI());
        try {
            WSDLWriter wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            wsdlWriter.writeWSDL(wsdlDefinition, byteArrayOutputStream);
            byte[] wsdlResourceContent = byteArrayOutputStream.toByteArray();

            // create a resource this wsdlResourceContent and put it to the registry with the name
            // importedResourceName (in some path)

            String wsdlPath = getWSDLPath(registryBasePath, importedResourceName);
            Resource wsdlResource = new ResourceImpl();

            if (metadata != null) {
                wsdlResource.setMediaType(metadata.getMediaType());
                wsdlResource.setDescription(metadata.getDescription());
            }

            // getting the paramentes
            if (wsdlDefinition.getQName() != null) {
                String name = wsdlDefinition.getQName().getLocalPart();
                if (name != null) {
                    wsdlResource.addProperty("registry.wsdl.Name", name);
                }
            }

            if (wsdlDefinition.getDocumentationElement() != null) {
                String document = wsdlDefinition.getDocumentationElement().getTextContent();
                if (document != null) {
                    wsdlResource.addProperty("registry.wsdl.documentation", document);
                }
            }

            String targetNamespace = wsdlDefinition.getTargetNamespace();
            wsdlResource.addProperty("registry.wsdl.TargetNamespace", targetNamespace);

            wsdlResource.setContent(wsdlResourceContent);

            registry.put(wsdlPath, wsdlResource);

            if (metadata !=null && original) {
                metadata.addProperty("registry.wsdl.TargetNamespace", targetNamespace);
                if (wsdlDefinition.getQName() != null) {
                    String name = wsdlDefinition.getQName().getLocalPart();
                    if (name != null) {
                        metadata.addProperty("registry.wsdl.Name", name);
                    }
                }
                if (wsdlDefinition.getDocumentationElement() != null) {
                    String document = wsdlDefinition.getDocumentationElement().getTextContent();
                    if (document != null) {
                        metadata.addProperty("registry.wsdl.documentation", document);
                    }
                }
            }
            // add the associations
            String associatedWSDL;
            for (Object association : associations) {
                associatedWSDL = (String)association;
                associationsBuffer.add(new Association(wsdlPath, associatedWSDL,
                       CommonConstants.DEPENDS));
            }

        } catch (WSDLException e) {
            throw new RegistryException("Invalid WSDL file");
        }
    }

    private String getWSDLPath(String registryBasePath, String importedResourceName) {
        String wsdlPath;
        if (RegistryConstants.ROOT_PATH.equals(registryBasePath)) {
            wsdlPath = RegistryConstants.ROOT_PATH + importedResourceName;
        } else {
            wsdlPath = registryBasePath + RegistryConstants.PATH_SEPARATOR + importedResourceName;
        }
        return wsdlPath;
    }

    private void changeLocations(Element element, Map changedLocationMap) {
        NodeList nodeList = element.getChildNodes();
        String tagName;
        for (int i = 0; i < nodeList.getLength(); i++) {
            tagName = nodeList.item(i).getLocalName();
            if (IMPORT_TAG.equals(tagName) || INCLUDE_TAG.equals(tagName)) {
                processImport(nodeList.item(i), changedLocationMap);
            }
        }
    }

    private void processImport(Node importNode, Map changedLocationMap) {
        NamedNodeMap nodeMap = importNode.getAttributes();
        Node attribute;
        String attributeValue;
        for (int i = 0; i < nodeMap.getLength(); i++) {
            attribute = nodeMap.item(i);
            if (attribute.getNodeName().equals("schemaLocation")) {
                attributeValue = attribute.getNodeValue();
                if (changedLocationMap.get(attributeValue) != null) {
                    attribute.setNodeValue((String)changedLocationMap.get(attributeValue));
                }
            }
        }
    }

    private void persistAssociations(Registry registry, List <Association> associations)
            throws RegistryException {
        Iterator <Association> associationIterator = associations.iterator();

        while(associationIterator.hasNext()) {

            Association association = associationIterator.next();
            registry.addAssociation(association.getSourcePath(), association.getDestinationPath(),
                                    association.getAssociationType());
        }
    }
}
