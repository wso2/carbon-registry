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
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;

public class APIUtils {

	/**
	 * Extracts the data from swagger and creates an API registry artifact.
	 *
	 * @param swaggerDocObject Swagger Json Object.
	 * @param swaggerVersion   Swagger version.
	 * @return The API metadata
	 * @throws org.wso2.carbon.registry.core.exceptions.RegistryException If swagger content is invalid.
	 */
	public static OMElement createAPIArtifact(JsonObject swaggerDocObject, String swaggerVersion)
			throws RegistryException {

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

		JsonObject infoObject = swaggerDocObject.get("info").getAsJsonObject();
		name.setText(getChildElementText(infoObject, "title"));
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
}
