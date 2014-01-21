/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.extensions.aspects;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.jdbc.Repository;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.Aspect;
import org.wso2.carbon.registry.core.*;

import java.util.ArrayList;
import java.util.List;

public class DefaultLifecycle extends Aspect {

    public static final String PHASE_CREATED = "created";
    public static final String PHASE_DEVELOPED = "developed";
    public static final String PHASE_TESTED = "tested";
    public static final String PHASE_DEPLOYED = "deployed";
    private static final String ORIGINAL_PATH = "originalPath";

    static final String[] phases = new String[]{
            PHASE_CREATED, PHASE_DEVELOPED, PHASE_TESTED, PHASE_DEPLOYED
    };

    // Property to store phase name
    public static final String PHASE_PROPERTY = "DefaultLifecycle.phase";

    // The only action this lifecycle supports is "promote", which moves to the next phase
    public static final String ACTION = "promote";

    public void associate(Resource resource, Registry registry) throws RegistryException {
        // We start off in the CREATED phase
        resource.setProperty(PHASE_PROPERTY, PHASE_CREATED);
    }

    public void dissociate(RequestContext context) {
        context.getResource().removeProperty(PHASE_PROPERTY);
    }

    /**
     * Do something (change state) - action names are lifecycle-specific, and it's up to the
     * implementation to decide if a given transition is allowed, and what to do if so.
     *
     * @param context the RequestContext containing all the state about this request
     * @param action  action to perform
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *          If the condition is not met or some thing is wrong
     */
    public void invoke(RequestContext context, String action) throws RegistryException {
        if (!ACTION.equals(action)) {
            throw new RegistryException("Unsupported lifecycle action '" + action +
                                        "'.  Only valid action is '" + ACTION + "'");
        }

        RegistryContext registryContext = context.getRegistry().getRegistryContext();
        Repository repository = registryContext.getRepository();

        Resource resource = context.getResource();
        String originalPath = resource.getProperty(ORIGINAL_PATH);
        if (originalPath == null ) {
            resource.setProperty(ORIGINAL_PATH , resource.getPath());
            originalPath = resource.getPath();
            repository.put(originalPath, resource);
        }

        String currentPhase = resource.getProperty(PHASE_PROPERTY);
        if (currentPhase == null)
            throw new RegistryException("DefaultLifecycle: Resource '" + resource.getPath() +
                                        "' has no phase property");

        int i = 0;
        while (i < phases.length) {
            if (phases[i].equals(currentPhase)) {
                break;
            }
            i++;
        }
        if (i == phases.length) {
            // No such phase!
            throw new RegistryException("Resource " + resource.getPath() +
                                        " is in an invalid lifecycle phase ('" + currentPhase +
                                        "')");
        }
        if (i == phases.length - 1) {
            // We're at the end, no more promoting to do
            throw new RegistryException(
                    "Promotion disallowed - resource is at the end of the DefaultLifecycle");
        }

        String nextPhase = phases[i + 1];
        String newResourcePath = "/" + nextPhase + originalPath;

        repository.copy(new ResourcePath(resource.getPath()), new ResourcePath(newResourcePath));
        Resource newResource = repository.get(newResourcePath);
        newResource.setProperty(PHASE_PROPERTY, nextPhase);
        repository.put(newResourcePath,  newResource);
    }

    private static List<String> promote = new ArrayList<String>();
    static {
        promote.add(ACTION);
    }
    public String [] getAvailableActions(RequestContext context) {
        return promote.toArray(new String[1]);
    }
}
