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

import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.security.vault.util.SecureVaultUtil;

import java.io.UnsupportedEncodingException;

public class RegistrySecurityAdminService extends RegistryAbstractAdmin {

	private static Log log = LogFactory.getLog(RegistrySecurityAdminService.class);

	/**
	 * Method to do the encryption operation by invoking CryptoUtil
	 * 
	 * @param plainTextValue	Plain text value.
	 * @return			Encrypted value.
	 * @throws CryptoException  	Throws while error during encryption.
	 */
	public String doEncrypt(String plainTextValue) throws CryptoException {
		 return SecureVaultUtil.doEncrypt(plainTextValue);
	}

	/**
	 * Method to decrypt a property, when key of the property is provided.
	 *
	 * @param key			Key of the property.
	 * @return 			Decrypted property value.
	 * @throws RegistryException	Throws while error during decryption.
	 */
	public String getDecryptedPropertyValue(String key) throws RegistryException {
		return SecureVaultUtil.getDecryptedPropertyValue(key);
	}

	/**
	 * Method to decrypt a property, when encrypted value is provided.
	 *
	 * @param encryptedValue                encrypted property value.
	 * @return 				decrypted property value.
	 * @throws CryptoException              Throws when an error occurs during decryption.
	 * @throws UnsupportedEncodingException Throws when an error occurs during byte array to string conversion.
	 */
	public String doDecrypt(String encryptedValue) throws CryptoException, UnsupportedEncodingException {
		return SecureVaultUtil.doDecrypt(encryptedValue);
	}

}
