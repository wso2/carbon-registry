/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SimpleTimeZone;

/**
 * A handler which can manage project metadata resources.
 */
public class ProjectMediaTypeHandler extends Handler {

    private String mediaType = "application/vnd.wso2-project+xml";
    private String location;

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void put(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            Resource resource = requestContext.getResource();
            Object content = resource.getContent();
            String payload;
            if (content instanceof String) {
                payload = (String) content;
            } else {
                payload = RegistryUtils.decodeBytes((byte[]) content);
            }
            try {
                populateProjectProperties(AXIOMUtil.stringToOM(payload),
                        resource);
                String path = location + RegistryUtils.getResourceName(
                        requestContext.getResourcePath().getPath());
                requestContext.getRegistry().put(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                        path, resource);
                requestContext.setProcessingComplete(true);
            } catch (ParseException e) {
                throw new RegistryException("Unable to parse project configuration", e);
            } catch (XMLStreamException e) {
                throw new RegistryException("Unable to parse project configuration", e);
            }
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    public void importResource(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            Resource resource;
            if (requestContext.getResource() == null) {
                resource = new ResourceImpl();
                resource.setMediaType(mediaType);
            } else {
                resource = requestContext.getResource();
            }
            String sourceURL = requestContext.getSourceURL();
            InputStream inputStream;
            try {
                if (sourceURL != null && sourceURL.toLowerCase().startsWith("file:")) {
                    String msg = "The source URL must not be file in the server's local file system";
                    throw new RegistryException(msg);
                }
                inputStream = new URL(sourceURL).openStream();
            } catch (IOException e) {
                throw new RegistryException("The URL " + sourceURL + " is incorrect.", e);
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int nextChar;
            try {
                while ((nextChar = inputStream.read()) != -1) {
                    outputStream.write(nextChar);
                }
                outputStream.flush();
            } catch (IOException e) {
                throw new RegistryException("Error while reading project definition", e);
            }
            byte[] bytes = outputStream.toByteArray();
            resource.setContent(bytes);
            try {
                populateProjectProperties(AXIOMUtil.stringToOM(RegistryUtils.decodeBytes(bytes)),
                        resource);
            } catch (ParseException e) {
                throw new RegistryException("Unable to parse project configuration", e);
            } catch (XMLStreamException e) {
                throw new RegistryException("Unable to parse project configuration", e);
            }
            String path = location + RegistryUtils.getResourceName(
                    requestContext.getResourcePath().getPath());
            requestContext.getRegistry().put(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                    path, resource);
            requestContext.setProcessingComplete(true);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    // In JDK6, you'll need to box float values into Float in order to avoid class cast issues when
    // using String#format(). Therefore, this exclusion is to get rid of the unwanted warnings.
    @SuppressWarnings("UnnecessaryBoxing")
    private void populateProjectProperties(OMElement payload, Resource projectResource)
            throws ParseException {
        final String MS_PROJECT_NS = payload.getNamespace().getNamespaceURI();
        final SimpleDateFormat PROJECT_TIME_FORMAT =
                new SimpleDateFormat("'PT'H'H'm'M's'S'");
        PROJECT_TIME_FORMAT.setTimeZone(new SimpleTimeZone(0, "UTC"));

        String startDate = null;
        String finishDate = null;
        long total;
        long remaining;
        long duration = 0l;
        float cost = 0.0f;
        float remainingCost = 0.0f;

        OMElement startDateElement =
                payload.getFirstChildWithName(new QName(MS_PROJECT_NS, "StartDate"));
        if (startDateElement != null) {
            startDate = startDateElement.getText();
        }
        OMElement finishDateElement =
                payload.getFirstChildWithName(new QName(MS_PROJECT_NS, "FinishDate"));
        if (finishDateElement != null) {
            finishDate = finishDateElement.getText();
        }

        Map<Integer, Float> rates = new LinkedHashMap<Integer, Float>();

        OMElement resourcesElement =
                payload.getFirstChildWithName(new QName(MS_PROJECT_NS, "Resources"));
        if (resourcesElement != null) {
            Iterator resourceElements = resourcesElement
                    .getChildrenWithName(new QName(MS_PROJECT_NS, "Resource"));
            while (resourceElements.hasNext()) {
                OMElement resourceElement = (OMElement) resourceElements.next();
                OMElement idElement = resourceElement.getFirstChildWithName(
                        new QName(MS_PROJECT_NS, "ID"));

                OMElement standardRateElement = resourceElement.getFirstChildWithName(
                        new QName(MS_PROJECT_NS, "StandardRate"));
                rates.put(Integer.parseInt(idElement.getText()),
                        Float.parseFloat(standardRateElement.getText()));
            }
        }

        OMElement assignmentsElement =
                payload.getFirstChildWithName(new QName(MS_PROJECT_NS, "Assignments"));
        long work = 0;
        long remainingWork = 0;
        if (assignmentsElement != null) {
            Iterator assignmentElements = assignmentsElement
                    .getChildrenWithName(new QName(MS_PROJECT_NS, "Assignment"));
            while (assignmentElements.hasNext()) {
                OMElement assignmentElement = (OMElement) assignmentElements.next();
                OMElement resourceElement = assignmentElement.getFirstChildWithName(
                        new QName(MS_PROJECT_NS, "ResourceUID"));
                if (Long.parseLong(resourceElement.getText()) < 0) {
                    // no resource assigned would mean that this item is an aggregation of
                    // assignments.
                    continue;
                }
                OMElement workElement = assignmentElement.getFirstChildWithName(
                        new QName(MS_PROJECT_NS, "Work"));
                long time = PROJECT_TIME_FORMAT.parse(workElement.getText()).getTime();
                work += time;
                cost += (time / 1000f / 60f / 60f) * rates.get(
                        Integer.parseInt(resourceElement.getText()));

                OMElement remainingWorkElement = assignmentElement.getFirstChildWithName(
                        new QName(MS_PROJECT_NS, "RemainingWork"));
                time = PROJECT_TIME_FORMAT.parse(remainingWorkElement.getText()).getTime();
                remainingWork += time;
                remainingCost += (time / 1000f / 60f / 60f) * rates.get(
                        Integer.parseInt(resourceElement.getText()));
            }
        }

        total = (work / 1000 / 60 / 60);
        remaining = (remainingWork / 1000 / 60 / 60);

        OMElement tasksElement =
                payload.getFirstChildWithName(new QName(MS_PROJECT_NS, "Tasks"));

        if (tasksElement != null) {
            Iterator taskElements = tasksElement
                    .getChildrenWithName(new QName(MS_PROJECT_NS, "Task"));
            while (taskElements.hasNext()) {
                OMElement taskElement = (OMElement) taskElements.next();
                OMElement typeElement = taskElement.getFirstChildWithName(
                        new QName(MS_PROJECT_NS, "Type"));
                if (Integer.parseInt(typeElement.getText()) < 0) {
                    // no resource assigned would mean that this item is an aggregation of
                    // assignments.
                    continue;
                }
                OMElement durationElement = taskElement.getFirstChildWithName(
                        new QName(MS_PROJECT_NS, "Duration"));
                duration += PROJECT_TIME_FORMAT.parse(
                        durationElement.getText()).getTime() / 1000 / 60 / 60 / 8;
            }
        }

        if (startDate != null) {
            projectResource.setProperty("Start Date", startDate);
        }

        if (finishDate != null) {
            projectResource.setProperty("Finish Date", finishDate);
        }

        if (duration > 0) {
            projectResource.setProperty("Duration", duration + " days");
        }

        if (total > 0) {
            projectResource.setProperty("Scheduled Work", total + " hours");
        }

        if (remaining > 0) {
            projectResource.setProperty("Remaining Work", remaining + " hours");
        }

        if ((total - remaining) > 0) {
            projectResource.setProperty("Actual Work", (total - remaining) + " hours");
        }

        if (cost > 0) {
            projectResource.setProperty("Scheduled Cost", "$" + String.format("%.2f",
                    new Float(cost)));
        }

        if (remainingCost > 0) {
            projectResource.setProperty("Remaining Cost", "$" + String.format("%.2f",
                    new Float(remainingCost)));
        }

        if ((cost - remainingCost) > 0) {
            projectResource.setProperty("Actual Cost", "$" + String.format("%.2f",
                    new Float((cost - remainingCost))));
        }
    }
}
