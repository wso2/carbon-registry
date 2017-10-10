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
import org.wso2.carbon.registry.common.beans.utils.Comment;


public class CommentBeanTest extends TestCase {


    private CommentBean commentBean;

    @Override
    protected void setUp() throws Exception {
        commentBean = new CommentBean();
        super.setUp();
    }


    public void testGetComments() throws Exception {
        assertNull(commentBean.getComments());
        Comment comment = new Comment();
        Comment[] comments = new Comment[1];
        comments[0] = comment;
        commentBean.setComments(comments);
        assertEquals(1, commentBean.getComments().length);
        assertEquals(comments, commentBean.getComments());
    }

    public void testGetErrorMessage() throws Exception {
        assertNull(commentBean.getErrorMessage());
        commentBean.setErrorMessage("Error Message");
        assertEquals("Error Message", commentBean.getErrorMessage());
    }

    public void testIsPutAllowed() throws Exception {
        assertFalse(commentBean.isPutAllowed());

        commentBean.setPutAllowed(false);
        assertFalse(commentBean.isPutAllowed());

        commentBean.setPutAllowed(true);
        assertTrue(commentBean.isPutAllowed());
    }

    public void testIsVersionView() throws Exception {
        assertFalse(commentBean.isVersionView());

        commentBean.setVersionView(false);
        assertFalse(commentBean.isVersionView());

        commentBean.setVersionView(true);
        assertTrue(commentBean.isVersionView());
    }

    public void testIsLoggedIn() throws Exception {
        assertFalse(commentBean.isLoggedIn());

        commentBean.setLoggedIn(false);
        assertFalse(commentBean.isLoggedIn());

        commentBean.setLoggedIn(true);
        assertTrue(commentBean.isLoggedIn());
    }

    public void testGetPathWithVersion() throws Exception {
        assertNull(commentBean.getPathWithVersion());

        commentBean.setPathWithVersion("/_system/governance/trunk:version=1");

        assertEquals("/_system/governance/trunk:version=1", commentBean.getPathWithVersion());

    }
}