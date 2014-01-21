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

package org.wso2.carbon.registry.ws.client.test;

import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;


public class RemotePerfTest extends TestSetup {
	
	private static int concurrentUsers = 10;
	private static int workerClass = 1;
	private static int iterationsNumber = 10;
    public static final int ITERATIONS = 1000;
    public static final int NUM_USERS = concurrentUsers;
    public static final int WORKER_CLASS = workerClass;


    private static WSRegistryServiceClient adminRegistry = registry;

    public RemotePerfTest(String text) {
        super(text);
    }


    public static void testWorker1() throws Exception {

        int numUsers = concurrentUsers;

        Worker[] workers = new Worker[numUsers];
        for (int i = 0; i < numUsers; i++) {

            Worker worker = new Worker1("T" + i, iterationsNumber, registry);
            //System.out.println("inside loop");
            workers[i] = worker;
        }

        long time1 = System.nanoTime();

        for (int i = 0; i < numUsers; i++) {
            workers[i].start();
        }

        for (int i = 0; i < numUsers; i++) {
            workers[i].join();
        }

        long time2 = System.nanoTime();
        long elapsedTime = time2 - time1;
        System.out.println("Time taken for the whole test: " + elapsedTime / 1000000 + " ms");
    }

    public static void testWorker2() throws Exception {

        int numUsers = concurrentUsers;

        Worker[] workers = new Worker[numUsers];
        for (int i = 0; i < numUsers; i++) {
            Worker worker = new Worker2("T" + i, iterationsNumber, registry);
            workers[i] = worker;
        }

        long time1 = System.nanoTime();

        for (int i = 0; i < numUsers; i++) {
            workers[i].start();
        }

        for (int i = 0; i < numUsers; i++) {
            workers[i].join();
        }

        long time2 = System.nanoTime();
        long elapsedTime = time2 - time1;
        System.out.println("Time taken for the whole test: " + elapsedTime / 1000000 + " ms");
    }

    public static void testWorker3() throws Exception {

        int numUsers = concurrentUsers;

        Worker[] workers = new Worker[numUsers];
        for (int i = 0; i < numUsers; i++) {
            Worker worker = new Worker3("T" + i, iterationsNumber, registry);
            workers[i] = worker;
        }

        long time1 = System.nanoTime();

        for (int i = 0; i < numUsers; i++) {
            workers[i].start();
        }

        for (int i = 0; i < numUsers; i++) {
            workers[i].join();
        }

        long time2 = System.nanoTime();
        long elapsedTime = time2 - time1;
        System.out.println("Time taken for the whole test: " + elapsedTime / 1000000 + " ms");
    }

    public static void testWorker4() throws Exception {

        int numUsers = concurrentUsers;

        Worker[] workers = new Worker[numUsers];
        for (int i = 0; i < numUsers; i++) {
            Worker worker = new Worker4("T" + i, iterationsNumber, registry);
            workers[i] = worker;
        }

        long time1 = System.nanoTime();

        for (int i = 0; i < numUsers; i++) {
            workers[i].start();
        }

        for (int i = 0; i < numUsers; i++) {
            workers[i].join();
        }

        long time2 = System.nanoTime();
        long elapsedTime = time2 - time1;
        System.out.println("Time taken for the whole test: " + elapsedTime / 1000000 + " ms");
    }

    public static void testWorker5() throws Exception {

        int numUsers = concurrentUsers;

        Worker[] workers = new Worker[numUsers];
        for (int i = 0; i < numUsers; i++) {
            Worker worker = new Worker5("T" + i, iterationsNumber, registry);
            workers[i] = worker;
        }

        long time1 = System.nanoTime();

        for (int i = 0; i < numUsers; i++) {
            workers[i].start();
        }

        for (int i = 0; i < numUsers; i++) {
            workers[i].join();
        }

        long time2 = System.nanoTime();
        long elapsedTime = time2 - time1;
        System.out.println("Time taken for the whole test: " + elapsedTime / 1000000 + " ms");
    }

}
