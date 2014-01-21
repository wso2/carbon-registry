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
package org.wso2.carbon.registry.uddi.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.FileManipulator;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * @scr.component name="registry.uddi.component" immediate="true"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
public class JUDDIServiceComponent {

    private static final Log log = LogFactory.getLog(JUDDIServiceComponent.class);
    private static final String ENABLE = "enable";
    private static final String UDDI_SYSTEM_PROPERTY = "uddi";
    private static final String TEMP_WEBAPP_DIR = "webapps";

    protected void activate(ComponentContext ctxt) {
        if(ENABLE.equalsIgnoreCase(System.getProperty(UDDI_SYSTEM_PROPERTY))){
            try {
                copyWebAppIfNotExist();
            } catch (IOException ignore) {
            log.error("Error occurred while copying inbuilt webapps to web app dir" +ignore.getMessage());
            }
        }
    }


    private void copyWebAppIfNotExist() throws IOException {
        String tempWebAppDir = CarbonUtils.getCarbonHome()
                + File.separator + "repository" + File.separator + "resources" + File.separator + TEMP_WEBAPP_DIR;

        String webAppDir = CarbonUtils.getCarbonHome() +File.separator + "repository" +
                File.separator + "deployment" +File.separator+ "server" +File.separator+"webapps";
        File pluginsDir = new File(tempWebAppDir);
        String[] children = pluginsDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("juddi") && name.toLowerCase().endsWith(".war");
            }
        }
        );

        for(String childName:children){
            String source = tempWebAppDir + File.separator + childName;
            File sourceFile = new File(source);
            File dstFile = new File(webAppDir + File.separator + childName);
            log.info("Copying webapp " + sourceFile.getAbsolutePath() + " to " + dstFile.getAbsolutePath());
            if(!dstFile.exists()) {
                FileManipulator.copyFile(sourceFile, dstFile);
            }
        }


    }

    /**
     * @param ctxt
     */
    protected void deactivate(ComponentContext ctxt) {
    }

    /**
     * @param contextService
     */
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
    }

    /**
     * @param contextService
     */
    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
    }

}