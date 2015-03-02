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
import org.apache.axiom.om.OMNamespace;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

public class SwaggerProcessor {

	/**
	 * Reading content form the provided input stream.
	 *
	 * @param inputStream Input stream to read.
	 * @return content as a {@link java.io.ByteArrayOutputStream}
	 * @throws RegistryException If a failure occurs when reading the content.
	 */
	private ByteArrayOutputStream readSourceContent(InputStream inputStream)
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

		return outputStream;

	}

	/**
	 * Saves the swagger file as a registry artifact.
	 *
	 * @param requestContext Information about the current request.
	 * @param inputStream    Input stream to read content.
	 * @param commonLocation Root location of the swagger artifacts.
	 * @throws RegistryException
	 */
	public void addSwaggerToRegistry(RequestContext requestContext, InputStream inputStream,
	                                 String commonLocation) throws RegistryException {

		Resource resource = requestContext.getResource();

		if (resource == null) {
			resource = new ResourceImpl();
			resource.setMediaType(CommonConstants.SWAGGER_MEDIA_TYPE);
		}

		String version = resource.getProperty(RegistryConstants.VERSION_PARAMETER_NAME);
		String apiName = resource.getProperty("apiName");

		if (version == null) {
			version = CommonConstants.SWAGGER_SPEC_VERSION_DEFAULT_VALUE;
			resource.setProperty(RegistryConstants.VERSION_PARAMETER_NAME, version);
		}

		ByteArrayOutputStream swaggerContent = readSourceContent(inputStream);

		//TODO: VALIDATE SWAGGER AGAINST SWAGGER SCHEMA

		String resourcePath = requestContext.getResourcePath().getPath();
		String swaggerFileName = resourcePath
				.substring(resourcePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);

		Registry registry = requestContext.getRegistry();
		Registry systemRegistry = CommonUtil.getUnchrootedSystemRegistry(requestContext);
		RegistryContext registryContext = requestContext.getRegistryContext();

		//Creating a collection if not exists.
		if (!systemRegistry.resourceExists(commonLocation)) {
			systemRegistry.put(commonLocation, systemRegistry.newCollection());
		}

		//setting up the swagger location
		String actualPath;
		if (!resourcePath.startsWith(commonLocation) && !resourcePath.equals(RegistryUtils
				                                                                     .getAbsolutePath(
						                                                                     registryContext,
						                                                                     RegistryConstants.PATH_SEPARATOR +
						                                                                     swaggerFileName)) &&
		    !resourcePath.equals(RegistryUtils.getAbsolutePath(registryContext,
		                                                       RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
		                                                       RegistryConstants.PATH_SEPARATOR +
		                                                       swaggerFileName))) {
			actualPath = resourcePath;
		} else {
			actualPath = commonLocation + apiName + RegistryConstants.PATH_SEPARATOR + version +
			             RegistryConstants.PATH_SEPARATOR + swaggerFileName;
		}

		String relativeArtifactPath =
				RegistryUtils.getRelativePath(registry.getRegistryContext(), actualPath);

		relativeArtifactPath = RegistryUtils.getRelativePathToOriginal(relativeArtifactPath,
		                                                               RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);

		//Creating a new resource to store in the registry
		Resource swaggerResource;
		if (registry.resourceExists(actualPath)) {
			swaggerResource = registry.get(actualPath);
		} else {
			swaggerResource = new ResourceImpl();

			Properties properties = resource.getProperties();

			if (properties != null) {
				Set keys = properties.keySet();
				for (Object key : keys) {
					List values = (List) properties.get(key);
					if (values != null) {
						for (Object value : values) {
							swaggerResource.addProperty((String) key, (String) value);
						}
					}
				}
			}
		}

		String resourceId = resource.getUUID();

		if (resourceId == null) {
			resourceId = UUID.randomUUID().toString();
		}
		swaggerResource.setMediaType(CommonConstants.SWAGGER_MEDIA_TYPE);
		swaggerResource.setUUID(resourceId);
		swaggerResource.setContent(swaggerContent.toByteArray());
		requestContext.setActualPath(actualPath);
		registry.put(actualPath, swaggerResource);
		((ResourceImpl) swaggerResource).setPath(relativeArtifactPath);
		requestContext.setResource(swaggerResource);

		OMElement data = createAPIArtifact(swaggerContent);
		addApiToregistry(requestContext, data);

	}

	private void addApiToregistry(RequestContext requestContext, OMElement data)
			throws RegistryException {

		Registry registry = requestContext.getRegistry();
		Resource apiResource = new ResourceImpl();

		apiResource.setMediaType("application/vnd.wso2-api+xml");

		apiResource.setProperty("Version", "1.0.0");

		apiResource.setContent(RegistryUtils.encodeString(data.toString()));

		String resourceId = apiResource.getUUID();

		if (resourceId == null) {
			resourceId = UUID.randomUUID().toString();
		}

		apiResource.setUUID(resourceId);

		String actualPath = "/_system/governance/apimgt/applicationdata/provider/admin/petstore/api_doc";

		registry.put(actualPath, apiResource);

	}

	private OMElement createAPIArtifact(ByteArrayOutputStream swaggerContent) throws RegistryException {
		String swagger = swaggerContent.toString();
		JsonParser parser = new JsonParser();
		JsonObject swaggerObject = parser.parse(swagger).getAsJsonObject();

		JsonElement versionElement = swaggerObject.get("swagger");
		String version;
		if(versionElement == null) {
			version = swaggerObject.get("swaggerVersion").getAsString();
		} else {
			version = swaggerObject.get("swagger").getAsString();
		}

		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace namespace =
				factory.createOMNamespace(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "");
		OMElement data = factory.createOMElement(CommonConstants.SERVICE_ELEMENT_ROOT, namespace);
		OMElement overview = factory.createOMElement("overview", namespace);

		if (version.equals("1.2")) {
			return null;
		} else if (version.equals("2.0")) {

			JsonObject infoObject = swaggerObject.get("info").getAsJsonObject();

			OMElement provider = factory.createOMElement("provider",namespace);
			provider.setText("admin");

			OMElement name = factory.createOMElement("name", namespace);
			name.setText(infoObject.get("title").getAsString());

			OMElement context = factory.createOMElement("context", namespace);
			context.setText(swaggerObject.get("host").getAsString());

			OMElement apiVersion = factory.createOMElement("version", namespace);
			apiVersion.setText(infoObject.get("version").getAsString());

			OMElement transports = factory.createOMElement("transports", namespace);
			transports.setText(swaggerObject.get("schemes").getAsString());

			OMElement description = factory.createOMElement("description", namespace);
			description.setText(infoObject.get("description").getAsString());

			overview.addChild(provider);
			overview.addChild(name);
			overview.addChild(context);
			overview.addChild(apiVersion);
			overview.addChild(transports);
			overview.addChild(description);

			data.addChild(overview);

			return data;
		}

		return null;

	}


}