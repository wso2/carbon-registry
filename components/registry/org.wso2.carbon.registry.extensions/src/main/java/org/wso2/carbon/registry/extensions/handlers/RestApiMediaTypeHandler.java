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
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import java.io.ByteArrayInputStream;

public class RestApiMediaTypeHandler extends Handler {

	@Override public void put(RequestContext requestContext) throws RegistryException {
		if (!CommonUtil.isUpdateLockAvailable()) {
			return;
		}
		CommonUtil.acquireUpdateLock();

		try {
			if (requestContext == null) {
				throw new RegistryException("The request context is not available.");
			}
			Resource resource = requestContext.getResource();
			Object o = resource.getContent();
			String resourcePath = requestContext.getResourcePath().getPath();
			Registry registry = requestContext.getRegistry();

			if (o == null || !(o instanceof byte[])) {
				String msg = "Invalid resource content.";
				throw new RegistryException(msg);
			}

			byte[] content = (byte[]) resource.getContent();
			ByteArrayInputStream in = new ByteArrayInputStream(content);
			OMElement docElement;
			try {
				StAXOMBuilder builder = new StAXOMBuilder(in);
				docElement = builder.getDocumentElement();
				System.out.println(docElement);
			} catch (Exception ae) {
				throw new RegistryException(
						"Unexpected error occurred in parsing the API content.");
			}


		} finally {
			CommonUtil.releaseUpdateLock();
		}
	}
}
