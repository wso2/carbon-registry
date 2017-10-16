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
package org.wso2.carbon.registry.properties.services;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.properties.beans.PropertiesBean;
import org.wso2.carbon.registry.properties.beans.RetentionBean;
import org.wso2.carbon.registry.properties.utils.Property;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PropertiesAdminServiceTest {

    private String resourcePath = "/_system/governance/repository/trunk/service/java/1.0.0/HelloService";
    private String versionResourcePath =  "/_system/governance/repository/trunk/service/java/1.0.0/HelloService;" +
            "version:1";
    private Resource resource;
    private Resource versionResource;

    @Before
    public void setup() throws AxisFault, RegistryException, UserStoreException {
        UserRegistry userRegistry = mock(UserRegistry.class);
        resource = new ResourceImpl();
        versionResource = new ResourceImpl();
        when(userRegistry.get(resourcePath)).thenReturn(resource);
        when(userRegistry.get(versionResourcePath)).thenReturn(versionResource);
        when(userRegistry.getUserName()).thenReturn("admin");
        when(userRegistry.getRegistryContext()).thenReturn(null);
        System.setProperty("carbon.home", "");

        AuthorizationManager authManager = mock(AuthorizationManager.class);
        when(authManager.isUserAuthorized("admin", resourcePath, ActionConstants
                .GET)).thenReturn(Boolean.TRUE);
        RegistryRealm registryRealm = mock(RegistryRealm.class);
        when(registryRealm.getAuthorizationManager()).thenReturn(authManager);
        when(userRegistry.getUserRealm()).thenReturn(registryRealm);

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
    public void testGetProperties() throws Exception {
        resource.setProperty("registry.mount.path", "_system_governance");
        PropertiesAdminService adminService = new PropertiesAdminService();
        PropertiesBean bean1 = adminService.getProperties(resourcePath, "yes");
        assertEquals(1, bean1.getProperties().length);
        assertArrayEquals(new String[]{"registry.mount.path"}, bean1.getSysProperties());

        PropertiesBean bean2 = adminService.getProperties(resourcePath, "no");
        assertEquals(1, bean2.getProperties().length);
        assertArrayEquals(new String[]{}, bean2.getSysProperties());
    }

    @Test(expected = RegistryException.class)
    public void testSetPropertyRegistryProperty() throws Exception {
        PropertiesAdminService adminService = new PropertiesAdminService();
        adminService.setProperty(resourcePath, "registry.mount.path", "_system_governance");
    }

    @Test
    public void testSetPropertyToReadOnlyRegistry() throws RegistryException {
        System.setProperty("carbon.repo.write.mode", "false");
        PropertiesAdminService adminService = new PropertiesAdminService();
        adminService.setProperty(resourcePath, "mount.path", "_system_governance");
        PropertiesBean propertiesBean = adminService.getProperties(resourcePath, "true");
        assertArrayEquals(new String[]{}, propertiesBean.getLifecycleProperties());
        assertArrayEquals(new Property[]{}, propertiesBean.getProperties());
        assertArrayEquals(new String[]{}, propertiesBean.getSysProperties());
        assertArrayEquals(new String[]{}, propertiesBean.getValidationProperties());
        System.clearProperty("carbon.repo.write.mode");
    }

    @Test
    public void testSetProperty() throws RegistryException {
        System.setProperty("carbon.repo.write.mode", "true");
        PropertiesAdminService adminService = new PropertiesAdminService();
        adminService.setProperty(resourcePath, "mount.path", "_system_governance");
        PropertiesBean propertiesBean = adminService.getProperties(resourcePath, "true");
        assertArrayEquals(new String[]{}, propertiesBean.getLifecycleProperties());
        assertEquals(1, propertiesBean.getProperties().length);
        assertArrayEquals(new String[]{"mount.path"}, propertiesBean.getSysProperties());
        assertArrayEquals(new String[]{}, propertiesBean.getValidationProperties());
        System.clearProperty("carbon.repo.write.mode");
    }

    @Test(expected = RegistryException.class)
    public void testSetDuplicateProperty() throws RegistryException {
        System.setProperty("carbon.repo.write.mode", "true");
        resource.setProperty("mount.path", "_system_config");
        PropertiesAdminService adminService = new PropertiesAdminService();
        adminService.setProperty(resourcePath, "mount.path", "_system_governance");
        System.clearProperty("carbon.repo.write.mode");
    }

    @Test
    public void testUpdatePropertyToReadOnlyRegistry() throws RegistryException {
        System.setProperty("carbon.repo.write.mode", "false");
        PropertiesAdminService adminService = new PropertiesAdminService();
        adminService.updateProperty(resourcePath, "mount.path", "_system_governance", "mount.path");
        PropertiesBean propertiesBean = adminService.getProperties(resourcePath, "true");
        assertArrayEquals(new String[]{}, propertiesBean.getLifecycleProperties());
        assertArrayEquals(new Property[]{}, propertiesBean.getProperties());
        assertArrayEquals(new String[]{}, propertiesBean.getSysProperties());
        assertArrayEquals(new String[]{}, propertiesBean.getValidationProperties());
        System.clearProperty("carbon.repo.write.mode");
    }

    @Test(expected = RegistryException.class)
    public void testUpdatePropertyRegistryProperty() throws Exception {
        PropertiesAdminService adminService = new PropertiesAdminService();
        adminService.updateProperty(resourcePath, "registry.mount.path", "_system_governance", "registry.mount" +
                ".path");
    }

    @Test(expected = RegistryException.class)
    public void testUpdatePropertyWithDifferentKey() throws RegistryException {
        System.setProperty("carbon.repo.write.mode", "true");
        resource.setProperty("mount.path", "_system_config");
        PropertiesAdminService adminService = new PropertiesAdminService();
        adminService.updateProperty(resourcePath, "mount.path", "_system_governance", "registry.mount.path");
        System.clearProperty("carbon.repo.write.mode");
    }

    @Test
    public void updateProperty() throws Exception {
        System.setProperty("carbon.repo.write.mode", "true");
        resource.setProperty("mount.path", "_system_config");
        PropertiesAdminService adminService = new PropertiesAdminService();
        adminService.updateProperty(resourcePath, "mount.path", "_system_governance", "mount.path");
        PropertiesBean propertiesBean = adminService.getProperties(resourcePath, "true");
        assertArrayEquals(new String[]{}, propertiesBean.getLifecycleProperties());
        Property[] properties = propertiesBean.getProperties();
        assertEquals(1, properties.length);
        assertEquals("_system_governance", properties[0].getValue());

        assertArrayEquals(new String[]{"mount.path"}, propertiesBean.getSysProperties());
        assertArrayEquals(new String[]{}, propertiesBean.getValidationProperties());
        System.clearProperty("carbon.repo.write.mode");
    }

    @Test
    public void testRemovePropertyToReadOnlyRegistry() throws RegistryException {
        System.setProperty("carbon.repo.write.mode", "false");
        resource.setProperty("mount.path", "_system_config");
        PropertiesAdminService adminService = new PropertiesAdminService();
        adminService.removeProperty(resourcePath, "mount.path");
        PropertiesBean propertiesBean = adminService.getProperties(resourcePath, "true");
        assertArrayEquals(new String[]{"mount.path"}, propertiesBean.getSysProperties());
        System.clearProperty("carbon.repo.write.mode");
    }

    @Test
    public void testRemoveProperty() throws Exception {
        System.setProperty("carbon.repo.write.mode", "true");
        resource.setProperty("mount.path", "_system_config");
        PropertiesAdminService adminService = new PropertiesAdminService();
        adminService.removeProperty(resourcePath, "mount.path");
        PropertiesBean propertiesBean = adminService.getProperties(resourcePath, "true");
        assertArrayEquals(new String[]{}, propertiesBean.getLifecycleProperties());
        Property[] properties = propertiesBean.getProperties();
        assertEquals(0, properties.length);

        assertArrayEquals(new String[]{}, propertiesBean.getSysProperties());
        assertArrayEquals(new String[]{}, propertiesBean.getValidationProperties());
        System.clearProperty("carbon.repo.write.mode");
    }

    @Test
    public void testSetRetentionPropertiesToReadOnlyRegistry() throws Exception {
        System.setProperty("carbon.repo.write.mode", "false");
        RetentionBean retentionBean = new RetentionBean();
        resource.setProperty("mount.path", "_system_config");
        PropertiesAdminService adminService = new PropertiesAdminService();
        assertFalse(adminService.setRetentionProperties(resourcePath, retentionBean));
        System.clearProperty("carbon.repo.write.mode");
    }

    @Test
    public void testSetRetentionPropertiesWithoutBean() throws Exception {
        System.setProperty("carbon.repo.write.mode", "true");
        resource.setProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME, "admin");
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        resource.setProperty(CommonConstants.RETENTION_FROM_DATE_PROP_NAME,
                df.format(Calendar.getInstance().getTime()));
        resource.setProperty(CommonConstants.RETENTION_TO_DATE_PROP_NAME,
                df.format(Calendar.getInstance().getTime()));
        resource.setProperty(CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME, "true");
        resource.setProperty(CommonConstants.RETENTION_DELETE_LOCKED_PROP_NAME, "false");
        PropertiesAdminService adminService = new PropertiesAdminService();
        assertTrue(adminService.setRetentionProperties(resourcePath, null));
        assertNull(resource.getProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME));
        assertNull(resource.getProperty(CommonConstants.RETENTION_FROM_DATE_PROP_NAME));
        assertNull(resource.getProperty(CommonConstants.RETENTION_TO_DATE_PROP_NAME));
        assertNull(resource.getProperty(CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME));
        assertNull(resource.getProperty(CommonConstants.RETENTION_DELETE_LOCKED_PROP_NAME));
        System.clearProperty("carbon.repo.write.mode");
    }

    @Test
    public void testSetRetentionProperties() throws Exception {
        System.setProperty("carbon.repo.write.mode", "true");
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        RetentionBean retentionBean = new RetentionBean();
        retentionBean.setDeleteLocked(true);
        String fromDate = df.format(Calendar.getInstance().getTime());
        retentionBean.setFromDate(fromDate);
        retentionBean.setReadOnly(true);
        String toDate = df.format(Calendar.getInstance().getTime());
        retentionBean.setToDate(toDate);
        retentionBean.setWriteLocked(true);
        retentionBean.setUserName("admin");
        PropertiesAdminService adminService = new PropertiesAdminService();
        assertTrue(adminService.setRetentionProperties(resourcePath, retentionBean));
        assertEquals("admin", resource.getProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME));
        assertEquals(fromDate, resource.getProperty(CommonConstants.RETENTION_FROM_DATE_PROP_NAME));
        assertEquals(toDate, resource.getProperty(CommonConstants.RETENTION_TO_DATE_PROP_NAME));
        assertEquals("true", resource.getProperty(CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME));
        assertEquals("true", resource.getProperty(CommonConstants.RETENTION_DELETE_LOCKED_PROP_NAME));
        System.clearProperty("carbon.repo.write.mode");
    }

    @Test
    public void testGetRetentionProperties() throws Exception {
        System.setProperty("carbon.repo.write.mode", "true");
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String fromDate = df.format(Calendar.getInstance().getTime());
        resource.setProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME, "admin");
        resource.setProperty(CommonConstants.RETENTION_FROM_DATE_PROP_NAME, fromDate);
        String toDate = df.format(Calendar.getInstance().getTime());
        resource.setProperty(CommonConstants.RETENTION_TO_DATE_PROP_NAME, toDate);
        resource.setProperty(CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME, "true");
        resource.setProperty(CommonConstants.RETENTION_DELETE_LOCKED_PROP_NAME, "false");
        PropertiesAdminService adminService = new PropertiesAdminService();
        RetentionBean retentionBean = adminService.getRetentionProperties(resourcePath);
        assertEquals(fromDate, retentionBean.getFromDate());
        assertEquals(toDate, retentionBean.getToDate());
        assertEquals("admin", retentionBean.getUserName());
        assertTrue(retentionBean.getWriteLocked());
        assertFalse(retentionBean.getDeleteLocked());
        System.clearProperty("carbon.repo.write.mode");
    }

    @Test
    public void testGetRetentionPropertiesForVersionResource() throws Exception {
        System.setProperty("carbon.repo.write.mode", "true");
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String fromDate = df.format(Calendar.getInstance().getTime());
        resource.setProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME, "admin");
        resource.setProperty(CommonConstants.RETENTION_FROM_DATE_PROP_NAME, fromDate);
        String toDate = df.format(Calendar.getInstance().getTime());
        resource.setProperty(CommonConstants.RETENTION_TO_DATE_PROP_NAME, toDate);
        resource.setProperty(CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME, "true");
        resource.setProperty(CommonConstants.RETENTION_DELETE_LOCKED_PROP_NAME, "false");
        PropertiesAdminService adminService = new PropertiesAdminService();
        RetentionBean retentionBean = adminService.getRetentionProperties(versionResourcePath);
        assertEquals(fromDate, retentionBean.getFromDate());
        assertEquals(toDate, retentionBean.getToDate());
        assertEquals("admin", retentionBean.getUserName());
        assertTrue(retentionBean.getWriteLocked());
        assertFalse(retentionBean.getDeleteLocked());
        assertTrue(retentionBean.getReadOnly());
        System.clearProperty("carbon.repo.write.mode");
    }
}
