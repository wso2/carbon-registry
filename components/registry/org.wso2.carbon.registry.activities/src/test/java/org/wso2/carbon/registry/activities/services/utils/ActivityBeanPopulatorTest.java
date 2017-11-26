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
package org.wso2.carbon.registry.activities.services.utils;

import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.wso2.carbon.registry.activities.services.TestUtils;
import org.wso2.carbon.registry.common.beans.ActivityBean;
import org.wso2.carbon.registry.core.LogEntry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import java.io.IOException;
import java.util.Date;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActivityBeanPopulatorTest {

    private UserRegistry userRegistry;

    @Before
    public void setup() throws UserStoreException, IOException, RegistryException {
        userRegistry = mock(UserRegistry.class);
        when(userRegistry.getUserName()).thenReturn("admin");
        RegistryRealm registryRealm = PowerMockito.mock(RegistryRealm.class);
        UserStoreManager userStoreManager = PowerMockito.mock(UserStoreManager.class);
        when(userStoreManager.getRoleListOfUser("admin"))
                .thenReturn(new String[]{"admin", "internal/everyone", "internal/publisher"});
        when(userStoreManager.getRoleListOfUser("danesh"))
                .thenReturn(new String[]{"internal/everyone", "internal/publisher"});
        when(registryRealm.getUserStoreManager()).thenReturn(userStoreManager);
        RealmConfiguration realmConfiguration = new RealmConfiguration();
        realmConfiguration.setAdminRoleName("admin");
        when(registryRealm.getRealmConfiguration()).thenReturn(realmConfiguration);
        when(userRegistry.getUserRealm()).thenReturn(registryRealm);
    }

    @Test
    public void testPopulateWithoutFilters() throws Exception {
        doReturn(true).when(userRegistry).resourceExists(anyString());
        LogEntry[] resultEntries = TestUtils.readLogEntries("all-log-entry.csv");
        doReturn(resultEntries).when(userRegistry).getLogs(anyString(), eq(-1),
                anyString(), (Date)anyObject(), (Date)anyObject(), anyBoolean());
        String resourcePath = "";
        String username = "admin";
        String fromDate = "10/10/2016";
        String toDate = null;
        String filter = null;
        ActivityBean activityBean = ActivityBeanPopulator.populate(userRegistry, username, resourcePath, fromDate, toDate, filter, null);
        String[] activities = activityBean.getActivity();
        String errorMessage = activityBean.getErrorMessage();
        assertNotNull(activities);
        assertNull(errorMessage);
        assertEquals(38, activities.length);

    }

    @Test
    public void testPopulateWithNormalUser() throws Exception {
        when(userRegistry.getUserName()).thenReturn("danesh");
        doReturn(true).when(userRegistry).resourceExists(anyString());
        LogEntry[] resultEntries = TestUtils.readLogEntries("all-log-entry.csv");
        doReturn(resultEntries).when(userRegistry).getLogs(anyString(), eq(-1),
                anyString(), (Date)anyObject(), (Date)anyObject(), anyBoolean());
        String resourcePath = "";
        String username = "admin";
        String fromDate = "10/10/2016";
        String toDate = null;
        String filter = null;
        ActivityBean activityBean = ActivityBeanPopulator.populate(userRegistry, username, resourcePath, fromDate, toDate, filter, null);
        String[] activities = activityBean.getActivity();
        String errorMessage = activityBean.getErrorMessage();
        assertNotNull(activities);
        assertNull(errorMessage);
        assertEquals(19, activities.length);
    }

    @Test
    public void testPopulateWithAddFilter() throws Exception {
        when(userRegistry.getUserName()).thenReturn("admin");
        doReturn(true).when(userRegistry).resourceExists(anyString());
        LogEntry[] resultEntries = TestUtils.readLogEntries("add-log-entry.csv");
        doReturn(resultEntries).when(userRegistry).getLogs(anyString(), eq(0),
                anyString(), (Date)anyObject(), (Date)anyObject(), anyBoolean());
        String resourcePath = "";
        String username = "";
        String fromDate = "";
        String toDate = "";
        String filter = "resourceAdd";
        ActivityBean activityBean = ActivityBeanPopulator.populate(userRegistry, username, resourcePath, fromDate, toDate, filter, null);
        String[] activities = activityBean.getActivity();
        String errorMessage = activityBean.getErrorMessage();
        assertNotNull(activities);
        assertNull(errorMessage);
        assertEquals(2, activities.length);
    }

    @Test
    public void testPopulateWithInvalidDate() throws Exception {
        when(userRegistry.getUserName()).thenReturn("admin");
        doReturn(true).when(userRegistry).resourceExists(anyString());
        LogEntry[] resultEntries = TestUtils.readLogEntries("add-log-entry.csv");
        doReturn(resultEntries).when(userRegistry).getLogs(anyString(), eq(0),
                anyString(), (Date)anyObject(), (Date)anyObject(), anyBoolean());
        String resourcePath = "";
        String username = "";
        String fromDate = "10/152016";
        String toDate = "";
        String filter = "resourceAdd";
        ActivityBean activityBean = ActivityBeanPopulator.populate(userRegistry, username, resourcePath, fromDate, toDate, filter, null);
        String[] activities = activityBean.getActivity();
        String errorMessage = activityBean.getErrorMessage();
        assertNotNull(errorMessage);
        assertEquals("Failed to get activities for generating activity search results Date format is invalid: " +
                "10/152016", errorMessage);
        assertNull(activities);
    }

    @Test
    public void testPopulateWithRegistryError() throws Exception {
        when(userRegistry.getUserName()).thenReturn("admin");
        doThrow(new RegistryException("Error while checking the resource")).when(userRegistry).resourceExists(anyString
                ());
        LogEntry[] resultEntries = TestUtils.readLogEntries("add-log-entry.csv");
        doReturn(resultEntries).when(userRegistry).getLogs(anyString(), eq(0),
                anyString(), (Date)anyObject(), (Date)anyObject(), anyBoolean());
        String resourcePath = "";
        String username = "";
        String fromDate = "";
        String toDate = "";
        String filter = "resourceAdd";
        ActivityBean activityBean = ActivityBeanPopulator.populate(userRegistry, username, resourcePath, fromDate, toDate, filter, null);
        String[] activities = activityBean.getActivity();
        String errorMessage = activityBean.getErrorMessage();
        assertNull(errorMessage);
        assertNotNull(activities);
        assertEquals(2, activities.length);
        assertTrue(activities[0].startsWith("false"));
    }

}
