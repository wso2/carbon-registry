/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.extensions.handlers;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.WADLProcessor;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class WADLMediaTypeHandler extends Handler {
    private String locationTag = "location";

    private OMElement schemaLocationConfiguration;
    private String schemaLocation;
    private OMElement wadlLocationConfiguration;
    private String wadlLocation;
    private boolean disableWADLValidation = false;


    public OMElement getWADLLocationConfiguration() {
        return wadlLocationConfiguration;
    }

    public void setWadlLocationConfiguration(OMElement locationConfiguration) throws RegistryException {
        Iterator confElements = locationConfiguration.getChildElements();
        while (confElements.hasNext()) {
            OMElement confElement = (OMElement)confElements.next();
            if (confElement.getQName().equals(new QName(locationTag))) {
                wadlLocation = confElement.getText();
                if(!wadlLocation.startsWith(RegistryConstants.PATH_SEPARATOR)){
                    wadlLocation = RegistryConstants.PATH_SEPARATOR + wadlLocation;
                }
                if(wadlLocation.endsWith(RegistryConstants.PATH_SEPARATOR)){
                    wadlLocation = wadlLocation.substring(0, wadlLocation.length() - 1);
                }
            }
        }
        WADLProcessor.setCommonWADLLocation(wadlLocation);
        this.wadlLocationConfiguration = locationConfiguration;
    }

    public OMElement getSchemaLocationConfiguration() {
        return schemaLocationConfiguration;
    }

    public void setSchemaLocationConfiguration(OMElement locationConfiguration) throws RegistryException {
        Iterator confElements = locationConfiguration.getChildElements();
        while (confElements.hasNext()) {
            OMElement confElement = (OMElement)confElements.next();
            if (confElement.getQName().equals(new QName(locationTag))) {
                schemaLocation = confElement.getText();
                if (!schemaLocation.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                    schemaLocation = RegistryConstants.PATH_SEPARATOR + schemaLocation;
                }
                if (!schemaLocation.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    schemaLocation = schemaLocation + RegistryConstants.PATH_SEPARATOR;
                }
            }
        }

        WADLProcessor.setCommonSchemaLocation(schemaLocation);
        this.schemaLocationConfiguration = locationConfiguration;
    }

    public void setDisableWADLValidation(String disableWADLValidation) {
        this.disableWADLValidation = Boolean.getBoolean(disableWADLValidation);
    }

    public void put(RequestContext requestContext) throws RegistryException {
        try{
            if (!CommonUtil.isUpdateLockAvailable()) {
                return;
            }
            CommonUtil.acquireUpdateLock();
            Resource resource = requestContext.getResource();
            String resourcePath = requestContext.getResourcePath().getPath();
            WADLProcessor wadlProcessor = new WADLProcessor(requestContext);
            wadlProcessor.addWadlToRegistry(requestContext, resource,
                    resourcePath, disableWADLValidation);
            requestContext.setProcessingComplete(true);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    public void importResource(RequestContext requestContext) throws RegistryException {
        try{
            if (!CommonUtil.isUpdateLockAvailable()) {
                return;
            }
            CommonUtil.acquireUpdateLock();
            WADLProcessor wadlProcessor = new WADLProcessor(requestContext);
            wadlProcessor.importWADLToRegistry(requestContext, getChrootedWADLLocation(
                    requestContext.getRegistryContext()), disableWADLValidation);
            requestContext.setProcessingComplete(true);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    private String getChrootedWADLLocation(RegistryContext registryContext) {
            return RegistryUtils.getAbsolutePath(registryContext,
                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + wadlLocation);
        }
}
