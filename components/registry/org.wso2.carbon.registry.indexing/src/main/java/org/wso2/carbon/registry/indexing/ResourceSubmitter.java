/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.indexing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.LogEntry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.internal.IndexingServiceComponent;
import org.wso2.carbon.registry.indexing.utils.IndexingUtils;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.WaitBeforeShutdownObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * run() method of this class checks the resources which have been changed since last index time and
 * submits them for indexing. This uses registry logs to detect resources that need to be indexed.
 * An instance of this class should be executed with a ScheduledExecutorService so that run() method
 * runs periodically.
 */
public class ResourceSubmitter implements Runnable {

    private static Log log = LogFactory.getLog(ResourceSubmitter.class);

    private IndexingManager indexingManager;
    private boolean taskComplete = false;
    private boolean isShutdown = false;

    protected ResourceSubmitter(IndexingManager indexingManager) {
        this.indexingManager = indexingManager;
        Utils.setWaitBeforeShutdownObserver(new WaitBeforeShutdownObserver() {
            public void startingShutdown() {
                isShutdown = true;
            }

            public boolean isTaskComplete() {
                return taskComplete;
            }
        });
    }

    /**
     * This method checks the resources which have been changed since last index time and
     * submits them for indexing. This uses registry logs to detect resources that need to be
     * indexed. This method handles interrupts properly so that it is compatible with the
     * Executor framework
     */
    @SuppressWarnings({ "REC_CATCH_EXCEPTION" })
    public void run() {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            try {
                Date currentTime = indexingManager.getLastAccessTime(MultitenantConstants.SUPER_TENANT_ID);
                indexingManager.setLastAccessTime(MultitenantConstants.SUPER_TENANT_ID,
                        submitResource(currentTime, MultitenantConstants.SUPER_TENANT_ID,
                                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
            Tenant[] allTenants = RegistryCoreServiceComponent.getRealmService().getTenantManager().getAllTenants();
            for (Tenant tenant : allTenants) {
                PrivilegedCarbonContext.startTenantFlow();
                try {
                    int tenantId = tenant.getId();
                    Date currentTime = indexingManager.getLastAccessTime(tenantId);
                    indexingManager.setLastAccessTime(tenantId, submitResource(currentTime,
                            tenantId, tenant.getDomain()));
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        } catch (UserStoreException ignored) {

        }
    }

    private Date submitResource(Date currentTime, int tenantId, String tenantDomain) {
        if (!IndexingServiceComponent.canIndexTenant(tenantId)) {
            return currentTime;
        }
        if (isShutdown || Thread.currentThread().isInterrupted()) {
            // interruption can happen due to shutdown or some other reason.
            taskComplete = true;
            return currentTime; // To be compatible with shutdownNow() method on the executor service
        }
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantDomain(tenantDomain);
        carbonContext.setTenantId(tenantId);
        try {
            UserRegistry registry = indexingManager.getRegistry(tenantId);
            if (registry == null) {
                log.warn("Unable to submit resource for tenant " + tenantId + ". Unable to get registry instance");
                return currentTime;
            }
            String lastAccessTimeLocation = indexingManager.getLastAccessTimeLocation();

            LogEntry[] entries = registry.getLogs(null, LogEntry.ALL, null, indexingManager.getLastAccessTime(tenantId),
                    new Date(), false);
            Arrays.sort(entries, new Comparator<LogEntry>() {

                public int compare(LogEntry o1, LogEntry o2) {
                    return o1.getDate().compareTo(o2.getDate());
                }
            });
            if (entries.length > 0) {
                Date temp = entries[entries.length - 1].getDate();
                if (currentTime == null || currentTime.before(temp)) {
                    currentTime = temp;
                }
            }
            for (LogEntry logEntry : entries) {
                String path = logEntry.getResourcePath();
                try {
//                    Resource resourceToIndex = null;
                    if (path.equals(lastAccessTimeLocation)) {
                        continue;
                    }
                    if (logEntry.getAction() == (LogEntry.DELETE_RESOURCE)) {
                        indexingManager.deleteFromIndex(logEntry.getResourcePath(), tenantId);
                        if (log.isDebugEnabled()) {
                            log.debug("Resource Deleted: Resource at " + path +
                                    " will be deleted from Indexing Server");
                        }
                    } else if (IndexingUtils.isAuthorized(registry, path, ActionConstants.GET) && registry
                                    .resourceExists(path)) {
                        if (logEntry.getAction() == LogEntry.UPDATE) {
                            indexingManager.submitFileForIndexing(tenantId, tenantDomain, path, null);
                            if (log.isDebugEnabled()) {
                                log.debug("Resource Updated: Resource at " + path +
                                        " has been submitted to the Indexing Server");
                            }
                        } else if (logEntry.getAction() == LogEntry.DELETE_COMMENT) {
                            indexingManager.submitFileForIndexing(tenantId, tenantDomain, path, null);
                            if (log.isDebugEnabled()) {
                                log.debug("Resource comment deleted: Resource at " + path +
                                        " has been submitted to the Indexing Server");
                            }
                        } else if (logEntry.getAction() == LogEntry.REMOVE_ASSOCIATION) {
                            indexingManager.submitFileForIndexing(tenantId, tenantDomain, path, null);
                            if (log.isDebugEnabled()) {
                                log.debug("Resource association removed: Resource at " + path +
                                        " has been submitted to the Indexing Server");
                            }
                        } else if (logEntry.getAction() == LogEntry.REMOVE_TAG) {
                            indexingManager.submitFileForIndexing(tenantId, tenantDomain, path, null);
                            if (log.isDebugEnabled()) {
                                log.debug("Resource tag removed: Resource at " + path +
                                        " has been submitted to the Indexing Server");
                            }
                        } else if (logEntry.getAction() == LogEntry.ADD) {
                            indexingManager.submitFileForIndexing(tenantId, tenantDomain, path, null);
                            if (log.isDebugEnabled()) {
                                log.debug("Resource Inserted: Resource at " + path +
                                        " has been submitted to the Indexing Server");
                            }
                        } else if (logEntry.getAction() == LogEntry.TAG) {
                            indexingManager.submitFileForIndexing(tenantId, tenantDomain, path, null);
                            if (log.isDebugEnabled()) {
                                log.debug("Resource tag added: Resource at " + path +
                                        " has been submitted to the Indexing Server");
                            }
                        } else if (logEntry.getAction() == LogEntry.COMMENT) {
                            indexingManager.submitFileForIndexing(tenantId, tenantDomain, path, null);
                            if (log.isDebugEnabled()) {
                                log.debug("Resource comment added: Resource at " + path +
                                        " has been submitted to the Indexing Server");
                            }
                        } else if (logEntry.getAction() == LogEntry.ADD_ASSOCIATION) {
                            indexingManager.submitFileForIndexing(tenantId, tenantDomain, path, null);
                            if (log.isDebugEnabled()) {
                                log.debug("Resource association added: Resource at " + path +
                                        " has been submitted to the Indexing Server");
                            }
                        } else if (logEntry.getAction() == (LogEntry.MOVE)) {
                            indexingManager.submitFileForIndexing(tenantId, tenantDomain, path, null);
                            indexingManager.deleteFromIndex(logEntry.getActionData(), tenantId);
                            if (log.isDebugEnabled()) {
                                log.debug("Resource Moved: Resource at " + path +
                                        " has been submitted to the Indexing Server");
                            }
                        } else if (logEntry.getAction() == (LogEntry.COPY)) {
                            path = logEntry.getActionData();
                            indexingManager.submitFileForIndexing(tenantId, tenantDomain, path, null);
                            if (log.isDebugEnabled()) {
                                log.debug("Resource Copied : Resource at " + path +
                                        " has been submitted to the Indexing Server");
                            }
                        } else if (logEntry.getAction() == (LogEntry.RENAME)) {
                            indexingManager.submitFileForIndexing(tenantId, tenantDomain, path, null);
                            if (log.isDebugEnabled()) {
                                log.debug("Resource Renamed : Resource at " + path +
                                        " has been submitted to the Indexing Server");
                            }
                        }
                    }
                } catch (Exception e) { // to ease debugging
                    log.warn("An error occurred while submitting the resource for indexing, path: "
                            + path, e);
                }
            }
            if (log.isTraceEnabled()) {
                log.trace("last successfully indexed activity time is : " +
                        indexingManager.getLastAccessTime(tenantId).toString());
            }
        } catch (Throwable e) {
            // Throwable is caught to prevent termination of the executor
            log.warn("An error occurred while submitting resources for indexing", e);
        }
        return currentTime;
    }
}
