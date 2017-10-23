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

package org.wso2.carbon.registry.common.beans;

import junit.framework.TestCase;

public class ReportConfigurationBeanTest extends TestCase {

    private ReportConfigurationBean reportConfigurationBean;

    @Override
    protected void setUp() throws Exception {
        reportConfigurationBean = new ReportConfigurationBean();
        super.setUp();
    }

    public void testGetReportClass() throws Exception {
        assertNull(reportConfigurationBean.getReportClass());
        reportConfigurationBean.setReportClass("org.wso2.registry.report.Classs");
        assertEquals("org.wso2.registry.report.Classs", reportConfigurationBean.getReportClass());
    }

    public void testGetName() throws Exception {
        assertNull(reportConfigurationBean.getName());
        reportConfigurationBean.setName("SampleReport");
        assertEquals("SampleReport", reportConfigurationBean.getName());
    }

    public void testGetTemplate() throws Exception {
        assertNull(reportConfigurationBean.getTemplate());
        reportConfigurationBean.setTemplate("ReportTemplate");
        assertEquals("ReportTemplate", reportConfigurationBean.getTemplate());
    }

    public void testGetType() throws Exception {
        assertNull(reportConfigurationBean.getType());
        reportConfigurationBean.setType("PDF");
        assertEquals("PDF", reportConfigurationBean.getType());
    }

    public void testGetAttributes() throws Exception {
        assertEquals(0, reportConfigurationBean.getAttributes().length);
        String[] attributes = new String[1];
        attributes[0] = "overview_name";
        reportConfigurationBean.setAttributes(attributes);
        assertEquals(1, reportConfigurationBean.getAttributes().length);
        assertEquals("overview_name", reportConfigurationBean.getAttributes()[0]);
    }

    public void testGetCronExpression() throws Exception {
        assertNull(reportConfigurationBean.getCronExpression());
        reportConfigurationBean.setCronExpression("* * * * 10");
        assertEquals("* * * * 10", reportConfigurationBean.getCronExpression());
    }
    
    public void testGetUsername() throws Exception {
        assertNull(reportConfigurationBean.getUsername());
        reportConfigurationBean.setUsername("admin");
        assertEquals("admin", reportConfigurationBean.getUsername());
    }

    public void testGetResourcePath() throws Exception {
        assertNull(reportConfigurationBean.getResourcePath());
        reportConfigurationBean.setResourcePath("/_system/governance/trunk");
        assertEquals("/_system/governance/trunk", reportConfigurationBean.getResourcePath());
    }

    public void testIsScheduled() throws Exception {
        assertFalse(reportConfigurationBean.isScheduled());
        reportConfigurationBean.setScheduled(true);
        assertTrue(reportConfigurationBean.isScheduled());
    }

    public void testEquals() throws Exception {
        ReportConfigurationBean reportConfigurationBean1 = new ReportConfigurationBean();
        reportConfigurationBean1.setName("Sample");
        reportConfigurationBean1.setTemplate("ReportTemplate");
        reportConfigurationBean1.setType("PDF");
        reportConfigurationBean1.setReportClass("org.wso2.registry.report.Classs");

        ReportConfigurationBean reportConfigurationBean2 = new ReportConfigurationBean();
        reportConfigurationBean2.setName("Sample");
        reportConfigurationBean2.setTemplate("ReportTemplate");
        reportConfigurationBean2.setType("PDF");
        reportConfigurationBean2.setReportClass("org.wso2.registry.report.Classs");

        assertTrue(reportConfigurationBean2.equals(reportConfigurationBean1));


    }
}