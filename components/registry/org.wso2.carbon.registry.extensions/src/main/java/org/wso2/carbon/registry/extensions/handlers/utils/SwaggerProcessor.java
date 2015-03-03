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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

public class SwaggerProcessor {

	private static final Log log = LogFactory.getLog(SwaggerProcessor.class);

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
	 * @throws RegistryException If a failure occurs when adding the swagger to registry.
	 */
	public void addSwaggerToRegistry(RequestContext requestContext, InputStream inputStream,
	                                 String commonLocation) throws RegistryException {

		Resource resource = requestContext.getResource();

		if (resource == null) {
			log.debug("Resource is null in the RequestContent object.");
			resource = new ResourceImpl();
			resource.setMediaType(CommonConstants.SWAGGER_MEDIA_TYPE);
		}

		String version = resource.getProperty(RegistryConstants.VERSION_PARAMETER_NAME);
		String apiName = resource.getProperty("apiName");
		String provider = resource.getProperty("provider");

		if (version == null) {
			version = CommonConstants.SWAGGER_SPEC_VERSION_DEFAULT_VALUE;
			resource.setProperty(RegistryConstants.VERSION_PARAMETER_NAME, version);
		}

		ByteArrayOutputStream swaggerContent = readSourceContent(inputStream);

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
			actualPath = commonLocation + provider + RegistryConstants.PATH_SEPARATOR + apiName +
			             RegistryConstants.PATH_SEPARATOR + version +
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
			//Setting properties to the new resource
			if (properties != null) {
				log.info("Adding existing properties to the new resource. ");
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
		//Setting resource media type : application/swagger+json (proposed media type to swagger specs)
		swaggerResource.setMediaType(CommonConstants.SWAGGER_MEDIA_TYPE);
		//setting resource UUID
		swaggerResource.setUUID(resourceId);
		//Setting swagger as the resource content
		swaggerResource.setContent(swaggerContent.toByteArray());
		//Setting actual path
		requestContext.setActualPath(actualPath);
		//Saving in to the registry
		registry.put(actualPath, swaggerResource);
		((ResourceImpl) swaggerResource).setPath(relativeArtifactPath);
		requestContext.setResource(swaggerResource);

		//Creating API artifact from the swagger content
		OMElement data = createAPIArtifact(swaggerContent, provider);
		String apiPath = addApiToregistry(requestContext, data, provider, apiName);

		//Adding associations to the resources
		registry.addAssociation(apiPath, actualPath, CommonConstants.DEPENDS);
		registry.addAssociation(actualPath, apiPath, CommonConstants.USED_BY);
	}

	/**
	 * Saves the API registry artifact created from the imported swagger definition.
	 *
	 * @param requestContext Information about the current request.
	 * @param data           API artifact metadata
	 * @param provider       API provider
	 * @param apiName        Name of the API
	 * @throws RegistryException If a failure occurs when adding the api to registry.
	 */
	private String addApiToregistry(RequestContext requestContext, OMElement data, String provider,
	                              String apiName) throws RegistryException {

		Registry registry = requestContext.getRegistry();
		Resource apiResource = new ResourceImpl();

		apiResource.setMediaType(CommonConstants.API_MEDIA_TYPE);

		OMElement overview = data.getFirstChildWithName(
				new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "overview"));
		String apiVersion = overview.getFirstChildWithName(
				new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "version")).getText();

		if (apiVersion == null) {
			apiVersion = CommonConstants.API_VERSION_DEFAULT_VALUE;
		}
		apiResource.setProperty(RegistryConstants.VERSION_PARAMETER_NAME, apiVersion);
		apiResource.setContent(RegistryUtils.encodeString(data.toString()));

		String resourceId = apiResource.getUUID();

		if (resourceId == null) {
			resourceId = UUID.randomUUID().toString();
		}

		apiResource.setUUID(resourceId);
		String actualPath = getChrootedApiLocation(requestContext.getRegistryContext()) + provider +
		                    RegistryConstants.PATH_SEPARATOR + apiName +
		                    RegistryConstants.PATH_SEPARATOR + apiVersion + "/api";

		registry.put(actualPath, apiResource);

		return actualPath;

	}

	/**
	 * Extracts the data from swagger and creates an API registry artifact.
	 *
	 * @param swaggerContent Swagger json content
	 * @param providerName   API provider
	 * @return The API metadata
	 * @throws RegistryException If swagger content is invalid.
	 */
	private OMElement createAPIArtifact(ByteArrayOutputStream swaggerContent, String providerName)
			throws RegistryException {
		String swagger = swaggerContent.toString();
		JsonParser parser = new JsonParser();
		JsonObject swaggerObject = parser.parse(swagger).getAsJsonObject();

		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace namespace =
				factory.createOMNamespace(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "");
		OMElement data = factory.createOMElement(CommonConstants.SERVICE_ELEMENT_ROOT, namespace);
		OMElement overview = factory.createOMElement("overview", namespace);
		OMElement provider = factory.createOMElement("provider", namespace);
		OMElement name = factory.createOMElement("name", namespace);
		OMElement context = factory.createOMElement("context", namespace);
		OMElement apiVersion = factory.createOMElement("version", namespace);
		OMElement transports = factory.createOMElement("transports", namespace);
		OMElement description = factory.createOMElement("description", namespace);

		JsonElement versionElement = swaggerObject.get("swaggerVersion");

		if (versionElement == null) {
			versionElement = swaggerObject.get("swagger");
		}

		String swaggerVersion;
		if (versionElement != null) {
			swaggerVersion = versionElement.getAsString();
		} else {
			throw new RegistryException("Invalid swagger version. ");
		}
		JsonObject infoObject = swaggerObject.get("info").getAsJsonObject();
		name.setText(getChildElementText(infoObject, "title"));
		description.setText(getChildElementText(infoObject, "description"));
		provider.setText(providerName);

		if (swaggerVersion.equals("2.0")) {
			String host = getChildElementText(swaggerObject, "host");
			String basePath = getChildElementText(swaggerObject, "basepath");

			if (host != null && basePath != null) {
				context.setText(host + basePath);
			}
			apiVersion.setText(getChildElementText(infoObject, "version"));
			transports.setText(getChildElementText(swaggerObject, "schemes"));
		} else if (swaggerVersion.equals("1.2")) {
			apiVersion.setText(getChildElementText(swaggerObject, "apiVersion"));
		}

		overview.addChild(provider);
		overview.addChild(name);
		overview.addChild(context);
		overview.addChild(apiVersion);
		overview.addChild(transports);
		overview.addChild(description);

		data.addChild(overview);

		return data;

	}

	/**
	 * Returns a Json element as a string
	 *
	 * @param object Json Object
	 * @param key    Element key
	 * @return Element value
	 */
	private String getChildElementText(JsonObject object, String key) {
		JsonElement element = object.get(key);
		if (element != null) {
			return object.get(key).getAsString();
		}
		return null;
	}

	/**
	 * Returns the root location of the API.
	 *
	 * @param registryContext Registry context
	 * @return the root location of the API artifact.
	 */
	private String getChrootedApiLocation(RegistryContext registryContext) {
		String relativeLocation = "/apimgt/applicationdata/provider/";
		return RegistryUtils.getAbsolutePath(registryContext,
		                                     RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
		                                     relativeLocation);
	}

}
