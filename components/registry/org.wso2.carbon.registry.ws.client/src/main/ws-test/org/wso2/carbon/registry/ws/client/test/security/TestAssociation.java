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

import junit.framework.Test;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.utils.RegistryUtils;


import java.util.List;


public class TestAssociation extends SecurityTestSetup {

    public TestAssociation(String text) {
        super(text);
    }

    public void testAddAssociationToResource() throws Exception {

        Resource r2 = registry.newResource();
        String path = "/testk12/testa/testbsp/test.txt";
        r2.setContent(RegistryUtils.encodeString("this is the content"));
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");

        registry.put(path, r2);
        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");


        assertTrue("association Destination path not exist", associationPathExists(path, "/vtr2121/test"));
        assertTrue("association Destination path not exist", associationPathExists(path, "/vtr2122/test"));
        assertTrue("association Destination path not exist", associationPathExists(path, "/vtr2123/test"));


        assertTrue("association Type not exist", associationTypeExists(path, "testasstype1"));
        assertTrue("association Type not exist", associationTypeExists(path, "testasstype2"));
        assertTrue("association Type not exist", associationTypeExists(path, "testasstype3"));

        assertTrue("association Source path not exist", associationSourcepathExists(path, path));
        assertTrue("association Source path not exist", associationSourcepathExists(path, path));
        assertTrue("association Source path not exist", associationSourcepathExists(path, path));

    }

    public void testAddAssociationToCollection() throws Exception {

        Resource r2 = registry.newCollection();
        String path = "/assocol1/assocol2/assoclo3";
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");

        registry.put(path, r2);
        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");


        assertTrue("association Destination path not exist", associationPathExists(path, "/vtr2121/test"));
        assertTrue("association Destination path not exist", associationPathExists(path, "/vtr2122/test"));
        assertTrue("association Destination path not exist", associationPathExists(path, "/vtr2123/test"));


        assertTrue("association Type not exist", associationTypeExists(path, "testasstype1"));
        assertTrue("association Type not exist", associationTypeExists(path, "testasstype2"));
        assertTrue("association Type not exist", associationTypeExists(path, "testasstype3"));

        assertTrue("association Source path not exist", associationSourcepathExists(path, path));
        assertTrue("association Source path not exist", associationSourcepathExists(path, path));
        assertTrue("association Source path not exist", associationSourcepathExists(path, path));

    }

    public void testAddAssociationToRoot() throws Exception {

        Resource r2 = registry.newCollection();

        String path = "/";

        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");


        assertTrue("association Destination path not exist", associationPathExists(path, "/vtr2121/test"));
        assertTrue("association Destination path not exist", associationPathExists(path, "/vtr2122/test"));
        assertTrue("association Destination path not exist", associationPathExists(path, "/vtr2123/test"));


        assertTrue("association Type not exist", associationTypeExists(path, "testasstype1"));
        assertTrue("association Type not exist", associationTypeExists(path, "testasstype2"));
        assertTrue("association Type not exist", associationTypeExists(path, "testasstype3"));

        assertTrue("association Source path not exist", associationSourcepathExists(path, path));
        assertTrue("association Source path not exist", associationSourcepathExists(path, path));
        assertTrue("association Source path not exist", associationSourcepathExists(path, path));

    }

    public void testGetResourceAssociation() throws Exception {

        Resource r2 = registry.newResource();
        String path = "/testk1234/testa/testbsp/test.txt";
        r2.setContent(RegistryUtils.encodeString("this is the content"));
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");

        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");


        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"));
        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"));
        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"));


        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype1"));
        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype2"));
        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype3"));

        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype1"));
        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype2"));
        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype3"));

    }

    public void testGetCollectionAssociation() throws Exception {

        Resource r2 = registry.newCollection();
        String path = "/getcol1/getcol2/getcol3";
        r2.setDescription("this is test desc");
        r2.setProperty("test2", "value2");

        registry.put(path, r2);
        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");

        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"));
        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"));
        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"));


        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype1"));
        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype2"));
        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype3"));

        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype1"));
        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype2"));
        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype3"));

    }

//    public void testGetRootAssociation() throws Exception {
//
//        Resource r2 = registry.newCollection();
//        String path = "/";
//        r2.setDescription("this is test desc");
//        r2.setProperty("test2", "value2");
//
//        registry.put(path, r2);
//        registry.addAssociation(path, "/vtr21211/test", "testasstype1");
//        registry.addAssociation(path, "/vtr21221/test", "testasstype2");
//        registry.addAssociation(path, "/vtr21231/test", "testasstype3");
//
//        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype1", "/vtr21211/test"));
//        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype2", "/vtr21221/test"));
//        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype3", "/vtr21231/test"));
//
//        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype1"));
//        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype2"));
//        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype3"));
//
//        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype1"));
//        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype2"));
//        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype3"));
//
//    }

    public void testRemoveResourceAssociation() throws Exception {

        Resource r2 = registry.newResource();
        String path = "/testk123456/testa/testbsp/test.txt";
        r2.setContent(RegistryUtils.encodeString("this is the content"));
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");

        registry.put(path, r2);
        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");


        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"));
        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"));
        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"));


        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype1"));
        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype2"));
        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype3"));

        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype1"));
        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype2"));
        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype3"));

        registry.removeAssociation(path, "/vtr2121/test", "testasstype1");
        registry.removeAssociation(path, "/vtr2122/test", "testasstype2");
        registry.removeAssociation(path, "/vtr2123/test", "testasstype3");


        assertFalse("association Destination path exists", getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"));
        assertFalse("association Destination path exists", getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"));
        assertFalse("association Destination path exists", getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"));

        assertFalse("association Type not exist", getAssocitionbyType(path, "testasstype1"));
        assertFalse("association Type not exist", getAssocitionbyType(path, "testasstype2"));
        assertFalse("association Type not exist", getAssocitionbyType(path, "testasstype3"));

        assertFalse("association Source path not exist", getAssocitionbySourceByType(path, "testasstype1"));
        assertFalse("association Source path not exist", getAssocitionbySourceByType(path, "testasstype2"));
        assertFalse("association Source path not exist", getAssocitionbySourceByType(path, "testasstype3"));
    }

    public void testRemoveCollectionAssociation() throws Exception {

        Resource r2 = registry.newCollection();
        String path = "/assoColremove1/assoColremove2/assoColremove3";
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");

        registry.put(path, r2);
        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");


        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"));
        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"));
        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"));


        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype1"));
        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype2"));
        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype3"));

        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype1"));
        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype2"));
        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype3"));

        registry.removeAssociation(path, "/vtr2121/test", "testasstype1");
        registry.removeAssociation(path, "/vtr2122/test", "testasstype2");
        registry.removeAssociation(path, "/vtr2123/test", "testasstype3");


        assertFalse("association Destination path exists", getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"));
        assertFalse("association Destination path exists", getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"));
        assertFalse("association Destination path exists", getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"));

        assertFalse("association Type not exist", getAssocitionbyType(path, "testasstype1"));
        assertFalse("association Type not exist", getAssocitionbyType(path, "testasstype2"));
        assertFalse("association Type not exist", getAssocitionbyType(path, "testasstype3"));

        assertFalse("association Source path not exist", getAssocitionbySourceByType(path, "testasstype1"));
        assertFalse("association Source path not exist", getAssocitionbySourceByType(path, "testasstype2"));
        assertFalse("association Source path not exist", getAssocitionbySourceByType(path, "testasstype3"));

    }

//    public void testRemoveRootAssociation() throws Exception {
//
//        Resource r2 = registry.newCollection();
//        String path = "/";
//        r2.setDescription("this is test desc");
//        r2.setMediaType("plain/text");
//        r2.setProperty("test2", "value2");
//
//        registry.put(path, r2);
//        registry.addAssociation(path, "/vtr21212/test", "testasstype11");
//        registry.addAssociation(path, "/vtr21222/test", "testasstype21");
//        registry.addAssociation(path, "/vtr21232/test", "testasstype31");
//
//
//        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype11", "/vtr21212/test"));
//        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype21", "/vtr21222/test"));
//        assertTrue("association Destination path not exist", getAssocitionbyDestinationByType(path, "testasstype31", "/vtr21232/test"));
//
//
//        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype11"));
//        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype21"));
//        assertTrue("association Type not exist", getAssocitionbyType(path, "testasstype31"));
//
//        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype11"));
//        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype21"));
//        assertTrue("association Source path not exist", getAssocitionbySourceByType(path, "testasstype31"));
//
//        registry.removeAssociation(path, "/vtr21212/test", "testasstype11");
//        registry.removeAssociation(path, "/vtr21222/test", "testasstype21");
//        registry.removeAssociation(path, "/vtr21232/test", "testasstype31");
//
//
//        assertFalse("association Destination path exists", getAssocitionbyDestinationByType(path, "testasstype11", "/vtr21212/test"));
//        assertFalse("association Destination path exists", getAssocitionbyDestinationByType(path, "testasstype21", "/vtr21222/test"));
//        assertFalse("association Destination path exists", getAssocitionbyDestinationByType(path, "testasstype31", "/vtr21232/test"));
//
//        assertFalse("association Type not exist", getAssocitionbyType(path, "testasstype11"));
//        assertFalse("association Type not exist", getAssocitionbyType(path, "testasstype21"));
//        assertFalse("association Type not exist", getAssocitionbyType(path, "testasstype31"));
//
//        assertFalse("association Source path not exist", getAssocitionbySourceByType(path, "testasstype11"));
//        assertFalse("association Source path not exist", getAssocitionbySourceByType(path, "testasstype21"));
//        assertFalse("association Source path not exist", getAssocitionbySourceByType(path, "testasstype31"));
//
//    }

    /*  public void testRemoveCollectionAssociationwithSpaces () throws Exception {

        Resource r2 = registry.newCollection();
        String path = "/assoColremove11123/assoColremove21/assoColremove31";
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2","value2");

        registry.put(path, r2);
        registry.addAssociation(path,"/vtr2121 test/test test","testasstype1 space");
        registry.addAssociation(path,"/vtr2122 test/test test","testasstype2 space");
        registry.addAssociation(path,"/vtr2123 test/test test","testasstype3 space");

        assertFalse("association Type not exist" ,getAssocitionbyType(path , "testasstype1 space"));
        assertTrue("association Destination path not exist" ,getAssocitionbyDestinationByType(path , "testasstype1 space", "/vtr2121 test/test test" ));
        assertTrue("association Destination path not exist" ,getAssocitionbyDestinationByType(path , "testasstype2 space", "/vtr2122 test/test test" ));
        assertTrue("association Destination path not exist" ,getAssocitionbyDestinationByType(path , "testasstype3 space", "/vtr2123 test/test test" ));

    assertTrue("association Destination path not exist" ,associationPathExists(path,"/vtr2121 test/test test"));
    assertTrue("association Destination path not exist" ,associationPathExists(path,"/vtr2122 test/test test"));
    assertTrue("association Destination path not exist" ,associationPathExists(path,"/vtr2123 test/test test"));


    assertTrue("association Type not exist" ,associationTypeExists(path,"testasstype1 space1"));
    assertTrue("association Type not exist" ,associationTypeExists(path,"testasstype1 space2"));
    assertTrue("association Type not exist" ,associationTypeExists(path,"testasstype1 space3"));

    assertTrue("association Source path not exist" ,associationSourcepathExists(path, path));
    assertTrue("association Source path not exist" ,associationSourcepathExists(path, path));
    assertTrue("association Source path not exist" ,associationSourcepathExists(path, path));


        assertTrue("association Type not exist" ,getAssocitionbyType(path , "testasstype1 space"));
        assertTrue("association Type not exist" ,getAssocitionbyType(path , "testasstype2 space"));
        assertTrue("association Type not exist" ,getAssocitionbyType(path , "testasstype3 space"));

        assertTrue("association Source path not exist" ,getAssocitionbySourceByType(path , "testasstype1 space"));
        assertTrue("association Source path not exist" ,getAssocitionbySourceByType(path , "testasstype2 space"));
        assertTrue("association Source path not exist" ,getAssocitionbySourceByType(path , "testasstype3 space"));

        registry.removeAssociation(path, "/vtr2121/test", "testasstype1 space");
        registry.removeAssociation(path, "/vtr2122/test", "testasstype2 space");
        registry.removeAssociation(path, "/vtr2123/test", "testasstype3 space");


        assertFalse("association Destination path exists" ,getAssocitionbyDestinationByType(path , "testasstype1 space", "/vtr2121 test/test test" ));
        assertFalse("association Destination path exists" ,getAssocitionbyDestinationByType(path , "testasstype2 space", "/vtr2122 test/test test" ));
        assertFalse("association Destination path exists" ,getAssocitionbyDestinationByType(path , "testasstype3 space", "/vtr2123 test/test test" ));

        assertFalse("association Type not exist" ,getAssocitionbyType(path , "testasstype1 space"));
        assertFalse("association Type not exist" ,getAssocitionbyType(path , "testasstype2 space"));
        assertFalse("association Type not exist" ,getAssocitionbyType(path , "testasstype3 space"));

        assertFalse("association Source path not exist" ,getAssocitionbySourceByType(path , "testasstype1 space"));
        assertFalse("association Source path not exist" ,getAssocitionbySourceByType(path , "testasstype2 space"));
        assertFalse("association Source path not exist" ,getAssocitionbySourceByType(path , "testasstype3 space"));

    }*/

    public static boolean resourceExists(RemoteRegistry registry, String fileName) throws Exception {
        boolean value = registry.resourceExists(fileName);
        return value;
    }

    public boolean associationPathExists(String path, String assoPath)
            throws Exception {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;

        for (int i = 0; i < association.length; i++) {
            //System.out.println(association[i].getDestinationPath());
            if (assoPath.equals(association[i].getDestinationPath()))
                value = true;
        }


        return value;
    }

    public boolean associationTypeExists(String path, String assoType) throws Exception {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;

        for (int i = 0; i < association.length; i++) {
            association[i].getAssociationType();
            if (assoType.equals(association[i].getAssociationType()))
                value = true;
        }


        return value;
    }

    public boolean associationSourcepathExists(String path, String sourcePath) throws Exception {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;

        for (int i = 0; i < association.length; i++) {
            association[i].getAssociationType();
            if (sourcePath.equals(association[i].getSourcePath()))
                value = true;
        }

        return value;
    }

    public boolean getAssocitionbyType(String path, String type) throws Exception {

        Association[] asso;
        asso = registry.getAssociations(path, type);

        boolean assoFound = false;
        if (asso == null) return assoFound;
        for (Association a2 : asso) {

            if (a2.getAssociationType().equals(type)) {
                assoFound = true;
                break;
            }
        }
        return assoFound;
    }

    public boolean getAssocitionbySourceByType(String path, String type) throws Exception {

        Association[] asso;
        asso = registry.getAssociations(path, type);

        boolean assoFound = false;
        if (asso == null) return assoFound;
        for (Association a2 : asso) {

            if (a2.getSourcePath().equals(path)) {
                assoFound = true;
                break;
            }
        }
        return assoFound;
    }

    public boolean getAssocitionbyDestinationByType(String path, String type, String destinationPath) throws Exception {

        Association[] asso;
        asso = registry.getAssociations(path, type);


        boolean assoFound = false;

        if (asso == null) return assoFound;
        for (Association a2 : asso) {

            if (a2.getDestinationPath().equals(destinationPath)) {
                assoFound = true;
                break;
            }
        }
        return assoFound;
    }

    public boolean associationNotExists(String path) throws Exception {
        Association association[] = registry.getAllAssociations(path);
        boolean value = true;
        if (association.length > 0)
            value = false;
        return value;
    }

    public boolean getProperty(String path, String key, String value) throws Exception {
        Resource r3 = registry.newResource();
        try {
            r3 = registry.get(path);
        }
        catch (Exception e) {
            fail((new StringBuilder()).append("Couldn't get file from the path :").append(path).toString());
        }
        List propertyValues = r3.getPropertyValues(key);
        Object valueName[] = propertyValues.toArray();
        boolean propertystatus = containsString(valueName, value);
        return propertystatus;
    }

    private boolean containsString(Object[] array, String value) {
        boolean found = false;
        for (Object anArray : array) {
            String s = anArray.toString();
            if (s.startsWith(value)) {
                found = true;
                break;
            }
        }
        return found;
    }

}
