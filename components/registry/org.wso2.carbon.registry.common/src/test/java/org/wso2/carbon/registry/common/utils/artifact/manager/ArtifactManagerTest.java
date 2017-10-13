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

package org.wso2.carbon.registry.common.utils.artifact.manager;

import junit.framework.TestCase;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.nio.file.Paths;


public class ArtifactManagerTest extends TestCase {

    private static final String basedir = Paths.get("").toAbsolutePath().toString();
    private static final String testDir = Paths.get(basedir, "src", "test", "resources").toString();
    private static final File testSampleDirectory = Paths.get("target", "carbon-utils-test-directory").toFile();

    private ArtifactManager artifactManager;

    @Override
    protected void setUp() throws Exception {
        testSampleDirectory.mkdirs();
        System.setProperty(ServerConstants.CARBON_HOME, testDir);
        artifactManager = ArtifactManager.getArtifactManager();
    }

    @Override
    protected void tearDown() throws Exception {
        testSampleDirectory.delete();
        super.tearDown();
    }

    public void testGetArtifactManager() throws Exception {
        ArtifactManager artifactManagerLocal = ArtifactManager.getArtifactManager();
        assertNotNull(artifactManagerLocal);
        assertEquals(artifactManager, artifactManagerLocal);

    }

    public void testGetTenantArtifactRepository() throws Exception {
        String tmpDir = testSampleDirectory.getAbsolutePath();
        System.setProperty("java.io.tmpdir", tmpDir);
        System.setProperty("carbon.home", "");
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234);
            ArtifactRepository artifactRepository = artifactManager.getTenantArtifactRepository();
            assertNotNull(artifactRepository);

            String trunk = "/_system/governance/trunk";

            //add a path
            artifactRepository.addArtifact(trunk);

            //check path is exist
            assertEquals(1, artifactRepository.getArtifacts().size());
            assertTrue(artifactRepository.getArtifacts().contains(trunk));

            String service = "/_system/governance/service";
            //add another path
            artifactRepository.addArtifact(service);

            //check both paths
            assertEquals(2, artifactRepository.getArtifacts().size());
            assertTrue(artifactRepository.getArtifacts().contains(service));
            assertTrue(artifactRepository.getArtifacts().contains(trunk));

            //remove paths
            artifactRepository.removeArtifact(trunk);
            artifactRepository.removeArtifact(service);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }
}