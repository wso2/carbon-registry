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

/**
 * This class contains methods to read swagger documents from a given input stream and parse the swagger document in to
 * a JSON object and save the document in to the registry.
 *
 * This class will be initialized from the {@link org.wso2.carbon.registry.extensions.handlers.SwaggerMediaTypeHandler}
 * class when a resource that has a media type of application+swagger+json has to be processed. This class will invoke
 * necessary methods to create a REST Service from the imported swagger definition.
 *
 * @see org.wso2.carbon.registry.extensions.handlers.SwaggerMediaTypeHandler
 * @see org.wso2.carbon.registry.extensions.handlers.utils.RESTServiceUtils
 */
public class SwaggerProcessor {

	private static final Log log = LogFactory.getLog(SwaggerProcessor.class);

	private RequestContext requestContext;
	private Registry registry;
	private JsonParser parser;
	private String swaggerResourcesPath;
	private String documentVersion;

	public SwaggerProcessor(RequestContext requestContext) {
		this.parser = new JsonParser();
		this.requestContext = requestContext;
		this.registry = requestContext.getRegistry();
	}

	/**
	 * Saves the swagger file as a registry artifact.
	 *
	 * @param inputStream           input stream to read content.
	 * @param commonLocation        root location of the swagger artifacts.
	 * @param sourceUrl             source URL.
	 * @throws RegistryException    If a failure occurs when adding the swagger to registry.
	 */
	public void processSwagger(InputStream inputStream, String commonLocation, String sourceUrl)
			throws RegistryException {
		//create a collection if not exists.
		createCollection(commonLocation);

		//Reading resource content and content details.
		ByteArrayOutputStream swaggerContentStream = CommonUtil.readSourceContent(inputStream);
		JsonObject swaggerDocObject = getSwaggerObject(swaggerContentStream.toString());
		String swaggerVersion = getSwaggerVersion(swaggerDocObject);
		documentVersion = requestContext.getResource().getProperty(RegistryConstants.VERSION_PARAMETER_NAME);
		if (documentVersion == null) {
			documentVersion = CommonConstants.SWAGGER_DOC_VERSION_DEFAULT_VALUE;
		}
		String swaggerResourcePath = getSwaggerDocumentPath(commonLocation, swaggerDocObject);

		OMElement data = null;
		/*
		Switches from the swagger version and process document adding process and the REST Service creation process
		using the relevant documents.
		 */
		if (SwaggerConstants.SWAGGER12_VERSION.equals(swaggerVersion)) {
			addSwaggerDocumentToRegistry(swaggerContentStream, swaggerResourcePath, documentVersion);
			List<JsonObject> resourceObjects =
					addResourceDocsToRegistry(swaggerDocObject, sourceUrl, swaggerResourcePath);
			data = (resourceObjects != null) ?
			       RESTServiceUtils.createRestServiceArtifact(swaggerDocObject, swaggerVersion, resourceObjects) : null;

		} else if (SwaggerConstants.SWAGGER2_VERSION.equals(swaggerVersion)) {
			addSwaggerDocumentToRegistry(swaggerContentStream, swaggerResourcePath, documentVersion);
			data = RESTServiceUtils.createRestServiceArtifact(swaggerDocObject, swaggerVersion, null);
		}

		/*
		If REST Service content is not empty, saves the REST service and adds the relevant associations.
		 */
		if (data != null) {
			String servicePath = RESTServiceUtils.addServiceToRegistry(requestContext, data);
			registry.addAssociation(servicePath, swaggerResourcePath, CommonConstants.DEPENDS);
			registry.addAssociation(swaggerResourcePath, servicePath, CommonConstants.USED_BY);
		} else {
			log.warn("Service content is null. Cannot create the REST Service artifact.");
		}

		CommonUtil.closeOutputStream(swaggerContentStream);
	}

	/**
	 * Saves a swagger document in the registry.
	 *
	 * @param contentStream         resource content.
	 * @param path                  resource path.
	 * @param documentVersion       version of the swagger document.
	 * @throws RegistryException    If fails to add the swagger document to registry.
	 */
	private void addSwaggerDocumentToRegistry(ByteArrayOutputStream contentStream, String path, String documentVersion)
			throws RegistryException {
		Resource resource;
		/*
		Checks if a resource is already exists in the given path.
		If exists,
			Compare resource contents and if updated, updates the document, if not skip the updating process
		If not exists,
			Creates a new resource and add to the resource path.
		 */
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
				throw new RegistryException(CommonConstants.INVALID_CONTENT);
			}
			if (resourceContent.equals(contentStream.toString())) {
				log.info("Old content is same as the new content. Skipping the put action.");
				return;
			}
		} else {
			//If a resource does not exist in the given path.
			resource = new ResourceImpl();
		}

		String resourceId = (resource.getUUID() == null) ? UUID.randomUUID().toString() : resource.getUUID();

		resource.setUUID(resourceId);
		resource.setMediaType(CommonConstants.SWAGGER_MEDIA_TYPE);
		resource.setContent(contentStream.toByteArray());
		resource.addProperty(RegistryConstants.VERSION_PARAMETER_NAME, documentVersion);
		registry.put(path, resource);
	}

	/**
	 * Creates a collection in the given common location.
	 *
	 * @param commonLocation        location to create the collection.
	 * @throws RegistryException    If fails to create a collection at given location.
	 */
	private void createCollection(String commonLocation) throws RegistryException {
		Registry systemRegistry = CommonUtil.getUnchrootedSystemRegistry(requestContext);
		//Creating a collection if not exists.
		if (!systemRegistry.resourceExists(commonLocation)) {
			systemRegistry.put(commonLocation, systemRegistry.newCollection());
		}
	}

	/**
	 * Adds swagger 1.2 api resource documents to registry and returns a list of resource documents as JSON objects.
	 *
	 * @param swaggerDocObject      swagger document JSON object.
	 * @param sourceUrl             source url of the swagger document.
	 * @param swaggerDocPath        swagger document path. (path of the registry)
	 * @return                      List of api resources.
	 * @throws RegistryException    If fails to import or save resource docs to the registry.
	 */
	private List<JsonObject> addResourceDocsToRegistry(JsonObject swaggerDocObject, String sourceUrl,
	                                                   String swaggerDocPath) throws RegistryException {

		if(sourceUrl == null) {
			log.debug(CommonConstants.EMPTY_URL);
			log.warn("Resource paths cannot be read. Creating the REST service might fail.");
		}

		List<JsonObject> resourceObjects = new ArrayList<>();
		//Adding Resource documents to registry.
		JsonArray pathResources = swaggerDocObject.get(SwaggerConstants.APIS).getAsJsonArray();
		ByteArrayOutputStream resourceContentStream = null;
		InputStream resourceInputStream = null;
		String path;

		/*
		Loops through apis array of the swagger 1.2 api-doc and reads all the resource documents and saves them in to
		the registry.
		 */
		for (JsonElement pathResource : pathResources) {
			JsonObject resourceObj = pathResource.getAsJsonObject();
			path = resourceObj.get(SwaggerConstants.PATH).getAsString();
			try {
				resourceInputStream = new URL(sourceUrl + path).openStream();
			} catch (IOException e) {
				throw new RegistryException("The URL " + sourceUrl + path + " is incorrect.", e);
			} finally{
				CommonUtil.closeInputStream(resourceInputStream);
			}
			resourceContentStream = CommonUtil.readSourceContent(resourceInputStream);
			resourceObjects.add(parser.parse(resourceContentStream.toString()).getAsJsonObject());
			path = swaggerResourcesPath + path;
			//Save Resource document to registry
			addSwaggerDocumentToRegistry(resourceContentStream, path, documentVersion);
			//Adding an dependency to API_DOC
			registry.addAssociation(swaggerDocPath, path, CommonConstants.DEPENDS);
		}

		CommonUtil.closeOutputStream(resourceContentStream);

		return resourceObjects;
	}

	/**
	 * Configures the swagger resource path form its content and returns the swagger document path.
	 *
	 * @param rootLocation  root location of the swagger files.
	 * @param content       swagger content.
	 * @return              Common resource path.
	 */
	private String getSwaggerDocumentPath(String rootLocation, JsonObject content) {
		String swaggerDocPath = requestContext.getResourcePath().getPath();
		String swaggerDocName =
				swaggerDocPath.substring(swaggerDocPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
		JsonObject infoObject = content.get(SwaggerConstants.INFO).getAsJsonObject();
		String serviceName = infoObject.get(SwaggerConstants.TITLE).getAsString().replaceAll("\\s", "");
		String serviceProvider = CarbonContext.getThreadLocalCarbonContext().getUsername();

		swaggerResourcesPath =  rootLocation + serviceProvider + RegistryConstants.PATH_SEPARATOR + serviceName +
		       RegistryConstants.PATH_SEPARATOR + documentVersion;

		return swaggerResourcesPath + RegistryConstants.PATH_SEPARATOR + swaggerDocName;
	}

	/**
	 * Parses the swagger content and return as a JsonObject
	 *
	 * @param swaggerContent    content as a String.
	 * @return                  Swagger document as a JSON Object.
	 */
	private JsonObject getSwaggerObject(String swaggerContent) {
		JsonElement swaggerElement = parser.parse(swaggerContent);

		if (swaggerElement != null) {
			return swaggerElement.getAsJsonObject();
		} else {
			throw new JsonParseException("Unexpected error occurred when parsing the swagger content");
		}
	}

	/**
	 * Returns swagger version
	 *
	 * @param swaggerDocObject      swagger JSON.
	 * @return                      Swagger version.
	 * @throws RegistryException    If swagger version is unsupported.
	 */
	private String getSwaggerVersion(JsonObject swaggerDocObject) throws RegistryException {
		//Getting the swagger version
		JsonElement swaggerVersionElement = swaggerDocObject.get(SwaggerConstants.SWAGGER_VERSION);
		swaggerVersionElement =
				(swaggerVersionElement == null) ? swaggerDocObject.get(SwaggerConstants.SWAGGER2_VERSION) :
				swaggerVersionElement;
		if (swaggerVersionElement == null) {
			throw new RegistryException("Unsupported swagger version.");
		}
		return swaggerVersionElement.getAsString();
	}

}
