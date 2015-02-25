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
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.Repository;
import org.wso2.carbon.registry.core.jdbc.VersionRepository;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

public class SwaggerProcessor {

	private Registry registry;
	private Repository repository;
	private VersionRepository versionRepository;

	public SwaggerProcessor(RequestContext requestContext) {
		registry = requestContext.getRegistry();
		repository = requestContext.getRepository();
		versionRepository = requestContext.getVersionRepository();
	}

	public void populateAPI(String swaggerContent, RequestContext requestContext)
			throws RegistryException {

		Resource resource = new ResourceImpl();
		resource.setMediaType("application/vnd.wso2-restapi");
		String s = "<metadata xmlns=\"http://www.wso2.org/governance/metadata\">" +
		           "<information>" +
		           "<title>Test</title>" +
		           "<description>testing description</description>" +
		           "<host>http://localhost</host>" +
		           "<basePath>/petstore</basePath>" +
		           "<version>1.0.0</version>" +
		           "</information>" +
		           "<contact>" +
		           "<name>Test</name>" +
		           "<url>http://localhost</url>" +
		           "<email>http://localhost</email>" +
		           "</contact>" +
		           "<licence>" +
		           "<name>Apache</name>" +
		           "<url>http://localhost</url>" +
		           "</licence>" +
		           "<interface>" +
		           "<swaggerURL>http://localhost</swaggerURL>" +
		           "<schemes>https</schemes>" +
		           "<consumes>application/json</consumes>" +
		           "<produces>text/plain</produces>" +
		           "</interface>" +
		           "</metadata>";

		resource.setContent(RegistryUtils.encodeString(s));

		String actualPath = "/_system/governance/apimgt/applicationdata/Test2/1.2.0/api";
		requestContext.setResourcePath(new ResourcePath(actualPath));
		requestContext.setResource(resource);
		registry.put(actualPath, resource);
	}

}
