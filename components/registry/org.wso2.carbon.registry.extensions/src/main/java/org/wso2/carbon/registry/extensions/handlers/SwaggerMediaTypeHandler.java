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

package org.wso2.carbon.registry.extensions.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.SwaggerProcessor;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.*;

public class SwaggerMediaTypeHandler extends Handler {

	private static final Log log = LogFactory.getLog(SwaggerMediaTypeHandler.class);

	private String location;
	private OMElement swaggerlocationConfiguration;

	public OMElement getSwaggerLocationConfiguration() {
		return swaggerlocationConfiguration;
	}

	public void setSwaggerLocationConfiguration(OMElement locationConfiguration) throws RegistryException {
		Iterator confElements = locationConfiguration.getChildElements();
		while (confElements.hasNext()) {
			OMElement confElement = (OMElement)confElements.next();
			if (confElement.getQName().equals(new QName("location"))) {
				location = confElement.getText();
				if (!location.startsWith(RegistryConstants.PATH_SEPARATOR)) {
					location = RegistryConstants.PATH_SEPARATOR + location;
				}
				if (!location.endsWith(RegistryConstants.PATH_SEPARATOR)) {
					location = location + RegistryConstants.PATH_SEPARATOR;
				}
			}
		}
		this.swaggerlocationConfiguration = locationConfiguration;
	}

	@Override public void put(RequestContext requestContext) throws RegistryException {
		if (!CommonUtil.isUpdateLockAvailable()) {
			return;
		}
		CommonUtil.acquireUpdateLock();

		try {
			if (requestContext == null) {
				throw new RegistryException("The request context is not available.");
			}
			SwaggerProcessor processor = new SwaggerProcessor();
			Resource resource = requestContext.getResource();
			Object content = resource.getContent();
			String resourceContent;

			if(content instanceof String) {
				resourceContent = (String)content;
				resource.setContent(RegistryUtils.encodeString(resourceContent));
			} else if(content instanceof byte[]) {
				resourceContent = RegistryUtils.decodeBytes((byte[])content);
			} else {
				log.error("Resource content is not valid.");
				throw new RegistryException("Resource content is not valid.");
			}

			requestContext.setProcessingComplete(true);
		} finally {
			CommonUtil.releaseUpdateLock();
		}
	}

	private void addSwaggerToRegistry(RequestContext requestContext, InputStream inputStream)
			throws RegistryException {

		Resource resource;
		if (requestContext.getResource() == null) {
			resource = new ResourceImpl();
			resource.setMediaType(CommonConstants.SWAGGER_MEDIA_TYPE);
		} else {
			resource = requestContext.getResource();
		}

		String version =
				requestContext.getResource().getProperty(RegistryConstants.VERSION_PARAMETER_NAME);

		if (version == null) {
			version = CommonConstants.SWAGGER_VERSION_DEFAULT_VALUE;
			resource.setProperty(RegistryConstants.VERSION_PARAMETER_NAME, version);
		}

		//ByteArrayOutputStream resourceContent = readSourceContent(inputStream);

		String resourcePath = requestContext.getResourcePath().getPath();
		String swaggerFileName = resourcePath
				.substring(resourcePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);

		Registry systemRegistry = CommonUtil.getUnchrootedSystemRegistry(requestContext);
		RegistryContext registryContext = requestContext.getRegistryContext();
		String commonLocation = getChrootedLocation(registryContext);

		//Creating new collection
		if (!systemRegistry.resourceExists(commonLocation)) {
			systemRegistry.put(commonLocation, systemRegistry.newCollection());
		}

		String swaggerPath;
		if (!resourcePath.startsWith(commonLocation)
		    && !resourcePath.equals(RegistryUtils.getAbsolutePath(registryContext,
		                                                          RegistryConstants.PATH_SEPARATOR +
		                                                          swaggerFileName))
		    && !resourcePath.equals(RegistryUtils.getAbsolutePath(registryContext,
		                                                          RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
		                                                          RegistryConstants.PATH_SEPARATOR + swaggerFileName))) {
			swaggerPath = resourcePath;
		} else {
			swaggerPath = commonLocation + version + "/" + swaggerFileName;
		}


		Registry registry = requestContext.getRegistry();

		String relativeArtifactPath = RegistryUtils.getRelativePath(registry.getRegistryContext(), swaggerPath);

		relativeArtifactPath = RegistryUtils.getRelativePathToOriginal(relativeArtifactPath,
		                                                               RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);

		Resource newResource;
		if (registry.resourceExists(swaggerPath)) {
			newResource = registry.get(swaggerPath);
		} else {
			newResource = new ResourceImpl();
			Properties properties = resource.getProperties();
			if (properties != null) {
				List<String> linkProperties = Arrays
						.asList(RegistryConstants.REGISTRY_LINK, RegistryConstants.REGISTRY_USER,
						        RegistryConstants.REGISTRY_MOUNT, RegistryConstants.REGISTRY_AUTHOR,
						        RegistryConstants.REGISTRY_MOUNT_POINT,
						        RegistryConstants.REGISTRY_TARGET_POINT,
						        RegistryConstants.REGISTRY_ACTUAL_PATH,
						        RegistryConstants.REGISTRY_REAL_PATH);
				for (Map.Entry<Object, Object> e : properties.entrySet()) {
					String key = (String) e.getKey();
					if (!linkProperties.contains(key)) {
						newResource.setProperty(key, (List<String>) e.getValue());
					}
				}
			}
		}

		newResource.setMediaType(CommonConstants.SWAGGER_MEDIA_TYPE);
		String swaggerResourceUUID = resource.getUUID();
		if (swaggerResourceUUID == null) {
			swaggerResourceUUID = UUID.randomUUID().toString();
		}
		newResource.setUUID(swaggerResourceUUID);
		//newResource.setContent(new String(resourceContent.toByteArray()));
		addSwaggerToRegistry(requestContext, swaggerPath, requestContext.getSourceURL(),
		                     newResource, registry);
		((ResourceImpl)newResource).setPath(relativeArtifactPath);

		requestContext.setResource(newResource);

	}

	private String getChrootedLocation(RegistryContext registryContext) {
		return RegistryUtils.getAbsolutePath(registryContext,
		                                     RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
		                                     location);
	}

	protected void addSwaggerToRegistry(RequestContext context, String path, String url,
	                                    Resource resource, Registry registry) throws RegistryException {
		context.setActualPath(path);
		registry.put(path, resource);
	}
}
