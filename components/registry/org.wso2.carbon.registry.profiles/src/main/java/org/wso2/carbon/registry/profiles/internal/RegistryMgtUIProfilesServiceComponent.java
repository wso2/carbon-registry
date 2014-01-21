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

package org.wso2.carbon.registry.profiles.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.app.RemoteRegistryService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.URLMatcher;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.profiles.handlers.ProfilesAddHandler;
import org.wso2.carbon.registry.profiles.utils.CommonUtil;

/**
 * @scr.component name="org.wso2.carbon.registry.profiles" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 */
public class RegistryMgtUIProfilesServiceComponent {

    private static Log log = LogFactory.getLog(RegistryMgtUIProfilesServiceComponent.class);

    private Registry registry = null;

    protected void activate(ComponentContext context) {
        log.debug("******* Registry Profiles Management bundle is activated ******* ");
    }

    protected void deactivate(ComponentContext context) {
        log.debug("Registry Profiles Management bundle is deactivated ");
    }

    protected void setRegistryService(RegistryService registryService) {
        CommonUtil.setRegistryService(registryService);

        if (registryService instanceof RemoteRegistryService) {
            log.warn("Profiles are not available on Remote Registry");
            return;
        }
        try {
            // We can't get Registry from Utils, as the MessageContext is not available at
            // activation time.
            Registry configSystemRegistry = registryService.getConfigSystemRegistry();
            if (registry != null && registry == configSystemRegistry) {
                // Handler has already been set.
                return;
            }
            registry = configSystemRegistry;
            if (registry == null ||
                    registry.getRegistryContext() == null ||
                    registry.getRegistryContext().getHandlerManager() == null) {
                String msg = "Error Initializing Registry Profile Handler";
                log.error(msg);
            } else {
                URLMatcher filter = new URLMatcher();
//                filter.setGetPattern(RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
//                        RegistryConstants.CONFIG_REGISTRY_BASE_PATH + RegistryConstants.PROFILES_PATH) +
//                        ".*/profiles");
//                filter.setPutPattern(RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
//                        RegistryConstants.CONFIG_REGISTRY_BASE_PATH + RegistryConstants.PROFILES_PATH) +
//                        ".*/profiles");
                filter.setGetPattern(RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                        "/") + ".*" + RegistryConstants.PROFILES_PATH + ".*/profiles");
                filter.setPutPattern(RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                        "/") + ".*" + RegistryConstants.PROFILES_PATH + ".*/profiles");
                ProfilesAddHandler handler = new ProfilesAddHandler();
                registry.getRegistryContext().getHandlerManager().addHandler(null, filter, handler);
            }
        } catch (Exception e) {
            String msg = "Error Initializing Registry Profiles Handler";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        CommonUtil.setRegistryService(null);
    }
}
