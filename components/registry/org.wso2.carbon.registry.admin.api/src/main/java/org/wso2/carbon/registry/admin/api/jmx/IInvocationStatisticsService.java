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
package org.wso2.carbon.registry.admin.api.jmx;

/**
 * Contains API to obtain method invocation statistics in registry and governance components.
 */
public interface IInvocationStatisticsService {

    /**
     * Method to obtain the list of methods that have been invoked so far.
     *
     * @return list of method names.
     */
    String[] getInvokedMethods();

    /**
     * Method to obtain the invocation count.
     *
     * @param methodName the name of the method.
     *
     * @return number of calls.
     */
    long getInvocationCounts(String methodName);

}
