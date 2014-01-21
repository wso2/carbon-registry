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

package org.wso2.carbon.registry.activities.services.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.service.RegistryService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class CommonUtil {

    private static final Log log = LogFactory.getLog(CommonUtil.class);

    private static RegistryService registryService;

    public static synchronized void setRegistryService(RegistryService service) {
        registryService = service;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static UserRegistry getRegistry(HttpSession session) throws RegistryException {
        if (session == null) {
            return org.wso2.carbon.registry.common.utils.CommonUtil.getUserRegistry(registryService);
        }
        return org.wso2.carbon.registry.common.utils.CommonUtil.getUserRegistry(
                registryService, session);
    }
}
