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

import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.util.ArrayList;
import java.util.List;

public class ResourceHandling extends SecurityTestSetup {

    public ResourceHandling(String text) {
        super(text);
    }

    public void testResourceCopy() throws Exception {

        try {
            String path = "/f95/f2/r1";
            String commentPath = path + RegistryConstants.URL_SEPARATOR + "comments";
            String new_path = "/f96/f2/r1";
            String commentPathNew = new_path + RegistryConstants.URL_SEPARATOR + "comments";
            Resource r1 = registry.newResource();
            r1.setDescription("This is a file to be renamed");
            byte[] r1content = RegistryUtils.encodeString("R2 content");
            r1.setContent(r1content);
            r1.setMediaType("txt");

            Comment c1 = new Comment();
            c1.setResourcePath(path);
            c1.setText("This is a test comment1");

            Comment c2 = new Comment();
            c2.setResourcePath(path);
            c2.setText("This is a test comment2");

            r1.setProperty("key1", "value1");
            r1.setProperty("key2", "value2");

            registry.put(path, r1);
            registry.addComment(path, c1);
            registry.addComment(path, c2);
            registry.applyTag(path, "tag1");
            registry.applyTag(path, "tag2");
            registry.applyTag(path, "tag3");
            registry.rateResource(path, 4);

            Resource r2 = registry.get(path);

            assertEquals("Properties are not equal", r1.getProperty("key1"), r2.getProperty("key1"));
            assertEquals("Properties are not equal", r1.getProperty("key2"), r2.getProperty("key2"));
            assertEquals("File content is not matching", RegistryUtils.decodeBytes((byte[]) r1.getContent()),
                    RegistryUtils.decodeBytes((byte[]) r2.getContent()));
            assertTrue(c1.getText() + " is not associated for resource" + path,
                    containsComment(commentPath, c1.getText()));
            assertTrue(c2.getText() + " is not associated for resource" + path,
                    containsComment(commentPath, c2.getText()));
            assertTrue("Tag1 is not exist", containsTag(path, "tag1"));
            assertTrue("Tag2 is not exist", containsTag(path, "tag2"));
            assertTrue("Tag3 is not exist", containsTag(path, "tag3"));

            float rating = registry.getAverageRating(path);
            assertEquals("Rating is not mathching", rating, (float) 4.0, (float) 0.01);
            assertEquals("Media type not exist", r1.getMediaType(), r2.getMediaType());
//            assertEquals("Authour name is not exist", r1.getAuthorUserName(), r2.getAuthorUserName());
            assertEquals("Description is not exist", r1.getDescription(), r2.getDescription());

            String new_path_returned;
            new_path_returned = registry.rename(path, new_path);

            assertEquals("New resource path is not equal", new_path, new_path_returned);

            /*get renamed resource details*/

            Resource r1Renamed = registry.get(new_path);

            assertEquals("File content is not matching", RegistryUtils.decodeBytes((byte[]) r2.getContent()),
                    RegistryUtils.decodeBytes((byte[]) r1Renamed.getContent()));
            assertEquals("Properties are not equal", r2.getProperty("key1"),
                    r1Renamed.getProperty("key1"));
            assertEquals("Properties are not equal", r2.getProperty("key2"),
                    r1Renamed.getProperty("key2"));
            assertTrue(c1.getText() + " is not associated for resource" + new_path,
                    containsComment(commentPathNew, c1.getText()));
            assertTrue(c2.getText() + " is not associated for resource" + new_path,
                    containsComment(commentPathNew, c2.getText()));
            assertTrue("Tag1 is not copied", containsTag(new_path, "tag1"));
            assertTrue("Tag2 is not copied", containsTag(new_path, "tag2"));
            assertTrue("Tag3 is not copied", containsTag(new_path, "tag3"));

            float rating1 = registry.getAverageRating(new_path);
            assertEquals("Rating is not copied", rating1, (float) 4.0, (float) 0.01);
            assertEquals("Media type not copied", r2.getMediaType(), r1Renamed.getMediaType());
            assertEquals("Authour Name is not copied", r2.getAuthorUserName(),
                    r1Renamed.getAuthorUserName());
            assertEquals("Description is not exist", r2.getDescription(), r1Renamed.getDescription());

        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }

    public void testCollectionCopy() throws Exception {

        try {

            String path = "/c9011/c1/c2";
            String commentPath = path + RegistryConstants.URL_SEPARATOR + "comments";
            String new_path = "/c9111/c1/c3";
            String commentPathNew = new_path + RegistryConstants.URL_SEPARATOR + "comments";
            Resource r1 = registry.newCollection();
            r1.setDescription("This is a file to be renamed");

            Comment c1 = new Comment();
            c1.setResourcePath(path);
            c1.setText("This is first test comment");

            Comment c2 = new Comment();
            c2.setResourcePath(path);
            c2.setText("This is secound test comment");

            r1.setProperty("key1", "value1");
            r1.setProperty("key2", "value2");

            registry.put(path, r1);
            registry.addComment(path, c1);
            registry.addComment(path, c2);
            registry.applyTag(path, "tag1");
            registry.applyTag(path, "tag2");
            registry.applyTag(path, "tag3");
            registry.rateResource(path, 4);

            Resource r2 = registry.get(path);

            assertEquals("Properties are not equal", r1.getProperty("key1"),
                    r2.getProperty("key1"));
            assertEquals("Properties are not equal", r1.getProperty("key2"),
                    r2.getProperty("key2"));
            assertTrue(c1.getText() + " is not associated for resource" + path,
                    containsComment(commentPath, c1.getText()));
            assertTrue(c2.getText() + " is not associated for resource" + path,
                    containsComment(commentPath, c2.getText()));
            assertTrue("Tag1 is not copied", containsTag(path, "tag1"));
            assertTrue("Tag2 is not copied", containsTag(path, "tag2"));
            assertTrue("Tag3 is not copied", containsTag(path, "tag3"));

            float rating = registry.getAverageRating(path);
            assertEquals("Rating is not mathching", rating, (float) 4.0, (float) 0.01);
//            assertEquals("Authour name is not exist", r1.getAuthorUserName(), r2.getAuthorUserName());

            String new_path_returned;
            new_path_returned = registry.rename(path, new_path);

            assertEquals("New resource path is not equal", new_path, new_path_returned);

            /*get renamed resource details*/

            Resource r1Renamed = registry.get(new_path);

            assertEquals("Properties are not equal", r2.getProperty("key1"),
                    r1Renamed.getProperty("key1"));
            assertEquals("Properties are not equal", r2.getProperty("key2"),
                    r1Renamed.getProperty("key2"));
            assertTrue(c1.getText() + " is not associated for resource" + new_path,
                    containsComment(commentPathNew, c1.getText()));
            assertTrue(c2.getText() + " is not associated for resource" + new_path,
                    containsComment(commentPathNew, c2.getText()));
            assertTrue("Tag1 is not copied", containsTag(new_path, "tag1"));
            assertTrue("Tag2 is not copied", containsTag(new_path, "tag2"));
            assertTrue("Tag3 is not copied", containsTag(new_path, "tag3"));

            float rating1 = registry.getAverageRating(new_path);
            assertEquals("Rating is not copied", rating1, (float) 4.0, (float) 0.01);

//            assertEquals("Author Name is not copied", r1.getAuthorUserName(), r2.getAuthorUserName());

        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }

    public void testGetResourceoperation() throws Exception {

        Resource r2 = registry.newResource();
        String path = "/testk/testa/derby.log";
        r2.setContent(RegistryUtils.encodeString("this is the content"));
        r2.setDescription("this is test desc this is test desc this is test desc this is test desc this is test desc " +
                "this is test desc this is test desc this is test desc this is test descthis is test desc ");
        r2.setMediaType("plain/text");
        registry.put(path, r2);

        r2.discard();

        Resource r3 = registry.newResource();

//        assertEquals("Author names are not Equal", "admin", r3.getAuthorUserName());

        r3 = registry.get(path);

        assertEquals("Author User names are not Equal", "admin", r3.getAuthorUserName());
        assertNotNull("Created time is null", r3.getCreatedTime());
        assertEquals("Author User names are not Equal", "admin", r3.getAuthorUserName());
        assertEquals("Description is not Equal", "this is test desc this is test desc this is test desc this is test" +
                " desc this is test desc this is test desc this is test desc this is test desc this is test descthis is " +
                "test desc ", r3.getDescription());
        assertNotNull("Get Id is null", r3.getId());
        assertNotNull("LastModifiedDate is null", r3.getLastModified());
        assertEquals("Last Updated names are not Equal", "admin", r3.getLastUpdaterUserName());
        //System.out.println(r3.getMediaType());
        assertEquals("Media Type is not equal", "plain/text", r3.getMediaType());
        assertEquals("parent Path is not equal", "/testk/testa", r3.getParentPath());
        assertEquals("parent Path is not equal", path, r3.getPath());
        assertEquals("Get stated wrong", 0, r3.getState());

        String st = r3.getPermanentPath();
//        assertTrue("Permenent path contanin the string" + path + " verion", st.contains("/testk/testa/derby.log;version:"));
    }

    public void testGetCollectionoperation() throws Exception {

        Resource r2 = registry.newCollection();
        String path = "/testk2/testa/testc";
        r2.setDescription("this is test desc");
        r2.setProperty("test2", "value2");
        registry.put(path, r2);

        r2.discard();

        Resource r3 = registry.get(path);

//        assertEquals("Author names are not Equal", "admin", r3.getAuthorUserName());
//        assertEquals("Author User names are not Equal", "admin", r3.getAuthorUserName());
        // System.out.println(r3.getCreatedTime());
        //assertNotNull("Created time is null", r3.getCreatedTime());
//        assertEquals("Author User names are not Equal", "admin", r3.getAuthorUserName());
        //System.out.println("Desc" + r3.getDescription());
        //assertEquals("Description is not Equal", "this is test desc", r3.getDescription());
        assertNotNull("Get Id is null", r3.getId());
        assertNotNull("LastModifiedDate is null", r3.getLastModified());
        assertEquals("Last Updated names are not Equal", "admin", r3.getLastUpdaterUserName());
        //System.out.println("Media Type:" + r3.getMediaType());
        //assertEquals("Media Type is not equal","unknown",r3.getMediaType());
        assertEquals("parent Path is not equal", "/testk2/testa", r3.getParentPath());
        assertEquals("Get stated wrong", 0, r3.getState());

        registry.createVersion(path);

//         System.out.println(r3.getParentPath());
//      System.out.println(r3.getPath());

        assertEquals("Permenent path doesn't contanin the string", "/testk2/testa", r3.getParentPath());
        assertEquals("Path doesn't contanin the string", path, r3.getPath());

//        String st = r3.getPermanentPath();
//        assertTrue("Permenent path contanin the string" + path +" verion", st.contains("/testk2/testa/testc;version:"));


    }


    private boolean containsComment(String pathValue, String commentText) throws Exception {

        Comment[] commentsArray = null;
        List commentTexts = new ArrayList();

        try {
            Resource commentsResource = registry.get(pathValue);
            commentsArray = (Comment[]) commentsResource.getContent();
            for (Comment comment : commentsArray) {
                Resource commentResource = registry.get(comment.getPath());
                commentTexts.add(commentResource.getContent());
            }
        } catch (RegistryException e) {
            e.printStackTrace();
        }

        boolean found = false;
        if (commentTexts.contains(commentText)) {
            found = true;
        }

        return found;
    }

    private boolean containsTag(String tagPath, String tagText) throws Exception {

        Tag[] tags = null;

        try {
            tags = registry.getTags(tagPath);
        } catch (RegistryException e) {
            e.printStackTrace();
        }

        boolean tagFound = false;
        for (int i = 0; i < tags.length; i++) {
            if (tags[i].getTagName().equals(tagText)) {
                tagFound = true;
                break;
            }
        }

        return tagFound;
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
