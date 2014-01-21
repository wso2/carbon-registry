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

import org.apache.chemistry.opencmis.commons.enums.Action;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.cmis.impl.UnversionedDocumentTypeHandler;

import java.util.Set;

/**
 * Instances of this class represent a non versionable cmis:document backed by an underlying Registry <code>Node</code>.
 */
public class RegistryUnversionedDocument extends RegistryDocument {
    
    public RegistryUnversionedDocument(Registry repository, Resource node, RegistryTypeManager typeManager, PathManager pathManager) {
        super(repository, node, typeManager, pathManager);
    }

    @Override
    protected Resource getContextNode() throws RegistryException {
        return getNode();
    }

    @Override
    protected Set<Action> compileAllowableActions(Set<Action> aas) {
        Set<Action> result = super.compileAllowableActions(aas);
        setAction(result, Action.CAN_GET_ALL_VERSIONS, false);
        setAction(result, Action.CAN_CHECK_OUT, false);
        setAction(result, Action.CAN_CANCEL_CHECK_OUT, false);
        setAction(result, Action.CAN_CHECK_IN, false);
        return result;
    }
    
    /*
     * TODO
     * 
     * */
    @Override
    protected String getTypeIdInternal() {
        return UnversionedDocumentTypeHandler.DOCUMENT_UNVERSIONED_TYPE_ID;
    }

    @Override
    public boolean isVersionable() {
        return false;
    }
    @Override
    protected boolean isLatestVersion() {
        return true;
    }

    @Override
    protected boolean isMajorVersion() {
        return true;
    }

    @Override
    protected boolean isLatestMajorVersion() {
        return true;
    }

    @Override
    protected String getVersionLabel() {
        return "0.0";
    }

    @Override
    protected boolean isCheckedOut() {
        return false;
    }

    @Override
    protected String getCheckedOutId() {
        return null;   
    }

    @Override
    protected String getCheckedOutBy() throws RegistryException {
        return null;
    }

    @Override
    protected String getCheckInComment() {
        return "";   
    }

	@Override
	protected RegistryObject create(Resource resource) {
		UnversionedDocumentTypeHandler handler = new UnversionedDocumentTypeHandler(getRepository(), pathManager, typeManager);
		
		return handler.getGregNode(resource); 
	}


}
