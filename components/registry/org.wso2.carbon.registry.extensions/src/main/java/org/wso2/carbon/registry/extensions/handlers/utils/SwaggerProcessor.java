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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
	 * @param sourceUrl      Source URL.
	 * @throws RegistryException If a failure occurs when adding the swagger to registry.
	 */
	public void processSwagger(InputStream inputStream, String commonLocation, String sourceUrl)
			throws RegistryException {

		Registry systemRegistry = CommonUtil.getUnchrootedSystemRegistry(requestContext);
		//Creating a collection if not exists.
		if (!systemRegistry.resourceExists(commonLocation)) {
			systemRegistry.put(commonLocation, systemRegistry.newCollection());
		}
		//Reading resource content
		ByteArrayOutputStream swaggerContent = readSourceContent(inputStream);
		JsonObject swaggerDocObject = getSwaggerObject(swaggerContent.toString());
		String swaggerVersion = getSwaggerVersion(swaggerDocObject);

		//Validate against schema
		if (!APIUtils.isValidSwaggerContent(swaggerDocObject, swaggerVersion)) {
			throw new RegistryException("Swagger content is not valid.");
		}

		//Configuring the location to save the swagger resource.
		String resourcePath = requestContext.getResourcePath().getPath();
		String apiDocName = resourcePath
				.substring(resourcePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
		String commonResourcePath =
				getCommonPathFromContent(commonLocation, swaggerContent.toString());
		resourcePath = commonResourcePath + RegistryConstants.PATH_SEPARATOR + apiDocName;

		OMElement data = null;

		if (swaggerVersion.equals("1.2")) {
			addSwaggerDocumentToRegistry(swaggerContent, resourcePath);

			List<JsonObject> resourceObjects = new ArrayList<JsonObject>();
			if (sourceUrl != null) {
				//Adding APIs to registry.
				JsonArray apis = swaggerDocObject.get(APIConstants.APIS).getAsJsonArray();
				ByteArrayOutputStream apiContent;
				InputStream apiInputStream;
				String path;
				for (JsonElement api : apis) {
					JsonObject apiObject = api.getAsJsonObject();
					path = apiObject.get(APIConstants.PATH).getAsString();
					try {
						apiInputStream = new URL(sourceUrl + path).openStream();
					} catch (IOException e) {
						throw new RegistryException(
								"The URL " + sourceUrl + path + " is incorrect.", e);
					}
					apiContent = readSourceContent(apiInputStream);
					resourceObjects.add(parser.parse(apiContent.toString()).getAsJsonObject());
					path = commonResourcePath + path;
					//Save API Resource document to registry
					addSwaggerDocumentToRegistry(apiContent, path);
					//Adding an dependency to API_DOC
					registry.addAssociation(resourcePath, path, CommonConstants.DEPENDS);
				}
				//Creating the api artifact from the swagger content.
				data = APIUtils.createAPIArtifact(swaggerDocObject, swaggerVersion,
				                                  resourceObjects);
			} else {
				log.warn("Cannot read API resources, RestService may not get updated.");
			}
		} else if (swaggerVersion.equals("2.0")) {
			addSwaggerDocumentToRegistry(swaggerContent, resourcePath);
			data = APIUtils.createAPIArtifact(swaggerDocObject, swaggerVersion, null);
		}

		if (data != null) {
			//Saving the api artifact in the registry.
			String apiPath = APIUtils.addApiToRegistry(requestContext, data);
			//Adding associations to the resources
			registry.addAssociation(apiPath, resourcePath, CommonConstants.DEPENDS);
			registry.addAssociation(resourcePath, apiPath, CommonConstants.USED_BY);
		} else {
			log.warn("API content is null.");
		}
	}

	/**
	 * Saves a swagger document in the registry.
	 *
	 * @param swaggerContent Resource content.
	 * @param path           Resource path.
	 * @throws RegistryException
	 */
	private void addSwaggerDocumentToRegistry(ByteArrayOutputStream swaggerContent, String path)
			throws RegistryException {
		Resource resource;
		if (registry.resourceExists(path)) {
			//If a resource existing in the given path.
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
			//If a resource does not exist in the given path.
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
	 * Returns the common swagger resources path.
	 *
	 * @param rootLocation Root location of the swagger files.
	 * @return Common resource path.
	 */
	private String getCommonPathFromContent(String rootLocation, String content) {

		JsonObject contentObject = parser.parse(content).getAsJsonObject();
		JsonObject apiInfoObject = contentObject.get(APIConstants.INFO).getAsJsonObject();

		String apiName = apiInfoObject.get(APIConstants.TITLE).getAsString().replaceAll("\\s", "");
		JsonElement apiVersionElement = contentObject.get(APIConstants.API_VERSION);

		apiVersionElement = (apiVersionElement == null) ? apiInfoObject.get("version") : apiVersionElement;

		String version = apiVersionElement.getAsString();
		String apiProvider = CarbonContext.getThreadLocalCarbonContext().getUsername();

		return rootLocation + apiProvider + RegistryConstants.PATH_SEPARATOR + apiName +
		       RegistryConstants.PATH_SEPARATOR + version;
	}

	/**
	 * Parses the swagger content and return as a JsonObject
	 *
	 * @param swaggerContent Content as a String.
	 * @return Swagger document as a JSON Object.
	 */
	private JsonObject getSwaggerObject(String swaggerContent) {
		JsonElement swaggerElement = parser.parse(swaggerContent);

		if (swaggerElement != null) {
			return swaggerElement.getAsJsonObject();
		} else {
			throw new JsonParseException(
					"Unexpected error occurred when parsing the swagger content");
		}
	}

	/**
	 * Returns swagger version
	 *
	 * @param swaggerDocObject Swagger JSON.
	 * @return Swagger version.
	 * @throws RegistryException If swagger version is unsupported.
	 */
	private String getSwaggerVersion(JsonObject swaggerDocObject) throws RegistryException {
		//Getting the swagger version
		JsonElement swaggerVersionElement = swaggerDocObject.get(APIConstants.SWAGGER_VERSION);
		swaggerVersionElement = (swaggerVersionElement == null) ?
		                        swaggerDocObject.get(APIConstants.SWAGGER2_VERSION) :
		                        swaggerVersionElement;
		if (swaggerVersionElement == null) {
			throw new RegistryException("Unsupported swagger version.");
		}
		return swaggerVersionElement.getAsString();
	}

}
