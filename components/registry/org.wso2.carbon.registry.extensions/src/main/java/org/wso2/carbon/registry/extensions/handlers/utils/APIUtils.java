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
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;

import java.util.List;

public class APIUtils {

	private static final Log log = LogFactory.getLog(APIUtils.class);
	private static final OMFactory factory = OMAbstractFactory.getOMFactory();
	public static final String APIS = "apis";
	public static final String URI_TEMPLATE = "URITemplate";
	public static final String URL_PATTERN = "urlPattern";
	public static final OMNamespace DEFAULT_NAMESPACE = null;

	/**
	 * Extracts the data from swagger and creates an API registry artifact.
	 *
	 * @param swaggerDocObject Swagger Json Object.
	 * @param swaggerVersion   Swagger version.
	 * @return The API metadata
	 * @throws org.wso2.carbon.registry.core.exceptions.RegistryException If swagger content is invalid.
	 */
	public static OMElement createAPIArtifact(JsonObject swaggerDocObject, String swaggerVersion,
	                                          List<JsonObject> resourceObjects)
			throws RegistryException {

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
		OMElement uriTemplate = null;

		JsonObject infoObject = swaggerDocObject.get("info").getAsJsonObject();
		name.setText(getChildElementText(infoObject, "title").replaceAll("\\s", ""));
		description.setText(getChildElementText(infoObject, "description"));
		provider.setText(CurrentSession.getUser());

		if (swaggerVersion.equals("2.0")) {
			String host = getChildElementText(swaggerDocObject, "host");
			String basePath = getChildElementText(swaggerDocObject, "basepath");

			if (host != null && basePath != null) {
				context.setText(host + basePath);
			}
			apiVersion.setText(getChildElementText(infoObject, "version"));
			transports.setText(getChildElementText(swaggerDocObject, "schemes"));

		} else if (swaggerVersion.equals("1.2")) {
			apiVersion.setText(getChildElementText(swaggerDocObject, "apiVersion"));
			uriTemplate = createURITemplateFromSwagger12(resourceObjects);
		}

		overview.addChild(provider);
		overview.addChild(name);
		overview.addChild(context);
		overview.addChild(apiVersion);
		overview.addChild(transports);
		overview.addChild(description);

		if(uriTemplate != null) {
			data.addChild(uriTemplate);
		}
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
	private static String getChildElementText(JsonObject object, String key) {
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
	private static OMElement createURITemplateFromSwagger12(List<JsonObject> resourceObjects) {

		OMElement uriTemplateElement = factory.createOMElement(URI_TEMPLATE, DEFAULT_NAMESPACE);

		for(JsonObject resourceObject : resourceObjects) {

			OMElement resourceElement = factory.createOMElement("resource", DEFAULT_NAMESPACE);
			resourceElement.addAttribute("name", resourceObject.get("resourcePath").getAsString(),
			                      DEFAULT_NAMESPACE);

			JsonArray apis = resourceObject.getAsJsonArray(APIS);

			//Iterating through the Paths
			for(JsonElement api : apis) {
				JsonObject apiObj = api.getAsJsonObject();

				OMElement urlPatternElement = factory.createOMElement(URL_PATTERN, DEFAULT_NAMESPACE);
				urlPatternElement.addAttribute("path", apiObj.get("path").getAsString(),
				                         DEFAULT_NAMESPACE);

				JsonArray methods = apiObj.getAsJsonArray("operations");

				OMElement operationsElement = factory.createOMElement("operations", DEFAULT_NAMESPACE);

				//Iterating through HTTP methods (Actions)
				for(JsonElement method : methods) {
					JsonObject methodObj = method.getAsJsonObject();
					JsonArray parameters = methodObj.getAsJsonArray("parameters");

					OMElement operationElement = factory.createOMElement("operation", DEFAULT_NAMESPACE);

					OMElement httpVerbElement = factory.createOMElement("httpVerb", DEFAULT_NAMESPACE);
					OMElement parametersElement = factory.createOMElement("parameters", DEFAULT_NAMESPACE);
					OMElement summaryElement = factory.createOMElement("summary", DEFAULT_NAMESPACE);
					summaryElement.setText(methodObj.get("summary").getAsString());
					httpVerbElement.setText(methodObj.get("method").getAsString());

					//Iterating through action parameters
					for(JsonElement parameter : parameters) {
						JsonObject paramObj = parameter.getAsJsonObject();

						OMElement parameterElement = factory.createOMElement("parameter", DEFAULT_NAMESPACE);
						parameterElement.addAttribute("required", paramObj.get("required").getAsString(), DEFAULT_NAMESPACE);
						OMElement nameElement = factory.createOMElement("name", DEFAULT_NAMESPACE);
						nameElement.setText(paramObj.get("name").getAsString());
						OMElement typeElement = factory.createOMElement("paramType", DEFAULT_NAMESPACE);
						typeElement.setText(paramObj.get("paramType").getAsString());
						OMElement descriptionElement = factory.createOMElement("description", DEFAULT_NAMESPACE);
						descriptionElement.setText(paramObj.get("description").getAsString());
						OMElement dataTypeElement = factory.createOMElement("dataType", DEFAULT_NAMESPACE);
						dataTypeElement.setText(paramObj.get("type").getAsString());

						parameterElement.addChild(nameElement);
						parameterElement.addChild(descriptionElement);
						parameterElement.addChild(typeElement);
						parameterElement.addChild(dataTypeElement);

						parametersElement.addChild(parameterElement);
					}

					operationElement.addChild(httpVerbElement);
					operationElement.addChild(summaryElement);
					operationElement.addChild(parametersElement);

					operationsElement.addChild(operationElement);
				}
				urlPatternElement.addChild(operationsElement);
				resourceElement.addChild(urlPatternElement);
			}
			uriTemplateElement.addChild(resourceElement);
		}

		return uriTemplateElement;
	}
}
