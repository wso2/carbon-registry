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

package org.wso2.carbon.registry.properties.services;

import org.wso2.carbon.registry.admin.api.properties.IPropertiesAdminService;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.properties.beans.PropertiesBean;
import org.wso2.carbon.registry.properties.beans.RetentionBean;
import org.wso2.carbon.registry.properties.utils.PropertiesBeanPopulator;

import java.util.Properties;

/**
 * The admin service that will be used in managing properties.
 */
public class PropertiesAdminService extends RegistryAbstractAdmin implements
        IPropertiesAdminService<PropertiesBean,RetentionBean> {
    
    /**
     * Method to return all the properties of a given resource.
     * 
     * @param path path of the resource.
     * @param viewProps currently supported values "true", "false". if "true" show system properties.
     *
     * @return The properties bean.
     *
     * @throws RegistryException throws if there is an error.
     */
    public PropertiesBean getProperties(String path, String viewProps) throws RegistryException {
        UserRegistry registry = (UserRegistry)getRootRegistry();
        return PropertiesBeanPopulator.populate(registry, path, viewProps);
    }

    /**
     * Method to add a property, if there already exist a property with the same name, this
     * will add the value to the existing property name. (So please remove the old property with
     * the same name before calling this method).
     *
     * @param path path of the resource.
     * @param name property name.
     * @param value property value.
     *
     * @throws RegistryException throws if there is an error.
     */
    public void setProperty(String path, String name, String value) throws RegistryException {

        if(name != null && name.startsWith("registry.")) {
            throw new RegistryException("Property cannot start with the \"registry.\" prefix. " +
                    "Property name " + name + ". Resource path = " + path);
        }
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return;
        }
        Resource resource = registry.get(path);

        if(resource.getProperties().keySet().contains(name)) {
            throw new RegistryException("Cannot duplicate property name. Please choose a different name. " +
                    "Property name " + name + ". Resource path = " + path);
        }

        resource.addProperty(name, value);
        registry.put(resource.getPath(), resource);
        resource.discard();
    }

    /**
     * Method to update a property (This removes the old property with the oldName)
     *
     * @param path path of the resource.
     * @param name property name.
     * @param value property value.
     * @param oldName old name of the property.
     *
     * @throws RegistryException throws if there is an error.
     */
    public void updateProperty(String path, String name, String value, String oldName) throws
            RegistryException {

        if(name != null && name.startsWith("registry.")) {
            throw new RegistryException("Property cannot start with the \"registry.\" prefix. " +
                    "Property name " + name + ". Resource path = " + path);
        }

        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return;
        }
        Resource resource = registry.get(path);

        if(resource.getProperties().keySet().contains(name) && !name.equals(oldName)) {
            throw new RegistryException("Cannot duplicate property name. Please choose a different name. " +
                    "Property name " + name + ". Resource path = " + path);
        }

        if (oldName.equals(name)) {
            resource.setProperty(name, value);
        } else {
            resource.setProperty(name, value);
            resource.removeProperty(oldName);
        }
        registry.put(resource.getPath(), resource);
        resource.discard();
    }

    /**
     * Method to remove property.
     *
     * @param path path of the resource.
     * @param name property name.
     *
     * @throws RegistryException throws if there is an error.
     */
    public void removeProperty(String path, String name) throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return;
        }
        Resource resource = registry.get(path);
        resource.removeProperty(name);
        registry.put(resource.getPath(), resource);
        resource.discard();
    }

    /**
     * Method to set resource retention properties of a resource
     * @param path Path of the resource
     * @param bean RetentionBean which encapsulates retention properties
     * @return true if operation succeeds false otherwise
     * @throws RegistryException
     */
    public boolean setRetentionProperties(String path, RetentionBean bean) throws RegistryException {

        // Fixing REGISTRY-789  - disallowing setting retention properties for versioned resources
        if(path.matches(".*;version:\\d$")) {
            throw new RegistryException("User is not authorized to change retention properties" +
                    " of resource versions. Resource path = " + path);
        }

        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }
        Resource resource = registry.get(path);
        if (resource.getProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME) != null &&
                !resource.getProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME).equals(
                        registry.getUserName())) {
            throw new RegistryException("User is not authorized to change retention properties" +
                    " of this resource. Resource path = " + path);
        }

        if (bean == null) {
            resource.removeProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME);
            resource.removeProperty(CommonConstants.RETENTION_FROM_DATE_PROP_NAME);
            resource.removeProperty(CommonConstants.RETENTION_TO_DATE_PROP_NAME);
            resource.removeProperty(CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME);
            resource.removeProperty(CommonConstants.RETENTION_DELETE_LOCKED_PROP_NAME);
        } else {
            resource.setProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME, registry.getUserName());
            resource.setProperty(CommonConstants.RETENTION_FROM_DATE_PROP_NAME, bean.getFromDate());
            resource.setProperty(CommonConstants.RETENTION_TO_DATE_PROP_NAME, bean.getToDate());
            resource.setProperty(CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME,
                    String.valueOf(bean.getWriteLocked()));
            resource.setProperty(CommonConstants.RETENTION_DELETE_LOCKED_PROP_NAME,
                    String.valueOf(bean.getDeleteLocked()));
        }
        registry.put(resource.getPath(), resource);
        return true;
    }

    /**
     * Method to get resource retention properties of a given resource
     * @param path path of the resource
     * @return RetentionBean which encapsulates retention properties
     * @throws RegistryException
     */
    public RetentionBean getRetentionProperties(String path) throws RegistryException {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        Resource resource = registry.get(path);

        RetentionBean bean = new RetentionBean();
        ResourcePath resourcePath = new ResourcePath(path);
        String userName = resource.getProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME);
        if (userName == null) {
            /* Consistency is assumed. If this property is not set, that means no retention for that
            resource
             */
            if(!resourcePath.isCurrentVersion()){
                String originalPath = resourcePath.getPath();
                resource = registry.get(originalPath);

                userName = resource.getProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME);
                bean.setReadOnly(true);
            }else{
                return null;
            }
        } else {
            bean.setReadOnly(!userName.equals(registry.getUserName()));
        }
        bean.setUserName(userName);
        bean.setFromDate(resource.getProperty(CommonConstants.RETENTION_FROM_DATE_PROP_NAME));
        bean.setToDate(resource.getProperty(CommonConstants.RETENTION_TO_DATE_PROP_NAME));
        bean.setWriteLocked(Boolean.parseBoolean(resource.getProperty(
                CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME)));
        bean.setDeleteLocked(Boolean.parseBoolean(resource.getProperty(
                CommonConstants.RETENTION_DELETE_LOCKED_PROP_NAME)));
        return bean;
    }
}
