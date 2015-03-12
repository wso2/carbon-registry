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
import java.util.Map;
import java.util.Set;

public class APIUtils {

	private static final Log log = LogFactory.getLog(APIUtils.class);

	private static final OMFactory factory = OMAbstractFactory.getOMFactory();
	public static final OMNamespace DEFAULT_NAMESPACE = null;

	public static final String OVERVIEW = "overview";
	public static final String PROVIDER = "provider";
	public static final String NAME = "name";
	public static final String CONTEXT = "context";
	public static final String VERSION = "version";
	public static final String TRANSPORTS = "transports";
	public static final String DESCRIPTION = "description";
	public static final String URI_TEMPLATE = "URITemplate";
	public static final String URL_PATTERN = "urlPattern";
	public static final String RESOURCE = "resource";
	public static final String PATH = "path";
	public static final String OPERATIONS = "operations";
	public static final String PARAMETERS = "parameters";
	public static final String OPERATION = "operation";
	public static final String HTTP_VERB = "httpVerb";
	public static final String PARAMETER = "parameter";
	public static final String PARAM_TYPE = "paramType";
	public static final String DATA_TYPE = "dataType";
	public static final String REQUIRED = "required";
	private static final String SUMMARY = "summary";

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
		OMElement overview = factory.createOMElement(OVERVIEW, namespace);
		OMElement provider = factory.createOMElement(PROVIDER, namespace);
		OMElement name = factory.createOMElement(NAME, namespace);
		OMElement context = factory.createOMElement(CONTEXT, namespace);
		OMElement apiVersion = factory.createOMElement(VERSION, namespace);
		OMElement transports = factory.createOMElement(TRANSPORTS, namespace);
		OMElement description = factory.createOMElement(DESCRIPTION, namespace);
		OMElement uriTemplate = null;

		JsonObject infoObject = swaggerDocObject.get(APIConstants.INFO_OBJECT).getAsJsonObject();
		name.setText(getChildElementText(infoObject, APIConstants.TITLE).replaceAll("\\s", ""));
		description.setText(getChildElementText(infoObject, APIConstants.DESCRIPTION));
		provider.setText(CurrentSession.getUser());

		if (swaggerVersion.equals(APIConstants.SWAGGER_VERSION_2)) {
			String host = getChildElementText(swaggerDocObject, APIConstants.HOST);
			String basePath = getChildElementText(swaggerDocObject, APIConstants.BASEPATH);

			if (host != null && basePath != null) {
				context.setText(host + basePath);
			}
			apiVersion.setText(getChildElementText(infoObject, "version"));
			transports.setText(getChildElementText(swaggerDocObject, APIConstants.SCHEMES));
			uriTemplate = createURITemplateFromSwagger2(swaggerDocObject);
		} else if (swaggerVersion.equals(APIConstants.SWAGGER_VERSION_1_2)) {
			apiVersion.setText(getChildElementText(swaggerDocObject, APIConstants.API_VERSION));
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

		for (JsonObject resourceObject : resourceObjects) {
			JsonArray apis = resourceObject.getAsJsonArray(APIConstants.APIS);

			//Iterating through the Paths
			for (JsonElement api : apis) {
				JsonObject apiObj = api.getAsJsonObject();

				OMElement urlPatternElement =
						factory.createOMElement(URL_PATTERN, DEFAULT_NAMESPACE);
				urlPatternElement.addAttribute(PATH, apiObj.get(APIConstants.PATH).getAsString(),
				                               DEFAULT_NAMESPACE);

				JsonArray methods = apiObj.getAsJsonArray(APIConstants.OPERATIONS);

				//Iterating through HTTP methods (Actions)
				for (JsonElement method : methods) {
					JsonObject methodObj = method.getAsJsonObject();
					JsonArray parameters = methodObj.getAsJsonArray(APIConstants.PARAMETERS);

					OMElement operationElement =
							factory.createOMElement(OPERATION, DEFAULT_NAMESPACE);

					OMElement httpVerbElement =
							factory.createOMElement(HTTP_VERB, DEFAULT_NAMESPACE);
					OMElement parametersElement =
							factory.createOMElement(PARAMETERS, DEFAULT_NAMESPACE);
					OMElement summaryElement = factory.createOMElement(SUMMARY, DEFAULT_NAMESPACE);
					summaryElement.setText(methodObj.get(APIConstants.SUMMARY).getAsString());
					httpVerbElement.setText(methodObj.get(APIConstants.METHOD).getAsString());

					if (parameters != null) {
						//Iterating through action parameters
						for (JsonElement parameter : parameters) {
							JsonObject paramObj = parameter.getAsJsonObject();

							OMElement parameterElement =
									factory.createOMElement(PARAMETER, DEFAULT_NAMESPACE);
							parameterElement.addAttribute(REQUIRED,
							                              paramObj.get(APIConstants.REQUIRED)
							                                      .getAsString(),
							                              DEFAULT_NAMESPACE);
							OMElement nameElement =
									factory.createOMElement(NAME, DEFAULT_NAMESPACE);
							nameElement.setText(paramObj.get(APIConstants.NAME).getAsString());
							OMElement typeElement =
									factory.createOMElement(PARAM_TYPE, DEFAULT_NAMESPACE);
							typeElement
									.setText(paramObj.get(APIConstants.PARAM_TYPE).getAsString());
							OMElement descriptionElement =
									factory.createOMElement(DESCRIPTION, DEFAULT_NAMESPACE);
							descriptionElement
									.setText(paramObj.get(APIConstants.DESCRIPTION).getAsString());
							OMElement dataTypeElement =
									factory.createOMElement(DATA_TYPE, DEFAULT_NAMESPACE);
							dataTypeElement.setText(paramObj.get(APIConstants.TYPE).getAsString());

							//Adding parameter element children
							parameterElement.addChild(nameElement);
							parameterElement.addChild(descriptionElement);
							parameterElement.addChild(typeElement);
							parameterElement.addChild(dataTypeElement);
							//Adding the parameter element to parameters element.
							parametersElement.addChild(parameterElement);
						}
					}
					//Adding operation element children.
					operationElement.addChild(httpVerbElement);
					operationElement.addChild(summaryElement);
					operationElement.addChild(parametersElement);
					//Adding the operation element to the urlPattern element.
					urlPatternElement.addChild(operationElement);
				}
				//Adding urlPattern element to URITemplate element.
				uriTemplateElement.addChild(urlPatternElement);
			}
		}

		return uriTemplateElement;
	}

	private static OMElement createURITemplateFromSwagger2(JsonObject swaggerDocObject) {

		OMElement uriTemplateElement = factory.createOMElement(URI_TEMPLATE, DEFAULT_NAMESPACE);

		JsonObject paths = swaggerDocObject.get(APIConstants.PATHS).getAsJsonObject();
		Set<Map.Entry<String, JsonElement>> pathSet = paths.entrySet();

		for (Map.Entry path : pathSet) {
			OMElement urlPatternElement = factory.createOMElement(URL_PATTERN, DEFAULT_NAMESPACE);
			urlPatternElement.addAttribute(PATH, path.getKey().toString(), DEFAULT_NAMESPACE);
			JsonObject urlPattern = ((JsonElement)path.getValue()).getAsJsonObject();
			Set<Map.Entry<String, JsonElement>> operationSet = urlPattern.entrySet();

			for (Map.Entry operationEntry : operationSet) {
				JsonObject operation = ((JsonElement)operationEntry.getValue()).getAsJsonObject();

				OMElement operationElement = factory.createOMElement(OPERATION, DEFAULT_NAMESPACE);
				OMElement httpVerbElement = factory.createOMElement(HTTP_VERB, DEFAULT_NAMESPACE);
				OMElement summaryElement = factory.createOMElement(SUMMARY, DEFAULT_NAMESPACE);
				OMElement parametersElement =
						factory.createOMElement(PARAMETERS, DEFAULT_NAMESPACE);

				httpVerbElement.setText(operationEntry.getKey().toString());
				summaryElement.setText(operation.get(SUMMARY).getAsString());

				JsonArray parameters = operation.getAsJsonArray(APIConstants.PARAMETERS);

				if (parameters != null) {
					//Iterating through action parameters
					for (JsonElement parameter : parameters) {
						JsonObject paramObj = parameter.getAsJsonObject();

						OMElement parameterElement =
								factory.createOMElement(PARAMETER, DEFAULT_NAMESPACE);
						parameterElement.addAttribute(REQUIRED, paramObj.get(APIConstants.REQUIRED)
						                                                .getAsString(),
						                              DEFAULT_NAMESPACE);
						OMElement nameElement = factory.createOMElement(NAME, DEFAULT_NAMESPACE);
						nameElement.setText(paramObj.get(APIConstants.NAME).getAsString());
						OMElement typeElement =
								factory.createOMElement(PARAM_TYPE, DEFAULT_NAMESPACE);
						typeElement.setText(paramObj.get("in").getAsString());
						OMElement descriptionElement =
								factory.createOMElement(DESCRIPTION, DEFAULT_NAMESPACE);
						descriptionElement
								.setText(paramObj.get(APIConstants.DESCRIPTION).getAsString());

						//Adding parameter element children
						parameterElement.addChild(nameElement);
						parameterElement.addChild(descriptionElement);
						parameterElement.addChild(typeElement);
						//Adding the parameter element to parameters element.
						parametersElement.addChild(parameterElement);
					}
				}
				//Adding operation element children.
				operationElement.addChild(httpVerbElement);
				operationElement.addChild(summaryElement);
				operationElement.addChild(parametersElement);

				urlPatternElement.addChild(operationElement);
			}
			uriTemplateElement.addChild(urlPatternElement);
		}
		return uriTemplateElement;
	}
}
