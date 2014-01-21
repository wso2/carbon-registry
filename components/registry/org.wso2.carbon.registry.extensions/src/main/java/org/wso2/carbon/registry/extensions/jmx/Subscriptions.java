/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.jmx;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.admin.api.jmx.ISubscriptionsService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class Subscriptions implements SubscriptionsMBean {

    private ISubscriptionsService implBean;

    public void setImplBean(ISubscriptionsService implBean) {
        this.implBean = implBean;
    }

    public String subscribe(String endpoint, boolean isRestEndpoint, String path,
                            String eventName) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            if (implBean == null) {
                return "";
            }
            return implBean.subscribe(endpoint, isRestEndpoint, path, eventName);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void unsubscribe(String id) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            if (implBean != null) {
                implBean.unsubscribe(id);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public String[] getEventNames() {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            if (implBean == null) {
                return new String[0];
            }
            return implBean.getEventNames();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public String[] getList() {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            if (implBean == null) {
                return new String[0];
            }
            return implBean.getList();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
