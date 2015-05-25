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
package org.wso2.carbon.registry.reporting.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

/**
 * @scr.component name="org.wso2.carbon.registry.reporting" immediate="true"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 */
public class ReportingServiceComponent {

    private static final Log log = LogFactory.getLog(ReportingServiceComponent.class);
    private static final String REPORTING_TASK_MANAGER = "registryReportingTasks";
    private static TaskService taskService;

    /**
     * Method to trigger when the OSGI component become active.
     *
     * @param context the component context
     */
    protected void activate(ComponentContext context) {
        log.debug("******* Registry Reporting bundle is activated ******* ");
    }

    /**
     * Method to trigger when the OSGI component become inactive.
     *
     * @param context the component context
     */
    protected void deactivate(ComponentContext context) {
        log.debug("******* Registry Reporting bundle is deactivated ******* ");
    }

    public static TaskManager getTaskManager(int tenantId) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            try {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                TaskManager taskManager =  taskService.getTaskManager(REPORTING_TASK_MANAGER);
                taskService.registerTaskType(REPORTING_TASK_MANAGER);
                return taskManager;
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        } catch (TaskException e) {
            log.error("Unable to obtain task manager", e);
        }
        return null;
    }

    public void setTaskService(TaskService taskService) {
        updateTaskService(taskService);

    }

    public void unsetTaskService(TaskService taskService) {
        updateTaskService(null);
    }

    // Method to update task service.
    private static void updateTaskService(TaskService service) {
        taskService = service;
    }
}
