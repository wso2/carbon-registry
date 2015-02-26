/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.extensions.handlers.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class SwaggerProcessor {

	private static final Log log = LogFactory.getLog(SwaggerProcessor.class);
	private String commonSwaggerLocation = "/trunk/swaggers";

	private OMElement extractApiElement(String jsonContent) throws RegistryException {

		JsonObject swaggerElement = parseSwagger(jsonContent).getAsJsonObject();

		if (swaggerElement == null || swaggerElement.isJsonNull()) {
			throw new RegistryException("Swagger content is empty. ");
		}

		OMFactory omFactory = OMAbstractFactory.getOMFactory();

		OMElement metadata = omFactory.createOMElement(new QName("metadata"));

		//creating info element

		JsonObject infoObject = swaggerElement.getAsJsonObject("info");

		return null;
	}

	private JsonElement parseSwagger(String jsonContent) throws RegistryException {

		JsonParser jsonParser = new JsonParser();

		JsonElement swaggerElement;
		try {
			swaggerElement = jsonParser.parse(jsonContent);
		} catch (Exception e) {
			throw new RegistryException("Unexpected error occurred when parsing the swagger file. ",
			                            e);
		}

		return swaggerElement;
	}

	public void populateAPI(String swaggerContent, RequestContext requestContext)
			throws RegistryException {

		this.extractApiElement(swaggerContent);
	}

	public void addSwaggerToRegistry(RequestContext requestContext, String resourceContent,
	                                 String commonLocation) throws RegistryException {

		OMElement swaggerElement;
		try {
			swaggerElement = AXIOMUtil.stringToOM(resourceContent);
		} catch (Exception e) {
			log.error("Resource metadata does not include valid XML content.");
			throw new RegistryException("Unexpected error when parsing the metadata.");
		}
		OMElement overviewElement = getElementFromContent(swaggerElement, "overview");
		String sourceURL = getElementFromContent(overviewElement, "url").getText();
		String version = getElementFromContent(overviewElement, "version").getText();
		String apiName = getElementFromContent(overviewElement, "name").getText();

		swaggerElement = addSwaggerContent(swaggerElement, sourceURL);

		Resource resource;
		if (requestContext.getResource() == null) {
			resource = new ResourceImpl();
			resource.setMediaType(CommonConstants.SWAGGER_MEDIA_TYPE);
		} else {
			resource = requestContext.getResource();
		}

		//configuring location
		String resourcePath = requestContext.getResourcePath().getPath();
		String swaggerFileName =
				sourceURL.substring(sourceURL.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);

		Registry systemRegistry = CommonUtil.getUnchrootedSystemRegistry(requestContext);

		if (commonLocation == null) {
			commonLocation =
					RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + commonSwaggerLocation;
		}

		//Creating new collection
		if (!systemRegistry.resourceExists(commonLocation)) {
			systemRegistry.put(commonLocation, systemRegistry.newCollection());
		}

		String actualPath = commonLocation + apiName + RegistryConstants.PATH_SEPARATOR + version +
		                    RegistryConstants.PATH_SEPARATOR + swaggerFileName;

		Registry registry = requestContext.getRegistry();

		String relativeArtifactPath = RegistryUtils
				.getRelativePath(registry.getRegistryContext(), actualPath);

		relativeArtifactPath = RegistryUtils.getRelativePathToOriginal(relativeArtifactPath,
		                                                               RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);

		Resource newResource;
		if (registry.resourceExists(actualPath)) {
			newResource = registry.get(actualPath);
		} else {
			newResource = new ResourceImpl();
			Properties properties = resource.getProperties();
			if (properties != null) {
				List<String> linkProperties = Arrays
						.asList(RegistryConstants.REGISTRY_LINK, RegistryConstants.REGISTRY_USER,
						        RegistryConstants.REGISTRY_MOUNT, RegistryConstants.REGISTRY_AUTHOR,
						        RegistryConstants.REGISTRY_MOUNT_POINT,
						        RegistryConstants.REGISTRY_TARGET_POINT,
						        RegistryConstants.REGISTRY_ACTUAL_PATH,
						        RegistryConstants.REGISTRY_REAL_PATH);
				for (Map.Entry<Object, Object> e : properties.entrySet()) {
					String key = (String) e.getKey();
					if (!linkProperties.contains(key)) {
						newResource.setProperty(key, (List<String>) e.getValue());
					}
				}
			}
		}

		newResource.setMediaType(CommonConstants.SWAGGER_MEDIA_TYPE);
		String swaggerResourceUUID = resource.getUUID();
		if (swaggerResourceUUID == null) {
			swaggerResourceUUID = UUID.randomUUID().toString();
		}
		newResource.setUUID(swaggerResourceUUID);
		newResource.setContent(swaggerElement.toString());
		requestContext.setActualPath(actualPath);
		registry.put(actualPath, newResource);
		((ResourceImpl)newResource).setPath(relativeArtifactPath);

		requestContext.setResource(newResource);

	}

	private OMElement addSwaggerContent(OMElement swaggerElement, String sourceURL) throws RegistryException {

		if (sourceURL == null) {
			log.error("Swagger definition url is null.");
			throw new RegistryException("Source url is empty.");
		}
		InputStream inputStream;
		try {
			if (sourceURL.toLowerCase().startsWith("file:")) {
				String msg =
						"The source URL must not be file in the server's local file system";
				throw new RegistryException(msg);
			}
			inputStream = new URL(sourceURL).openStream();
		} catch (IOException e) {
			throw new RegistryException("The URL " + sourceURL + " is incorrect.", e);
		}
		String swaggerContent = readSourceContent(inputStream);

		OMFactory omFactory = OMAbstractFactory.getOMFactory();
		swaggerElement = omFactory.createOMElement(new QName("metadata"));
		OMElement overviewElement = omFactory.createOMElement(new QName("overview"));
		OMElement swaggerContentElement = omFactory.createOMElement(new QName("swaggerContent"));
		OMText swaggerContentText = omFactory.createOMText(swaggerContentElement, swaggerContent);

		swaggerContentElement.addChild(swaggerContentText);
		overviewElement.addChild(swaggerContentElement);
		swaggerElement.addChild(overviewElement);

		swaggerElement.declareDefaultNamespace(CommonConstants.SERVICE_ELEMENT_NAMESPACE);

		return swaggerElement;
	}

	private String readSourceContent(InputStream inputStream)
			throws RegistryException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		int nextChar;
		try {
			while ((nextChar = inputStream.read()) != -1) {
				outputStream.write(nextChar);
			}
			outputStream.flush();
		} catch (IOException e) {
			throw new RegistryException("Exception occurred while reading swagger content", e);
		}

		return  new String(outputStream.toByteArray());
	}

	private OMElement getElementFromContent(OMElement element, String localName) {
		return element.getFirstChildWithName(new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, localName));
	}
}
