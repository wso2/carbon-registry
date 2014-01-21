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

package org.wso2.carbon.registry.reporting.utils;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.reporting.AbstractReportGenerator;
import org.wso2.carbon.registry.reporting.annotation.Property;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

public class Utils {

    public static ByteArrayOutputStream getReportContentStream(String reportClass,
                                                               String template,
                                                               String type,
                                                               Map<String, String> attributes,
                                                               Registry registry)
            throws Exception {
        AbstractReportGenerator reportGenerator =
                (AbstractReportGenerator) RegistryUtils.loadClass(reportClass).newInstance();
        reportGenerator.setRegistry(registry);
        Method[] declaredMethods = reportGenerator.getClass().getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (method.isAnnotationPresent(Property.class)) {
                String name = method.getName();
                if (name.startsWith("set")) {
                    name = name.substring("set".length());
                }
                name = name.substring(0, 1).toLowerCase() + name.substring(1);
                String value = attributes.get(name);
                if (value == null && method.getAnnotation(Property.class).mandatory()) {
                    throw new IOException("A mandatory field " + name + " was not set");
                }
                method.invoke(reportGenerator, value);
            }
        }
        return reportGenerator.execute(template, type);
    }
}
