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

package org.wso2.carbon.registry.activities.services;

import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.activities.services.utils.ActivityBeanPopulator;
import org.wso2.carbon.registry.activities.services.utils.CommonUtil;
import org.wso2.carbon.registry.common.IActivityService;
import org.wso2.carbon.registry.common.beans.ActivityBean;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActivityService extends RegistryAbstractAdmin implements IActivityService {

    public void setSession(String sessionId, HttpSession session) {
    }

    public void removeSession(String sessionId) {
    }

    public ActivityBean getActivities(String userName, String resourcePath, String fromDate,
                                      String toDate, String filter, String pageStr, String sessionId)
            throws RegistryException {
        UserRegistry registry = (UserRegistry)getRootRegistry();
        try{
            return ActivityBeanPopulator.populate(registry, userName, resourcePath,
                    fromDate, toDate, filter, pageStr);
        } catch(Exception e) {
            return null;
        }
    }
}
