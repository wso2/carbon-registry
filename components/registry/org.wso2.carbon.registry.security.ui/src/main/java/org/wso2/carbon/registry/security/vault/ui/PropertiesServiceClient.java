/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.security.vault.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.common.ui.UIConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceStub;
import org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean;
import org.wso2.carbon.registry.properties.stub.beans.xsd.RetentionBean;
import org.wso2.carbon.registry.properties.stub.utils.xsd.Property;
import org.wso2.carbon.registry.security.stub.RegistrySecurityAdminServiceCryptoExceptionException;
import org.wso2.carbon.registry.security.stub.RegistrySecurityAdminServiceStub;
import org.wso2.carbon.registry.security.vault.cipher.tool.CipherTool;
import org.wso2.carbon.registry.security.vault.util.SecureVaultConstants;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import java.rmi.RemoteException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class PropertiesServiceClient {

    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String PATH = "path";
    private static final Log log = LogFactory.getLog(PropertiesServiceClient.class);

    private PropertiesAdminServiceStub propertAdminServicestub;
    private RegistrySecurityAdminServiceStub securityAdminServiceStub;
    private HttpSession session;
    private CipherTool cipherTool;

	public PropertiesServiceClient(ServletConfig config, HttpSession session)
	                                                                         throws RegistryException {
		this.session = session;
		String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
		String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), this.session);
		ConfigurationContext configContext =
		                                     (ConfigurationContext) config.getServletContext()
		                                                                  .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
		

		
		String propertyEPR = backendServerURL + "PropertiesAdminService";
		String registrySecurityEPR = backendServerURL + "RegistrySecurityAdminService";

		try {
			propertAdminServicestub = new PropertiesAdminServiceStub(configContext, propertyEPR);

			ServiceClient client = propertAdminServicestub._getServiceClient();
			Options option = client.getOptions();
			option.setManageSession(true);
			option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
			
			//
			securityAdminServiceStub = new RegistrySecurityAdminServiceStub(configContext, registrySecurityEPR);

			ServiceClient securityclient = propertAdminServicestub._getServiceClient();
			Options securityoption = securityclient.getOptions();
			securityoption.setManageSession(true);
			securityoption.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

					
			cipherTool = new CipherTool(securityAdminServiceStub);
		} catch (AxisFault axisFault) {
			String msg = "Failed to initiate resource service client. " + axisFault.getMessage();
			log.error(msg, axisFault);
			throw new RegistryException(msg, axisFault);
		}
	}

	/**
	 * Retrieving the length of the properties in selected registry resource
	 * 
	 * @return
	 * @throws RegistryException
	 */
	public int getPropertiesLenght() throws RegistryException {
		String path = SecureVaultConstants.ENCRYPTED_PROPERTY_CONFIG_REGISTRY_PATH;
		PropertiesBean bean = null;
		try {
			bean = propertAdminServicestub.getProperties(path, "no");
		} catch (Exception axisFault) {
			String msg = "Failed to initiate resource service client. " + axisFault.getMessage();
			log.error(msg, axisFault);
			throw new RegistryException(msg, axisFault);
		}
		return bean.getSysProperties().length;
	}

	public PropertiesBean getProperties(HttpServletRequest request, int pageNumber)
	                                                                               throws Exception {

		String path = SecureVaultConstants.ENCRYPTED_PROPERTY_CONFIG_REGISTRY_PATH;
		Boolean view = (Boolean) request.getSession().getAttribute(UIConstants.SHOW_SYSPROPS_ATTR);
		String viewProps;
		if (view != null) {
			if (view.booleanValue()) {
				viewProps = "yes";
			} else {
				viewProps = "no";
			}
		} else {
			viewProps = "no";
		}
		PropertiesBean bean = null;

		bean = propertAdminServicestub.getProperties(path, viewProps);
		int itemPerPage = (int) (RegistryConstants.ITEMS_PER_PAGE * 1.5);
		int start = (int) ((pageNumber) * itemPerPage);

		if (start >= 0 && bean.getSysProperties() != null && bean.getSysProperties().length > 0) {
			int length =
			             start > 0 ? ((bean.getSysProperties().length - start) - 1)
			                      : bean.getSysProperties().length;
			if (length > itemPerPage) {
				length = itemPerPage;
			}
			String[] prams = new String[length > 0 ? length : 1];
			for (int i = 0; i <= itemPerPage - 1; i++) {
				if (i < prams.length) {
					prams[i] = bean.getSysProperties()[i + start];
				}

			}
			bean.setSysProperties(prams);
		}

		if (bean == null) {
			return null;
		}
		if (bean.getLifecycleProperties() == null) {
			bean.setLifecycleProperties(new String[0]);
		}
		if (bean.getSysProperties() == null) {
			bean.setSysProperties(new String[0]);
		}
		if (bean.getValidationProperties() == null) {
			bean.setValidationProperties(new String[0]);
		}
		if (bean.getProperties() == null) {
			bean.setProperties(new Property[0]);
		}

		return bean;
	}

    /**
     * Method to add a property, if there already exist a property with the same name, this
     * will add the value to the existing property name. (So please remove the old property with
     * the same name before calling this method).
     *
     * @param request   Http request with parameters.
     * @throws RegistryException throws if there is an error.
     */
    public void setProperty(HttpServletRequest request) throws RegistryException {
        String path = SecureVaultConstants.ENCRYPTED_PROPERTY_CONFIG_REGISTRY_PATH;
        String name = (String) Utils.getParameter(request, NAME);
        String value = (String) Utils.getParameter(request, VALUE);

        try {
            // do the encryption..
            String encrypted = cipherTool.doEncryption(value);
            propertAdminServicestub.setProperty(path, name, encrypted);
        } catch (RemoteException | PropertiesAdminServiceRegistryExceptionException e) {
            throw new RegistryException("Failed to add property" + name + "to resource at path " + path, e);
        } catch (RegistrySecurityAdminServiceCryptoExceptionException e) {
            throw new RegistryException("Failed to encrypt the property " + name, e);
        }
    }

    /**
     * Method to update a property (This removes the old property with the oldName)
     *
     * @param request   Http request with parameters.
     * @throws RegistryException throws if there is an error.
     */
    public void updateProperty(HttpServletRequest request) throws RegistryException {
        String path = SecureVaultConstants.ENCRYPTED_PROPERTY_CONFIG_REGISTRY_PATH;
        String name = (String) Utils.getParameter(request, NAME);
        String value = (String) Utils.getParameter(request, VALUE);
        String oldName = (String) Utils.getParameter(request, "oldName");

        try {
            // do the encryption..
            String encrypted = cipherTool.doEncryption(value);
            propertAdminServicestub.updateProperty(path, name, encrypted, oldName);
        } catch (RemoteException | PropertiesAdminServiceRegistryExceptionException e) {
            throw new RegistryException("Failed to update the property" + name + "at resource path " + path, e);
        } catch (RegistrySecurityAdminServiceCryptoExceptionException e) {
            throw new RegistryException("Failed to encrypt the property " + name, e);
        }
    }

    /**
     * Method to remove property from a resource.
     *
     * @param request   Http request with parameters.
     * @throws RegistryException throws if there is an error.
     */
    public void removeProperty(HttpServletRequest request) throws RegistryException {
        String path = (String) Utils.getParameter(request, PATH);
        String name = (String) Utils.getParameter(request, NAME);
        try {
            propertAdminServicestub.removeProperty(path, name);
        } catch (RemoteException | PropertiesAdminServiceRegistryExceptionException e) {
            throw new RegistryException("Failed to remove the property" + name + "at resource path " + path, e);
        }
    }

    /**
     * Method to set resource retention properties of a resource.
     *
     * @param request   Http request with parameters.
     * @throws RegistryException throws if there is an error
     */
    public boolean setRetentionProperties(HttpServletRequest request) throws RegistryException {
        String path = request.getParameter(PATH);
        try {
            RetentionBean bean;
            String fromDate = request.getParameter("fromDate");
            if (fromDate == null || "".equals(fromDate)) {
                bean = null;
            } else {
                bean = new RetentionBean();
                bean.setFromDate(fromDate);
                bean.setToDate(request.getParameter("toDate"));
                String lockedOperationsParam = request.getParameter("lockedOperations");
                bean.setWriteLocked(lockedOperationsParam.contains("write"));
                bean.setDeleteLocked(lockedOperationsParam.contains("delete"));
            }
            propertAdminServicestub.setRetentionProperties(path, bean);
        } catch (RemoteException | PropertiesAdminServiceRegistryExceptionException e) {
            throw new RegistryException("Failed to add retention to resource at path" + path, e);

        }
        return true;
    }

    /**
     * Method to get resource retention properties of a given resource.
     *
     * @param request   Http request with parameters.
     * @throws RegistryException
     */
    public RetentionBean getRetentionProperties(HttpServletRequest request) throws RegistryException {
        String path = request.getParameter(PATH);
        try {
            return propertAdminServicestub.getRetentionProperties(request.getParameter(PATH));
        } catch (Exception e) {
            throw new RegistryException("Could not retrieve retention details at path" + path, e);
        }
    }

}
