package org.wso2.carbon.registry.activities.services;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.common.beans.ActivityBean;
import org.wso2.carbon.registry.core.LogEntry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.Date;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ActivityServiceTest {

    @Before
    public void setup() throws Exception {
        UserRegistry userRegistry = mock(UserRegistry.class);
        when(userRegistry.getUserName()).thenReturn("admin");
        RegistryRealm registryRealm = PowerMockito.mock(RegistryRealm.class);
        UserStoreManager userStoreManager = PowerMockito.mock(UserStoreManager.class);
        when(userStoreManager.getRoleListOfUser("admin"))
                .thenReturn(new String[]{"admin", "internal/everyone", "internal/publisher"});
        when(registryRealm.getUserStoreManager()).thenReturn(userStoreManager);
        RealmConfiguration realmConfiguration = new RealmConfiguration();
        realmConfiguration.setAdminRoleName("admin");
        when(registryRealm.getRealmConfiguration()).thenReturn(realmConfiguration);
        when(userRegistry.getUserRealm()).thenReturn(registryRealm);
        doReturn(true).when(userRegistry).resourceExists(anyString());
        LogEntry[] resultEntries = TestUtils.readLogEntries("add-log-entry.csv");
        doReturn(resultEntries).when(userRegistry).getLogs(anyString(), eq(0),
                anyString(), (Date)anyObject(), (Date)anyObject(), anyBoolean());

        MessageContext messageContext = new MessageContext();
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(RegistryConstants.ROOT_REGISTRY_INSTANCE)).thenReturn(userRegistry);
        when(servletRequest.getSession()).thenReturn(session);
        messageContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, servletRequest);
        AxisService axisService = new AxisService();
        axisService.addParameter(new Parameter(CarbonConstants.ADMIN_SERVICE_PARAM_NAME, "true"));
        messageContext.setAxisService(axisService);
        MessageContext.setCurrentMessageContext(messageContext);
    }

    @Test
    public void getActivities() throws Exception {
        ActivityService activityService = new ActivityService();
        String username = "admin";
        String resourcePath = "/_system/governance/apimgt/application";
        String fromDate = "10/10/2016";
        String toDate = "10/10/2017";
        String filter = "resourceAdd";
        String pgStr = null;
        String sessionId = null;
        ActivityBean activityBean = activityService.getActivities(username, resourcePath, fromDate, toDate, filter,
                pgStr, null);
        String[] activities = activityBean.getActivity();
        String errorMessage = activityBean.getErrorMessage();
        assertNotNull(activities);
        assertNull(errorMessage);
        assertEquals(2, activities.length);
    }

    @Test
    public void getActivitiesFailure() throws Exception {
        ActivityService activityService = new ActivityService();
        String username = "admin";
        String resourcePath = "/_system/governance/apimgt/application";
        String fromDate = "10/10/2016";
        String toDate = "10/10/2017";
        String filter = "all";
        String pgStr = null;
        String sessionId = null;
        ActivityBean activityBean = activityService.getActivities(username, resourcePath, fromDate, toDate, filter,
                pgStr, null);
        assertNull(activityBean);
    }

}
