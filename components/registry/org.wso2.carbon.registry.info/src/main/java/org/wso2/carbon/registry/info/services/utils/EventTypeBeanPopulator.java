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

package org.wso2.carbon.registry.info.services.utils;

import java.util.Map;
import java.util.Set;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.common.beans.EventTypeBean;
import org.wso2.carbon.registry.common.beans.utils.EventType;
import org.wso2.carbon.registry.info.Utils;

public class EventTypeBeanPopulator {

    public static EventTypeBean populate(UserRegistry userRegistry, String path) {
        EventTypeBean eventTypeBean = new EventTypeBean();
        ResourcePath resourcePath = new ResourcePath(path);
        try {
            if (Utils.getRegistryEventingService() == null || Utils.getRegistryEventingService().getEventTypes() == null) {
                throw new IllegalStateException("No Event Types defined");
            }
            else {
                Map eventTypeMap = Utils.getRegistryEventingService().getEventTypes();
                EventType[] eventTypes = new EventType[eventTypeMap.size()];
                Set<Map.Entry<String, String[]>> entrySet = eventTypeMap.entrySet();
                if (eventTypeMap.size() > 0) {
                    int count = 0;
                    for (Map.Entry<String, String[]> e : entrySet) {
                        if (Utils.getRegistryEventingService().isEventTypeExclusionRegistered(e.getKey(), path)) {
                            continue;    
                        }
                        EventType eventType = new EventType();
                        eventType.setId(e.getKey());
                        eventType.setResourceEvent(e.getValue()[0]);
                        eventType.setCollectionEvent(e.getValue()[1]);
                        eventTypes[count++] = eventType;
                    }
                }
                eventTypeBean.setEventTypes(eventTypes);
            }
        } catch (Exception e) {
            String msg = "Failed to get Event Types available for the resource " +
                    resourcePath + ". " + e.getMessage();
            eventTypeBean.setErrorMessage(msg);
        }
        return eventTypeBean;
    }
}
