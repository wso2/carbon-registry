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

import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FolderTypeDefinitionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.registry.cmis.RegistryFolder;
import org.wso2.carbon.registry.cmis.RegistryTypeManager;

import org.wso2.carbon.registry.cmis.util.CommonUtil;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.cmis.PathManager;

/**
 * Type handler that provides cmis:folder.
 */
public class FolderTypeHandler extends AbstractTypeHandler {

    public FolderTypeHandler(Registry repository, PathManager pathManager,
                             RegistryTypeManager typeManager) {
		super(repository, pathManager, typeManager);
	}

	private static final Logger log = LoggerFactory.getLogger(FolderTypeHandler.class);

    /*private static class FolderIdentifierMap extends DefaultIdentifierMapBase {

        public FolderIdentifierMap() {
            super("nt:folder");
            // xxx not supported: PARENT_ID, ALLOWED_CHILD_OBJECT_TYPE_IDS, PATH
        }
    }*/

    public String getTypeId() {
        return BaseTypeId.CMIS_FOLDER.value();
    }

    public TypeDefinition getTypeDefinition() {
        FolderTypeDefinitionImpl folderType = new FolderTypeDefinitionImpl();
        folderType.setBaseTypeId(BaseTypeId.CMIS_FOLDER);
        folderType.setIsControllableAcl(false);
        folderType.setIsControllablePolicy(false);
        folderType.setIsCreatable(true);
        folderType.setDescription("Folder");
        folderType.setDisplayName("Folder");
        folderType.setIsFileable(true);
        folderType.setIsFulltextIndexed(false);
        folderType.setIsIncludedInSupertypeQuery(true);
        folderType.setLocalName("Folder");
        folderType.setLocalNamespace(RegistryTypeManager.NAMESPACE);
        folderType.setIsQueryable(true);
        folderType.setQueryName(RegistryTypeManager.FOLDER_TYPE_ID);
        folderType.setId(RegistryTypeManager.FOLDER_TYPE_ID);


        RegistryTypeManager.addBasePropertyDefinitions(folderType);
        RegistryTypeManager.addFolderPropertyDefinitions(folderType);

        return folderType;
    }

    public RegistryFolder getGregNode(Resource node) {
        return new RegistryFolder(repository, node, typeManager, pathManager);
    }

    public RegistryFolder createFolder(RegistryFolder parentFolder, String name, Properties properties) {
        try {
        	
        	Collection node = repository.newCollection();
            String destinationPath = CommonUtil.getTargetPathOfNode(parentFolder, name);
        	repository.put(destinationPath, node);
        	Resource resource = repository.get(destinationPath);
        	// compile the properties
            RegistryFolder.setProperties(repository, resource, getTypeDefinition(), properties);

            return getGregNode(resource);
        }
        catch (RegistryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }
    }

}
