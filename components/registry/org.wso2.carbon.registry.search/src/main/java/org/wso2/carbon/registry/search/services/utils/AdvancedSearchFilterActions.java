/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.search.services.utils;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.search.beans.CustomSearchParameterBean;

import java.util.*;

public class AdvancedSearchFilterActions {


    public static CustomSearchParameterBean getAdvancedSearchQueryBean(UserRegistry userRegistry, String filterName)
            throws RegistryException {
        CustomSearchParameterBean bean = new CustomSearchParameterBean();

        Resource r = userRegistry.get("users/" + userRegistry.getUserName() + "/searchFilters/" + filterName);
        Properties props = r.getProperties();
        Map<String, String> propertyMap = new HashMap<String, String>();

        for (Map.Entry e : props.entrySet()) {
            if (!(e.getKey() instanceof String) || !(e.getValue() instanceof List)) {
                continue;
            }
            List valueList = ((List)e.getValue());
            if (valueList.size() == 0) {
                continue;
            }
            String key = (String) e.getKey();
            if (RegistryUtils.isHiddenProperty(key)) {
                continue;
            }
            propertyMap.put(key, (String)valueList.get(0));
        }
        List<String[]> propArray = new LinkedList<String[]>();
        for (Map.Entry<String, String> e : propertyMap.entrySet()) {
            String[] entry = new String[2];
            entry[0] = e.getKey();
            entry[1] = e.getValue();
            propArray.add(entry);
        }
        bean.setParameterValues(propArray.toArray(new String[propArray.size()][2]));
        return bean;
    }

    public static void saveAdvancedSearchQueryBean(UserRegistry userRegistry, CustomSearchParameterBean bean, String filterName)
            throws RegistryException {

        Resource r = userRegistry.newResource();
        if (bean.getParameterValues() != null) {
            for (String[] prop : bean.getParameterValues()) {
                r.setProperty(prop[0], prop[1]);
            }
        }
        userRegistry.put("users/" + userRegistry.getUserName() + "/searchFilters/" + filterName, r);


    }

    public static String[] getSavedFilterNames(UserRegistry configUserRegistry) throws RegistryException {
        String filterPath = "users/" + configUserRegistry.getUserName() + "/searchFilters";

        if (!configUserRegistry.resourceExists(filterPath)) {
            return null;
        }        
        Collection c = (Collection) configUserRegistry.get(filterPath);
        String[] children = c.getChildren();
        for (int i = 0; i < children.length; ++i) {
            children[i] = children[i].substring(children[i].lastIndexOf('/') + 1);
        }
        return children;

    }

}
