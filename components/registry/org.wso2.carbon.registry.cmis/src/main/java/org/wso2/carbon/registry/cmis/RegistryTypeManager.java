/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.cmis;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.Converter;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;
import org.apache.chemistry.opencmis.server.support.TypeManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.registry.cmis.impl.UnversionedDocumentTypeHandler;
import org.wso2.carbon.registry.cmis.util.CMISConstants;

import java.math.BigInteger;
import java.util.*;

/**
 *  This class corresponds to different CMIS types and handles the related mappings.
 */
public class RegistryTypeManager implements TypeManager {
    private static final Logger log = LoggerFactory.getLogger(RegistryTypeManager.class);

    public static final String DOCUMENT_TYPE_ID = "cmis:document";
    public static final String FOLDER_TYPE_ID = "cmis:folder";
    public static final String RELATIONSHIP_TYPE_ID = "cmis:relationship";
    public static final String POLICY_TYPE_ID = "cmis:policy";
    public static final String NAMESPACE = "http://wso2.org/greg";

    private final Map<String, TypeDefinitionContainerImpl> fTypes;

    public RegistryTypeManager() {
        fTypes = new HashMap<String, TypeDefinitionContainerImpl>();
    }

    /**
     * Adds a type to collection with inheriting base type properties.
     * @param type  type to add
     * @return  <code>true</code> iff the type was successfully added
     */
    public boolean addType(TypeDefinition type) {
        if (type == null) {
            return false;
        }

        if (fTypes.containsKey(type.getId())) {
            // can't overwrite a type
            return false;
        }

        AbstractTypeDefinition newType = (AbstractTypeDefinition) copyTypeDefinition(type);

        if (!newType.getBaseTypeId().value().equals(newType.getId())) {

            // find base type
            TypeDefinition baseType;
            if (newType.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT) {
                baseType = copyTypeDefinition(fTypes.get(DOCUMENT_TYPE_ID).getTypeDefinition());
            }
            else if (newType.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
                baseType = copyTypeDefinition(fTypes.get(FOLDER_TYPE_ID).getTypeDefinition());
            }
            else if (newType.getBaseTypeId() == BaseTypeId.CMIS_RELATIONSHIP) {
                baseType = copyTypeDefinition(fTypes.get(RELATIONSHIP_TYPE_ID).getTypeDefinition());
            }
            else if (newType.getBaseTypeId() == BaseTypeId.CMIS_POLICY) {
                baseType = copyTypeDefinition(fTypes.get(POLICY_TYPE_ID).getTypeDefinition());
            }
            else {
                return false;
            }

            // copy property definition
            for (PropertyDefinition<?> propDef : baseType.getPropertyDefinitions().values()) {
                ((AbstractPropertyDefinition<?>) propDef).setIsInherited(true);
                newType.addPropertyDefinition(propDef);
            }

        }

        // add it
        addTypeInternal(newType);

        log.info("Added type '" + newType.getId() + "'.");

        return true;
    }

    public TypeDefinition getType(String typeId) {
        TypeDefinitionContainer tc = fTypes.get(typeId);
        return tc == null ? null : tc.getTypeDefinition();
    }

    public static boolean isVersionable(TypeDefinition typeDef) {
        return (!typeDef.getId().equals(UnversionedDocumentTypeHandler.DOCUMENT_UNVERSIONED_TYPE_ID));
    }

    public static TypeDefinition copyTypeDefinition(TypeDefinition type) {
        return Converter.convert(Converter.convert(type));
    }

    /**
     * See CMIS 1.0 section 2.2.2.3 getTypeChildren
     */
    public TypeDefinitionList getTypeChildren(String typeId, boolean includePropertyDefinitions,
                                              BigInteger maxItems, BigInteger skipCount) {

        TypeDefinitionListImpl result = new TypeDefinitionListImpl(new ArrayList<TypeDefinition>());

        int skip = skipCount == null ? 0 : skipCount.intValue();
        if (skip < 0) {
            skip = 0;
        }

        int max = maxItems == null ? Integer.MAX_VALUE : maxItems.intValue();
        if (max < 1) {
            return result;
        }

        if (typeId == null) {
            if (skip < 1) {
                result.getList().add(copyTypeDefinition(fTypes.get(FOLDER_TYPE_ID).getTypeDefinition()));
                max--;
            }
            if (skip < 2 && max > 0) {
                result.getList().add(copyTypeDefinition(fTypes.get(DOCUMENT_TYPE_ID).getTypeDefinition()));
            }

            result.setHasMoreItems(result.getList().size() + skip < 2);
            result.setNumItems(BigInteger.valueOf(2));
        } else {
            TypeDefinitionContainer tc = fTypes.get(typeId);
            if (tc == null || tc.getChildren() == null) {
                return result;
            }

            for (TypeDefinitionContainer child : tc.getChildren()) {
                if (skip > 0) {
                    skip--;
                    continue;
                }

                result.getList().add(copyTypeDefinition(child.getTypeDefinition()));

                max--;
                if (max == 0) {
                    break;
                }
            }

            result.setHasMoreItems(result.getList().size() + skip < tc.getChildren().size());
            result.setNumItems(BigInteger.valueOf(tc.getChildren().size()));
        }

        if (!includePropertyDefinitions) {
            for (TypeDefinition type : result.getList()) {
                type.getPropertyDefinitions().clear();
            }
        }

        return result;
    }

    /**
     * See CMIS 1.0 section 2.2.2.4 getTypeDescendants
     */
    public List<TypeDefinitionContainer> getTypesDescendants(String typeId, BigInteger depth,
                                                             Boolean includePropertyDefinitions) {

        List<TypeDefinitionContainer> result = new ArrayList<TypeDefinitionContainer>();

        // check depth
        int d = depth == null ? -1 : depth.intValue();
        if (d == 0) {
            throw new CmisInvalidArgumentException("Depth must not be 0!");
        }

        // set property definition flag to default value if not set
        boolean ipd = Boolean.TRUE.equals(includePropertyDefinitions);

        if (typeId == null) {
            result.add(getTypesDescendants(d, fTypes.get(FOLDER_TYPE_ID), ipd));
            result.add(getTypesDescendants(d, fTypes.get(DOCUMENT_TYPE_ID), ipd));
        } else {
            TypeDefinitionContainer tc = fTypes.get(typeId);
            if (tc != null) {
                result.add(getTypesDescendants(d, tc, ipd));
            }
        }

        return result;
    }

    //GregTypeManager

    public TypeDefinitionContainer getTypeById(String typeId) {
        return fTypes.get(typeId);
    }

    public TypeDefinition getTypeByQueryName(String typeQueryName) {
        for (TypeDefinitionContainerImpl type : fTypes.values()) {
            TypeDefinition typeDef = type.getTypeDefinition();
            if (typeDef.getQueryName().equals(typeQueryName)) {
                return typeDef;
            }
        }

        return null;
    }

    public Collection<TypeDefinitionContainer> getTypeDefinitionList() {
        Collection<TypeDefinitionContainer> types = new ArrayList<TypeDefinitionContainer>(fTypes.size());
        types.addAll(fTypes.values());
        return types;
    }

    public List<TypeDefinitionContainer> getRootTypes() {
        List<TypeDefinitionContainer> types = new ArrayList<TypeDefinitionContainer>(2);
        types.add(fTypes.get(FOLDER_TYPE_ID));
        types.add(fTypes.get(DOCUMENT_TYPE_ID));
        return types;
    }

    public String getPropertyIdForQueryName(TypeDefinition typeDefinition, String propQueryName) {
        for (PropertyDefinition<?> pd : typeDefinition.getPropertyDefinitions().values()) {
            if (pd.getQueryName().equals(propQueryName)) {
                return pd.getId();
            }
        }

        return null;
    }

    public static void addBasePropertyDefinitions(AbstractTypeDefinition type) {
        type.addPropertyDefinition(createPropDef(PropertyIds.BASE_TYPE_ID, CMISConstants.BASE_TYPE_ID_VALUE, CMISConstants.BASE_TYPE_ID_VALUE,
                PropertyType.ID, Cardinality.SINGLE, Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.OBJECT_ID, CMISConstants.OBJECT_ID_VALUE, CMISConstants.OBJECT_ID_VALUE, PropertyType.ID,
                Cardinality.SINGLE, Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.OBJECT_TYPE_ID, CMISConstants.TYPE_ID_VALUE, CMISConstants.TYPE_ID_VALUE, PropertyType.ID,
                Cardinality.SINGLE, Updatability.ONCREATE, false, true));

        type.addPropertyDefinition(createPropDef(PropertyIds.NAME, CMISConstants.NAME_VALUE, CMISConstants.NAME_VALUE, PropertyType.STRING,
                Cardinality.SINGLE, Updatability.READWRITE, false, true));

        type.addPropertyDefinition(createPropDef(PropertyIds.CREATED_BY, CMISConstants.CREATED_BY_VALUE, CMISConstants.CREATED_BY_VALUE,
                PropertyType.STRING, Cardinality.SINGLE, Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.CREATION_DATE, CMISConstants.CREATION_DATE_VALUE, CMISConstants.CREATION_DATE_VALUE,
                PropertyType.DATETIME, Cardinality.SINGLE, Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.LAST_MODIFIED_BY, CMISConstants.LAST_MODIFIED_BY_VALUE, CMISConstants.LAST_MODIFIED_BY_VALUE,
                PropertyType.STRING, Cardinality.SINGLE, Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.LAST_MODIFICATION_DATE, CMISConstants.LAST_MODIFIED_DATE_VALUE,
                CMISConstants.LAST_MODIFIED_DATE_VALUE, PropertyType.DATETIME, Cardinality.SINGLE, Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.CHANGE_TOKEN, CMISConstants.CHANGE_TOKEN_VALUE, CMISConstants.CHANGE_TOKEN_VALUE,
                PropertyType.STRING, Cardinality.SINGLE, Updatability.READONLY, false, false));
    }

    public static void addFolderPropertyDefinitions(FolderTypeDefinitionImpl type) {
        type.addPropertyDefinition(createPropDef(PropertyIds.PARENT_ID, CMISConstants.PARENT_ID_VALUE, CMISConstants.PARENT_ID_VALUE, PropertyType.ID,
                Cardinality.SINGLE, Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS,
                CMISConstants.ALLOWED_CHILD_TYPE_VALUE, CMISConstants.ALLOWED_CHILD_TYPE_VALUE, PropertyType.ID, Cardinality.MULTI,
                Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.PATH, CMISConstants.PATH_VALUE, CMISConstants.PATH_VALUE, PropertyType.STRING,
                Cardinality.SINGLE, Updatability.READONLY, false, false));
    }

    public static void addDocumentPropertyDefinitions(DocumentTypeDefinitionImpl type) {
        type.addPropertyDefinition(createPropDef(PropertyIds.IS_IMMUTABLE, CMISConstants.IS_IMMUTABLE_VALUE, CMISConstants.IS_IMMUTABLE_VALUE,
                PropertyType.BOOLEAN, Cardinality.SINGLE, Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.IS_LATEST_VERSION, CMISConstants.IS_LATEST_VERSION_VALUE,
                CMISConstants.IS_LATEST_VERSION_VALUE, PropertyType.BOOLEAN, Cardinality.SINGLE, Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.IS_MAJOR_VERSION, CMISConstants.IS_MAJOR_VERSION_VALUE, CMISConstants.IS_MAJOR_VERSION_VALUE,
                PropertyType.BOOLEAN, Cardinality.SINGLE, Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.IS_LATEST_MAJOR_VERSION, CMISConstants.IS_LATEST_MAJOR_VERSION_VALUE,
                CMISConstants.IS_LATEST_MAJOR_VERSION_VALUE, PropertyType.BOOLEAN, Cardinality.SINGLE, Updatability.READONLY, false,
                false));

        type.addPropertyDefinition(createPropDef(PropertyIds.VERSION_LABEL, CMISConstants.VERSION_LABEL_VALUE, CMISConstants.VERSION_LABEL_VALUE,
                PropertyType.STRING, Cardinality.SINGLE, Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.VERSION_SERIES_ID, CMISConstants.VERSION_SERIES_ID_VALUE,
                CMISConstants.VERSION_SERIES_ID_VALUE, PropertyType.ID, Cardinality.SINGLE, Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT,
                CMISConstants.IS_VERSION_CHECKED_OUT_VALUE, CMISConstants.IS_VERSION_CHECKED_OUT_VALUE, PropertyType.BOOLEAN,
                Cardinality.SINGLE, Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID,
                CMISConstants.VERSION_CHECKED_OUT_ID_VALUE, CMISConstants.VERSION_CHECKED_OUT_ID_VALUE, PropertyType.ID, Cardinality.SINGLE,
                Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY,
                CMISConstants.VERSION_CHECKED_OUT_BY_VALUE, CMISConstants.VERSION_CHECKED_OUT_BY_VALUE, PropertyType.STRING, Cardinality.SINGLE,
                Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.CHECKIN_COMMENT, CMISConstants.CHECKIN_COMMENT_VALUE, CMISConstants.CHECKIN_COMMENT_VALUE,
                PropertyType.STRING, Cardinality.SINGLE, Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.CONTENT_STREAM_LENGTH, CMISConstants.CONTENT_STREAM_LENGTH_VALUE,
                CMISConstants.CONTENT_STREAM_LENGTH_VALUE, PropertyType.INTEGER, Cardinality.SINGLE, Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.CONTENT_STREAM_MIME_TYPE, CMISConstants.MIME_TYPE_VALUE, CMISConstants.MIME_TYPE_VALUE,
                PropertyType.STRING, Cardinality.SINGLE, Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.CONTENT_STREAM_FILE_NAME, CMISConstants.FILE_NAME_VALUE, CMISConstants.FILE_NAME_VALUE,
                PropertyType.STRING, Cardinality.SINGLE, Updatability.READONLY, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.CONTENT_STREAM_ID, CMISConstants.CONTENT_STREAM_ID_VALUE,
                CMISConstants.CONTENT_STREAM_ID_VALUE, PropertyType.ID, Cardinality.SINGLE, Updatability.READONLY, false, false));
    }

    /**
     * Creates a property definition object.
     */
    public static PropertyDefinition<?> createPropDef(String id, String displayName, String description,
                                                      PropertyType datatype, Cardinality cardinality, Updatability updateability, boolean inherited,
                                                      boolean required) {

        AbstractPropertyDefinition<?> result;

        switch (datatype) {
            case BOOLEAN:
                result = new PropertyBooleanDefinitionImpl();
                break;
            case DATETIME:
                result = new PropertyDateTimeDefinitionImpl();
                break;
            case DECIMAL:
                result = new PropertyDecimalDefinitionImpl();
                break;
            case HTML:
                result = new PropertyHtmlDefinitionImpl();
                break;
            case ID:
                result = new PropertyIdDefinitionImpl();
                break;
            case INTEGER:
                result = new PropertyIntegerDefinitionImpl();
                break;
            case STRING:
                result = new PropertyStringDefinitionImpl();
                break;
            case URI:
                result = new PropertyUriDefinitionImpl();
                break;
            default:
                throw new RuntimeException("Unknown datatype! Spec change?");
        }

        result.setId(id);
        result.setLocalName(id);
        result.setDisplayName(displayName);
        result.setDescription(description);
        result.setPropertyType(datatype);
        result.setCardinality(cardinality);
        result.setUpdatability(updateability);
        result.setIsInherited(inherited);
        result.setIsRequired(required);
        result.setIsQueryable(true);
        result.setQueryName(id);
        result.setIsOrderable(true);

        return result;
    }


    /**
     * Adds a type to collection.
     */
    private void addTypeInternal(AbstractTypeDefinition type) {
        if (type == null) {
            return;
        }

        TypeDefinitionContainerImpl tc = new TypeDefinitionContainerImpl();
        tc.setTypeDefinition(type);

        // add to parent
        if (type.getParentTypeId() != null) {
            TypeDefinitionContainerImpl tdc = fTypes.get(type.getParentTypeId());
            if (tdc != null) {
                if (tdc.getChildren() == null) {
                    tdc.setChildren(new ArrayList<TypeDefinitionContainer>());
                }
                tdc.getChildren().add(tc);
            }
        }

        fTypes.put(type.getId(), tc);
    }

    /**
     * Gathers the type descendants tree.
     */
    private static TypeDefinitionContainer getTypesDescendants(int depth, TypeDefinitionContainer tc,
                                                               boolean includePropertyDefinitions) {

        TypeDefinitionContainerImpl result = new TypeDefinitionContainerImpl();

        TypeDefinition type = copyTypeDefinition(tc.getTypeDefinition());
        if (!includePropertyDefinitions) {
            type.getPropertyDefinitions().clear();
        }

        result.setTypeDefinition(type);

        if (depth != 0) {
            if (tc.getChildren() != null) {
                result.setChildren(new ArrayList<TypeDefinitionContainer>());
                for (TypeDefinitionContainer tdc : tc.getChildren()) {
                    result.getChildren().add(
                            getTypesDescendants(depth < 0 ? -1 : depth - 1, tdc, includePropertyDefinitions));
                }
            }
        }

        return result;
    }

}
