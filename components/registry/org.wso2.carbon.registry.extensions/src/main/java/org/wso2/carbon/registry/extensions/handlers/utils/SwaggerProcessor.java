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

import com.google.gson.*;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

public class SwaggerProcessor {

	private static final Log log = LogFactory.getLog(SwaggerProcessor.class);
	private RequestContext requestContext;
	private Registry registry;
	private JsonParser parser;

	public SwaggerProcessor(RequestContext requestContext) {
		this.parser = new JsonParser();
		this.requestContext = requestContext;
		this.registry = requestContext.getRegistry();
	}

	/**
	 * Saves the swagger file as a registry artifact.
	 *
	 * @param inputStream    Input stream to read content.
	 * @param commonLocation Root location of the swagger artifacts.
	 * @throws RegistryException If a failure occurs when adding the swagger to registry.
	 */
	public void importSwaggerToRegistry(InputStream inputStream, String commonLocation,
	                                    String sourceUrl) throws RegistryException {

		Registry systemRegistry = CommonUtil.getUnchrootedSystemRegistry(requestContext);
		//Creating a collection if not exists.
		if (!systemRegistry.resourceExists(commonLocation)) {
			systemRegistry.put(commonLocation, systemRegistry.newCollection());
		}
		//Reading resource content
		ByteArrayOutputStream swaggerContent = readSourceContent(inputStream);

		JsonObject swaggerDocObject;
		try {
			swaggerDocObject = parser.parse(swaggerContent.toString()).getAsJsonObject();
		} catch (JsonParseException e) {
			throw new RegistryException("Unexpected error occurred when parsing the swagger content.", e);
		}

		//Getting the swagger version
		JsonElement swaggerVersionElement = swaggerDocObject.get("swaggerVersion");
		if (swaggerVersionElement == null) {
			swaggerVersionElement = swaggerDocObject.get("swagger");
		}
		if (swaggerVersionElement == null) {
			throw new RegistryException("Unsupported swagger version.");
		}
		String swaggerVersion = swaggerVersionElement.getAsString();

		//Validate against schema
		if(!APIUtils.isValidSwaggerContent(swaggerDocObject, swaggerVersion)) {
			throw new RegistryException("Swagger content is not valid.");
		}

		//Configuring the location to save the swagger resource.
		String resourcePath = requestContext.getResourcePath().getPath();
		String apiDocName = resourcePath
				.substring(resourcePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
		String commonResourcePath =
				getCommonPathFromContent(commonLocation, swaggerContent.toString());

		if (swaggerVersion.equals("1.2")) {
			addDocumentToRegistry(swaggerContent,
			                      commonResourcePath + RegistryConstants.PATH_SEPARATOR +
			                      apiDocName);

			//Adding APIs to registry.
			JsonArray apis = swaggerDocObject.get("apis").getAsJsonArray();

			ByteArrayOutputStream apiContent;
			InputStream apiInputStream;
			String path;
			for (JsonElement api : apis) {
				JsonObject apiObject = api.getAsJsonObject();
				path = apiObject.get("path").getAsString();
				try {
					apiInputStream = new URL(sourceUrl + path).openStream();
				} catch (IOException e) {
					throw new RegistryException("The URL " + sourceUrl + path + " is incorrect.",
					                            e);
				}
				apiContent = readSourceContent(apiInputStream);
				path = commonResourcePath + path;
				addDocumentToRegistry(apiContent, path);
			}
		} else if (swaggerVersion.equals("2.0")) {
			addDocumentToRegistry(swaggerContent,
			                      commonResourcePath + RegistryConstants.PATH_SEPARATOR +
			                      apiDocName);
		}

		//Creating the api artifact from the swagger content.
		OMElement data = APIUtils.createAPIArtifact(swaggerDocObject, swaggerVersion);
		//Saving the api artifact in the registry.
		addApiToregistry(data);

	}

	/**
	 * Saves a resource document in the registry.
	 *
	 * @param swaggerContent Resource content.
	 * @param path           Resource path.
	 * @throws RegistryException
	 */
	public void addDocumentToRegistry(ByteArrayOutputStream swaggerContent, String path)
			throws RegistryException {

		Resource resource;
		if (registry.resourceExists(path)) {
			resource = registry.get(path);
			Object resourceContentObj = resource.getContent();
			String resourceContent;
			if (resourceContentObj instanceof String) {
				resourceContent = (String) resourceContentObj;
				resource.setContent(RegistryUtils.encodeString(resourceContent));
			} else if (resourceContentObj instanceof byte[]) {
				resourceContent = RegistryUtils.decodeBytes((byte[]) resourceContentObj);
			} else {
				throw new RegistryException("Resource content is not valid.");
			}

			//Checking whether the existing resource is updated.
			if(resourceContent.equals(swaggerContent.toString())) {
				log.info("Old content is same as the new content. Skipping the put action.");
				return;
			}
		} else {
			resource = new ResourceImpl();
		}

		String resourceId = resource.getUUID();

		if (resourceId == null) {
			resourceId = UUID.randomUUID().toString();
		}

		//setting resource UUID
		resource.setUUID(resourceId);
		//Setting resource media type : application/swagger+json (proposed media type to swagger specs)
		resource.setMediaType(CommonConstants.SWAGGER_MEDIA_TYPE);
		//Setting swagger as the resource content
		resource.setContent(swaggerContent.toByteArray());
		//Saving in to the registry
		registry.put(path, resource);
	}

	/**
	 * Saves the API registry artifact created from the imported swagger definition.
	 *
	 * @param data    API artifact metadata.
	 * @throws RegistryException If a failure occurs when adding the api to registry.
	 */
	private String addApiToregistry(OMElement data) throws RegistryException {

		Registry registry = requestContext.getRegistry();
		Resource apiResource = new ResourceImpl();

		apiResource.setMediaType(CommonConstants.API_MEDIA_TYPE);

		OMElement overview = data.getFirstChildWithName(
				new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "overview"));
		String apiVersion = overview.getFirstChildWithName(
				new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "version")).getText();
		String apiName = overview.getFirstChildWithName(
				new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "name")).getText();

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
		String actualPath = getChrootedApiLocation(requestContext.getRegistryContext()) + CurrentSession.getUser() +
		                    RegistryConstants.PATH_SEPARATOR + apiName +
		                    RegistryConstants.PATH_SEPARATOR + apiVersion + "/api";

		registry.put(actualPath, apiResource);

		return actualPath;

	}

	/**
	 * Reading content form the provided input stream.
	 *
	 * @param inputStream Input stream to read.
	 * @return content as a {@link java.io.ByteArrayOutputStream}
	 * @throws RegistryException If a failure occurs when reading the content.
	 */
	public ByteArrayOutputStream readSourceContent(InputStream inputStream)
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

	/**
	 * Returns the common swagger resources path.
	 *
	 * @param rootLocation Root location of the swagger files.
	 * @return Common resource path.
	 */
	public String getCommonPathFromContent(String rootLocation, String content) {

		JsonObject contentObject = parser.parse(content).getAsJsonObject();
		JsonObject apiInfoObject = contentObject.get("info").getAsJsonObject();

		String apiName = apiInfoObject.get("title").getAsString().replaceAll("\\s", "");
		JsonElement apiVersionElement = contentObject.get("apiVersion");
		if (apiVersionElement == null) {
			apiVersionElement = apiInfoObject.get("version");
		}
		String version = apiVersionElement.getAsString();
		String apiProvider = CurrentSession.getUser();

		return rootLocation + apiProvider + RegistryConstants.PATH_SEPARATOR + apiName +
		       RegistryConstants.PATH_SEPARATOR + version;
	}

}
