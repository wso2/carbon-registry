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

package org.wso2.carbon.registry.cmis;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.registry.core.Resource;

/**
 * Utility class for mapping Registry paths to CMIS paths
 * However this is not used much since the greg root path is used for the cmis root path.
 */
public class PathManager {
    private static final Logger log = LoggerFactory.getLogger(PathManager.class);

    /**
     * Identifier of the root folder
     */
    public static final String CMIS_ROOT_ID = "/";

    /**
     * Root path
     */
    public static final String CMIS_ROOT_PATH = "/";

    private String gregRootPath = "/";

    /**
     * @return  the Registry root path
     */
    public String getGregRootPath() {
        return gregRootPath;
    }

    /**
     * Determines whether a Registry <code>Node</code> is the root node wrt. to this
     * <code>PathManager</code> instance. That is, whether the path of the node is
     * equal to this instance's Registry root path.
     * 
     * @param node
     * @return  <code>true</code> iff <code>node</code> is the root node wrt. to
     *      this <code>PathManager</code> instance.
     */
    public boolean isRoot(Resource node) {
        
    	return node.getPath().equals(gregRootPath);
    }

    /**
     * Determine the CMIS path given a Registry <code>Node</code>.
     *
     * @param node
     * @return
     * @throws IllegalArgumentException  when <code>node</code> is not part of the hierarchy
     */
    public String getPath(Resource node) {
        if (gregRootPath.length() > node.getPath().length()) {
		    throw new IllegalArgumentException("Node is not part of the hierarchy: " + node.getPath());
		}

		String path = node.getPath().substring(gregRootPath.length());
		return path.startsWith("/") ? path 
									 : '/' + path;
    
    }

    /**
     * @param cmisPath
     * @return  <code>true</code> iff <code>cmisPath</code> equals {@link PathManager#CMIS_ROOT_PATH}
     */
    public static boolean isRoot(String cmisPath) {
        return CMIS_ROOT_PATH.equals(cmisPath);
    }

    /**
     * @param cmisPath
     * @return  <code>true</code> iff <code>cmisPath</code>
     */
    public static boolean isAbsolute(String cmisPath) {
        return cmisPath.startsWith(CMIS_ROOT_PATH);
    }

    /**
     * Create a CMIS path from a parent path and a child element
     * @param cmisPath  parent path
     * @param child  child element
     * @return
     */
    public static String createCmisPath(String cmisPath, String child) {
        return cmisPath.length() > 0 && cmisPath.charAt(cmisPath.length() - 1) == '/'
                ? cmisPath + child
                : cmisPath + '/' + child;
    }

    /**
     * Relativize an CMIS path wrt. to a prefix.  
     * @param prefix
     * @param cmisPath
     * @return  a string <code>r</code> such that <code>prefix</code> + <code>r</code> = <code>cmisPath</code> 
     * @throws IllegalArgumentException  if <code>prefix</code> is not a prefix of <code>cmisPath</code>
     */
    public static String relativize(String prefix, String cmisPath) {
        if (cmisPath.startsWith(prefix)) {
            return cmisPath.substring(prefix.length());
        } else {
            throw new IllegalArgumentException(prefix + " is not a prefix of " + cmisPath);
        }
    }

    private static String normalize(String path) {
        if (path == null || path.length() == 0) {
            return CMIS_ROOT_PATH;
        }

        if (!path.startsWith("/")) {
            throw new CmisInvalidArgumentException("Root path must be absolute. Got: " + path);
        }

        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

}
