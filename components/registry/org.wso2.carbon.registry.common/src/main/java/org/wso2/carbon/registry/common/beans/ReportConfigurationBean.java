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
package org.wso2.carbon.registry.common.beans;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class ReportConfigurationBean {

    private String reportClass;
    private String name;
    private String template;
    private String type;
    private String[] attributes = new String[0];
    private String cronExpression;
    private String registryURL;
    private String username;
    private String password;
    private String resourcePath;
    private boolean isScheduled;

    public String getReportClass() {
        return reportClass;
    }

    public void setReportClass(String reportClass) {
        this.reportClass = reportClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void setAttributes(String[] attributes) {
        this.attributes = attributes;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getRegistryURL() {
        return registryURL;
    }

    public void setRegistryURL(String registryURL) {
        this.registryURL = registryURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @SuppressWarnings("unused")
    public boolean isScheduled() {
        return isScheduled;
    }

    public void setScheduled(boolean scheduled) {
        isScheduled = scheduled;
    }

	@Override
	public boolean equals(Object obj) {
		ReportConfigurationBean configuration = (ReportConfigurationBean) obj;
		
		if (configuration.getName().equals(this.getName())
				&& configuration.getTemplate().equals(this.getTemplate())
				&& configuration.getType().equals(this.getType())
				&& configuration.getReportClass().equals(this.getReportClass())) {
			return true;
		} else {
			return  false;
		}
	}
    
    
}
