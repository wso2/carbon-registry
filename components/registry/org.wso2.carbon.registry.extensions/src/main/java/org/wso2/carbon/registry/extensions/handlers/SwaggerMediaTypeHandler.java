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
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.SwaggerProcessor;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import java.util.Iterator;

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
				processor.addSwaggerToRegistry(requestContext, resourceContent, getChrootedLocation(requestContext.getRegistryContext()));
			} else {
				log.error("Resource content is not valid.");
				throw new RegistryException("Resource content is not valid.");
			}

			requestContext.setProcessingComplete(true);
		} finally {
			CommonUtil.releaseUpdateLock();
		}
	}

	private String getChrootedLocation(RegistryContext registryContext) {
		return RegistryUtils.getAbsolutePath(registryContext,
		                                     RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
		                                     location);
	}

}
