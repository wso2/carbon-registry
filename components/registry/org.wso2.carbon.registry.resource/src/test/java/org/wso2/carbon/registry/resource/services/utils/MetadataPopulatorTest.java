/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.resource.services.utils;

import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.resource.beans.MetadataBean;
import org.wso2.carbon.registry.resource.test.base.BaseTestCase;

public class MetadataPopulatorTest extends BaseTestCase {

    @Override
    public void setUp() throws RegistryException {
        super.setUp();

        Resource resource = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("Resource content");
        resource.setContent(r1content);
        resource.setMediaType("application/test");
        resource.setDescription("Sample Description");
        resource.setVersionableChange(true);
        registry.put("/test/2017/10/15", resource);
        registry.createVersion("/test/2017/10/15");
    }

    public void testPopulate() throws Exception {
        MetadataBean metadataBean = MetadataPopulator.populate("/test/2017/10/15;version:1", (UserRegistry) registry);
        assertEquals("application/test", metadataBean.getMediaType());
        assertEquals("false", metadataBean.getWriteLocked());

        assertEquals("false", metadataBean.getDeleteLocked());

        assertEquals("15", metadataBean.getResourceVersion());
        assertFalse(metadataBean.isCollection());
        assertEquals("/test/2017/10/15", metadataBean.getPath());

        assertEquals("/test/2017/10/15;version:1", metadataBean.getPathWithVersion());

        assertNotNull(metadataBean.getNavigatablePaths());
        assertEquals("/", metadataBean.getNavigatablePaths()[0].getNavigateName());
        assertEquals("/", metadataBean.getNavigatablePaths()[0].getNavigatePath());

        assertTrue(metadataBean.isVersionView());

        assertEquals("/test/2017/10/15", metadataBean.getActiveResourcePath());

        assertNotNull(metadataBean.getFormattedCreatedOn());
        assertEquals("admin", metadataBean.getAuthor());

        assertNotNull(metadataBean.getFormattedLastModified());

        assertEquals("admin", metadataBean.getLastUpdater());
        assertEquals("/test/2017/10/15;version:1",metadataBean.getPermalink());
        assertEquals("carbon registry base URL.", metadataBean.getServerBaseURL());
        assertEquals("Sample Description", metadataBean.getDescription());
        assertEquals("/test/2017/10/15;version:1", metadataBean.getContentPath());
        assertTrue(metadataBean.isPutAllowed());
    }

    @Override
    protected void tearDown() throws Exception {
        registry.delete("/test/2017/10/15");
        super.tearDown();
    }
}
