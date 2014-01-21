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
import org.apache.ws.commons.schema.XmlSchemaExternal;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.xml.sax.InputSource;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class SchemaFileProcessor {

    private Registry registry;
    private ArrayList schemaPath;

    /**
     * Buffer to hold associations untill all the resources are added. We should add associations
     * only after both ends (resources) of the association is added to the registry. Otherwise, it
     * will cause an error as the registry tries to find the both end resources before setting the
     * association.
     */
    private List<Association> associationsBuffer = new ArrayList<Association>();

    // remove this when it is not needed
    private int i;

    public SchemaFileProcessor(Registry registry) {
        this.registry = registry;
        schemaPath = new ArrayList();
    }

    /**
     * Import a schema file to the registry after saving all its includes and imports to the
     * registry and updating the schema locations accordingly.
     *
     * @param location         the original schema location
     * @param registryBasePath base path of the registry
     * @param processIncludes  true if we should recurse through includes
     * @param metadata         template Resource from which to obtain media-type, description, etc.
     * @return the resulting path of the new resource
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     */
    public String saveSchemaFileToRegistry(String location,
                                           String registryBasePath,
                                           boolean processIncludes,
                                           Resource metadata)
            throws RegistryException {
        return saveSchemaFileToRegistry(location, new HashMap(), registryBasePath, processIncludes,
                                        metadata);
    }

    /**
     * Import a schema file to the registry after saving all its includes and imports to the
     * registry and updating the schema locations accordingly.
     *
     * @param location           the original schema location
     * @param processedSchemaMap a Map from original URI (String) to new schema location (String)
     * @param registryBasePath   base path of the registry
     * @param processIncludes    true if we should recurse through includes
     * @param metadata           template Resource from which to obtain media-type, description,
     *                           etc.
     * @return the resulting path of the new resource
     * @throws RegistryException
     */
    public String saveSchemaFileToRegistry(String location,
                                           Map processedSchemaMap,
                                           String registryBasePath,
                                           boolean processIncludes,
                                           Resource metadata)
            throws RegistryException {

        XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
        InputSource inputSource = new InputSource(location);

        // Here we assue schema is correct. Schema validation is beyond our scope, so we don't
        // bother with a ValidationEventHandler.
        XmlSchema xmlSchema = xmlSchemaCollection.read(inputSource, null);

        String baseUri = xmlSchema.getSourceURI();
        String xsdFileName = baseUri.substring(baseUri.lastIndexOf("/") + 1);
        String savedName = xsdFileName.substring(0, xsdFileName.indexOf(".")) + ".xsd";

        // this is not an inline wsdl schema. so pass null to change map.
        calculateNewSchemaNames(xmlSchema, processedSchemaMap, new HashSet(), false,
                                processIncludes);
        saveSchemaFileToRegistry(xmlSchema, processedSchemaMap, null, new HashSet(), false,
                                 registryBasePath, processIncludes, metadata);
        return savedName;
    }

    /**
     * calculate the new schema file names to save the schema. Here we can not save the schema file
     * as it is since there may be recursive imports. So what we have to do is to first determine
     * the schema names to be saved and then change the schema locations accordingly. In this method
     * first we iterate through the imports and includes and find the names. have used the
     * visitedSchemas variable to keep track of the visited schemas to avoid the recursion.
     *
     * @param xmlSchema          the schema to we'd like to save into the registry
     * @param processedSchemaMap a Map from original URI (String) to new schema location (String)
     * @param visitedSchemas     a Set of previously visited schema source uris
     * @param isWSDLInlineSchema true if the given schema is an inline schema of a wsdl - if so, we
     *                           do not need to calculate a name for it
     * @param processIncludes    true if we should process includes
     */
    public void calculateNewSchemaNames(XmlSchema xmlSchema,
                                        Map processedSchemaMap,
                                        Set visitedSchemas,
                                        boolean isWSDLInlineSchema,
                                        boolean processIncludes) {

        if (processIncludes) {
            // first process the imports and includes
            XmlSchemaObjectCollection includes = xmlSchema.getIncludes();
            // set this as an visited schema to stop recursion
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
                        String sourceURI = innerSchema.getSourceURI();
                        // Process if we haven't already encountered this one
                        if (!processedSchemaMap.containsKey(sourceURI) &&
                            !visitedSchemas.contains(sourceURI)) {
                            calculateNewSchemaNames(innerSchema, processedSchemaMap, visitedSchemas,
                                                    false, processIncludes);
                        }
                    }
                }
            }
        }

        // after processing includes and imports save the xml schema
        if (!isWSDLInlineSchema) {
            String baseUri = xmlSchema.getSourceURI();
            String xsdFileName = baseUri.substring(baseUri.lastIndexOf("/") + 1);
            String fileNameToSave = xsdFileName.substring(0, xsdFileName.indexOf(".")) + ".xsd";
            while (processedSchemaMap.containsValue(fileNameToSave)) {
                fileNameToSave = xsdFileName.substring(0, xsdFileName.indexOf(".")) + ++i + ".xsd";
            }
            // add this entry to the processed wsdl map
            processedSchemaMap.put(baseUri, fileNameToSave);
        }
    }

    /**
     * Save the schemas to the registry. used the calculated names in the processedSchemaMap to
     * change the schema locations.
     *
     * @param xmlSchema          the schema to save
     * @param processedSchemaMap a Map from original URI (String) to new schema location (String)
     * @param changeSchemaNames  a Map from original URIs to changed URIs.  Used to update the WSDL
     *                           inline schema imports/includes when saving WSDLs.
     * @param visitedSchemas     a Set of schema URIs (Strings) that we've already visited
     * @param isWSDLInlineSchema true if this is an inline schema from a WSDL
     * @param registryBasePath   the base path of the registry
     * @param processIncludes    true if we should recurse into includes of this schema
     * @param metadata           template Resource for metadata (media-type, description)
     * @throws RegistryException
     */
    public void saveSchemaFileToRegistry(XmlSchema xmlSchema,
                                         Map processedSchemaMap,
                                         Map changeSchemaNames,
                                         Set visitedSchemas,
                                         boolean isWSDLInlineSchema,
                                         String registryBasePath,
                                         boolean processIncludes,
                                         Resource metadata) throws RegistryException {

        List associations = new ArrayList();
        if (processIncludes) {
            // first process the imports and includes
            XmlSchemaObjectCollection includes = xmlSchema.getIncludes();
            // set this as an visited schema to stop recursion
            visitedSchemas.add(xmlSchema.getSourceURI());
            if (includes != null) {
                for (Iterator iter = includes.getIterator(); iter.hasNext();) {
                    Object externalComponent = iter.next();
                    if (externalComponent instanceof XmlSchemaExternal) {
                        XmlSchemaExternal xmlSchemaExternal = (XmlSchemaExternal)externalComponent;
                        String sourceURI = xmlSchemaExternal.getSchema().getSourceURI();
                        if (!visitedSchemas.contains(sourceURI)) {
                            saveSchemaFileToRegistry(xmlSchemaExternal.getSchema(),
                                                     processedSchemaMap, null, visitedSchemas,
                                                     false, registryBasePath, processIncludes,
                                                     null);
                        }

                        // add the new name to changeschema map
                        // have to do before change the schema location
                        String newLocation = (String)processedSchemaMap.get(sourceURI);
                        if (isWSDLInlineSchema) {
                            changeSchemaNames.put(xmlSchemaExternal.getSchemaLocation(),
                                                  newLocation);
                        } else {

                        }
                        // set the new location
                        xmlSchemaExternal.setSchemaLocation(newLocation);
                        String innerFileNameToSave = (String)processedSchemaMap.get(sourceURI);
                        String innerXSDPath = getXSDPath(registryBasePath, innerFileNameToSave);
                        associations.add(innerXSDPath);
                    }
                }
            }
        }

        // after processing includes and imports save the xml schema
        if (!isWSDLInlineSchema) {
            String fileNameToSave = (String)processedSchemaMap.get(xmlSchema.getSourceURI());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            xmlSchema.write(byteArrayOutputStream);
            byte[] xsdContent = byteArrayOutputStream.toByteArray();

            Resource xsdResource = new ResourceImpl();
            if (metadata != null) {
                xsdResource.setMediaType(metadata.getMediaType());
                xsdResource.setDescription(metadata.getDescription());
            }
            xsdResource.setContent(xsdContent);

            String xsdPath = getXSDPath(registryBasePath, fileNameToSave);
            String targetNamespace = xmlSchema.getTargetNamespace();
            xsdResource.addProperty("targetNamespace", targetNamespace);

            registry.put(xsdPath, xsdResource);
            schemaPath.add(xsdPath);
            String associationPath;
            for (Object association : associations) {
                associationPath = (String)association;
                associationsBuffer.add(new Association(xsdPath, associationPath, 
                       CommonConstants.DEPENDS));
            }

        }
    }

    private String getXSDPath(String registryBasePath, String fileNameToSave) {
        String xsdPath;
        if (RegistryConstants.ROOT_PATH.equals(registryBasePath)) {
            xsdPath = RegistryConstants.ROOT_PATH + fileNameToSave;
        } else {
            xsdPath = registryBasePath + RegistryConstants.PATH_SEPARATOR + fileNameToSave;
        }
        return xsdPath;
    }

    public void persistAssociations() throws RegistryException {

        Iterator<Association> associationIterator = associationsBuffer.iterator();

        while (associationIterator.hasNext()) {

            Association association = associationIterator.next();
            registry.addAssociation(association.getSourcePath(), association.getDestinationPath(),
                                    association.getAssociationType());
        }
    }


    public ArrayList getSchemaPath() {
        return schemaPath;
    }
}
