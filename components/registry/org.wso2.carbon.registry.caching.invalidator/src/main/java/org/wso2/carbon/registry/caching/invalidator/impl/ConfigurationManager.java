/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.caching.invalidator.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Global cache invalidation configuration manager which extract configuration parameters from cache.xml
 */
public class ConfigurationManager {
    private static final Log log = LogFactory.getLog(ConfigurationManager.class);

    private static boolean subscribed = false;

    private static boolean enabled = false;

    private static Properties cacheConfiguration = new Properties();

    private static List<String> sentMsgBuffer;

    public static boolean init(){
        String configFilePath = CarbonUtils.getCarbonHome() + File.separator + "repository"
                + File.separator + "conf" + File.separator + "cache.properties";
        FileInputStream fileInputStream = null;
        try{
            fileInputStream = new FileInputStream(configFilePath);
            cacheConfiguration.load(fileInputStream);

            if(cacheConfiguration.containsKey("enabled")) {
                enabled = Boolean.parseBoolean(cacheConfiguration.getProperty("enabled"));
            }

            if(!enabled){
                log.info("Global cache invalidation is offline according to cache.properties configurations");
            }

        } catch (IOException ioException) {
            log.error("Global cache invalidation : Error while reading cache.properties file", ioException);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ioException) {
                    log.error("Global cache invalidation : Error while reading cache.properties file", ioException);
                }
            }
        }
        return enabled;
    }

    public static List<String> getSentMsgBuffer() {
        if(sentMsgBuffer == null){
            sentMsgBuffer = new ArrayList<String>();
        }
        return sentMsgBuffer;
    }

    public static Properties getCacheConfiguration() {
        return cacheConfiguration;
    }

    public static boolean isSubscribed() {
        return subscribed;
    }

    public static void setSubscribed(boolean subscribed) {
        ConfigurationManager.subscribed = subscribed;
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
