/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.registry.extensions.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;

/**
 * This handler implementation handles the delete process of services, collections and resources with Subscriptions.
 */
public class DeleteSubscriptionHandler extends Handler {

    private static final String TOPIC_INDEX_PATH = "/_system/governance/event/topicIndex";
    private static final String SUBSCRIPTION_PATH_PREFIX = "/_system/governance/event/topics/registry/notifications";
    private static final String NOTIFICATION_PREFIX = "/registry/notifications";
    private static final String WS_SUBSCRIPTION = "ws.subscriptions";

    public void delete(RequestContext requestContext) throws RegistryException {

        if(!isDeleteLockAvailable()){
            return;
        }
        acquireDeleteLock();

        try {
        String deletedResourcePath = requestContext.getResourcePath().getPath();

        final Registry registry = requestContext.getRegistry();
        Resource resource = registry.getMetaData(deletedResourcePath);

        boolean isCollection = false;
        if (resource.getMediaType() == null) {
            isCollection = true;
        }

        if (!registry.resourceExists(TOPIC_INDEX_PATH)) {
            return;
        }

        Resource topicIndexResource = registry.get(TOPIC_INDEX_PATH);

        Properties properties = topicIndexResource.getProperties();

        List<String> propertiesToDelete = new ArrayList<String>();
        List<String> pathsToDelete = new ArrayList<String>();

        Iterator<?> iterator = properties.entrySet().iterator();

        while(iterator.hasNext()) {
            Entry<String, List<String>> entry = (Entry<String, List<String>>) iterator.next();

            if (entry.getValue().size() > 1) {
                throw new RegistryException("Multiple properties available for same UUID.");
            }

            String pathArry = entry.getValue().get(0);
            if (!pathArry.startsWith(NOTIFICATION_PREFIX)) {
                continue;
            }
            String propertyPath = pathArry.substring(NOTIFICATION_PREFIX.length());
            String absPropPath = propertyPath.substring(1).substring(propertyPath.substring(1).indexOf("/"));

            if (deletedResourcePath.equalsIgnoreCase(absPropPath)) {
                String key = entry.getKey();
                if (!propertiesToDelete.contains(key)) {
                    propertiesToDelete.add(key);
                }

                if (!isCollection) {
                    String firstPath = propertyPath.substring(0, propertyPath.lastIndexOf("/"));
                    String lastPath = propertyPath.substring(propertyPath.lastIndexOf("/") + 1);
                    if (lastPath.lastIndexOf(".") != -1) {
                        String fileName = lastPath.substring(0, lastPath.lastIndexOf("."));
                        String extension = lastPath.substring(lastPath.lastIndexOf(".") + 1);
                        StringBuffer sb = new StringBuffer();
                        sb.append(firstPath).append("/").append(fileName).append("/").append(extension);
                        propertyPath = sb.toString();
                    }
                }

                String pathToDelete = SUBSCRIPTION_PATH_PREFIX + propertyPath.replaceAll("\\.", "/");
                StringBuffer sb = new StringBuffer();
                sb.append(pathToDelete).append("/").append(WS_SUBSCRIPTION).append("/").append(key);
                pathToDelete = sb.toString();

				if (!pathsToDelete.contains(pathToDelete)) {
				 	pathsToDelete.add(pathToDelete);
				}
            }
        }

        for (String property : propertiesToDelete) {
            topicIndexResource.removeProperty(property);
            registry.put(TOPIC_INDEX_PATH, topicIndexResource);
        }

		for (String path: pathsToDelete) {
			if(registry.resourceExists(path)) {
                registry.delete(path);
			}
		}

        } finally {
            releaseDeleteLock();
        }

    }

    private static ThreadLocal<Boolean> deleteInProgress = new ThreadLocal<Boolean>() {
        protected Boolean initialValue() {
            return false;
        }
    };

    public static boolean isDeleteLockAvailable() {
        return !deleteInProgress.get();
    }

    public static void acquireDeleteLock() {
        deleteInProgress.set(true);
    }

    public static void releaseDeleteLock() {
        deleteInProgress.set(false);
    }
}
