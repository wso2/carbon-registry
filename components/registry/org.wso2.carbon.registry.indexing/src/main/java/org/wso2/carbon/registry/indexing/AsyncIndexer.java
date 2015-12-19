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
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.indexing.indexer.IndexDocumentCreator;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.registry.indexing.solr.SolrClient;
import org.wso2.carbon.utils.WaitBeforeShutdownObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The run() method of this class takes files from a blocking queue and indexes them.
 * An instance of this class should be executed with a ScheduledExecutorService so that run() method
 * runs periodically.
 */
public class AsyncIndexer implements Runnable {

    private static Log log = LogFactory.getLog(AsyncIndexer.class);
    private final SolrClient client;
    private LinkedBlockingQueue<File2Index> queue = new LinkedBlockingQueue<File2Index>();
    private boolean canAcceptFiles = true;
    int poolSize = 50;

    @SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
    public static class File2Index {
        public byte[] data;
        public String mediaType;
        public String path;
        public String lcName;
        public String lcState;
        public String sourceURL;

        public int tenantId;
        public String tenantDomain;

        public File2Index(byte[] data, String mediaType, String path, int tenantId, String tenantDomain) {
            this.data = data;
            this.mediaType = mediaType;
            this.path = path;
            this.tenantId = tenantId;
            this.tenantDomain = tenantDomain;
        }

        public File2Index(byte[] data, String mediaType, String path, int tenantId, String tenantDomain,
                String lcName, String lcState) {
            this.data = data;
            this.mediaType = mediaType;
            this.path = path;
            this.tenantId = tenantId;
            this.tenantDomain = tenantDomain;
            this.lcName = lcName;
            this.lcState = lcState;
        }

        public File2Index (String path, int tenantId, String tenantDomain, String sourceURL) {
            this.path = path;
            this.tenantId = tenantId;
            this.tenantDomain = tenantDomain;
            this.sourceURL = sourceURL;
        }
    }

    public void addFile(File2Index file2Index) {
        if (canAcceptFiles) {
            queue.offer(file2Index);
        } else {
            log.warn("Can't accept resource for indexing. Shutdown in progress: path=" +
                    file2Index.path);
        }
    }

    protected AsyncIndexer() throws RegistryException {
        try {
            client = SolrClient.getInstance();
            Utils.setWaitBeforeShutdownObserver(new WaitBeforeShutdownObserver() {
                public void startingShutdown() {
                    canAcceptFiles = false;
                    do {
                        indexFile();
                    } while (queue.size() != 0);
                }

                public boolean isTaskComplete() {
                    // if the queue is not empty task is not complete.
                    return !(queue.size() > 0);
                }
            });
        } catch (IndexerException e) {
            throw new RegistryException("Error initializing Async Indexer " + e.getMessage(), e);
        }
    }

    public SolrClient getClient() {
        return client;
    }

    /**
     * This method retrieves resources submitted for indexing from a blocking queue and indexed them.
     * This handles interrupts properly so that it is compatible with the Executor framework.
     */
    public void run() {
      indexFile();
    }

    private boolean indexFile() {
        try {
            if(!canAcceptFiles){
                return false;
            }
            long batchSize = IndexingManager.getInstance().getBatchSize();
            long i =0;
            List<IndexingTask> taskList = new ArrayList<IndexingTask>();
            while (queue.size() > 0 && i <= batchSize) {
                ++i;
                IndexingTask indexingTask = new IndexingTask(queue.take());
                taskList.add(indexingTask);

            }
            if (taskList.size() > 0) {
                uploadFiles(taskList);
            }else {
                return true;
            }

        } catch (Throwable e) { // Throwable is caught to prevent the executor termination
            if (e instanceof InterruptedException) {
                return false; // to be compatible with executor framework. No need of logging anything
            } else {
                log.error("Error while indexing.", e);
            }
        }
        return true;
    }

    protected void uploadFiles(List<IndexingTask> tasks) throws RegistryException {

        poolSize = IndexingManager.getInstance().getIndexerPoolSize();
        if (poolSize <= 0) {
            for (IndexingTask task : tasks) {
                task.run();
            }
        } else {
            ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
            try {
                for (IndexingTask task : tasks) {
                    executorService.submit(task);
                }
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to submit indexing task ", e);
                }
            } finally {
                executorService.shutdown();
            }
        }
    }

    /**
     * This class is used to create index document for the indexing tasks submitted by indexing manager. This will
     * run in multi threaded environment and create index documents for resources submitted for indexing from a
     * blocking queue and submit them to SolR client.
     */
    protected static class IndexingTask implements Runnable {

        private static final String REGISTRY_RESOURCE_SYMLINK_PATH = "registry.resource.symlink.path";
        private File2Index fileData;

        // This class is an inner class of the AsyncIndexer class which is not instantiated outside.
        // Hence the constructor is made protected.
        protected IndexingTask(File2Index fileData) {
            this.fileData = fileData;
        }

        /**
         * This method is used to submit resources in order to create indexing document.
         *
         */
        public void run() {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(fileData.tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(fileData.tenantDomain);
                createIndexDocument(fileData);

            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        /**
         * This method is used to create index document which is submitted for SolR client.
         *
         * @param  file2Index resource submitted for indexing. (comes from a blocking queue)
         */
        private void createIndexDocument(File2Index file2Index) {
            RegistryContext registryContext = null;
            boolean skipCache = false;
            String resourcePath = file2Index.path;
            try {
                skipCache = IndexingManager.getInstance().isCacheSkipped();
                Registry registry = IndexingManager.getInstance().getRegistry(file2Index.tenantId);
                registryContext = registry.getRegistryContext();

                if (resourcePath != null) {
                    if (skipCache) {
                        // Add resource path as a no cache path. So the resource will be fetched from embedded registry.
                        registryContext.registerNoCachePath(resourcePath);
                        if (log.isDebugEnabled()) {
                            log.debug("Resource at path \"" + resourcePath + "\" is added as a no cache path");
                        }
                    }

                    Resource resource;
                    //Check whether resource exists before indexing the resource
                    if (registry.resourceExists(resourcePath) && (resource = registry.get(resourcePath)) != null) {
                        // Create the IndexDocument
                        IndexDocumentCreator indexDocumentCreator = new IndexDocumentCreator(file2Index, resource);
                        indexDocumentCreator.createIndexDocument();

                        // Here, we are checking whether a resource has a symlink associated to it, if so,
                        // we submit that symlink path in the indexer. see CARBON-11510.
                        String symlinkPath = resource.getProperty(REGISTRY_RESOURCE_SYMLINK_PATH);
                        if (symlinkPath != null) {
                            // Create the IndexDocument
                            file2Index.path = symlinkPath;
                            indexDocumentCreator = new IndexDocumentCreator(file2Index, resource);
                            indexDocumentCreator.createIndexDocument();
                        }
                    }

                }
            } catch (RegistryException | IndexerException e) {
                log.error("Error while indexing. Resource at path " + "\"" + resourcePath + "\"" + "could not be "
                        + "indexed" + e);
            } finally {
                if (skipCache && registryContext != null) {
                    registryContext.removeNoCachePath(resourcePath);
                    if (log.isDebugEnabled()) {
                        log.debug("Resource at path \"" + resourcePath + "\" is removed from no cache path");
                    }
                }
            }
        }
    }
}
