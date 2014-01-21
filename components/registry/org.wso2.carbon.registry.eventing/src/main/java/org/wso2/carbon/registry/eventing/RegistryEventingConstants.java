/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.eventing;

import org.wso2.carbon.event.ws.internal.util.EventingConstants;
import org.wso2.carbon.registry.common.eventing.RegistryEvent;

public class RegistryEventingConstants implements EventingConstants {
    public static final String EVENTING_SERVICE_NAME = "RegistryEventingService";
    public static final String DO_REST = "doRest";
    public static final String NOT_VERIFIED = "notVerfied";
    public static final String TOPIC_PREFIX = RegistryEvent.TOPIC_PREFIX;
}
