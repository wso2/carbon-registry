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

package org.wso2.carbon.registry.security.vault;

import java.util.Properties;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.security.vault.internal.SecurityServiceHolder;
import org.wso2.carbon.registry.security.vault.util.SecureVaultConstants;
import org.wso2.carbon.registry.security.vault.util.SecureVaultUtil;
import org.wso2.securevault.DecryptionProvider;
import org.wso2.securevault.secret.SecretRepository;

/**
 * Holds all secrets in a file
 */
public class RegistrySecretRepository implements SecretRepository {

    private static Log log = LogFactory.getLog(RegistrySecretRepository.class);

    /* Parent secret repository */
    private SecretRepository parentRepository;

    public RegistrySecretRepository() {
        super();
    }

    /**
     * @param alias Alias name for look up a secret
     * @return Secret if there is any , otherwise ,alias itself
     * @see org.wso2.securevault.secret.SecretRepository
     */
    public String getSecret(String alias) {

        UserRegistry registry = null;
        try {
            registry = SecurityServiceHolder.getInstance().getRegistryService().getConfigSystemRegistry(
                    CarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (RegistryException e) {
            log.error("Can not proceed decyption due to the secret repository intialization error");
            return null;
        }

        String propertyValue = "";

        if (registry != null) {
            try {
                if (registry.resourceExists(SecureVaultConstants.ENCRYPTED_PROPERTY_STORAGE_PATH)) {
                    Resource registryResource =
                            registry.get(SecureVaultConstants.ENCRYPTED_PROPERTY_STORAGE_PATH);
                    propertyValue = registryResource.getProperty(alias);
                }

            } catch (RegistryException e) {
                log.error("Can not proceed decyption due to the secret repository intialization error");
                return null;
            }
        }
        DecryptionProvider decyptProvider = CipherInitializer.getInstance().getDecryptionProvider();

        if (decyptProvider == null) {
            log.error("Can not proceed decyption due to the secret repository intialization error");
            return null;
        }

        String decryptedText = new String(decyptProvider.decrypt(propertyValue.trim().getBytes()));

        if (log.isDebugEnabled()) {
            log.info("evaluation completed succesfully " + decryptedText);
        }
        return decryptedText;

    }

    /**
     * @param alias Alias name for look up a encrypted Value
     * @return encrypted Value if there is any , otherwise ,alias itself
     * @see org.wso2.securevault.secret.SecretRepository
     */
    public String getEncryptedData(String alias) {
        return null;
    }

    public void doEncrypt(String plainTextValue, String alias) {
        try {
            createRegistryResource();
            UserRegistry registry = SecurityServiceHolder.getInstance().getRegistryService().getConfigSystemRegistry();
            Resource registryResource = registry.get(SecureVaultConstants.ENCRYPTED_PROPERTY_STORAGE_PATH);
            String encryptedValue = SecureVaultUtil.encryptValue(plainTextValue);
            registryResource.addProperty(alias, encryptedValue);
            registry.put(SecureVaultConstants.ENCRYPTED_PROPERTY_STORAGE_PATH, registryResource);
        } catch (RegistryException | AxisFault e) {
        }
    }

    public void setParent(SecretRepository parent) {
        this.parentRepository = parent;
    }

    public SecretRepository getParent() {
        return this.parentRepository;
    }

    @Override
    public void init(Properties arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    private void createRegistryResource() throws RegistryException {
        try {
            UserRegistry registry = SecurityServiceHolder.getInstance().getRegistryService().getConfigSystemRegistry();

            // creating vault-specific storage repository (this happens only if
            // not resource not existing)
            if (!registry.resourceExists(SecureVaultConstants.ENCRYPTED_PROPERTY_STORAGE_PATH)) {
                org.wso2.carbon.registry.core.Collection secureVaultCollection = registry.newCollection();
                registry.put(SecureVaultConstants.ENCRYPTED_PROPERTY_STORAGE_PATH, secureVaultCollection);
            }
        } catch (RegistryException e) {
            throw new RegistryException("Error while intializing the registry");
        }
    }

}
