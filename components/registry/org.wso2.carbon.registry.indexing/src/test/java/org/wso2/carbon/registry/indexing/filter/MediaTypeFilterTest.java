/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.indexing.filter;

import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.Repository;
import org.wso2.carbon.registry.core.jdbc.VersionRepository;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.IndexingHandler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MediaTypeFilterTest {

    private ResourceImpl resource = null;
    private UserRegistry registry = null;
    private Repository repository = null;
    private VersionRepository versionRepository = null;

    @Before
    public void setup() throws RegistryException {
        registry = mock(UserRegistry.class);
        repository = mock(Repository.class);
        versionRepository = mock(VersionRepository.class);
        resource = new ResourceImpl();
        resource.setPath("/_system/governance/trunk/soapservices/eu/dataaccess/footballpool/1.0.0/Info");
        resource.setMediaType("application/vnd.wso2-soap-service+xml");
        when(repository.get("/_system/governance/trunk/soapservices/eu/dataaccess/footballpool/1.0.0/Info"))
                .thenReturn(resource);

    }

    @Test
    public void getMediaTypeRegEx() throws Exception {
        String mediaTypeRegEx = "application/(.)+\\+json";
        MediaTypeFilter mediaTypeFilter = new MediaTypeFilter(mediaTypeRegEx);
        assertEquals(mediaTypeRegEx, mediaTypeFilter.getMediaTypeRegEx());
    }

    @Test
    public void setMediaTypeRegEx() throws Exception {
        String mediaTypeRegEx = "application/(.)+\\+json";
        MediaTypeFilter mediaTypeFilter = new MediaTypeFilter();
        mediaTypeFilter.setMediaTypeRegEx(mediaTypeRegEx);
        assertEquals(mediaTypeRegEx, mediaTypeFilter.getMediaTypeRegEx());
    }

    @Test
    public void equals() throws Exception {
        MediaTypeFilter filter1 = new MediaTypeFilter("application/*");
        MediaTypeFilter filter2 = filter1;
        assertTrue(filter2.equals(filter1));

        MediaTypeFilter filter3 = new MediaTypeFilter();
        filter3.setMediaTypeRegEx("application/*");
        filter3.setInvert("false");
        assertTrue(filter1.equals(filter3));

        filter3.setInvert("true");
        assertFalse(filter1.equals(filter3));
        assertFalse(filter1.equals(null));
        assertFalse(filter1.equals(new IndexingHandler()));
    }

    @Test
    public void handleGet() throws Exception {
        RequestContext requestContext = new RequestContext(registry, repository, versionRepository);
        requestContext.setResource(resource);
        MediaTypeFilter filter = new MediaTypeFilter("application/(.)+\\+xml");
        assertTrue(filter.handleGet(requestContext));

        ResourcePath resourcePath = requestContext.getResourcePath();
        if (resourcePath == null) {
            resourcePath = new ResourcePath("/_system/governance/trunk/soapservices/eu/dataaccess/footballpool/1.0.0" +
                    "/Info");
        }
        //request context doesn't have the resource. resource will retrieve from repository
        requestContext.setResource(null);
        assertTrue(filter.handleGet(requestContext));

        //new resource path
        resourcePath.setParameter("new", "");
        resourcePath.setParameter("mediaType", "application/vnd.wso2-soap-service+xml");
        assertTrue(filter.handleGet(requestContext));

        //resource mismatch
        resourcePath.setParameter("new", "");
        resourcePath.setParameter("mediaType", "application/vnd.wso2-soap-service+json");
        assertFalse(filter.handleGet(requestContext));
    }

    @Test
    public void handlePut() throws Exception {
        RequestContext requestContext = new RequestContext(registry, repository, versionRepository);
        requestContext.setResource(resource);
        MediaTypeFilter filter = new MediaTypeFilter("application/(.)+\\+xml");
        assertTrue(filter.handlePut(requestContext));

        //handle put with out resource.
        requestContext.setResource(null);
        assertFalse(filter.handlePut(requestContext));
    }

    @Test
    public void handleImportResource() throws Exception {
        RequestContext requestContext = new RequestContext(registry, repository, versionRepository);
        requestContext.setResource(resource);
        MediaTypeFilter filter = new MediaTypeFilter("application/(.)+\\+xml");
        assertTrue(filter.handleImportResource(requestContext));

        //handle put with out resource.
        requestContext.setResource(null);
        assertFalse(filter.handleImportResource(requestContext));
    }

    @Test
    public void handleDelete() throws Exception {
        RequestContext requestContext = new RequestContext(registry, repository, versionRepository);
        requestContext.setResource(resource);
        MediaTypeFilter filter = new MediaTypeFilter("application/(.)+\\+xml");
        assertTrue(filter.handleDelete(requestContext));

        //handle put with out resource.
        requestContext.setResource(null);
        ResourcePath resourcePath = requestContext.getResourcePath();
        if (resourcePath == null) {
            resourcePath = new ResourcePath("/_system/governance/trunk/soapservices/eu/dataaccess/footballpool/1.0.0" +
                    "/Info");
            requestContext.setResourcePath(resourcePath);
        }
        assertTrue(filter.handleDelete(requestContext));
    }

    @Test
    public void handlePutChild() throws Exception {
        Collection collection = new CollectionImpl();
        collection.setMediaType(null);
        when(repository.get("/_system/governance/trunk/soapservices/eu/dataaccess/footballpool/1.0.0"))
                .thenReturn(collection);
        RequestContext requestContext = new RequestContext(registry, repository, versionRepository);
        requestContext.setResource(resource);
        MediaTypeFilter filter = new MediaTypeFilter("application/(.)+\\+xml");
        assertFalse(filter.handlePutChild(requestContext));

        collection.setMediaType("application/vnd.wso2-soap-service+xml");
        assertTrue(filter.handlePutChild(requestContext));
    }

    @Test(expected = RegistryException.class)
    public void handlePutChildWithParentResource() throws Exception {
        Resource resource1 = new ResourceImpl();
        resource1.setMediaType(null);
        when(repository.get("/_system/governance/trunk/soapservices/eu/dataaccess/footballpool/1.0.0"))
                .thenReturn(resource1);
        RequestContext requestContext = new RequestContext(registry, repository, versionRepository);
        requestContext.setResource(resource);
        MediaTypeFilter filter = new MediaTypeFilter("application/(.)+\\+xml");
        assertFalse(filter.handlePutChild(requestContext));
    }

    @Test
    public void handleImportChild() throws Exception {
        Collection collection = new CollectionImpl();
        collection.setMediaType(null);
        when(repository.get("/_system/governance/trunk/soapservices/eu/dataaccess/footballpool/1.0.0"))
                .thenReturn(collection);
        RequestContext requestContext = new RequestContext(registry, repository, versionRepository);
        requestContext.setResource(resource);
        MediaTypeFilter filter = new MediaTypeFilter("application/(.)+\\+xml");
        assertFalse(filter.handleImportChild(requestContext));

        collection.setMediaType("application/vnd.wso2-soap-service+xml");
        assertTrue(filter.handleImportChild(requestContext));
    }

}