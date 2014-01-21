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
package org.wso2.carbon.registry.common.ui.utils;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    private String key;
    private List<TreeNode> childNodes;

    public TreeNode(String key) {
        this.key = key;
    }

    public TreeNode(String key, String value) {
        this.key = key;
        addChild(value);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public TreeNode[] getChildNodes() {
        if (childNodes == null) {
            return null;
        }
        return childNodes.toArray(new TreeNode[childNodes.size()]);
    }

    public void setChildNodes(List<TreeNode> childNodes) {
        this.childNodes = childNodes;
    }

    public Object addChild(Object childNode) {
        if (childNodes == null) {
            childNodes = new ArrayList<TreeNode>();
        }
        if (childNode instanceof String) {
            childNodes.add(new TreeNode((String)childNode));
        }
        else if (childNode instanceof TreeNode) {
            childNodes.add((TreeNode)childNode);
        }
        return childNode;
    }
}
