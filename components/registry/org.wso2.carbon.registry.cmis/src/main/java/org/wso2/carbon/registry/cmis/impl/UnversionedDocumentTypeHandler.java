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

package org.wso2.carbon.registry.cmis.impl;

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.wso2.carbon.registry.cmis.RegistryDocument;
import org.wso2.carbon.registry.cmis.RegistryTypeManager;
import org.wso2.carbon.registry.cmis.RegistryUnversionedDocument;
import org.wso2.carbon.registry.cmis.util.CMISConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.cmis.PathManager;

/**
 * Type handler that provides cmis:unversioned-document.
 */
public class UnversionedDocumentTypeHandler extends DocumentTypeHandler {

    public static final String DOCUMENT_UNVERSIONED_TYPE_ID = "cmis:unversioned-document";

    public UnversionedDocumentTypeHandler(Registry repository,
			PathManager pathManager, RegistryTypeManager typeManager) {
		super(repository, pathManager, typeManager);
	}

    @Override
    public String getTypeId() {
        return DOCUMENT_UNVERSIONED_TYPE_ID;
    }

    @Override
    public TypeDefinition getTypeDefinition() {

        DocumentTypeDefinitionImpl unversionedDocument = new DocumentTypeDefinitionImpl();
        unversionedDocument.initialize(super.getTypeDefinition());

        unversionedDocument.setDescription(CMISConstants.DESC_UNVERSIONED_DOCUMENT);
        unversionedDocument.setDisplayName(CMISConstants.DESC_UNVERSIONED_DOCUMENT);
        unversionedDocument.setLocalName(CMISConstants.DESC_UNVERSIONED_DOCUMENT);
        unversionedDocument.setIsQueryable(true);
        unversionedDocument.setQueryName(DOCUMENT_UNVERSIONED_TYPE_ID);
        unversionedDocument.setId(DOCUMENT_UNVERSIONED_TYPE_ID);
        unversionedDocument.setParentTypeId(RegistryTypeManager.DOCUMENT_TYPE_ID);

        unversionedDocument.setIsVersionable(false);
        unversionedDocument.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);

        RegistryTypeManager.addBasePropertyDefinitions(unversionedDocument);
        RegistryTypeManager.addDocumentPropertyDefinitions(unversionedDocument);

        return unversionedDocument;
    }

    
    @Override
    public RegistryDocument getGregNode(Resource node) {
        return new RegistryUnversionedDocument(repository, node, typeManager, pathManager);
    }
}

