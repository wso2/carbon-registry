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

package org.wso2.carbon.registry.resource.ui.clients;

import org.wso2.carbon.registry.core.utils.MediaTypesUtils;
import org.wso2.carbon.ui.deployment.beans.CustomUIDefenitions;

import javax.servlet.http.HttpSession;
import java.util.Map;

public class CustomUIHandler {

    public static String getCustomViewUI(String mediaType, HttpSession session) {

        if (mediaType == null) {
            return null;
        }

        CustomUIDefenitions customUIDefenitions = (CustomUIDefenitions) session.getServletContext().
                getAttribute(CustomUIDefenitions.CUSTOM_UI_DEFENITIONS);
        @SuppressWarnings("unchecked")
        Map<String, String> customViewUIMap =
                (Map<String, String>)session.getAttribute("customViewUI");
        if (customViewUIMap != null) {
            String customViewUI = customViewUIMap.get(mediaType);
            if (customViewUI != null) {
                return customViewUI;
            }
        }
        return customUIDefenitions.getCustomViewUI(mediaType);
    }

    public static String getCustomAddUI(String mediaType, HttpSession session) {

        if (mediaType == null) {
            return null;
        }

       CustomUIDefenitions customUIDefenitions = (CustomUIDefenitions) session.getServletContext().
                getAttribute(CustomUIDefenitions.CUSTOM_UI_DEFENITIONS);
        @SuppressWarnings("unchecked")
        Map<String, String> customAddUIMap =
                (Map<String, String>)session.getAttribute("customAddUI");
        if (customAddUIMap != null) {
            String customAddUI = customAddUIMap.get(mediaType);
            if (customAddUI != null) {
                return customAddUI;
            }
        }
        return customUIDefenitions.getCustomAddUI(MediaTypesUtils.getMimeTypeFromHumanReadableMediaType(mediaType));
    }
}
