/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.filters;

import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;

/**
 * Same as media type matcher except it has the addAssociation
 */
public class EndpointMediaTypeMatcher extends MediaTypeMatcher {
    public EndpointMediaTypeMatcher() {
        setMediaType(CommonConstants.ENDPOINT_MEDIA_TYPE);
    }

    public boolean handleAddAssociation(RequestContext requestContext)
            throws RegistryException {
        Resource resource =  requestContext.getRepository().get(requestContext.getSourcePath());

        if (resource != null) {
            String mType = resource.getMediaType();
            if (mType != null && (invert != mType.equals(CommonConstants.ENDPOINT_MEDIA_TYPE))) {
                return true;
            }
        }

        return false;
    }

    public boolean handleRemoveAssociation(RequestContext requestContext)
            throws RegistryException {
        Resource resource =  requestContext.getRepository().get(requestContext.getSourcePath());

        if (resource != null) {
            String mType = resource.getMediaType();
            if (mType != null && (invert != mType.equals(CommonConstants.ENDPOINT_MEDIA_TYPE))) {
                return true;
            }
        }

        return false;
    }
}
