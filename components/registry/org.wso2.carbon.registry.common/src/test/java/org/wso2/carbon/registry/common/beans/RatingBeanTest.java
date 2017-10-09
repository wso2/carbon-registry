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

package org.wso2.carbon.registry.common.beans;

import junit.framework.TestCase;

public class RatingBeanTest extends TestCase {

    private RatingBean ratingBean;

    @Override
    protected void setUp() throws Exception {
        ratingBean = new RatingBean();
        super.setUp();
    }

    public void testGetErrorMessage() throws Exception {
        assertNull(ratingBean.getErrorMessage());
        ratingBean.setErrorMessage("Error Message");
        assertEquals("Error Message", ratingBean.getErrorMessage());
    }

    public void testGetUserRating() throws Exception {
        assertEquals(-1, ratingBean.getUserRating());
        ratingBean.setUserRating(3);
        assertEquals(3, ratingBean.getUserRating());
    }

    public void testGetAverageRating() throws Exception {
        assertEquals(0.0f, ratingBean.getAverageRating());
        ratingBean.setAverageRating(3f);
        assertEquals(3f, ratingBean.getAverageRating());
    }

    public void testGetUserStars() throws Exception {
        assertEquals(5, ratingBean.getUserStars().length);
        String[] starts = ratingBean.getUserStars();
        starts[0] = "5";
        ratingBean.setUserStars(starts);
        assertEquals(5, ratingBean.getUserStars().length);
        assertEquals("5", ratingBean.getUserStars()[0]);
    }

    public void testGetAverageStars() throws Exception {
        assertEquals(5, ratingBean.getAverageStars().length);
        String[] starts = ratingBean.getAverageStars();
        starts[0] = "5";
        ratingBean.setAverageStars(starts);
        assertEquals(5, ratingBean.getAverageStars().length);
        assertEquals("5", ratingBean.getAverageStars()[0]);
    }

    public void testIsPutAllowed() throws Exception {
        assertFalse(ratingBean.isPutAllowed());

        ratingBean.setPutAllowed(true);
        assertTrue(ratingBean.isPutAllowed());
    }

    public void testIsVersionView() throws Exception {
        assertFalse(ratingBean.isVersionView());
        ratingBean.setVersionView(true);
        assertTrue(ratingBean.isVersionView());
    }

    public void testIsLoggedIn() throws Exception {
        assertFalse(ratingBean.isLoggedIn());
        ratingBean.setLoggedIn(true);
        assertTrue(ratingBean.isLoggedIn());
    }

    public void testGetPathWithVersion() throws Exception {
        assertNull(ratingBean.getPathWithVersion());

        ratingBean.setPathWithVersion("/_system/governance/trunk;version=1");

        assertEquals("/_system/governance/trunk;version=1", ratingBean.getPathWithVersion());
    }
}