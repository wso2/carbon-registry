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

package org.wso2.carbon.registry.relations.services.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class CommonUtil {

    private static final Log log = LogFactory.getLog(CommonUtil.class);

    private static RegistryService registryService;

    public static synchronized void setRegistryService(RegistryService service) {
        registryService = service;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static Association[] getAssociations(Registry registry, String path)
            throws RegistryException {
        Association[] asso = registry.getAllAssociations(path);
        if (asso == null || asso.length == 0) {
            return asso;
        }
        List<Association> assoList = new LinkedList<Association>();
        for (Association a : asso) {
            if (a.getDestinationPath() != null &&
                    (registry.resourceExists(a.getDestinationPath()) ||
                            a.getDestinationPath().matches("^[a-zA-Z]+://.*"))) {
                assoList.add(a);
            }
        }
        if (assoList.size() == 0) {
            return new Association[0];
        }
        Arrays.sort(assoList.toArray(new Association[assoList.size()]),
                new Comparator<Association>() {
                    public int compare(Association o1, Association o2) {
                        int result = RegistryUtils.getResourceName(o1.getDestinationPath()).
                                compareToIgnoreCase(
                                        RegistryUtils.getResourceName(o2.getDestinationPath()));
                        if (result != 0) {
                            return result;
                        }
                        return o1.getDestinationPath().compareTo(o2.getDestinationPath());
                    }
        });
        return assoList.toArray(new Association[assoList.size()]);
    }
}
