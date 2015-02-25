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
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SwaggerProcessor {

	private static final Log log = LogFactory.getLog(SwaggerProcessor.class);

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

	public void addSwaggerToRegistry(RequestContext requestContext, String resourceContent)
			throws RegistryException {

	}

	private OMElement addSwaggerContent(String resourceContent) throws RegistryException {
		OMElement swaggerElement;
		try {
			swaggerElement = AXIOMUtil.stringToOM(resourceContent);
		} catch (Exception e) {
			log.error("Resource metadata does not include valid XML content.");
			throw new RegistryException("Unexpected error when parsing the metadata.");
		}

		OMElement overviewElement = getElementFromContent(swaggerElement, "overview");
		String sourceURL = getElementFromContent(overviewElement, "url").getText();

		if (sourceURL == null) {
			log.error("Swagger definition url is null.");
			throw new RegistryException("Source url is empty.");
		}

		InputStream inputStream = new ByteArrayInputStream(resourceContent.getBytes());
		String swaggerContent = readSourceContent(inputStream);

		OMFactory omFactory = OMAbstractFactory.getOMFactory();
		swaggerElement = omFactory.createOMElement(new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "metadata"));
		OMElement swaggerContentElement = omFactory.createOMElement(new QName("swaggerContent"));
		OMText swaggerContentText = omFactory.createOMText(swaggerContentElement, swaggerContent);

		swaggerContentElement.addChild(swaggerContentText);
		overviewElement.addChild(swaggerContentElement);
		swaggerElement.addChild(overviewElement);S

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
