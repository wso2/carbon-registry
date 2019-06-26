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
package org.wso2.carbon.registry.security.vault.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.security.vault.observers.TenantDeploymentListenerImpl;
import org.wso2.carbon.registry.security.vault.service.RegistrySecurityService;
import org.wso2.carbon.registry.security.vault.util.SecureVaultUtil;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import java.io.UnsupportedEncodingException;
import java.util.Stack;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "registry.security", 
         immediate = true)
public class RegistrySecurityServiceComponent {

    private static Log log = LogFactory.getLog(RegistrySecurityServiceComponent.class);

    private static Stack<ServiceRegistration> registrations = new Stack<ServiceRegistration>();

    public RegistrySecurityServiceComponent() {
    }

    @Activate
    protected void activate(ComponentContext ctxt) {
        registrations.push(ctxt.getBundleContext().registerService(RegistrySecurityService.class.getName(), new RegistrySecurityServiceImpl(), null));
        TenantDeploymentListenerImpl listener = new TenantDeploymentListenerImpl();
        registrations.push(ctxt.getBundleContext().registerService(Axis2ConfigurationContextObserver.class.getName(), listener, null));
        try {
            SecureVaultUtil.createRegistryResource(-1234);
        } catch (RegistryException ignore) {
        }
        if (log.isDebugEnabled()) {
            log.debug("Registry security component activated");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        while (!registrations.empty()) {
            registrations.pop().unregister();
        }
        log.debug("Registry security component deactivated");
    }

    @Reference(
             name = "registry.service", 
             service = RegistryService.class,
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService regService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService bound to the ESB initialization process");
        }
        SecurityServiceHolder.getInstance().setRegistryService(regService);
    }

    protected void unsetRegistryService(RegistryService regService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unbound from the ESB environment");
        }
        SecurityServiceHolder.getInstance().setRegistryService(null);
    }

    @Reference(
             name = "server.configuration", 
             service = ServerConfigurationService.class,
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetServerConfigurationService")
    protected void setServerConfigurationService(ServerConfigurationService serverConfiguration) {
        SecurityServiceHolder.getInstance().setServerConfigurationService(serverConfiguration);
    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfiguration) {
        SecurityServiceHolder.getInstance().setServerConfigurationService(null);
    }


    private static class RegistrySecurityServiceImpl implements RegistrySecurityService {

        /**
         * Method to do the encryption operation.
         *
         * @param plainTextValue	plain text value.
         * @return			encrypted value.
         * @throws CryptoException	Throws when an error occurs during encryption.
         */
        @Override
        public String doEncrypt(String plainTextValue) throws CryptoException {
            return SecureVaultUtil.doEncrypt(plainTextValue);
        }

        /**
         * Method to decrypt a property, when key of the property is provided.
         *
         * @param key			key of the property.
         * @return 			decrypted property value.
         * @throws RegistryException	Throws when an error occurs during decryption.
         */
        @Override
        public String getDecryptedPropertyValue(String key) throws RegistryException {
            return SecureVaultUtil.getDecryptedPropertyValue(key);
        }

        /**
         * Method to decrypt a property, when encrypted value is provided.
         *
         * @param encryptedValue                encrypted value.
         * @return 				decrypted  value.
         * @throws CryptoException              Throws when an error occurs during decryption.
         * @throws UnsupportedEncodingException Throws when an error occurs during byte array to string conversion.
         */
        @Override
        public String doDecrypt(String encryptedValue) throws CryptoException, UnsupportedEncodingException {
            return SecureVaultUtil.doDecrypt(encryptedValue);
        }
    }
}

