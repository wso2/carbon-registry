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
package org.wso2.carbon.registry.reporting.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ntask.core.AbstractTask;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class ReportingTask extends AbstractTask {

    private static final Log log = LogFactory.getLog(ReportingTask.class);

    public void execute() {
        try {
            Registry registry = new RemoteRegistry(getProperties().get("reporting.registry.url"),
                    getProperties().get("reporting.registry.username"),
                    getProperties().get("reporting.registry.password"));
            Map<String, String> attributes = new HashMap<String, String>();
            for (Map.Entry<String, String> e : getProperties().entrySet()) {
                if (!e.getKey().startsWith("reporting.")) {
                    attributes.put(e.getKey(), e.getValue());
                }
            }
            try {
                String type = getProperties().get("reporting.type");
                ByteArrayOutputStream stream = Utils.getReportContentStream(
                        getProperties().get("reporting.class"),
                        getProperties().get("reporting.template"),
                        type, attributes, registry);
                try {
                    Resource resource = registry.newResource();
                    resource.setContentStream(new ByteArrayInputStream(stream.toByteArray()));
                    if (type.toLowerCase().equals("pdf")) {
                        resource.setMediaType("application/pdf");
                    } else if (type.toLowerCase().equals("excel")) {
                        resource.setMediaType("application/vnd.ms-excel");
                    } else if (type.toLowerCase().equals("html")) {
                        resource.setMediaType("application/html");
                    }
                    registry.put(getProperties().get("reporting.resource.path"), resource);
                } finally {
                    stream.close();
                }
            } catch (Exception e) {
                log.error("Unable to obtain reporting content stream", e);
            }
        } catch (MalformedURLException e) {
            log.error("Invalid Registry Connection URL", e);
        } catch (RegistryException e) {
            log.error("Unable to connect to remote registry", e);
        }
    }
}
