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

import org.wso2.carbon.registry.core.statistics.StatisticsCollector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InvocationStatistics implements InvocationStatisticsMBean, StatisticsCollector {

    private Map<String, AtomicLong> map =
            new ConcurrentHashMap<String, AtomicLong>();

    public String[] getInvokedMethods() {
        return map.keySet().toArray(new String[map.size()]);
    }

    public void collect(Object... objects) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
        String className = stackTraceElement.getClassName();
        String methodName = className.substring(className.lastIndexOf(".") + 1) +
                "." + stackTraceElement.getMethodName();
        AtomicLong atomicLong = map.get(methodName);
        if (atomicLong == null) {
            map.put(methodName, new AtomicLong(1));
        } else {
            atomicLong.incrementAndGet();
        }
    }

    public long getInvocationCounts(String methodName) {
        return map.get(methodName).get();
    }
}
