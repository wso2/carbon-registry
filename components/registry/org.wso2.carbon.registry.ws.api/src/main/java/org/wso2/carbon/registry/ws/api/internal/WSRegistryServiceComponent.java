/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.ws.api.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.ws.api.utils.CommonUtil;
import org.wso2.carbon.registry.ws.api.utils.WSDeploymentInterceptor;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="registry.ws.api.component" immediate="true"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 *                policy="dynamic" bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 */
public class WSRegistryServiceComponent {

	private static Log log = LogFactory.getLog(WSRegistryServiceComponent.class);

    private RegistryService registryService;

    private ConfigurationContext configContext;
	 /**
     * @param ctxt
     */
    protected void activate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("Registry WS API bundle is activated");
        }
        try {
        	AxisConfiguration config = configContext.getAxisConfiguration();
        	WSDeploymentInterceptor interceptor = new WSDeploymentInterceptor();
        	interceptor.init(config);
        	config.addObservers(interceptor);
        } catch (Throwable e) {
            log.error("Error occured while updating Registry WS API service", e);
        }
    }

    /**
     * @param ctxt
     */
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("Identity RP bundle is deactivated");
        }
    }
    
    /**
     * @param contextService
     */
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.info("ConfigurationContextService set in Registry WS API bundle");
        }
        configContext = contextService.getServerConfigContext();
//        configContext.getAxisConfiguration().addObservers(new WSDeploymentInterceptor());
    }

    /**
     * @param contextService
     */
    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.info("ConfigurationContextService unset in Registry WS API bundle");
        }
        configContext = null;
    }

    protected void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
        CommonUtil.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        this.registryService = null;
        CommonUtil.setRegistryService(null);
    }
}
