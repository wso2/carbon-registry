/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.extensions.handlers;

import java.util.List;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;


public class ExtensionsSymLinkHandler extends Handler {

    @Override
    public void createLink(RequestContext requestContext) throws RegistryException {
        String symlinkPath = requestContext.getResourcePath().getPath();
        String targetResourcePath = requestContext.getTargetPath();

        if (requestContext.getRegistry().resourceExists(targetResourcePath)) {
            Resource r = requestContext.getRegistry().get(targetResourcePath);
            r.addProperty("registry.resource.symlink.path", symlinkPath);
            requestContext.getRegistry().put(targetResourcePath, r);
        }

    }

	@Override
	public void delete(RequestContext requestContext) throws RegistryException {
		if (!CommonUtil.isUpdateLockAvailable()) {
			return;
		}
		CommonUtil.acquireUpdateLock();
		try {
			Resource resource = requestContext.getRegistry().get(
					requestContext.getResourcePath().getPath());
			List<String> symlinkPaths = resource.getPropertyValues("registry.resource.symlink.path");
            if (symlinkPaths != null && symlinkPaths.size() > 0) {
			for (String symlinkPath : symlinkPaths) {
				if (symlinkPath != null) {
					if (requestContext.getRegistry()
							.resourceExists(symlinkPath)) {
						requestContext.getRegistry().delete(symlinkPath);
					}
				}
			}
        }

		} finally {
			CommonUtil.releaseUpdateLock();
		}
	}
}
