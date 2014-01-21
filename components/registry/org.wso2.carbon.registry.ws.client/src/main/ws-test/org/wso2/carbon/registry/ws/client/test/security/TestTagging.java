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

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.TaggedResourcePath;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

public class TestTagging extends SecurityTestSetup {

    public TestTagging(String text) {
        super(text);
    }

    public void testAddTagging() throws Exception {
        // add a resource
        Resource r1 = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("q1 content");
        r1.setContent(r1content);
        registry.put("/d11/r1", r1);

//        RemoteRegistry q2Registry = new RemoteRegistry(baseURL, "q2", "");
        Resource r2 = registry.newResource();
        byte[] r2content = RegistryUtils.encodeString("q2 content");
        r2.setContent(r2content);
        registry.put("/d11/r2", r2);

//        RemoteRegistry q3Registry = new RemoteRegistry(baseURL, "q3", "");
        Resource r3 = registry.newResource();
        byte[] r3content = RegistryUtils.encodeString("q3 content");
        r3.setContent(r3content);
        registry.put("/d11/r3", r3);

        registry.applyTag("/d11/r1", "jsp");
        registry.applyTag("/d11/r2", "jsp");
        registry.applyTag("/d11/r3", "java long tag");

        TaggedResourcePath[] paths = registry.getResourcePathsWithTag("jsp");
        boolean artifactFound = false;
        for (TaggedResourcePath path : paths) {
            //System.out.println("Available resource paths:" + path.getResourcePath());

            if (path.getResourcePath().equals("/d11/r1")) {
                assertEquals("Path are not matching", "/d11/r1", path.getResourcePath());
                artifactFound = true;
                //break;
            }
        }
        assertTrue("/d11/r1 is not tagged with the tag \"jsp\"", artifactFound);

        Tag[] tags = null;


        try {
            tags = registry.getTags("/d11/r1");
        } catch (Exception e) {
            fail("Failed to get tags for the resource /d11/r1");
        }

        boolean tagFound = false;
        for (Tag tag : tags) {
            if (tag.getTagName().equals("jsp")) {
                tagFound = true;
                break;
            }
        }
        assertTrue("tag 'jsp' is not associated with the artifact /d11/r1", tagFound);

        /* try {
           //registry.delete("/d11");
       } catch (Exception e) {
           fail("Failed to delete test resources.");
       }
        */

        registry.getResourcePathsWithTag("jsp");

//        assertEquals("Tag based search should not return paths of deleted resources.", paths2.length, 0);
    }

    public void testDuplicateTagging() throws Exception {
        Resource r1 = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("q1 content");
        r1.setContent(r1content);
        registry.put("/d12/r1", r1);

        registry.applyTag("/d12/r1", "tag1");
        registry.applyTag("/d12/r1", "tag2");

        Tag[] tags = registry.getTags("/d12/r1");

        boolean tagFound = false;
        for (Tag tag : tags) {
            if (tag.getTagName().equals("tag1")) {
                tagFound = true;
                //System.out.println(tags[i].getTagName());
                //System.out.println(tags[i].getCategory());
                //System.out.println(tags[i].getTagCount());

                break;

            }
        }
        assertTrue("tag 'tag1' is not associated with the artifact /d12/r1", tagFound);
    }

    public void testAddTaggingCollection() throws Exception {
        Collection r1 = registry.newCollection();
        registry.put("/d13/d14", r1);
        registry.applyTag("/d13/d14", "col_tag1");

        Tag[] tags = registry.getTags("/d13/d14");

        boolean tagFound = false;
        for (Tag tag : tags) {
            if (tag.getTagName().equals("col_tag1")) {
                tagFound = true;
                break;
            }
        }
        assertTrue("tag 'col_tag1' is not associated with the artifact /d13/d14", tagFound);
    }

    public void testEditTagging() throws Exception {
        Resource r1 = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("q1 content");
        r1.setContent(r1content);
        registry.put("/d14/d13/r1", r1);

        registry.applyTag("/d14/d13/r1", "tag1");
        registry.applyTag("/d14/d13/r1", "tag2");

        Tag[] tags = registry.getTags("/d14/d13/r1");

        boolean tagFound = false;
        for (Tag tag : tags) {
            if (tag.getTagName().equals("tag1")) {
                tagFound = true;
                //System.out.println(tag.getTagName());
                assertEquals("Tag names are not equals", "tag1", tag.getTagName());
                //System.out.println(tag.getCategory());
                assertEquals("Tag category not equals", 1, tag.getCategory());
                //System.out.println(tag.getTagCount());
                assertEquals("Tag count not equals", 1, (int) (tag.getTagCount()));
                //System.out.println(tags.length);
                assertEquals("Tag length not equals", 2, tags.length);

                tag.setTagName("tag1_updated");
                break;

            }
        }

        TaggedResourcePath[] paths = null;
        try {

            paths = registry.getResourcePathsWithTag("tag1");

        } catch (Exception e) {
            fail("Failed to get resources with tag 'tag1'");
        }
        boolean artifactFound = false;
        for (TaggedResourcePath path : paths) {
            if (path.getResourcePath().equals("/d14/d13/r1")) {
                // System.out.println(paths[1].getResourcePath());
                assertEquals("Path are not matching", "/d14/d13/r1", path.getResourcePath());
                //System.out.println(paths[1].getTagCount());
                assertEquals("Tag count not equals", 0, (int) (paths[0].getTagCount()));
//                System.out.println(paths[1].getTagCounts());
//                assertEquals("Tag count not equals",0,(paths[0].getTagCounts()));
                artifactFound = true;
                //break;
            }
        }
        assertTrue("/d11/r1 is not tagged with the tag \"jsp\"", artifactFound);
        assertTrue("tag 'col_tag1' is not associated with the artifact /d14/d13/r1", tagFound);
    }

    public void testRemoveResourceTagging() throws Exception {
        Resource r1 = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("q1 content");
        r1.setContent(r1content);
        registry.put("/d15/d14/r1", r1);

        registry.applyTag("/d15/d14/r1", "tag1");
        registry.applyTag("/d15/d14/r1", "tag2");

        Tag[] tags = registry.getTags("/d15/d14/r1");

        boolean tagFound = false;
        for (Tag tag : tags) {
            //System.out.println("Available tags:" + tags[i].getTagName());
            if (tag.getTagName().equals("tag1")) {
                tagFound = true;
                //System.out.println(tags[i].getTagName());
                //System.out.println(tags[i].getCategory());
                //System.out.println(tags[i].getTagCount());
                //System.out.println(tags.length);

                //break;

            }

        }

        assertTrue("tag 'tag1' is not associated with the artifact /d15/d14/r1", tagFound);

        /*remove tag goes here*/

        registry.removeTag("/d15/d14/r1", "tag1");

        TaggedResourcePath[] paths = registry.getResourcePathsWithTag("tag1");

        boolean artifactFound = false;
        for (TaggedResourcePath path : paths) {
            //System.out.println("tag1 Available at:" + paths[i].getResourcePath());
            if (path.getResourcePath().equals("/d15/d14/r1")) {
                //System.out.println(paths[i].getResourcePath());
                //System.out.println(paths[i].getTagCount());
                //System.out.println(paths[i].getTagCounts());
                artifactFound = true;
                //break;
            }
        }
        assertFalse("/d15/d14/r1 is not tagged with the tag \"tag1\"", artifactFound);
        //assertTrue("/d15/d14/r1 is not tagged with the tag \"tag1\"", artifactFound);
        assertTrue("tag 'tag1' is not associated with the artifact /d15/d14/r1", tagFound);
    }

    public void testRemoveCollectionTagging() throws Exception {
        CollectionImpl r1 = new CollectionImpl();
        r1.setAuthorUserName("Author q1 remove");
        registry.put("/d15/d14/d13/d12", r1);

        registry.applyTag("/d15/d14/d13", "tag1");
        registry.applyTag("/d15/d14/d13", "tag2");
        registry.applyTag("/d15/d14/d13", "tag3");

        Tag[] tags = registry.getTags("/d15/d14/d13");
        //System.out.println("getTagCount:" + tags[0].getTagCount());

        boolean tagFound = false;
        for (Tag tag : tags) {
            //System.out.println("Available tags:" + tags[i].getTagName());
            //System.out.println("getTagCount for:" + tags[i].getTagCount());
            if (tag.getTagName().equals("tag1")) {
                tagFound = true;
                //System.out.println("getTagName:" + tags[i].getTagName());
                //System.out.println("getCategory:" + tags[i].getCategory());
                //System.out.println("getTagCount:" + tags[i].getTagCount());
                //System.out.println("TagLength:" + tags.length);

                //break;

            }
        }

        assertTrue("tag 'tag1' is not associated with the artifact /d15/d14/d13", tagFound);

        /*remove tag goes here*/

        registry.removeTag("/d15/d14/d13", "tag1");

        TaggedResourcePath[] paths = registry.getResourcePathsWithTag("tag1");

        //System.out.println("Path tag counts:" + paths.length);
        boolean artifactFound = false;
        for (TaggedResourcePath path : paths) {
            //System.out.println("tag1 Available at:" + paths[i].getResourcePath());
            //System.out.println("getTagCounts:" + paths[i].getTagCounts());
            //System.out.println("getTagCount:" + paths[i].getTagCount());

            if (path.getResourcePath().equals("/d15/d14/d13")) {
                //System.out.println("getResourcePath:" + paths[i].getResourcePath());
                //System.out.println("getTagCount:" + paths[i].getTagCount());
                //System.out.println("getTagCounts:" + paths[i].getTagCounts());
                artifactFound = true;
                //break;
            }
        }
        assertFalse("/d15/d14/d13 is not tagged with the tag \"tag1\"", artifactFound);
        //assertTrue("/d15/d14/r1 is not tagged with the tag \"tag1\"", artifactFound);
        assertTrue("tag 'tag1' is not associated with the artifact /d15/d14/d13", tagFound);
    }

    public void testTagging() throws Exception {
        // add a resource
        Resource r1 = registry.newResource();
        byte[] r1content = RegistryUtils.encodeString("R1 content");
        r1.setContent(r1content);
        registry.put("/d11/r1", r1);

        Resource r2 = registry.newResource();
        byte[] r2content = RegistryUtils.encodeString("R2 content");
        r2.setContent(r2content);
        registry.put("/d11/r2", r2);

        Resource r3 = registry.newResource();
        byte[] r3content = RegistryUtils.encodeString("R3 content");
        r3.setContent(r3content);
        registry.put("/d11/r3", r3);

        registry.applyTag("/d11/r1", "JSP");
        registry.applyTag("/d11/r2", "jsp");
        registry.applyTag("/d11/r3", "jaVa");

        registry.applyTag("/d11/r1", "jsp");
        Tag[] r11Tags = registry.getTags("/d11/r1");
        assertEquals(1, r11Tags.length);

        TaggedResourcePath[] paths = registry.getResourcePathsWithTag("jsp");
        boolean artifactFound = false;
        for (TaggedResourcePath path : paths) {
            if (path.getResourcePath().equals("/d11/r1")) {
                artifactFound = true;
                break;
            }
        }
        assertTrue("/d11/r1 is not tagged with the tag \"jsp\"", artifactFound);

        Tag[] tags = registry.getTags("/d11/r1");

        boolean tagFound = false;
        for (Tag tag : tags) {
            if (tag.getTagName().equalsIgnoreCase("jsp")) {
                tagFound = true;
                break;
            }
        }
        assertTrue("tag 'jsp' is not associated with the artifact /d11/r1", tagFound);

        registry.delete("/d11");

        TaggedResourcePath[] paths2 = registry.getResourcePathsWithTag("jsp");

        assertEquals("Tag based search should not return paths of deleted resources.",
                paths2, null);
    }

}
