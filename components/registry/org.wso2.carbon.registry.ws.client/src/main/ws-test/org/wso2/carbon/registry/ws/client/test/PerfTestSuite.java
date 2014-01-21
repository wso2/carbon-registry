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
//import junit.framework.TestSuite;
//import junit.framework.Test;
//
//import java.io.IOException;
//
//public class PerfTestSuite extends TestSuite {
//
//    public static Test suite() throws IOException {
//        TestSuite suite = new TestSuite();
//
//        int workerNumber = Integer.parseInt(PropertyReader.loadRegistryProperties().getProperty("wokerClass"));
//        System.out.println("workerNumber" + workerNumber);
//
//        System.out.println("Remote Registry Perfomance Test Framework started .....!");
//
//
//        switch (workerNumber) {
//            case 1:
//                suite.addTest(new RemotePerfTest("testWorker1"));
//                break;
//            case 2:
//                suite.addTest(new RemotePerfTest("testWorker2"));
//                break;
//            case 3:
//                suite.addTest(new RemotePerfTest("testWorker3"));
//                break;
//            case 4:
//                suite.addTest(new RemotePerfTest("testWorker4"));
//                break;
//            case 5:
//                suite.addTest(new RemotePerfTest("testWorker5"));
//                break;
//            default:
//                System.out.println("Running all performance test scenarios....");
//                suite.addTestSuite(RemotePerfTest.class);
//                break;
//        }
//
////        System.out.println("Remote Registry Perf Test Framework finished, please check perf-results.txt file for excution report .....!");
//
//        return suite;
//    }
//}