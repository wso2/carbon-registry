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

package org.wso2.carbon.registry.indexing.service;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @scr.component name="registry.indexing" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */

public class ContentBasedSearchServiceComponet {

    private static Log log = LogFactory.getLog(ContentBasedSearchServiceComponet.class);

    protected void activate(ComponentContext ctxt) {
        log.debug("registry.mgt.ui.search bundle is activated ");
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("registry.mgt.ui.search bundle is deactivated ");
    }

    protected void setRegistryService(RegistryService registryService) {
        Utils.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        Utils.setRegistryService(null);
    }

}
