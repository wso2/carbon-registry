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

package org.wso2.carbon.registry.properties.utils;

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.pagination.PaginationUtils;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.Aspect;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.common.utils.UserUtil;
import org.wso2.carbon.registry.properties.beans.PropertiesBean;

import java.util.*;

/**
 * Class that hold the logic in populating the property beans.
 */
public class PropertiesBeanPopulator {

    /**
     * Populate the properties.
     *
     * @param registry the registry instance.
     * @param path the path of the resource.
     * @param viewProps whether the system properties are shown or not.
     *
     * @return the properties bean.
     *
     * @throws RegistryException if there is an failure in populating properties.
     */
    public static PropertiesBean populate(UserRegistry registry, String path, String viewProps)
            throws RegistryException {
        Resource resource = registry.get(path);
        ResourcePath resourcePath = new ResourcePath(path);
        PropertiesBean propertiesBean = new PropertiesBean();
        boolean isPutAllowed = false;
        if (CarbonContext.getThreadLocalCarbonContext().getUsername() != null &&
                !CarbonContext.getThreadLocalCarbonContext().getUsername().equals(resource.getProperty(CommonConstants.
                        RETENTION_USERNAME_PROP_NAME)) &&
                Boolean.parseBoolean(resource.getProperty(CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME))) {

            propertiesBean.setWriteLocked(resource.getProperty(CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME));
            propertiesBean.setDeleteLocked(resource.getProperty(CommonConstants.RETENTION_DELETE_LOCKED_PROP_NAME));

        } else {
            propertiesBean.setWriteLocked("false");
            propertiesBean.setDeleteLocked("false");
        }

        Properties properties = resource.getProperties();
        Set keySet = properties.keySet();
        Property [] propArray;
        if(keySet.size() != 0) {
        Object [] keys = keySet.toArray();

        List values;
        propArray = new Property [keys.length];
        for (int i=0; i<keys.length; i++) {
            Property prop = new Property();
            prop.setKey((String) keys[i]);
            values = (List) properties.get(keys[i]);
            prop.setValue((String) values.get(0));
            propArray[i] = prop;

            if (keys[i].equals("registry.link") &&
                    values.get(0).equals("true")) {
                 isPutAllowed = true;
            }
        }
        } else {
            propArray = new Property [0];
        }
        propertiesBean.setProperties(propArray);
        Boolean viewSysProps = false;
        if(viewProps.equalsIgnoreCase("yes")) {
            viewSysProps = true;
        }
        List<String> sysProperties = new ArrayList<String>();
        List <String> validationProperties = new ArrayList <String> ();
        List <String> lifecycleProperties = new ArrayList <String> ();

        for(Object key : keySet){
            String name = (String) key;
            if ((viewSysProps) || !name.startsWith("registry.")) {
                sysProperties.add(name);
            }
            if (name.startsWith("registry.wsdl") || name.startsWith("registry.wsi")) {
                validationProperties.add(name);
            } else if (name.startsWith("registry.lifecycle.") ||
                    name.equals(Aspect.AVAILABLE_ASPECTS)) {
                lifecycleProperties.add(name);
            }
        }
        Collections.sort(sysProperties);
        propertiesBean.setSysProperties(sysProperties.toArray(new String[sysProperties.size()]));
        propertiesBean.setValidationProperties(validationProperties.toArray(new String[validationProperties.size()]));
        propertiesBean.setLifecycleProperties(lifecycleProperties.toArray(new String[lifecycleProperties.size()]));

        propertiesBean.setVersionView(!resourcePath.isCurrentVersion());
        propertiesBean.setPathWithVersion(resourcePath.getPathWithVersion());
        if (!isPutAllowed) {
            isPutAllowed = UserUtil.isPutAllowed(registry.getUserName(), path, registry);
        }
        propertiesBean.setPutAllowed(isPutAllowed);
        propertiesBean.setLoggedIn(!RegistryConstants.ANONYMOUS_USER.equals(registry.getUserName()));
        return propertiesBean;
    }
}
