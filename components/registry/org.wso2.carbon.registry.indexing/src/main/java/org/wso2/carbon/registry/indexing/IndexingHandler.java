/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.indexing;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.MediaTypesUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.registry.indexing.solr.SolrClient;
import org.wso2.carbon.registry.indexing.utils.IndexingUtils;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * A handler that performs indexing of resources using a Apache Solr server, and then provides
 * results for content searches based on the indexes. A request for indexing is submitted whenever a
 * resource is added, moved, copied or renamed. Created indexes are cleaned up when resources
 * renamed, moved or deleted.
 * <p/>
 * Indexing is an asynchronous operations, and only the resources having a media type for which an
 * indexer is registered will be indexed. Indexing will not cause an impact on generic registry
 * operations, and is design to work as a background operation.
 */
public class IndexingHandler extends Handler {
    private static Log log = LogFactory.getLog(IndexingHandler.class);
    private volatile static AsyncIndexer asyncIndexer;

    /**
     * <property name="indexingUrl" type="xml" value="url"/>
     */

    public void put(RequestContext requestContext) throws RegistryException {
        if (log.isDebugEnabled()){
            log.debug(" Before put resources into indexer");
        }
        if (isIndexable(requestContext)) {
            return;
        }
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();

        submitFileForIndexing(getIndexer(), requestContext.getResource(), requestContext.getResourcePath().getPath(),
                null,carbonContext.getTenantId(), carbonContext.getTenantDomain() );
        if (log.isDebugEnabled()){
            log.debug(" After put resources into indexer");
        }
    }

    @Override
    public String move(RequestContext requestContext) throws RegistryException {
        if (isExecutingMountedHandlerChain(requestContext)) {
            return super.move(requestContext);
        }
        String oldPath = requestContext.getSourcePath();
        String newPath = requestContext.getTargetPath();
        int tenantId = CurrentSession.getTenantId();
        try {
            deleteFromIndex(oldPath, tenantId);
        } catch (SolrException e) {
            log.error("Could not delete file for Solr server", e);
        } catch (RegistryException e) {
            log.error("Could not delete file for Solr server", e);
        }
        Resource resource = requestContext.getRegistry().get(oldPath);
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        submitFileForIndexing(getIndexer(), resource, newPath, null, carbonContext.getTenantId(), carbonContext.getTenantDomain());
        return super.move(requestContext);
    }

    private void deleteFromIndex(String oldPath, int tenantId) throws RegistryException {
        getIndexer().getClient().deleteFromIndex(oldPath, tenantId);
    }

    @Override
    public String rename(RequestContext requestContext) throws RegistryException {
        if (isExecutingMountedHandlerChain(requestContext)) {
            return super.rename(requestContext);
        }
        String oldPath = requestContext.getSourcePath();
        String newPath = requestContext.getTargetPath();
        int tenantId = CurrentSession.getTenantId();
        try {
            deleteFromIndex(oldPath, tenantId);
        } catch (SolrException e) {
            log.error("Could not delete file for Solr server", e);
        } catch (RegistryException e) {
            log.error("Could not delete file for Solr server", e);
        }
        Resource resource = requestContext.getRegistry().get(oldPath);
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        submitFileForIndexing(getIndexer(), resource, newPath, null, carbonContext.getTenantId(), carbonContext.getTenantDomain());
        return super.rename(requestContext);
    }

    @Override
    public String copy(RequestContext requestContext) throws RegistryException {
        if (isExecutingMountedHandlerChain(requestContext)) {
            return super.rename(requestContext);
        }
        String oldPath = requestContext.getSourcePath();
        String newPath = requestContext.getTargetPath();
        Resource resource = requestContext.getRegistry().get(oldPath);
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();

        submitFileForIndexing(getIndexer(), resource, newPath, null, carbonContext.getTenantId(), carbonContext.getTenantDomain());
        return super.copy(requestContext);
    }

    private boolean isIndexable(RequestContext requestContext) {
        return isExecutingMountedHandlerChain(requestContext)
                || requestContext.getResource() == null
                || requestContext.getResource().getMediaType() == null
                || requestContext.getResource() instanceof Collection;
    }

    private boolean isExecutingMountedHandlerChain(RequestContext requestContext) {
        return (requestContext.getRegistry().getRegistryContext() == null)
                || requestContext.getRegistry().getRegistryContext().isClone();
    }

    @Override
    public Collection searchContent(RequestContext requestContext) throws RegistryException {
        String searchQuery = requestContext.getKeywords();
        UserRegistry registry = CurrentSession.getUserRegistry();
        SolrClient client;

        List<String> filteredResults = new ArrayList<String>();
        try {
            client = SolrClient.getInstance();
            SolrDocumentList results = client.query(searchQuery, CurrentSession.getTenantId());
            if (log.isDebugEnabled()){
                log.debug("result received "+ results);
            }
            for(int i = 0;i < results.getNumFound();i++){
                SolrDocument solrDocument = results.get(i);
                String path = getPathFromId((String)solrDocument.getFirstValue("id"));

                //if (AuthorizationUtils.authorize(path, ActionConstants.GET)){
                if(isAuthorized(registry,path, ActionConstants.GET)){
                    filteredResults.add(path);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("filtered results "+ filteredResults + " for user "+ registry.getUserName());
            }
        } catch (IndexerException e) {
            log.error("Unable to do Content Search", e);
        }
        String[] resourcePaths = filteredResults.toArray(new String[filteredResults.size()]);
        Collection searchResults = new CollectionImpl();
        searchResults.setContent(resourcePaths);
        return searchResults;
    }

    private String getPathFromId(String id) {
        return id.substring(0, id.lastIndexOf(IndexingConstants.FIELD_TENANT_ID));
    }

    private boolean isAuthorized(UserRegistry registry, String resourcePath, String action) throws RegistryException {
        UserRealm userRealm = registry.getUserRealm();
        String userName = getLoggedInUserName();
        try {
            if (!userRealm.getAuthorizationManager().isUserAuthorized(userName,
                    resourcePath, action)) {
                return false;
            }
        } catch (UserStoreException e) {
            throw new RegistryException("Error while authorizing " + resourcePath
                    + " with user " + userName + ":" + e.getMessage(), e);
        }

        return true;
    }

    private String getLoggedInUserName() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    public void importResource(RequestContext requestContext) throws RegistryException {
        if (isIndexable(requestContext)) {
            return;
        }

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();

        submitFileForIndexing(getIndexer(), requestContext.getResource(), requestContext.getResourcePath().getPath(), requestContext.getSourceURL(), carbonContext.getTenantId(), carbonContext.getTenantDomain() );
    }

    public void delete(RequestContext requestContext) throws RegistryException {
        final String id = requestContext.getResourcePath().getPath();
        final int tenantId = CurrentSession.getTenantId();
        new Thread(new Runnable() {
            int tid = tenantId;
            public void run() {
                try {
                    deleteFromIndex(id, tid);
                } catch (SolrException e) {
                    log.error("Could not delete file for Solr server", e);
                } catch (RegistryException e) {
                    log.error("Could not delete file for Solr server", e);
                }
            }
        }).start();
    }

    public void putChild(RequestContext requestContext) throws RegistryException {

    }

    public void importChild(RequestContext requestContext) throws RegistryException {
    }

    private AsyncIndexer getIndexer() throws RegistryException {
        try {
            if (asyncIndexer == null) {
                synchronized (this) {
                    if (asyncIndexer == null) {
                        asyncIndexer = null;//AsyncIndexer.getInstance();
                        asyncIndexer = new AsyncIndexer();
                        new Thread(asyncIndexer).start();
                    }
                }
            }
            return asyncIndexer;
        } catch (SolrException e) {
            throw new RegistryException(e.getMessage(),e);
        }
    }

    private void submitFileForIndexing(AsyncIndexer indexer, Resource resource, String path, String sourceURL, int tenantId, String tenantDomain) {
        //if media type is null, mostly it is not a file. We will skip.
        String mediaType = resource.getMediaType();
        if (mediaType == null && path != null) {
            try {
                mediaType = MediaTypesUtils.getMediaType(RegistryUtils.getResourceName(path));
            } catch (RegistryException ignored) {
                // We are only making an attempt to determine the media type.
            }
        }
        if (mediaType == null || IndexingManager.getInstance().getIndexerForMediaType(mediaType)
                == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Submitting file "+ path + " for Indexing");
        }
        try {
            String lcName = resource.getProperty("registry.LC.name");
            String lcState = lcName != null ? resource.getProperty("registry.lifecycle." + lcName + ".state") : null;
            indexer.addFile(new File2Index(IndexingUtils.getByteContent(resource, sourceURL),mediaType,path,
                    CurrentSession.getTenantId(), tenantDomain, lcName, lcState));

        } catch (RegistryException e) {
            log.error("An error occurred while submitting file for indexing", e);
        }
    }

}
