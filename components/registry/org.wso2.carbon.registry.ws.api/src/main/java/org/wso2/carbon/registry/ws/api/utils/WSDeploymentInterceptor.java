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
package org.wso2.carbon.registry.ws.api.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.CarbonUtils;

public class WSDeploymentInterceptor implements AxisObserver {

	private static final String WS_REGISTRY_SERVICE_NAME = "WSRegistryService";

    private static final Log log = LogFactory.getLog(WSDeploymentInterceptor.class);

	private static final String WS_API_SEC_POLICY_FILE = "ws-api-sec-policy.xml";
	
	public void init(AxisConfiguration axisConfiguration) {
		 if (log.isDebugEnabled()) {
	            log.debug("Initializing Registry WS-API Component");
	        }

	        try {
	            AxisService service = axisConfiguration.getService(WS_REGISTRY_SERVICE_NAME);
	            if (service != null) {
	                engageSecurity(service);
	            }
	        } catch (AxisFault ignore) {

	        }


	}

	public void moduleUpdate(AxisEvent arg0, AxisModule arg1) {
		// TODO Auto-generated method stub

	}

	public void serviceGroupUpdate(AxisEvent arg0, AxisServiceGroup arg1) {
		// TODO Auto-generated method stub

	}

	public void serviceUpdate(AxisEvent event, AxisService service) {
		if (event.getEventType() == AxisEvent.SERVICE_DEPLOY
                && WS_REGISTRY_SERVICE_NAME.equals(service.getName())) {
            try {
            	engageSecurity(service);
            } catch (Exception e) {
                log.error("Error while updating " + WS_REGISTRY_SERVICE_NAME
                        + " in WSDeploymentInterceptor", e);
                throw new RuntimeException(e);
            }
        }

	}
	
	/**
     * Updates WSRegistryService with Crypto information
     *
     * @param config AxisConfiguration
     * @throws Exception
     */
    private void engageSecurity(AxisService service) {
    	// Get the WS Service to update security policy with keystore information
        
    	String path = CarbonUtils.getCarbonConfigDirPath() + File.separator + WS_API_SEC_POLICY_FILE;
        FileInputStream in = null;
        try {
            in = new FileInputStream(path);
            AxisModule module = service.getAxisConfiguration().getModule("rampart");
            if (module == null) {
                log.error("Rampart module is not available in the system. Unable to engage " +
                        "security on the Registry WS-API Component");
                return;
            }

            log.info("Loading security policy for the Registry WS-API Component from " + path);
            Policy policy = PolicyEngine.getPolicy(in);
            SecurityUtil.addUser();
            service.getPolicySubject().attachPolicy(policy);
            service.engageModule(module);

        } catch (FileNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("No security policies specified for the Registry WS-API Component");
            }
        } catch (AxisFault e) {
            log.error("Error while engaging security on the Registry WS-API Component", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) { }
            }
        }

    }

	public void addParameter(Parameter arg0) throws AxisFault {
		// TODO Auto-generated method stub

	}

	public void deserializeParameters(OMElement arg0) throws AxisFault {
		// TODO Auto-generated method stub

	}

	public Parameter getParameter(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Parameter> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isParameterLocked(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeParameter(Parameter arg0) throws AxisFault {
		// TODO Auto-generated method stub

	}

}
