/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

/**
 * An example "review and approval" lifecycle which demonstrates how property settings can
 * affect the behavior of Aspects.  Once this lifecycle has been associated with a resource,
 * only the user specified in the "reviewer" property will see the available "approve" and
 * "deny" actions.
 */
public class ReviewLifecycle extends Lifecycle {
    public static final String REVIEWER = "reviewer";
    protected final String ORIGINAL_PATH = "registry.originalPath";

    public void associate(Resource resource, Registry registry) throws RegistryException {
        // Might be interesting to do this as follows:
        //  check for "reviewer" property and fail if not present, or if it doesn't match
        //    an existing registry user
        //  allow the reviewer user to have write permission (required for them to update)
        //  copy the resource into "/people/{reviewer}/to-review/..." (this lets them subscribe
        //    to the feed to get notified of new items in their queue)
        //  set the "approval" property to "in review"
        //
        // If we do it that way, then we need to make sure that we also set an "originalPath"
        // property on the copied resource so that when the reviewer selects approve/reject,
        // we can record the status on the "real" object (and then maybe delete the copy?)
        //
        // We really need symbolic links, which would solve this in a nicer way.
        String reviewer = resource.getProperty(REVIEWER);
        if (reviewer == null) {
            throw new RegistryException("No " + REVIEWER + " property");
        }

        try {
            UserRealm userRealm = CurrentSession.getUserRealm();
            if (!userRealm.getUserStoreManager().isExistingUser(reviewer)) {
                throw new RegistryException("No such user '" + reviewer + "'");
            }
        } catch (UserStoreException e) {
            throw new RegistryException("User Store Exception", e);
        }

        final String path = resource.getPath();
        String name = path;
        int idx = name.lastIndexOf("/");
        name = name.substring(idx + 1, name.length());
        if (name.length() == 0) {
            throw new RegistryException("Can't associate Review to root resource");
        }

        final String newPath = "/people/" + reviewer + "/to-review/" + name;
        registry.copy(path, newPath);
        Resource r = registry.get(newPath);
        registry.addAssociation(newPath, path, "original");

        // Need a better way for this to work 
        r.setProperty("registry.Aspects", "Review");

        registry.put(newPath, r);
    }

    public void invoke(RequestContext context, String action) throws RegistryException {
        String value;
        if ("approve".equals(action)) {
            value = "approved";
        } else if ("reject".equals(action)) {
            value = "rejected";
        } else {
            throw new RegistryException("Not a valid action");
        }
        Registry registry = context.getRegistry();
        Association[] associations =
                registry.getAssociations(context.getResourcePath().getPath(), "original");
        if (associations == null) {
            throw new RegistryException("No original resource to approve");
        }
        final Resource resource = context.getRepository().get(associations[0].getDestinationPath());
        resource.setProperty("approval", value);
        context.getRepository().put(resource.getPath(), resource);
    }

    public String[] getAvailableActions(RequestContext context) {
        String reviewer = context.getResource().getProperty(REVIEWER);
        if (CurrentSession.getUser().equals(reviewer)) {
            return new String [] { "approve", "reject" };
        }
        return new String[0];
    }
}
