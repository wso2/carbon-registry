/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.rest.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is to validate pagination param and path.
 */
public class ValidationUtils {

    private static Log log = LogFactory.getLog(ValidationUtils.class);

    protected static boolean validatePath(String resourcePath) {
        // null check for resource path.
        if (resourcePath == null || resourcePath.length() == 0) {
            if(log.isDebugEnabled()){
                log.debug("Artifact path is not valid path : " +resourcePath);
            }
            return false;
        } else {
            return true;
        }
    }

    protected static boolean validatePagination(int start, int size) {

        if (start < 0 || size < 0) {
            if(log.isDebugEnabled()){
                log.debug("Pagination details not valid start : "+start + "size : "+size);
            }
            return false;
        }

        if(start >0 && size == 0){
            return false;
        }
        return true;
    }
}

