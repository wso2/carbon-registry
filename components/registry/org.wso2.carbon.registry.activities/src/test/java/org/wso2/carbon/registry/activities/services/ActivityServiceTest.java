/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.registry.activities.services;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.common.beans.ActivityBean;
import org.wso2.carbon.registry.core.LogEntry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ActivityServiceTest {

    private UserRegistry userRegistry;

    @Before
    public void setup() throws Exception {

        userRegistry = mock(UserRegistry.class);
        RegistryRealm registryRealm = mock(RegistryRealm.class);
        UserStoreManager userStoreManager = mock(UserStoreManager.class);

        when(userRegistry.getUserName()).thenReturn("admin");

        when(userStoreManager.getRoleListOfUser("admin"))
                .thenReturn(new String[]{"admin", "internal/everyone", "internal/publisher"});

        when(registryRealm.getUserStoreManager()).thenReturn(userStoreManager);

        RealmConfiguration realmConfiguration = new RealmConfiguration();
        realmConfiguration.setAdminRoleName("admin");
        when(registryRealm.getRealmConfiguration()).thenReturn(realmConfiguration);

        when(userRegistry.getUserRealm()).thenReturn(registryRealm);
        when(userRegistry.resourceExists(anyString())).thenReturn(true);

        LogEntry[] resultEntries = TestUtils.readLogEntries("add-log-entry.csv");
        when(userRegistry.getLogs(
                anyString(),
                eq(0),
                anyString(),
                any(Date.class),
                any(Date.class),
                anyBoolean()
        )).thenReturn(resultEntries);

        // ---- MessageContext setup ----
        MessageContext messageContext = new MessageContext();

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        when(session.getAttribute(RegistryConstants.ROOT_REGISTRY_INSTANCE))
                .thenReturn(userRegistry);
        when(servletRequest.getSession()).thenReturn(session);

        messageContext.setProperty(
                HTTPConstants.MC_HTTP_SERVLETREQUEST,
                servletRequest
        );

        AxisService axisService = new AxisService();
        axisService.addParameter(
                new Parameter(CarbonConstants.ADMIN_SERVICE_PARAM_NAME, "true")
        );
        messageContext.setAxisService(axisService);

        MessageContext.setCurrentMessageContext(messageContext);
    }

    @Test
    public void getActivities() throws Exception {

        ActivityService activityService = new ActivityService();

        ActivityBean activityBean = activityService.getActivities(
                "admin",
                "/_system/governance/apimgt/application",
                "10/10/2016",
                "10/10/2017",
                "resourceAdd",
                null,
                null
        );

        assertNotNull(activityBean);
        assertNotNull(activityBean.getActivity());
        assertNull(activityBean.getErrorMessage());
        assertEquals(2, activityBean.getActivity().length);
    }

    @Test
    public void getActivitiesFailure() throws Exception {

        ActivityService activityService = new ActivityService();

        ActivityBean activityBean = activityService.getActivities(
                "admin",
                "/_system/governance/apimgt/application",
                "10/10/2016",
                "10/10/2017",
                "all",
                null,
                null
        );

        assertNull(activityBean);
    }
}
