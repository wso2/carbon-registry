/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.common.utils;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.context.MessageContext;
import org.mockito.ArgumentCaptor;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.securevault.SecretResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommonUtilTest extends TestCase {

    public void testGetUserRegistry() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        UserRegistry userRegistry = mock(UserRegistry.class);
        HttpSession httpSession = mock(HttpSession.class);
        httpSession.setAttribute(RegistryConstants.REGISTRY_USER, userRegistry);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(httpSession.getAttribute(RegistryConstants.REGISTRY_USER)).thenReturn(userRegistry);
        UserRegistry userRegistryReturned = CommonUtil.getUserRegistry(httpServletRequest);
        assertEquals(userRegistry, userRegistryReturned);

    }

    public void testGetUserRegistryError() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        UserRegistry userRegistry = mock(UserRegistry.class);
        HttpSession httpSession = mock(HttpSession.class);
        httpSession.setAttribute(RegistryConstants.REGISTRY_USER, userRegistry);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(httpSession.getAttribute(RegistryConstants.REGISTRY_USER)).thenReturn(null);
        try {
            CommonUtil.getUserRegistry(httpServletRequest);
            fail("Exception expected");
        } catch (RegistryException e) {
            assertEquals("User's Registry instance is not found. Users have to login to retrieve a registry instance. ",
                         e.getMessage());
        }


    }

    public void testGetServiceVersion() throws Exception {
        String content =
                "<metadata xmlns=\"http://www.wso2.org/governance/metadata\"><overview><name>Sample</name><version>1" +
                        ".0.0</version><namespace>UserA</namespace></overview></metadata>";
        OMElement XMLContent = AXIOMUtil.stringToOM(content);
        String version = CommonUtil.getServiceVersion(XMLContent);
        assertEquals("1.0.0", version);

    }

    public void testGetServiceVersionEmptyOverview() throws Exception {
        String content =
                "<metadata xmlns=\"http://www.wso2.org/governance/metadata\"><overview></overview></metadata>";
        OMElement XMLContent = AXIOMUtil.stringToOM(content);
        String version = CommonUtil.getServiceVersion(XMLContent);
        assertEquals("", version);

    }

    public void testGetServiceVersionEmptyVersion() throws Exception {
        String content =
                "<metadata xmlns=\"http://www.wso2" +
                        ".org/governance/metadata\"><overview><name>Sample</name><namespace>UserA</namespace" +
                        "></overview></metadata>";
        OMElement XMLContent = AXIOMUtil.stringToOM(content);
        String version = CommonUtil.getServiceVersion(XMLContent);
        assertEquals("", version);

    }

    public void testGetServiceVersion2() throws Exception {
        String content =
                "<metadata><Overview><name>Sample</name><Version>1" +
                        ".0.0</Version><namespace>UserA</namespace></Overview></metadata>";
        OMElement XMLContent = AXIOMUtil.stringToOM(content);
        String version = CommonUtil.getServiceVersion(XMLContent);
        assertEquals("1.0.0", version);

    }

    public void testGetUserRegistryEmptyMessageContext() throws Exception {
        try {
            MessageContext.setCurrentMessageContext(null);
            RegistryService registryService = mock(RegistryService.class);
            CommonUtil.getUserRegistry(registryService);
            fail("Exception expected");
        } catch (RegistryException e) {
            assertEquals("Could not get the user's Registry session. Message context not found.", e.getMessage());
        }

    }

    public void testGetUserRegistryUsingRegistryService() throws Exception {
        RegistryService registryService = mock(RegistryService.class);
        MessageContext messageContext = mock(MessageContext.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        UserRegistry userRegistry = mock(UserRegistry.class);
        HttpSession httpSession = mock(HttpSession.class);
        MessageContext.setCurrentMessageContext(messageContext);
        when(messageContext.getProperty("transport.http.servletRequest")).thenReturn(httpServletRequest);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        httpSession.setAttribute(RegistryConstants.USER_REGISTRY, userRegistry);
        when(httpSession.getAttribute(RegistryConstants.USER_REGISTRY)).thenReturn(userRegistry);
        UserRegistry userRegistryReturned = CommonUtil.getUserRegistry(registryService);
        assertEquals(userRegistry, userRegistryReturned);

    }

    public void testGetUserRegistryUsingInvalidRegistry() throws Exception {
        RegistryService registryService = mock(RegistryService.class);
        MessageContext messageContext = mock(MessageContext.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpSession httpSession = mock(HttpSession.class);
        MessageContext.setCurrentMessageContext(messageContext);
        when(messageContext.getProperty("transport.http.servletRequest")).thenReturn(httpServletRequest);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        httpSession.setAttribute(RegistryConstants.USER_REGISTRY, null);
        when(httpSession.getAttribute(RegistryConstants.USER_REGISTRY)).thenReturn(null);
        try {
            CommonUtil.getUserRegistry(registryService);
            fail("Exception expected");
        } catch (RegistryException e) {
            assertEquals("User's Registry instance is not found. Users have to login to retrieve a registry instance. ",
                         e.getMessage());
        }
    }

    public void testGetUserRegistryFromSession() throws Exception {
        HttpSession httpSession = mock(HttpSession.class);
        UserRegistry userRegistry = mock(UserRegistry.class);
        httpSession.setAttribute(RegistryConstants.USER_REGISTRY, userRegistry);
        when(httpSession.getAttribute(RegistryConstants.USER_REGISTRY)).thenReturn(userRegistry);
        UserRegistry userRegistryReturned = CommonUtil.getUserRegistry(null, httpSession);
        assertEquals(userRegistry, userRegistryReturned);

    }

    public void testGetUserRegistryFromSessionError() throws Exception {
        HttpSession httpSession = mock(HttpSession.class);
        when(httpSession.getAttribute(RegistryConstants.USER_REGISTRY)).thenReturn(null);
        try {
            CommonUtil.getUserRegistry(null, httpSession);
        } catch (RegistryException e) {
            assertEquals("User's Registry instance is not found. Users have to login to retrieve a registry instance. ",
                         e.getMessage());
        }
    }

    public void testGetUserRegistryFromSessionWithUser() throws Exception {
        HttpSession httpSession = mock(HttpSession.class);
        UserRegistry userRegistry = mock(UserRegistry.class);
        RegistryService registryService = mock(RegistryService.class);
        String username = "admin";
        when(httpSession.getAttribute(RegistryConstants.USER_REGISTRY)).thenReturn(null);
        when(httpSession.getAttribute("logged-user")).thenReturn(username);
        when(registryService.getUserRegistry(username)).thenReturn(userRegistry);
        UserRegistry userRegistryReturned = CommonUtil.getUserRegistry(registryService, httpSession);
        assertEquals(userRegistry, userRegistryReturned);
    }

    public void testInvalidateAllSessions() {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpSession httpSession = mock(HttpSession.class);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        CommonUtil.invalidateAllSessions(httpServletRequest);
    }

    public void testSendContent() throws Exception {
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        PrintWriter printWriter = mock(PrintWriter.class);
        when(httpServletResponse.getWriter()).thenReturn(printWriter);
        CommonUtil.sendContent(httpServletResponse, "Sample");

    }

    public void testSendContentErrorcase() throws Exception {
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        IOException exception = new IOException("Custom error");
        when(httpServletResponse.getWriter()).thenThrow(exception);
        CommonUtil.sendContent(httpServletResponse, "Sample");


    }

    public void testSendErrorContent() throws Exception {
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        PrintWriter printWriter = mock(PrintWriter.class);
        when(httpServletResponse.getWriter()).thenReturn(printWriter);
        CommonUtil.sendErrorContent(httpServletResponse, "Error");
    }

    public void testSendErrorContentErrorCase() throws Exception {
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        IOException exception = new IOException("Custom error");
        when(httpServletResponse.getWriter()).thenThrow(exception);
        CommonUtil.sendErrorContent(httpServletResponse, "Error");
    }



    public void testAddErrorMessage() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpSession httpSession = mock(HttpSession.class);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        CommonUtil.addErrorMessage(httpServletRequest, "Custom error message");
    }

    public void testAddErrorMessage2() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpSession httpSession = mock(HttpSession.class);
        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(httpSession.getAttribute(CommonConstants.ERROR_MESSAGE)).thenReturn("ERROR");
        CommonUtil.addErrorMessage(httpServletRequest, "Custom error message 2");
    }

    public void testRedirect() throws Exception {
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        CommonUtil.redirect(httpServletResponse, "http://napagoda.com");
        verify(httpServletResponse).sendRedirect(captor.capture());
        assertEquals("http://napagoda.com", captor.getValue());
    }

    public void testFormatDateNullCase() throws Exception {
        assertNull(CommonUtil.formatDate(null));
    }

    public void testFormatDate() throws Exception {
        assertNotNull(CommonUtil.formatDate(new Date()));
    }

    public void testFormatDateTwoDaysAgo() throws Exception {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, -2);
        Date previousDate = calendar.getTime();
        assertNotNull(CommonUtil.formatDate(previousDate));
    }

    public void testFormatDateTwoHoursAgo() throws Exception {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.HOUR_OF_DAY, -2);
        Date previousDate = calendar.getTime();
        assertNotNull(CommonUtil.formatDate(previousDate));
    }

    public void testFormatDateCustomDate() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1985);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DATE, 26);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.HOUR_OF_DAY, 00);
        calendar.set(Calendar.SECOND, 00);
        Date date = calendar.getTime();
        String formatDate = CommonUtil.formatDate(date);
        if (!formatDate.startsWith("on 26 Jan 00:01:00 1985")) {
            fail("Expected format : on 26 Jan 00:01:00 1985 (on Sat Jan 26 00:00:00 IST 1985) Received : " +
                         formatDate);
        }
    }

    public void testComputeDate() throws Exception {
        String computedDate = CommonUtil.computeDate("01/26/1985").toString();
        if (!computedDate.contains("Sat Jan 26 00:00:00") || !computedDate.contains("1985")) {
            fail("Expected date : Sat Jan 26 00:00:00 IST 1985 Received : " + computedDate);
        }
    }

    public void testComputeDateWrongFormat() throws Exception {
        try {
            CommonUtil.computeDate("1985-01-26");
            fail("exception expected");
        } catch (RegistryException e) {
            assertEquals("Date format is invalid: 1985-01-26", e.getMessage());
        }
    }

    public void testComputeDateNullCases() throws Exception {
        assertNull(CommonUtil.computeDate(""));
        assertNull(CommonUtil.computeDate(null));
    }

    public void testPopulateAverageStars() throws Exception {
        ResourceData resourceData = new ResourceData();
        resourceData.setAverageRating(5f);
        CommonUtil.populateAverageStars(resourceData);
        assertEquals("04", resourceData.getAverageStars()[0]);
        assertEquals("04", resourceData.getAverageStars()[1]);
        assertEquals("04", resourceData.getAverageStars()[2]);
        assertEquals("04", resourceData.getAverageStars()[3]);
        assertEquals("04", resourceData.getAverageStars()[4]);

        resourceData.setAverageRating(3.51f);
        CommonUtil.populateAverageStars(resourceData);
        assertEquals("04", resourceData.getAverageStars()[0]);
        assertEquals("04", resourceData.getAverageStars()[1]);
        assertEquals("04", resourceData.getAverageStars()[2]);
        assertEquals("02", resourceData.getAverageStars()[3]);
        assertEquals("00", resourceData.getAverageStars()[4]);

        resourceData.setAverageRating(3.63f);
        CommonUtil.populateAverageStars(resourceData);
        assertEquals("04", resourceData.getAverageStars()[0]);
        assertEquals("04", resourceData.getAverageStars()[1]);
        assertEquals("04", resourceData.getAverageStars()[2]);
        assertEquals("03", resourceData.getAverageStars()[3]);
        assertEquals("00", resourceData.getAverageStars()[4]);

        resourceData.setAverageRating(3.4123f);
        CommonUtil.populateAverageStars(resourceData);
        assertEquals("04", resourceData.getAverageStars()[0]);
        assertEquals("04", resourceData.getAverageStars()[1]);
        assertEquals("04", resourceData.getAverageStars()[2]);
        assertEquals("02", resourceData.getAverageStars()[3]);
        assertEquals("00", resourceData.getAverageStars()[4]);

        resourceData.setAverageRating(3.987f);
        CommonUtil.populateAverageStars(resourceData);
        assertEquals("04", resourceData.getAverageStars()[0]);
        assertEquals("04", resourceData.getAverageStars()[1]);
        assertEquals("04", resourceData.getAverageStars()[2]);
        assertEquals("04", resourceData.getAverageStars()[3]);
        assertEquals("00", resourceData.getAverageStars()[4]);

        resourceData.setAverageRating(0.111f);
        CommonUtil.populateAverageStars(resourceData);
        assertEquals("00", resourceData.getAverageStars()[0]);
        assertEquals("00", resourceData.getAverageStars()[1]);
        assertEquals("00", resourceData.getAverageStars()[2]);
        assertEquals("00", resourceData.getAverageStars()[3]);
        assertEquals("00", resourceData.getAverageStars()[4]);

        resourceData.setAverageRating(0.131f);
        CommonUtil.populateAverageStars(resourceData);
        assertEquals("01", resourceData.getAverageStars()[0]);
        assertEquals("00", resourceData.getAverageStars()[1]);
        assertEquals("00", resourceData.getAverageStars()[2]);
        assertEquals("00", resourceData.getAverageStars()[3]);
        assertEquals("00", resourceData.getAverageStars()[4]);
    }

    public void testGetServerBaseURL() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        StringBuffer url = new StringBuffer("https://localhost:9443/wso2registry/");
        when(httpServletRequest.getRequestURL()).thenReturn(url);
        assertEquals("https://localhost:9443", CommonUtil.getServerBaseURL(httpServletRequest));
    }

    public void testGenerateOptionsFor() throws Exception {
        String[] options = {"Chandana", "Danesh", "Kasun"};
        assertEquals("<option value=\"Chandana\" selected>Chandana</option>\n" +
                             "<option value=\"Danesh\">Danesh</option>\n" +
                             "<option value=\"Kasun\">Kasun</option>\n",
                     CommonUtil.generateOptionsFor("Chandana", options));
    }

    public void testIsLatestVersion() throws Exception {
        assertFalse(CommonUtil.isLatestVersion("1", "2"));
        assertFalse(CommonUtil.isLatestVersion("1", "2"));

        assertTrue(CommonUtil.isLatestVersion("2", "2"));
    }

    public void testAttributeArrayToMap() throws Exception {
        String[] array = {"Key1|Value1", "Key2|Value2", "key3|Value3"};
        Map<String, String> populatedMap = CommonUtil.attributeArrayToMap(array);
        assertEquals(3, populatedMap.size());

        String[] populatedArray = CommonUtil.mapToAttributeArray(populatedMap);
        assertEquals(3, populatedArray.length);
    }

    public void testGetResolvedPassword() throws Exception {
        //Null Secret Resolver case
        String pass = "admin";
        String returnedPassword= CommonUtil.getResolvedPassword(null, null, pass);
        assertEquals(pass, returnedPassword);

        //Null config case
        SecretResolver secretResolver = mock(SecretResolver.class);
        when(secretResolver.isInitialized()).thenReturn(true);
        returnedPassword= CommonUtil.getResolvedPassword(secretResolver, null, pass);
        assertEquals(pass, returnedPassword);

        //Null config case
        String configName = "registry";
        when(secretResolver.isTokenProtected("wso2registry." + configName + ".password")).thenReturn(true);
        when(secretResolver.resolve("wso2registry." + configName + ".password")).thenReturn("mockpass");
        returnedPassword= CommonUtil.getResolvedPassword(secretResolver, configName, pass);
        assertEquals("mockpass", returnedPassword);

    }
}