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

import java.util.Properties;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.base.ServerConfiguration;

public class SecurityUtil {

	 private static final Log log = LogFactory.getLog(SecurityUtil.class);

	    static String policyString = "<wsp:Policy wsu:Id=\"SigOnly\"" +
	            "                    xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"" +
	            "                    xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">" +
	            "            <wsp:ExactlyOne>" +
	            "                <wsp:All>" +
	            "                    <sp:AsymmetricBinding xmlns:sp=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy\">" +
	            "                        <wsp:Policy>" +
	            "                            <sp:InitiatorToken>" +
	            "                                <wsp:Policy>" +
	            "                                    <sp:X509Token" +
	            "                                            sp:IncludeToken=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/AlwaysToRecipient\">" +
	            "                                        <wsp:Policy>" +
	            "                                            <sp:RequireThumbprintReference/>" +
	            "                                            <sp:WssX509V3Token10/>" +
	            "                                        </wsp:Policy>" +
	            "                                    </sp:X509Token>" +
	            "                                </wsp:Policy>" +
	            "                            </sp:InitiatorToken>" +
	            "                            <sp:RecipientToken>" +
	            "                                <wsp:Policy>" +
	            "                                    <sp:X509Token" +
	            "                                            sp:IncludeToken=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/Never\">" +
	            "                                        <wsp:Policy>" +
	            "                                            <sp:RequireThumbprintReference/>" +
	            "                                            <sp:WssX509V3Token10/>" +
	            "                                        </wsp:Policy>" +
	            "                                    </sp:X509Token>" +
	            "                                </wsp:Policy>" +
	            "                            </sp:RecipientToken>" +
	            "                            <sp:AlgorithmSuite>" +
	            "                                <wsp:Policy>" +
	            "                                    <sp:Basic256/>" +
	            "                                </wsp:Policy>" +
	            "                            </sp:AlgorithmSuite>" +
	            "                            <sp:Layout>" +
	            "                                <wsp:Policy>" +
	            "                                    <sp:Strict/>" +
	            "                                </wsp:Policy>" +
	            "                            </sp:Layout>" +
	            "                            <sp:IncludeTimestamp/>" +
	            "                            <sp:OnlySignEntireHeadersAndBody/>" +
	            "                        </wsp:Policy>" +
	            "                    </sp:AsymmetricBinding>" +
	            "                    <sp:Wss10 xmlns:sp=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy\">" +
	            "                        <wsp:Policy>" +
	            "                            <sp:MustSupportRefKeyIdentifier/>" +
	            "                            <sp:MustSupportRefIssuerSerial/>" +
	            "                        </wsp:Policy>" +
	            "                    </sp:Wss10>" +
	            "                    <sp:SignedParts xmlns:sp=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy\">" +
	            "                        <sp:Body/>" +
	            "                    </sp:SignedParts>" +
	            "                </wsp:All>" +
	            "            </wsp:ExactlyOne>" +
	            "        </wsp:Policy>";

	public static Policy getSignOnlyPolicy() throws RegistryException {

	        Policy policy;

	        try {
	            OMElement policyOM = AXIOMUtil.stringToOM(policyString);
	            policy = PolicyEngine.getPolicy(policyOM);
	        } catch (Exception e) {
	            String msg = "error building policy from " + policyString;
	            log.error(msg);
	            throw new RegistryException(msg, e);
	        }

	        return policy;

	    }
}
