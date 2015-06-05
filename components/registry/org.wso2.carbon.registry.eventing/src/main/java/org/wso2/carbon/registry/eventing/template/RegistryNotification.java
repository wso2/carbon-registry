/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.eventing.template;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;


public class RegistryNotification implements NotificationTemplate {

    private static final String TEMPLATE_PATH = "/repository/components/org.wso2.carbon.governance/templates";
    private static final String DEFAULT_TEMPLATE_NAME = "default";
    private static final String TEMPLATE_EXT = ".html";
    private static final String DEFAULT_TEMPLATE = TEMPLATE_PATH + "/"+DEFAULT_TEMPLATE_NAME+TEMPLATE_EXT;


    private static final Log log = LogFactory.getLog(RegistryNotification.class);

    /**
     * This method used to populate email message body. As default behavior this will replace the $$message$$ content
     * of the template using input message.
     *
     * @param configRegistry  Configuration Registry
     * @param resourcePath   Registry Resource Path
     * @param message   Current
     * @param eventType  Eventing type
     * @return Populated email message body
     */
    public String populateEmailMessage(Registry configRegistry,String resourcePath,String message, String eventType) {
        String templateLocation = TEMPLATE_PATH +"/"+eventType.toLowerCase() +TEMPLATE_EXT;
        Resource template = null;
        try {
            if (configRegistry.resourceExists(templateLocation)){
               template = configRegistry.get(templateLocation);
            } else if (configRegistry.resourceExists(DEFAULT_TEMPLATE)){
                template = configRegistry.get(DEFAULT_TEMPLATE);
            }
            if (template != null) {
                Object object = template.getContent();
                if (object != null) {
                    String content;
                    if (object instanceof String){
                        content = (String)object;
                    }else {
                        content = new String((byte[])object);
                    }
                    return content.replace("$$message$$", message);
                }
            }
        } catch (RegistryException e) {
            log.warn("An error occurred while accessing email template from the registry");
        }

        return  null;
    }
}
