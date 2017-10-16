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

import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.resource.beans.CollectionContentBean;
import org.wso2.carbon.registry.resource.beans.ContentBean;
import org.wso2.carbon.registry.resource.beans.ContentDownloadBean;
import org.wso2.carbon.registry.resource.test.base.BaseTestCase;

import static org.mockito.Mockito.mock;

public class ContentUtilTest extends BaseTestCase {

    @Override
    public void setUp() throws RegistryException {
        super.setUp();

        Resource r1 = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("R1 content");
        r1.setContent(r1content);
        r1.setMediaType("application/test");
        registry.put("/test/2017/10/12", r1);
    }

    public void testGetCollectionContent() throws Exception {
        CollectionContentBean collectionContentBean = ContentUtil.getCollectionContent("/test/2017/10",
                                                                                       (UserRegistry) registry);
        assertEquals(1, collectionContentBean.getChildCount());
        assertEquals("/test/2017/10/12", collectionContentBean.getChildPaths()[0]);
    }

    public void testGetResourceDataZero() throws Exception {
        ResourceData resourceData = mock(ResourceData.class);
        registry.rateResource("/test/2017/10/12", 0);
        ResourceData[] resourceData1 = ContentUtil.getResourceData(new String[]{"/test/2017/10/12"},
                                                                   (UserRegistry) registry);
        assertEquals("00", resourceData1[0].getAverageStars()[0]);

        registry.rateResource("/test/2017/10/12", 1);
        ResourceData[] resourceData6 = ContentUtil.getResourceData(new String[]{"/test/2017/10/12"},
                                                                   (UserRegistry) registry);
        assertEquals("04", resourceData6[0].getAverageStars()[0]);
        assertEquals("00", resourceData6[0].getAverageStars()[1]);
        assertEquals("00", resourceData6[0].getAverageStars()[2]);
        assertEquals("00", resourceData6[0].getAverageStars()[3]);
        assertEquals("00", resourceData6[0].getAverageStars()[4]);

        registry.rateResource("/test/2017/10/12", 2);
        ResourceData[] resourceData2 = ContentUtil.getResourceData(new String[]{"/test/2017/10/12"},
                                                                   (UserRegistry) registry);
        assertEquals("04", resourceData2[0].getAverageStars()[0]);
        assertEquals("04", resourceData2[0].getAverageStars()[1]);
        assertEquals("00", resourceData2[0].getAverageStars()[2]);
        assertEquals("00", resourceData2[0].getAverageStars()[3]);
        assertEquals("00", resourceData2[0].getAverageStars()[4]);

        registry.rateResource("/test/2017/10/12", 3);
        ResourceData[] resourceData3 = ContentUtil.getResourceData(new String[]{"/test/2017/10/12"},
                                                                   (UserRegistry) registry);
        assertEquals("04", resourceData3[0].getAverageStars()[0]);
        assertEquals("04", resourceData3[0].getAverageStars()[1]);
        assertEquals("04", resourceData3[0].getAverageStars()[2]);
        assertEquals("00", resourceData3[0].getAverageStars()[3]);
        assertEquals("00", resourceData3[0].getAverageStars()[4]);

        registry.rateResource("/test/2017/10/12", 4);
        ResourceData[] resourceData4 = ContentUtil.getResourceData(new String[]{"/test/2017/10/12"},
                                                                   (UserRegistry) registry);
        assertEquals("04", resourceData4[0].getAverageStars()[0]);
        assertEquals("04", resourceData4[0].getAverageStars()[1]);
        assertEquals("04", resourceData4[0].getAverageStars()[2]);
        assertEquals("04", resourceData4[0].getAverageStars()[3]);
        assertEquals("00", resourceData4[0].getAverageStars()[4]);

        registry.rateResource("/test/2017/10/12", 5);
        ResourceData[] resourceData5 = ContentUtil.getResourceData(new String[]{"/test/2017/10/12"},
                                                                   (UserRegistry) registry);
        assertEquals("04", resourceData5[0].getAverageStars()[0]);
        assertEquals("04", resourceData5[0].getAverageStars()[1]);
        assertEquals("04", resourceData5[0].getAverageStars()[2]);
        assertEquals("04", resourceData5[0].getAverageStars()[3]);
        assertEquals("04", resourceData5[0].getAverageStars()[4]);
    }

    public void testGetContent() throws Exception {
        ContentBean contentBean = ContentUtil.getContent("/test/2017/10/12", (UserRegistry) registry);
        assertFalse(contentBean.isCollection());
        assertEquals("/test/2017/10/12", contentBean.getContentPath());
        assertEquals("false", contentBean.getAbsent());
        assertNull(contentBean.getRealPath());
        assertEquals("application/test", contentBean.getMediaType());
        assertTrue(contentBean.isPutAllowed());
        assertFalse(contentBean.isVersionView());
        assertTrue(contentBean.isLoggedIn());
        assertEquals("/test/2017/10/12", contentBean.getPathWithVersion());
    }

    public void testHasAssociations() throws Exception {
        assertFalse(ContentUtil.hasAssociations("/test/2017/10/12", "any", (UserRegistry) registry));
        Resource r2 = registry.newResource();
        byte[] r2content = RegistryUtils.encodeString("R2 content");
        r2.setContent(r2content);
        r2.setMediaType("application/test");
        registry.put("/test/2017/10/13", r2);
        registry.addAssociation("/test/2017/10/13", "/test/2017/10/12", "any");
        assertFalse(ContentUtil.hasAssociations("/test/2017/10/12", "any", (UserRegistry) registry));
        registry.delete("/test/2017/10/13");
    }

    public void testGetContentWithDependencies() throws Exception {
        ContentDownloadBean contentDownloadBean = ContentUtil.getContentWithDependencies("/test/2017/10/12",
                                                                                         (UserRegistry) registry);
        assertNotNull(contentDownloadBean);
        assertNull(contentDownloadBean.getLastUpdatedTime());
        assertNull(contentDownloadBean.getMediatype());
        assertNotNull(contentDownloadBean.getContent());
    }

    @Override
    protected void tearDown() throws Exception {
        registry.delete("/test/2017/10/12");
        super.tearDown();
    }
}
