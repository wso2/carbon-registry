/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.handler.listener;


import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.services.callback.LoginListener;
import org.wso2.carbon.core.services.callback.LoginEvent;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.handler.util.CommonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedList;
import java.util.List;

public class HandlerLoader implements LoginListener {

    private static Log log = LogFactory.getLog(HandlerLoader.class);

    private List<Integer> initializedTenants = new LinkedList<Integer>();

    public void onLogin(Registry configSystemRegistry, LoginEvent loginEvent) {
        try {
            if (initializedTenants.contains(loginEvent.getTenantId())) {
                return;
            }
            initializedTenants.add(loginEvent.getTenantId());
            PrivilegedCarbonContext.startTenantFlow();
            try {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(loginEvent.getTenantDomain());
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(loginEvent.getTenantId());
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(loginEvent.getUsername());
                CommonUtil.addDefaultHandlersIfNotAvailable(configSystemRegistry);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        } catch (Exception e) {
            String msg = "Error in adding the default handlers";
            log.error(msg, e);
        }
    }
}
