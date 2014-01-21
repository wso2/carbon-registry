/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.carbon.registry.extensions.jmx;

import org.wso2.carbon.registry.admin.api.jmx.ISubscriptionsService;

/**
 * This interface is created to avoid a typical JMX bean registration issue where the interface and
 * the implementation needs to be in the same package. The documentation is found in the base
 * interface.
 */
public interface SubscriptionsMBean extends ISubscriptionsService {

}
