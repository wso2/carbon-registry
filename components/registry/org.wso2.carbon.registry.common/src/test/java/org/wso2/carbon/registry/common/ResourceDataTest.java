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

package org.wso2.carbon.registry.common;

import junit.framework.TestCase;

import java.util.Calendar;

public class ResourceDataTest extends TestCase {

    private ResourceData resourceData;
    private Calendar calendar;

    @Override
    protected void setUp() throws Exception {
        resourceData = new ResourceData();
        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1985);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DATE, 26);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.HOUR_OF_DAY, 00);
        calendar.set(Calendar.SECOND, 00);
    }

    public void testGetName() throws Exception {
        assertNull(resourceData.getName());
        resourceData.setName("Sample");
        assertEquals("Sample", resourceData.getName());

        resourceData.setName("ResourceName?v=1");
        assertEquals("ResourceName", resourceData.getName());
    }


    public void testGetResourcePath() throws Exception {
        resourceData.setResourcePath("/");
        assertEquals("/", resourceData.getResourcePath());
        assertEquals("", resourceData.getRelativePath());

        resourceData.setResourcePath("/_system/");
        assertEquals("/_system/", resourceData.getResourcePath());
        assertEquals("_system/", resourceData.getRelativePath());

        resourceData.setResourcePath("/_system/governance");
        assertEquals("/_system/governance", resourceData.getResourcePath());
        assertEquals("_system/governance", resourceData.getRelativePath());
    }

    public void testGetRelativePath() throws Exception {
        assertNull(resourceData.getRelativePath());
        resourceData.setRelativePath("Sample");
        assertEquals("Sample", resourceData.getRelativePath());
    }

    public void testGetResourceType() throws Exception {
        assertNull(resourceData.getResourceType());
        resourceData.setResourceType("resource");
        assertEquals("resource", resourceData.getResourceType());
    }

    public void testGetAuthorUserName() throws Exception {
        assertNull(resourceData.getAuthorUserName());
        resourceData.setAuthorUserName("admin");
        assertEquals("admin", resourceData.getAuthorUserName());
    }

    public void testGetDescription() throws Exception {
        assertNull(resourceData.getDescription());
        resourceData.setDescription("Sample Description");
        assertEquals("Sample Description", resourceData.getDescription());
    }

    public void testGetAverageRating() throws Exception {
        assertEquals(0.0f, resourceData.getAverageRating());
        resourceData.setAverageRating(5f);
        assertEquals(5f, resourceData.getAverageRating());
    }

    public void testGetAverageStars() throws Exception {
        assertEquals(5, resourceData.getAverageStars().length);
        resourceData.setAverageStars(new String[]{"04", "04", "04", "04"});
        assertEquals(4, resourceData.getAverageStars().length);
    }

    public void testGetCreatedOn() throws Exception {
        resourceData.setCreatedOn(calendar);
        assertNotNull(resourceData.getCreatedOn());
        assertEquals(1985, resourceData.getCreatedOn().get(Calendar.YEAR));
        assertEquals(0, resourceData.getCreatedOn().get(Calendar.MONTH));
        assertEquals(26, resourceData.getCreatedOn().get(Calendar.DAY_OF_MONTH));
    }

    public void testGetFormattedCreatedOn() throws Exception {
        resourceData.setCreatedOn(calendar);
        assertEquals("on 26 Jan 00:01:00 1985 (on Sat Jan 26 00:00:00 IST 1985)", resourceData.getFormattedCreatedOn());
    }

    public void testIsDeleteAllowed() throws Exception {
        assertFalse(resourceData.isDeleteAllowed());
        resourceData.setDeleteAllowed(true);
        assertTrue(resourceData.isDeleteAllowed());
    }

    public void testIsPutAllowed() throws Exception {
        assertFalse(resourceData.isPutAllowed());
        resourceData.setPutAllowed(true);
        assertTrue(resourceData.isPutAllowed());
    }

    public void testGetTagCounts() throws Exception {
        assertNull(resourceData.getTagCounts());
        TagCount[] tagCounts = new TagCount[1];
        tagCounts[0] = new TagCount();
        tagCounts[0].setKey("Key");
        tagCounts[0].setValue(5l);
        resourceData.setTagCounts(tagCounts);
        assertEquals(1, resourceData.getTagCounts().length);
        for (int i = 0; i < resourceData.getTagCounts().length; i++) {
            assertEquals(resourceData.getTagCounts()[0].getKey(), "Key");
            assertEquals(resourceData.getTagCounts()[0].getValue(), new Long(5));
        }
    }

    public void testIsLink() throws Exception {
        assertFalse(resourceData.isLink());
        resourceData.setLink(true);
        assertTrue(resourceData.isLink());
    }

    public void testIsExternalLink() throws Exception {
        assertFalse(resourceData.isExternalLink());
        resourceData.setExternalLink(true);
        assertTrue(resourceData.isExternalLink());
    }

    public void testIsMounted() throws Exception {
        assertFalse(resourceData.isMounted());
        resourceData.setMounted(true);
        assertTrue(resourceData.isMounted());
    }

    public void testIsGetAllowed() throws Exception {
        assertFalse(resourceData.isGetAllowed());
        resourceData.setGetAllowed(true);
        assertTrue(resourceData.isGetAllowed());
    }

    public void testGetRealPath() throws Exception {
        assertNull(resourceData.getRealPath());
        resourceData.setRealPath("/_system");
        assertEquals("/_system", resourceData.getRealPath());
    }


    public void testGetAbsent() throws Exception {
        assertEquals("false", resourceData.getAbsent());
        resourceData.setAbsent("true");
        assertEquals("true", resourceData.getAbsent());
    }

}