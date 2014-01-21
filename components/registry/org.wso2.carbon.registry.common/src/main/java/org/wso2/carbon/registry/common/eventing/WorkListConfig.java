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
package org.wso2.carbon.registry.common.eventing;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Configuration for the work-list in registry.xml
 */
public final class WorkListConfig {

    private static final Log log = LogFactory.getLog(WorkListConfig.class);

    private String username;
    private String password;
    private String serverURL;
    private String remote ;

    public WorkListConfig() {
        String configPath = CarbonUtils.getRegistryXMLPath();
        if (configPath != null) {
            File registryXML = new File(configPath);
            if (registryXML.exists()) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(registryXML);
                    StAXOMBuilder builder = new StAXOMBuilder(
                            CarbonUtils.replaceSystemVariablesInXml(fileInputStream));
                    OMElement configElement = builder.getDocumentElement();
                    SecretResolver secretResolver = SecretResolverFactory.create(configElement, false);
                    OMElement workList =
                            configElement.getFirstChildWithName(new QName("workList"));
                    if (workList != null) {
                        username = workList.getFirstChildWithName(new QName("username")).getText();
                        password = CommonUtil.getResolvedPassword(secretResolver,"workList",
                                workList.getFirstChildWithName(new QName("password")).getText());
                        serverURL = workList.getAttributeValue(new QName("serverURL"));
                        remote = workList.getAttributeValue(new QName("remote"));
                    }
                } catch (XMLStreamException e) {
                    log.error("Unable to parse registry.xml", e);
                } catch (IOException e) {
                    log.error("Unable to read registry.xml", e);
                } catch (CarbonException e) {
                    log.error("An error occurred during system variable replacement", e);
                }
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getServerURL() {
        return serverURL;
    }

    public boolean isRemote() {
        return Boolean.valueOf(remote);
    }
}
