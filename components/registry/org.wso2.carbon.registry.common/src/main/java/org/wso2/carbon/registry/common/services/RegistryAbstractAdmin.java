/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.common.services;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.service.RegistryService;

import javax.servlet.http.HttpSession;

public abstract class RegistryAbstractAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(RegistryAbstractAdmin.class);

    public final static String SERVELT_SESSION = "comp.mgt.servlet.session";

    public Registry getRootRegistry() {
        if (getHttpSession() != null) {
            Registry registry =
                    (Registry) getHttpSession().getAttribute(
                            RegistryConstants.ROOT_REGISTRY_INSTANCE);
            if (registry != null) {
                return registry;
            } else {
                // TODO ideally there should also be an exception
                log.error("Unable to find root registry instance in http session");
            }
        }
        return null;
    }

    public Registry getRootRegistry(RegistryService registryService) {
        Registry registry = getRootRegistry();
        if (registry != null) {
            return registry;
        }
        HttpSession httpSession =
                (HttpSession) MessageContext.getCurrentMessageContext().getProperty(SERVELT_SESSION);
        if (httpSession != null) {
            registry = (Registry)
                    httpSession.getAttribute(RegistryConstants.ROOT_REGISTRY_INSTANCE);
            if (registry != null) {
                return registry;
            } else if (registryService != null) {
                PrivilegedCarbonContext carbonContext =  PrivilegedCarbonContext.getThreadLocalCarbonContext();
                int tenantId = carbonContext.getTenantId();
                String username = carbonContext.getUsername();
                try {
                    registry = registryService.getRegistry(username, tenantId);
                    httpSession.setAttribute(RegistryConstants.ROOT_REGISTRY_INSTANCE, registry);
                    return registry;
                } catch (Exception ignored) {
                    // We are not bothered about any errors in here.
                }
            }
        }
        return null;
    }

}
