///*
// * Copyright 2004,2005 The Apache Software Foundation.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.wso2.carbon.registry.ws.client.test;
//
//import org.wso2.carbon.registry.core.Resource;
//import org.wso2.carbon.registry.core.exceptions.RegistryException;
//import org.wso2.carbon.registry.core.utils.RegistryClientUtils;
//
//import java.io.*;
//
//
//public class FileSystemImportExport extends TestSetup {
//
//    public FileSystemImportExport(String text) {
//        super(text);
//    }
//
//    public void testFileImport() throws RegistryException {
//
//        String filePath = "../dbscripts";
//        File file = new File(filePath);
//        RegistryClientUtils.importToRegistry(file, "/krishantha", registry);
//        assertTrue("Resource not found.", registry.resourceExists("/krishantha/dbscripts/common/mysql-registry.sql"));
//        assertTrue("Resource not found.", registry.resourceExists("/krishantha/dbscripts/common/mssql-registry.sql"));
//        assertTrue("Resource not found.", registry.resourceExists("/krishantha/dbscripts/common/oracle-registry.sql"));
//        assertTrue("Resource not found.", registry.resourceExists("/krishantha/dbscripts/common/h2-registry.sql"));
//        assertTrue("Resource not found.", registry.resourceExists("/krishantha/dbscripts/common/derby-registry.sql"));
//
//        assertTrue("Resource not found.", registry.resourceExists("/krishantha/dbscripts/usermanager/um-h2.sql"));
//        assertTrue("Resource not found.", registry.resourceExists("/krishantha/dbscripts/usermanager/um-derby.sql"));
//        assertTrue("Resource not found.", registry.resourceExists("/krishantha/dbscripts/usermanager/um-mysql.sql"));
//        assertTrue("Resource not found.", registry.resourceExists("/krishantha/dbscripts/usermanager/um-oracle.sql"));
//        assertTrue("Resource not found.", registry.resourceExists("/krishantha/dbscripts/usermanager/um-hsql.sql"));
//
//        Resource r1 = registry.newResource();
//        r1 = registry.get("/krishantha/dbscripts/common/mysql-registry.sql");
//        r1.getContent();
//        String contain = new String((byte[]) r1.getContent());
//        assertTrue("Resource contain not found", containString(contain, "CREATE"));
//
//        r1 = registry.get("/krishantha/dbscripts/common/mysql-registry.sql");
//        r1.getContent();
//        String containUm = new String((byte[]) r1.getContent());
//        assertTrue("Resource contain not found", containString(containUm, "CREATE"));
//
//
//        String[] r1Versions1 = registry.getVersions("/krishantha/dbscripts/common/mysql-registry.sql");
//        assertTrue("Resource should have atleaset 1 version.", versionCount(r1Versions1));
//
//        String[] r1Versions2 = registry.getVersions("/krishantha/dbscripts/usermanager/um-h2.sql");
//        assertTrue("Resource should have atleaset 1 version.", versionCount(r1Versions2));
//
//        r1.discard();
//
//        RegistryClientUtils.importToRegistry(file, "/krishantha", registry);
//
//        Resource r2 = registry.newResource();
//        r2 = registry.get("/krishantha/dbscripts/common/mysql-registry.sql");
//        r2.getContent();
//        String contain2 = new String((byte[]) r2.getContent());
//        assertTrue("Resource contain not found", containString(contain2, "CREATE"));
//
//        r2 = registry.get("/krishantha/dbscripts/common/mysql-registry.sql");
//        r2.getContent();
//        String containUm2 = new String((byte[]) r2.getContent());
//        assertTrue("Resource contain not found", containString(containUm2, "CREATE"));
//
//
//        String[] r1Versions12 = registry.getVersions("/krishantha/dbscripts/common/mysql-registry.sql");
//        assertTrue("Resource should have atleaset 1 version.", versionCount(r1Versions12));
//
//        String[] r1Versions22 = registry.getVersions("/krishantha/dbscripts/usermanager/um-h2.sql");
//        assertTrue("Resource should have atleaset 1 version.", versionCount(r1Versions22));
//
//        r2.discard();
//
//    }
//
//    public void testFileExport() throws RegistryException, FileNotFoundException {
//
//        File file = new File("./export/test-dir/test");
//        RegistryClientUtils.exportFromRegistry(file, "/krishantha/", registry);
//
//
//        File f = new File("./export/test-dir/test/dbscripts/common/h2-registry.sql");
//        assertTrue("File doesn't exist at the location", f.exists());
//
//        File f1 = new File("./export/test-dir/test/dbscripts/usermanager/um-oracle.sql");
//        assertTrue("File doesn't exist at the location", f1.exists());
//
//        assertTrue("Resource contain not found", fileContainString("./export/test-dir/test/dbscripts/usermanager/um-oracle.sql", "CREATE"));
//        assertTrue("Resource contain not found", fileContainString("./export/test-dir/test/dbscripts/common/h2-registry.sql", "CREATE"));
//
//    }
//
//    public void testJarFileExport() throws RegistryException, FileNotFoundException {
//
//
//        String filePath = "./lib/junit-4.5.jar";
//        File file = new File(filePath);
//        RegistryClientUtils.importToRegistry(file, "/krishantha", registry);
//
//        assertTrue("Resource not found.", registry.resourceExists("/krishantha/junit-4.5.jar"));
//
//        File file2 = new File("./export/test-dir/test");
//        RegistryClientUtils.exportFromRegistry(file2, "/krishantha", registry);
//
//        File f1 = new File("./export/test-dir/test/junit-4.5.jar");
//        assertTrue("File doesn't exist at the location", f1.exists());
//
//        String[] r1Versions12 = registry.getVersions("/krishantha/junit-4.5.jar");
//        assertTrue("Resource should have atleaset 1 version.", versionCount(r1Versions12));
//
//    }
//
//    public static boolean containString(String str, String pattern) {
//        int s = 0;
//        int e = 0;
//        boolean value = false;
//
//        while ((e = str.indexOf(pattern, s)) >= 0) {
//            value = true;
//            return value;
//
//        }
//        return value;
//    }
//
//    public static boolean versionCount(String r1Versions[]) {
//        boolean versionCount = false;
//        //System.out.println("version length" + r1Versions.length);
//        if (r1Versions.length >= 1) {
//            versionCount = true;
//        }
//        return versionCount;
//    }
//
//    public static String slurp(InputStream in) throws IOException {
//        StringBuffer out = new StringBuffer();
//        byte[] b = new byte[4096];
//        for (int n; (n = in.read(b)) != -1;) {
//            out.append(new String(b, 0, n));
//        }
//        return out.toString();
//    }
//
//    public static boolean fileContainString(String path, String pattern) throws FileNotFoundException {
//        String st = null;
//        boolean valuefile = false;
//        InputStream is = new BufferedInputStream(new FileInputStream(path));
//        try {
//            st = slurp(is);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        if (containString(st, pattern)) {
//            valuefile = true;
//        }
//        return valuefile;
//    }
//}
