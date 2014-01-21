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

package org.wso2.carbon.registry.resource.ui;

import org.wso2.carbon.registry.resource.ui.processors.SetDescriptionProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import java.util.Map;
import java.util.HashMap;

public class CommandHandler {

    private static Map <String, UIProcessor> processors = new HashMap <String, UIProcessor> ();

    private static boolean initiated = false;

    public static String process(
            HttpServletRequest request, HttpServletResponse response, ServletConfig config)
            throws Exception {

        synchronized (processors) {
            if (!initiated) {
                initiated = true;
                init();
            }
        }

        String command = request.getParameter("command");
        UIProcessor processor = processors.get(command);
        return processor.process(request, response, config);
    }

    public static void init() {

        //processors.put("set.description", new SetDescriptionProcessor());
    }
}
