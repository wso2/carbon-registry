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

package org.wso2.carbon.registry.common.beans.utils;

import junit.framework.TestCase;

import java.util.Calendar;

public class CommentTest extends TestCase {

    private Comment comment;

    @Override
    protected void setUp() throws Exception {
        comment = new Comment();
        super.setUp();
    }

    public void testGetText() throws Exception {
        assertNull(comment.getText());
        comment.setText("Comment text");
        assertEquals("Comment text", comment.getText());
    }

    public void testGetUser() throws Exception {
        assertNull(comment.getUser());
        comment.setUser("Danesh");
        assertEquals("Danesh", comment.getUser());
    }

    public void testGetTime() throws Exception {
        assertNull(comment.getTime());
        Calendar calendar = Calendar.getInstance();
        comment.setTime(calendar);
        assertEquals(calendar, comment.getTime());
    }

    public void testGetResourcePath() throws Exception {
        assertNull(comment.getResourcePath());
        comment.setResourcePath("/_system/governance/trunk");
        assertEquals("/_system/governance/trunk", comment.getResourcePath());
    }

    public void testGetContent() throws Exception {
        assertNull(comment.getContent());
        comment.setContent("Content");
        assertEquals("Content", comment.getContent());
    }

    public void testGetDescription() throws Exception {
        assertNull(comment.getDescription());
        comment.setDescription("Description");
        assertEquals("Description", comment.getDescription());
    }

    public void testGetCommentPath() throws Exception {
        assertNull(comment.getCommentPath());
        comment.setCommentPath("/_system/governance/trunk");
        assertEquals("/_system/governance/trunk", comment.getCommentPath());
    }

    public void testGetMediaType() throws Exception {
        comment.setMediaType("application/xml");
        assertEquals("application/atom+xml", comment.getMediaType());
    }

    public void testGetAuthorUserName() throws Exception {
        assertNull(comment.getAuthorUserName());
        comment.setAuthorUserName("Danesh");
        assertEquals("Danesh", comment.getAuthorUserName());
    }

    public void testGetLastModified() throws Exception {
        assertNull(comment.getLastModified());
        Calendar calendar = Calendar.getInstance();
        comment.setLastModified(calendar);
        assertEquals(calendar, comment.getLastModified());
    }

    public void testGetCreatedTime() throws Exception {
        assertNull(comment.getCreatedTime());
        Calendar calendar = Calendar.getInstance();
        comment.setCreatedTime(calendar);
        assertEquals(calendar, comment.getCreatedTime());
    }
}