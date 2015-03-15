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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
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

import javax.xml.namespace.QName;
import java.util.*;

public class RESTServiceUtils {

	private static final Log log = LogFactory.getLog(RESTServiceUtils.class);

	private static final OMFactory factory = OMAbstractFactory.getOMFactory();
	public static final OMNamespace DEFAULT_NAMESPACE = null;

	private static final String OVERVIEW = "overview";
	private static final String PROVIDER = "provider";
	private static final String NAME = "name";
	private static final String CONTEXT = "context";
	private static final String VERSION = "version";
	private static final String TRANSPORTS = "transports";
	private static final String DESCRIPTION = "description";
	private static final String URI_TEMPLATE = "uritemplate";
	private static final String URL_PATTERN = "urlPattern";
	private static final String HTTP_VERB = "httpVerb";
	private static final String API_RELATIVE_LOCATION = "/apimgt/applicationdata/provider/";

	/**
	 * Extracts the data from swagger and creates an API registry artifact.
	 *
	 * @param swaggerDocObject Swagger Json Object.
	 * @param swaggerVersion   Swagger version.
	 * @return The API metadata
	 * @throws org.wso2.carbon.registry.core.exceptions.RegistryException If swagger content is invalid.
	 */
	public static OMElement createRestServiceArtifact(JsonObject swaggerDocObject,
	                                                  String swaggerVersion,
	                                                  List<JsonObject> resourceObjects)
			throws RegistryException {

		OMNamespace namespace =
				factory.createOMNamespace(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "");
		OMElement data = factory.createOMElement(CommonConstants.SERVICE_ELEMENT_ROOT, namespace);
		OMElement overview = factory.createOMElement(OVERVIEW, namespace);
		OMElement provider = factory.createOMElement(PROVIDER, namespace);
		OMElement name = factory.createOMElement(NAME, namespace);
		OMElement context = factory.createOMElement(CONTEXT, namespace);
		OMElement apiVersion = factory.createOMElement(VERSION, namespace);
		OMElement transports = factory.createOMElement(TRANSPORTS, namespace);
		OMElement description = factory.createOMElement(DESCRIPTION, namespace);
		List<OMElement> uriTemplates = null;

		JsonObject infoObject = swaggerDocObject.get(APIConstants.INFO).getAsJsonObject();
		//get api name.
		String apiName = getChildElementText(infoObject, APIConstants.TITLE).replaceAll("\\s", "");
		name.setText(apiName);
		context.setText(apiName);
		//get api description.
		description.setText(getChildElementText(infoObject, APIConstants.DESCRIPTION));
		//get api provider. (Current logged in user) : Alternative - CurrentSession.getUser();
		provider.setText(CarbonContext.getThreadLocalCarbonContext().getUsername());

		if (swaggerVersion.equals("2.0")) {
			apiVersion.setText(getChildElementText(infoObject, APIConstants.VERSION));
			transports.setText(getChildElementText(swaggerDocObject, APIConstants.SCHEMES));
			uriTemplates = createURITemplateFromSwagger2(swaggerDocObject);
		} else if (swaggerVersion.equals("1.2")) {
			apiVersion.setText(getChildElementText(swaggerDocObject, APIConstants.API_VERSION));
			uriTemplates = createURITemplateFromSwagger12(resourceObjects);
		}

		overview.addChild(provider);
		overview.addChild(name);
		overview.addChild(context);
		overview.addChild(apiVersion);
		overview.addChild(transports);
		overview.addChild(description);
		data.addChild(overview);

		if(uriTemplates != null) {
			for (OMElement uriTemplate : uriTemplates) {
				data.addChild(uriTemplate);
			}
		}

		return data;

	}

	/**
	 * Saves the API registry artifact created from the imported swagger definition.
	 *
	 * @param requestContext Information about current request.
	 * @param data           API artifact metadata.
	 * @throws RegistryException If a failure occurs when adding the api to registry.
	 */
	public static String addRESTServiceToRegistry(RequestContext requestContext, OMElement data) throws RegistryException {

		Registry registry = requestContext.getRegistry();
		//Creating new resource.
		Resource apiResource = new ResourceImpl();
		//setting API media type.
		apiResource.setMediaType(CommonConstants.REST_SERVICE_MEDIA_TYPE);

		OMElement overview = data.getFirstChildWithName(
				new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "overview"));
		String apiVersion = overview.getFirstChildWithName(
				new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "version")).getText();
		String apiName = overview.getFirstChildWithName(
				new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "name")).getText();
		apiVersion = (apiVersion == null) ? CommonConstants.API_VERSION_DEFAULT_VALUE : apiVersion;

		//set version property.
		apiResource.setProperty(RegistryConstants.VERSION_PARAMETER_NAME, apiVersion);
		//set content.
		apiResource.setContent(RegistryUtils.encodeString(data.toString()));

		String resourceId = apiResource.getUUID();
		//set resource UUID
		resourceId = (resourceId == null) ? UUID.randomUUID().toString() : resourceId;

		apiResource.setUUID(resourceId);
		String actualPath = getChrootedApiLocation(requestContext.getRegistryContext()) + CurrentSession.getUser() +
		                    RegistryConstants.PATH_SEPARATOR + apiName +
		                    RegistryConstants.PATH_SEPARATOR + apiVersion + "/api";
		//saving the api resource to repository.
		registry.put(actualPath, apiResource);

		return actualPath;

	}

	/**
	 * Returns a Json element as a string
	 *
	 * @param object Json Object
	 * @param key    Element key
	 * @return Element value
	 */
	public static String getChildElementText(JsonObject object, String key) {
		JsonElement element = object.get(key);
		if (element != null) {
			return object.get(key).getAsString();
		}
		return null;
	}

	public static boolean isValidSwaggerContent(JsonObject swaggerObject, String version) {
		//TODO: VALIDATE SWAGGER CONTENT AGAINST SCHEMA

		return true;
	}

	/**
	 * Contains the logic to create URITemplate XML Element from the swagger 1.2 resource.
	 *
	 * @param resourceObjects The API resource documents
	 * @return URITemplate element
	 */
	private static List<OMElement> createURITemplateFromSwagger12(List<JsonObject> resourceObjects) {

		List<OMElement> uriTemplates = new ArrayList<OMElement>();

		for (JsonObject resourceObject : resourceObjects) {
			JsonArray apis = resourceObject.getAsJsonArray(APIConstants.APIS);

			//Iterating through the Paths
			for (JsonElement api : apis) {
				JsonObject apiObj = api.getAsJsonObject();
				OMElement uriTemplateElement = factory.createOMElement(URI_TEMPLATE, null);
				OMElement urlPatternElement =
						factory.createOMElement(URL_PATTERN, DEFAULT_NAMESPACE);
				urlPatternElement.setText(apiObj.get(APIConstants.PATH).getAsString());

				JsonArray methods = apiObj.getAsJsonArray(APIConstants.OPERATIONS);

				//Iterating through HTTP methods (Actions)
				for (JsonElement method : methods) {
					JsonObject methodObj = method.getAsJsonObject();

					OMElement httpVerbElement =
							factory.createOMElement(HTTP_VERB, DEFAULT_NAMESPACE);
					httpVerbElement.setText(methodObj.get(APIConstants.METHOD).getAsString());

					//Adding urlPattern element to URITemplate element.
					uriTemplateElement.addChild(urlPatternElement);
					uriTemplateElement.addChild(httpVerbElement);
					uriTemplates.add(uriTemplateElement);
				}
			}

		}

		return uriTemplates;
	}

	private static List<OMElement> createURITemplateFromSwagger2(JsonObject swaggerDocObject) {

		List<OMElement> uriTemplates = new ArrayList<OMElement>();
		OMElement uriTemplateElement = factory.createOMElement(URI_TEMPLATE, DEFAULT_NAMESPACE);

		JsonObject paths = swaggerDocObject.get(APIConstants.PATHS).getAsJsonObject();
		Set<Map.Entry<String, JsonElement>> pathSet = paths.entrySet();

		for (Map.Entry path : pathSet) {
			OMElement urlPatternElement = factory.createOMElement(URL_PATTERN, DEFAULT_NAMESPACE);
			urlPatternElement.setText(path.getKey().toString());
			JsonObject urlPattern = ((JsonElement)path.getValue()).getAsJsonObject();
			Set<Map.Entry<String, JsonElement>> operationSet = urlPattern.entrySet();

			for (Map.Entry operationEntry : operationSet) {
				OMElement httpVerbElement = factory.createOMElement(HTTP_VERB, DEFAULT_NAMESPACE);

				httpVerbElement.setText(operationEntry.getKey().toString());

				uriTemplateElement.addChild(urlPatternElement);
				uriTemplateElement.addChild(httpVerbElement);
				uriTemplates.add(uriTemplateElement);
			}

		}
		return uriTemplates;
	}

	/**
	 * Returns the root location of the API.
	 *
	 * @param registryContext Registry context
	 * @return the root location of the API artifact.
	 */
	private  static String getChrootedApiLocation(RegistryContext registryContext) {
		return RegistryUtils.getAbsolutePath(registryContext,
		                                     RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
		                                     API_RELATIVE_LOCATION);
	}
}
