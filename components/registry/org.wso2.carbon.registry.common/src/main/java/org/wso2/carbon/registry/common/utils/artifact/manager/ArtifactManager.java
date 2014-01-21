/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.common.utils.artifact.manager;

import org.wso2.carbon.context.CarbonContext;

import java.util.HashMap;
import java.util.Map;

public class ArtifactManager {

    private static Map<Integer, ArtifactRepository> repositories;
    private static ArtifactManager thisInstance = new ArtifactManager();

    public static ArtifactManager getArtifactManager() {
        return thisInstance;
    }

    private ArtifactManager() {
        repositories = new HashMap<Integer, ArtifactRepository>();
    }

    public ArtifactRepository getTenantArtifactRepository() {
        int tid = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return this.getTenantArtifactRepository(tid);
    }

    private synchronized ArtifactRepository getTenantArtifactRepository(int tenantId) {
        ArtifactRepository repository = repositories.get(tenantId);
        if (repository == null) {
            repository = new ArtifactRepository();
            repositories.put(tenantId, repository);
        }
        return repository;
    }
    
}
