/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.custom.services;

import org.wso2.carbon.registry.admin.api.resource.ICustomUIService;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.resource.services.utils.*;
import org.wso2.carbon.registry.core.session.UserRegistry;

public class CustomUIService extends RegistryAbstractAdmin implements ICustomUIService {

    public String getTextContent(String path) throws Exception {
        UserRegistry registry = (UserRegistry)getRootRegistry();
        return GetTextContentUtil.getTextContent(path, registry);
    }

    public boolean updateTextContent(String path, String content) throws Exception {
        UserRegistry registry = (UserRegistry)getRootRegistry();
        UpdateTextContentUtil.updateTextContent(path, content, registry);
        return true;
    }

    public boolean addTextContent(
            String parentPath, String resourceName, String mediaType, String description, String content)
            throws Exception {
        UserRegistry registry = (UserRegistry)getRootRegistry();
        AddTextResourceUtil.addTextResource(parentPath, resourceName, mediaType, description, content, registry);
        return true;
    }

    public boolean isAuthorized(String path, String action) {
        return false;
    }
}
