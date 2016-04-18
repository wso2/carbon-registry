/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.registry.extensions.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.RESTServiceUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.SwaggerProcessor;
import org.wso2.carbon.registry.extensions.handlers.utils.WADLProcessor;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;

public class RESTServiceMediaTypeHandler extends Handler {
    private static final Log log = LogFactory.getLog(RESTServiceMediaTypeHandler.class);
    private static final String INTERFACE_ELEMENT_LOCAL_NAME = "interface";
    private String swaggerLocation;
    private String wadlLocation;

    /**
     * Extracts the common location for swagger docs from registry.xml entry
     *
     * @param locationConfiguration location configuration element
     */
    public void setSwaggerLocationConfiguration(OMElement locationConfiguration) {
        Iterator confElements = locationConfiguration.getChildElements();
        while (confElements.hasNext()) {
            OMElement confElement = (OMElement) confElements.next();
            if (CommonConstants.LOCATION_TAG.equals(confElement.getLocalName())) {
                swaggerLocation = confElement.getText();
                if (!swaggerLocation.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                    swaggerLocation = RegistryConstants.PATH_SEPARATOR + swaggerLocation;
                }
                if (!swaggerLocation.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    swaggerLocation = swaggerLocation + RegistryConstants.PATH_SEPARATOR;
                }
            }
        }
    }

    /**
     * Extracts the common location for wadl from registry.xml entry
     *
     * @param locationConfiguration location configuration element
     */
    public void setWadlLocationConfiguration(OMElement locationConfiguration) {
        Iterator confElements = locationConfiguration.getChildElements();
        while (confElements.hasNext()) {
            OMElement confElement = (OMElement) confElements.next();
            if (CommonConstants.LOCATION_TAG.equals(confElement.getLocalName())) {
                wadlLocation = confElement.getText();
                if (!wadlLocation.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                    wadlLocation = RegistryConstants.PATH_SEPARATOR + wadlLocation;
                }
                if (!wadlLocation.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    wadlLocation = wadlLocation + RegistryConstants.PATH_SEPARATOR;
                }
            }
        }
    }

    /**
     * Processes the PUT action for REST Service artifacts.
     *
     * @param requestContext        information about the current request.
     * @throws RegistryException    If fails due a handler specific error.
     */
    @Override
    public void put(RequestContext requestContext) throws RegistryException {

        //Acquiring the update lock if available.
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        Registry registry = requestContext.getRegistry();
        Resource resource = requestContext.getResource();

        if (resource == null) {
            throw new RegistryException(CommonConstants.RESOURCE_NOT_EXISTS);
        }

        Object resourceContent = resource.getContent();

        OMElement serviceInfoElement;
        String serviceInfo;

        if (resourceContent instanceof String) {
            serviceInfo = (String) resourceContent;
        } else {
            serviceInfo = RegistryUtils.decodeBytes((byte[]) resourceContent);
        }

        XMLStreamReader reader;
        try {
            reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(serviceInfo));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            serviceInfoElement = builder.getDocumentElement();
        } catch (XMLStreamException e) {
            StringBuilder msg = new StringBuilder("Error in parsing the service content of the service. ")
                    .append("The requested path to store the service: ")
                    .append(requestContext.getResourcePath().getPath()).append(".");
            log.error(msg.toString());
            throw new RegistryException(msg.toString(), e);
        }

        String serviceVersion = CommonUtil.getServiceVersion(serviceInfoElement);

        if (serviceVersion.length() == 0) {
            serviceVersion = CommonConstants.SERVICE_VERSION_DEFAULT_VALUE;
            CommonUtil.setServiceVersion(serviceInfoElement, serviceVersion);
            resource.setContent(serviceInfoElement.toString());
        }
        resource.setProperty(RegistryConstants.VERSION_PARAMETER_NAME, serviceVersion);

        InputStream inputStream = null;
        try {
            String servicePath = RESTServiceUtils.addServiceToRegistry(requestContext, serviceInfoElement);

            //Retrieve WADL or Swagger url if available.
            String swaggerUrl = null, wadlUrl = null;
            OMElement interfaceElement = serviceInfoElement.getFirstChildWithName(
                    new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, INTERFACE_ELEMENT_LOCAL_NAME, ""));

            if(interfaceElement != null) {
                OMElement swaggerElement = interfaceElement
                        .getFirstChildWithName(new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "swagger", ""));
                OMElement wadlElement = interfaceElement
                        .getFirstChildWithName(new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "wadl", ""));

                swaggerUrl = swaggerElement != null ? swaggerElement.getText().trim() : null;
                wadlUrl = wadlElement != null ? wadlElement.getText().trim() : null;
            }

            String swaggerPath = null, wadlPath = null;
            if (swaggerUrl != null && !(swaggerUrl.startsWith(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH))) {
                requestContext.setSourceURL(swaggerUrl);
                SwaggerProcessor processor = new SwaggerProcessor(requestContext);
                processor.setCreateRestServiceArtifact(false);
                inputStream = new URL(swaggerUrl).openStream();
                swaggerPath = processor.processSwagger(inputStream,
                        getChrootedLocation(requestContext.getRegistryContext(), swaggerLocation), swaggerUrl);

            }

            if (wadlUrl != null && !(wadlUrl.startsWith(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH))) {
                requestContext.setSourceURL(wadlUrl);
                WADLProcessor processor = new WADLProcessor(requestContext);
                processor.setCreateService(false);
                wadlPath = processor.importWADLToRegistry(requestContext, servicePath, true);
            }

            if (swaggerPath != null) {
                registry.addAssociation(servicePath, swaggerPath, CommonConstants.DEPENDS);
                registry.addAssociation(swaggerPath, servicePath, CommonConstants.USED_BY);
            }

            if (wadlPath != null) {
                registry.addAssociation(servicePath, wadlPath, CommonConstants.DEPENDS);
                registry.addAssociation(wadlPath, servicePath, CommonConstants.USED_BY);
            }

            requestContext.setProcessingComplete(true);
        } catch (IOException e) {
            throw new RegistryException("The URL is incorrect.", e);
        } finally {
            CommonUtil.releaseUpdateLock();
            CommonUtil.closeInputStream(inputStream);
        }

    }

    /**
     * Returns the root location of the Swagger.
     *
     * @param registryContext  registry context
     * @param resourceLocation resource location
     * @return The root location of the Swagger.
     */
    private String getChrootedLocation(RegistryContext registryContext, String resourceLocation) {
        return RegistryUtils
                .getAbsolutePath(registryContext, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + resourceLocation);
    }

}