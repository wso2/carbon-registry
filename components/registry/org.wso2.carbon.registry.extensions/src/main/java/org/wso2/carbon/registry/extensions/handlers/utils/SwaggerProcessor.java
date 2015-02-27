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

	}

}
