/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.registry.ws.client.test.security;

import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.util.ArrayList;
import java.util.List;

public class CommentTest extends SecurityTestSetup {

    public CommentTest(String text) {
        super(text);
    }

    public void testAddComment() throws Exception {
        Resource r1 = registry.newResource();
        String path = "/d112/r3";
        byte[] r1content = RegistryUtils.encodeString("R1 content");
        r1.setContent(r1content);
        registry.put(path, r1);

        String comment1 = "this is qa comment 4";
        String comment2 = "this is qa comment 5";
        Comment c1 = new Comment();
        c1.setResourcePath(path);
        c1.setText("This is default comment");
        c1.setUser("admin1");

        registry.addComment(path, c1);
        registry.addComment(path, new Comment(comment1));
        registry.addComment(path, new Comment(comment2));

        Comment[] comments = registry.getComments(path);

        boolean commentFound = false;

        for (Comment comment : comments) {
            if (comment.getText().equals(comment1)) {
                commentFound = true;
                //System.out.println(comment.getPath());
                assertEquals(comment1, comment.getText());
                assertEquals("admin", comment.getUser());
                assertEquals(path, comment.getResourcePath());
                //System.out.println(comment.getPath());
                //break;
            }

            if (comment.getText().equals(comment2)) {
                commentFound = true;
                assertEquals(comment2, comment.getText());
                assertEquals("admin", comment.getUser());
                assertEquals(path, comment.getResourcePath());
                //break;
            }

            if (comment.getText().equals("This is default comment")) {
                commentFound = true;
                assertEquals("This is default comment", comment.getText());
                assertEquals("admin", comment.getUser());
                //break;
            }
        }


        assertTrue("comment '" + comment1 +
                " is not associated with the artifact /d1/r3", commentFound);

        Resource commentsResource = registry.get("/d112/r3;comments");
        assertTrue("Comment collection resource should be a directory.",
                commentsResource instanceof Collection);
        comments = (Comment[]) commentsResource.getContent();

        List commentTexts = new ArrayList();
        for (Comment comment : comments) {
            Resource commentResource = registry.get(comment.getPath());
            commentTexts.add(commentResource.getContent());
        }

        assertTrue(comment1 + " is not associated for resource /d112/r3.",
                commentTexts.contains(comment1));
        assertTrue(comment2 + " is not associated for resource /d112/r3.",
                commentTexts.contains(comment2));
    }

    public void testAddCommentToResource() throws Exception {
        Resource r1 = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("R1 content");
        r1.setContent(r1content);
        registry.put("/d1/r3", r1);

        String comment1 = "this is qa comment 4";
        String comment2 = "this is qa comment 5";
        Comment c1 = new Comment();
        c1.setResourcePath("/d1/r3");
        c1.setText("This is default comment");
        c1.setUser("admin");

        registry.addComment("/d1/r3", c1);
        registry.addComment("/d1/r3", new Comment(comment1));
        registry.addComment("/d1/r3", new Comment(comment2));

        Comment[] comments = registry.getComments("/d1/r3");

        boolean commentFound = false;

        for (Comment comment : comments) {
            if (comment.getText().equals(comment1)) {
                commentFound = true;

//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
                //break;
            }

            if (comment.getText().equals(comment2)) {
                commentFound = true;
//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
                //break;
            }

            if (comment.getText().equals("This is default comment")) {
                commentFound = true;
//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
                //break;
            }
        }


        assertTrue("comment '" + comment1 +
                " is not associated with the artifact /d1/r3", commentFound);

        Resource commentsResource = registry.get("/d1/r3;comments");
        assertTrue("Comment collection resource should be a directory.",
                commentsResource instanceof Collection);
        comments = (Comment[]) commentsResource.getContent();

        List commentTexts = new ArrayList();
        for (Comment comment : comments) {
            Resource commentResource = registry.get(comment.getPath());
            commentTexts.add(commentResource.getContent());
        }

        assertTrue(comment1 + " is not associated for resource /d1/r3.",
                commentTexts.contains(comment1));
        assertTrue(comment2 + " is not associated for resource /d1/r3.",
                commentTexts.contains(comment2));

        /*try {
            //registry.delete("/d12");
        } catch (RegistryException e) {
            fail("Failed to delete test resources.");
        } */
    }

    public void testAddCommentToCollection() throws Exception {

        Resource r1 = registry.newCollection();
        r1.setDescription("this is a collection to add comment");

        registry.put("/d11/d12", r1);

        String comment1 = "this is qa comment 1 for collection d12";
        String comment2 = "this is qa comment 2 for collection d12";

        Comment c1 = new Comment();
        c1.setResourcePath("/d11/d12");
        c1.setText("This is default comment for d12");
        c1.setUser("admin");

        try {
            registry.addComment("/d11/d12", c1);
            registry.addComment("/d11/d12", new Comment(comment1));
            registry.addComment("/d11/d12", new Comment(comment2));
        } catch (RegistryException e) {
            fail("Valid commenting for resources scenario failed");
        }

        Comment[] comments = null;

        try {

            comments = registry.getComments("/d11/d12");
        } catch (RegistryException e) {
            fail("Failed to get comments for the resource /d11/d12");
        }

        boolean commentFound = false;

        for (Comment comment : comments) {
            if (comment.getText().equals(comment1)) {
                commentFound = true;

//       //System.out.println(comment.getText());
//       //System.out.println(comment.getResourcePath());
//       //System.out.println(comment.getUser());
//       //System.out.println(comment.getTime());
                //break;
            }

            if (comment.getText().equals(comment2)) {
                commentFound = true;
//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
                //break;
            }

            if (comment.getText().equals(c1.getText())) {
                commentFound = true;
//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
                //break;
            }
        }

        assertTrue("comment '" + comment1 +
                " is not associated with the artifact /d11/d12", commentFound);

        try {

            Resource commentsResource = registry.get("/d11/d12;comments");
            assertTrue("Comment collection resource should be a directory.",
                    commentsResource instanceof Collection);
            comments = (Comment[]) commentsResource.getContent();

            List commentTexts = new ArrayList();
            for (Comment comment : comments) {
                Resource commentResource = registry.get(comment.getPath());
                commentTexts.add(commentResource.getContent());
            }

            assertTrue(comment1 + " is not associated for resource /d11/d12.",
                    commentTexts.contains(comment1));
            assertTrue(comment2 + " is not associated for resource /d11/d12.",
                    commentTexts.contains(comment2));

        } catch (RegistryException e) {
            e.printStackTrace();
            fail("Failed to get comments form URL: /d11/d12;comments");
        }

/*try {
   //registry.delete("/d12");
} catch (RegistryException e) {
   fail("Failed to delete test resources.");
} */


    }

    public void testAddCommenttoRoot() throws Exception {

        String comment1 = "this is qa comment 1 for root";
        String comment2 = "this is qa comment 2 for root";


        Comment c1 = new Comment();
        c1.setResourcePath("/");
        c1.setText("This is default comment for root");
        c1.setUser("admin");

        try {
            registry.addComment("/", c1);
            registry.addComment("/", new Comment(comment1));
            registry.addComment("/", new Comment(comment2));
        } catch (RegistryException e) {
            fail("Valid commenting for resources scenario failed");
        }

        Comment[] comments = null;

        try {

            comments = registry.getComments("/");
        } catch (RegistryException e) {
            fail("Failed to get comments for the resource /");
        }

        boolean commentFound = false;

        for (Comment comment : comments) {
            if (comment.getText().equals(comment1)) {
                commentFound = true;

//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
//                //System.out.println("\n");
                //break;
            }

            if (comment.getText().equals(comment2)) {
                commentFound = true;
//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
//                //System.out.println("\n");
                //break;
            }

            if (comment.getText().equals(c1.getText())) {
                commentFound = true;
//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
//                //System.out.println("\n");
                //break;
            }
        }

        assertTrue("comment '" + comment1 +
                " is not associated with the artifact /", commentFound);

        try {

            Resource commentsResource = registry.get("/;comments");
            assertTrue("Comment collection resource should be a directory.",
                    commentsResource instanceof Collection);
            comments = (Comment[]) commentsResource.getContent();

            List commentTexts = new ArrayList();
            for (Comment comment : comments) {
                Resource commentResource = registry.get(comment.getPath());
                commentTexts.add(commentResource.getContent());
            }

            assertTrue(comment1 + " is not associated for resource /.",
                    commentTexts.contains(comment1));
            assertTrue(comment2 + " is not associated for resource /.",
                    commentTexts.contains(comment2));

        } catch (RegistryException e) {
            fail("Failed to get comments form URL: /;comments");
        }

/*try {
   //registry.delete("/d12");
} catch (RegistryException e) {
   fail("Failed to delete test resources.");
} */

    }

    public void testEditComment() throws Exception {
        Resource r1 = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("R1 content");
        r1.setContent(r1content);
        r1.setDescription("this is a resource to edit comment");
        registry.put("/c101/c11/r1", r1);

        Comment c1 = new Comment();
        c1.setResourcePath("/c10/c11/r1");
        c1.setText("This is default comment ");
        c1.setUser("admin");

        String commentPath = registry.addComment("/c101/c11/r1", c1);

        Comment[] comments = registry.getComments("/c101/c11/r1");

        boolean commentFound = false;

        for (Comment comment : comments) {
            if (comment.getText().equals(c1.getText())) {
                commentFound = true;
//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
//                //System.out.println("\n");
                //break;
            }
        }

        assertTrue("comment:" + c1.getText() +
                " is not associated with the artifact /c101/c11/r1", commentFound);

        try {

            Resource commentsResource = registry.get("/c101/c11/r1;comments");

            assertTrue("Comment resource should be a directory.",
                    commentsResource instanceof Collection);
            comments = (Comment[]) commentsResource.getContent();

            List commentTexts = new ArrayList();
            for (Comment comment : comments) {
                Resource commentResource = registry.get(comment.getPath());
                commentTexts.add(commentResource.getContent());
            }

            assertTrue(c1.getText() + " is not associated for resource /c101/c11/r1.",
                    commentTexts.contains(c1.getText()));
            registry.editComment(comments[0].getPath(), "This is the edited comment");
            comments = registry.getComments("/c101/c11/r1");
//            System.out.println(comments);
            Resource resource = registry.get(comments[0].getPath());
            assertEquals("This is the edited comment", resource.getContent());
        } catch (RegistryException e) {
            e.printStackTrace();
            fail("Failed to get comments form URL:/c101/c11/r1;comments");
        }

        /*Edit comment goes here*/
        String editedCommentString = "This is the edited comment";
        registry.editComment(commentPath, editedCommentString);
        Comment[] comments1 = registry.getComments("/c101/c11/r1");

        boolean editedCommentFound = false;
        boolean defaultCommentFound = true;

        for (Comment comment : comments1) {
            if (comment.getText().equals(editedCommentString)) {
                editedCommentFound = true;
            } else if (comment.getText().equals(c1.getText())) {
                defaultCommentFound = false;
//                //System.out.println(comment.getText());
//                //System.out.println(comment.getResourcePath());
//                //System.out.println(comment.getUser());
//                //System.out.println(comment.getTime());
//                //System.out.println("\n");
                //break;
            }
        }
        assertTrue("comment:" + editedCommentString +
                " is not associated with the artifact /c101/c11/r1", editedCommentFound);
        
    }

    public void testCommentDelete() throws Exception {
        String r1Path = "/c1d1/c1";
        Collection r1 = registry.newCollection();
        registry.put(r1Path, r1);

        String c1Path = registry.addComment(r1Path, new Comment("test comment1"));
        registry.addComment(r1Path, new Comment("test comment2"));

        Comment[] comments1 = registry.getComments(r1Path);

        assertEquals("There should be two comments.", comments1.length, 2);

        String[] cTexts1 = {comments1[0].getText(), comments1[1].getText()};

        assertTrue("comment is missing", containsString(cTexts1, "test comment1"));
        assertTrue("comment is missing", containsString(cTexts1, "test comment2"));

        registry.delete(c1Path);

        Comment[] comments2 = registry.getComments(r1Path);

        assertEquals("There should be one comment.", 1, comments2.length);

        String[] cTexts2 = {comments2[0].getText()};

        assertTrue("comment is missing", containsString(cTexts2, "test comment2"));
        assertTrue("deleted comment still exists", !containsString(cTexts2, "test comment1"));
    }

    private boolean containsString(String[] array, String value) {

        boolean found = false;
        for (String anArray : array) {
            if (anArray.startsWith(value)) {
                found = true;
                break;
            }
        }

        return found;
    }
}
