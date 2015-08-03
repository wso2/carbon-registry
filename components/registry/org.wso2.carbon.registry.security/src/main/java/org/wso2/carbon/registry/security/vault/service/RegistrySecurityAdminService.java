/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.registry.security.vault.service;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.security.vault.util.SecureVaultUtil;

public class RegistrySecurityAdminService extends RegistryAbstractAdmin {

	private static Log log = LogFactory.getLog(RegistrySecurityAdminService.class);

	/**
	 * Operation to do the encryption ops by invoking secure vault api
	 * 
	 * @param plainTextPass
	 * @return
	 * @throws AxisFault
	 */
	public String doEncrypt(String plainTextPass) throws AxisFault {
        return SecureVaultUtil.encryptValue(plainTextPass);

	}

    public String doDecrypt(String cipherText) throws AxisFault {
		// TODO:yet to implement
		return null;
	}

}
