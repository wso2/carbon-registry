/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class Worker4 extends Worker {

    public Worker4(String threadName, int iterations, WSRegistryServiceClient registry) {
        super(threadName, iterations, registry);
    }

    public void run() {

        //System.out.println("=================== STARTED THE THREAD " + threadName + "===================");

        // do many registry operations in a loop

        long time1 = System.nanoTime();

        int i = 0;
        try {
            long timePerThread = 0;
            for (i = 0; i < iterations; i++) {

                long start = System.nanoTime();
                // put a resource

                String rPath = basePath + "/original/r" + i;
                Resource r = registry.newResource();
                String content = "this is the content of first test resource.";
                r.setContent(RegistryUtils.encodeString(content));
                r.setProperty("p1", "v1");
                r.setProperty("p2", "v2");
                //long startPut = System.nanoTime();
                registry.put(rPath, r);
                //long endPut = System.nanoTime();
                //System.out.println("CSV,"+threadName+",put,"+(endPut-startPut));
                r.discard();
                Thread.yield();

                // read and update the resource

                registry.resourceExists(rPath);
                //long startGet = System.nanoTime();
                Resource rr = registry.get(rPath);
                //long endGet = System.nanoTime();
                //System.out.println("CSV,"+threadName+",get,"+(endGet-startGet));
                String content2 = "this is the modified content.";
                rr.setContent(content2);
                rr.setProperty("p1", "vvv1");
                //long startUpdate = System.nanoTime();
                registry.put(rPath, rr);
                //long endUpdate = System.nanoTime();
                //System.out.println("CSV,"+threadName+",update,"+(endUpdate-startUpdate));
                rr.discard();
                Thread.yield();

                // copy the resource
                String pathToCopy = basePath + "/copy/r" + i;
                registry.copy(rPath, pathToCopy);
                Thread.yield();

                // tag the resource
                //long applyTagStart = System.nanoTime();
                registry.applyTag(rPath, "test");
                //long applyTagEnd = System.nanoTime();
                //System.out.println("CSV,"+threadName+",applytag,"+(applyTagEnd-applyTagStart));

                //long getTagStart = System.nanoTime();
                registry.getTags(rPath);
                //long getTagEnd = System.nanoTime();


                registry.getResourcePathsWithTag("test");
                Thread.yield();

                // comment the resource
                registry.addComment(rPath, new Comment("this is a test resource. so it is ok to mess with this."));
                registry.getComments(rPath);
                Thread.yield();

                // rate the resource
                registry.rateResource(rPath, 3);
                registry.getAverageRating(rPath);
                Thread.yield();

                // add association between the original and the copy
//                registry.addAssociation(rPath, pathToCopy, "copy of");
//                registry.getAllAssociations(rPath);
//                registry.getAssociations(rPath, "copy of");
//                Thread.yield();

                // perform some aspect operations
//                registry.associateAspect(rPath, "Lifecycle");
//                registry.invokeAspect(rPath, "Lifecycle", "promote");
//                registry.invokeAspect(rPath, "Lifecycle", "promote");
//                registry.invokeAspect(rPath, "Lifecycle", "demote");
//                registry.getAspectActions(rPath, "Lifecycle");
//                Thread.yield();
//
                // versioning operations
                System.out.println(rPath);
                registry.createVersion(rPath);
                String[] versions = registry.getVersions(rPath);
                if (versions.length > 0) {
                    Resource rVersion = registry.get(versions[0]);
                    rVersion.discard();
                    registry.restoreVersion(versions[0]);
                }



                Thread.yield();

                registry.delete(rPath);
                Thread.yield();
                long end = System.nanoTime();
                timePerThread += (end - start);

                long averageTime = (end - start) / ((i + 1) * 1000000);
                System.out.println("CSV," + threadName + "," + "iteration," + i + 1 + "," + averageTime);


            }
            long averageTime = timePerThread / (iterations * 1000000);
            System.out.println("CSV-avg-time-per-thread," + threadName + "," + averageTime);

        } catch (RegistryException e) {

            String msg = "Failed to perform registry operations. Thread " + threadName +
                    " failed at iteration " + i + ". " + e.getMessage();
            e.printStackTrace();

        } catch (Exception e) {
            String msg = "Failed the thread " + threadName + " at iteration " + i + ". " + e.getMessage();
            e.printStackTrace();
        }

        long time2 = System.nanoTime();
        long elapsedTime = (time2 - time1) / (1000000 * iterations);
        System.out.println("AVG-TIME-PER-THREAD," + threadName + "," + elapsedTime);

        System.out.println("=================== COMPLETED THE THREAD " + threadName + "===================");
    }
}
