/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.extensions.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;

public class WSDLUtil {

    private static Log log = LogFactory.getLog(WSDLUtil.class);

    /**
     * Generates a unique name after mangling the given target namespace URL.
     * :// .  are converted to a / in the location.
     * Note a trailing / is always provided in the returned path.
     * @param commonSchemaLocation
     * @param targetNamespace
     * @returns the unique name corresponding to the URL mangled location.
     */
    public static String getUniqueNameAfterURLNameMangling(String commonSchemaLocation, String targetNamespace) {
        String resourcePath;
        targetNamespace = targetNamespace.replace("://", RegistryConstants.PATH_SEPARATOR);
        targetNamespace = targetNamespace.replace(".", RegistryConstants.PATH_SEPARATOR);
        targetNamespace = targetNamespace.replace("#", RegistryConstants.PATH_SEPARATOR);

        while (targetNamespace.indexOf("//") > 0) {
            targetNamespace = targetNamespace.replace("//", "/");
        }

        if (commonSchemaLocation.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            resourcePath = new StringBuilder()
                          .append(commonSchemaLocation)
                          .append(targetNamespace).toString();
        }
        else {
            resourcePath = new StringBuilder()
                    .append(commonSchemaLocation)
                    .append(RegistryConstants.PATH_SEPARATOR)
                    .append(targetNamespace).toString();
        }

        if (!targetNamespace.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            resourcePath = new StringBuilder().append(resourcePath).append(RegistryConstants.PATH_SEPARATOR).toString();
        }

        return resourcePath;
    }

    public static String getLocationPrefix(String registryPath) {
        String[] parts = registryPath.split("/");
        StringBuffer sb = new StringBuffer();
        for (int i=0; i < (parts.length - 2); i++) {
            sb.append("../");
        }
        String prefix = sb.toString();
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length()-1);
        }
        return prefix;
    }

    public static String computeRelativePathWithVersion(String basePath, String targetPath,
                                                        Registry registry) {
        String relativePath = computeRelativePath(basePath, targetPath);
        /*if (registry == null) {
            log.warn("The Registry Instance was undefined");
            return relativePath;
        }
        if (relativePath != null) {
            String version = "";
            try {
                String[] versions = registry.getVersions(targetPath);
                if (versions != null && versions.length > 0) {
                    version = versions[0].substring(versions[0].lastIndexOf(";version:"));
                }
            } catch (RegistryException e) {
                log.error("An error occurred while determining the latest version of the " +
                        "resource at the given path: " + targetPath, e);
            }
            relativePath += version;
        }*/
        return relativePath;
    }

    public static String computeRelativePath(String basePath, String targetPath) {
        //StringTokenizer basePathTokenizer = new StringTokenizer(basePath);
        String[] basePathParts = basePath.split("/");
        String[] targetPathParts = targetPath.split("/");

        // ift
        int i;
        for (i = 1; i < basePathParts.length -1 && i < targetPathParts.length - 1; i ++) {
            if (!basePathParts[i].equals(targetPathParts[i])) {
                break;
            }
        }

        StringBuffer prefix = new StringBuffer();
        int j = i;
        for (; i < basePathParts.length - 1; i ++) {
            prefix.append("../");
        }
        if (prefix.length() == 0){
            prefix.append("./");
        }
        for (; j < targetPathParts.length - 1; j ++) {
            prefix.append(targetPathParts[j]).append("/");
        }

        return prefix.toString() + targetPathParts[targetPathParts.length - 1];
    }
}
