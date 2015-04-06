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
package org.wso2.carbon.registry.task.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @scr.component name="org.wso2.carbon.registry.task" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 */
public class RegistryTaskServiceComponent {

    private static Log log = LogFactory.getLog(RegistryTaskServiceComponent.class);

    private static final String REGISTRY_TASK_MANAGER = "registryTasks";

    protected void activate(ComponentContext context) {
        log.debug("Registry Tasks bundle is activated ");
    }

    protected void deactivate(ComponentContext context) {
        log.debug("Registry Tasks bundle is deactivated ");
    }

    protected void setRegistryService(RegistryService registryService) {
    }

    protected void unsetRegistryService(RegistryService registryService) {
    }

    protected void setTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Task Service");
        }
        try {
            TaskManager taskManager = null;
            PrivilegedCarbonContext.startTenantFlow();
            try {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                taskManager = taskService.getTaskManager(REGISTRY_TASK_MANAGER);
                taskService.registerTaskType(REGISTRY_TASK_MANAGER);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
            if (taskManager != null) {
                registerTasks(taskManager);
                List<TaskInfo> allTasks = taskManager.getAllTasks();
                for (TaskInfo task : allTasks) {
                    taskManager.rescheduleTask(task.getName());
                }
            } else {
                log.warn("Unable to obtain an instance of a task manager");
            }
        } catch (Throwable e) {
            log.warn("Unable to schedule tasks", e);
        }
    }

    private void registerTasks(TaskManager taskManager) throws TaskException {
        String configPath = CarbonUtils.getRegistryXMLPath();
        if (configPath != null) {
            File registryXML = new File(configPath);
            if (registryXML.exists()) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(registryXML);
                    StAXOMBuilder builder = new StAXOMBuilder(
                            CarbonUtils.replaceSystemVariablesInXml(fileInputStream));
                    OMElement configElement = builder.getDocumentElement();
                    OMElement taskElement = configElement.getFirstChildWithName(new QName("tasks"));
                    if (taskElement != null) {
                        Iterator tasks = taskElement.getChildrenWithName(new QName("task"));
                        while (tasks.hasNext()) {
                            OMElement task = (OMElement) tasks.next();
                            String cronExpression = task.getFirstChildWithName(new QName(
                                    "trigger")).getAttributeValue(new QName("cron"));
                            TaskInfo.TriggerInfo trigger;
                            if (cronExpression != null) {
                                trigger = new TaskInfo.TriggerInfo(cronExpression);
                            } else {
                                log.warn("Only Cron-based triggers are supported right now");
                                continue;
                            }
                            String name = task.getAttributeValue(new QName("name"));
                            String clazz = task.getAttributeValue(new QName("class"));
                            if (name == null) {
                                name = clazz.substring(clazz.lastIndexOf(".") + 1);
                            }
                            Map<String, String> propertyMap = new LinkedHashMap<String, String>();
                            Iterator properties = task.getChildrenWithName(new QName("property"));
                            while (properties.hasNext()) {
                                OMElement property = (OMElement) properties.next();
                                propertyMap.put(property.getAttributeValue(new QName("key")),
                                        property.getAttributeValue(new QName("value")));
                            }
                            taskManager.registerTask(new TaskInfo(name, clazz, propertyMap, trigger));
                        }
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

    protected void unsetTaskService(TaskService taskService) {
        try {
            TaskManager taskManager = null;
            PrivilegedCarbonContext.startTenantFlow();
            try {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                        MultitenantConstants.SUPER_TENANT_ID);
                taskManager = taskService.getTaskManager(REGISTRY_TASK_MANAGER);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
            if (taskManager != null) {
                for (TaskInfo taskInfo : taskManager.getAllTasks()) {
                    taskManager.deleteTask(taskInfo.getName());
                }
            }
        } catch (TaskException e) {
            log.warn("Unable to clean-up scheduled tasks", e);
        }
    }
}
