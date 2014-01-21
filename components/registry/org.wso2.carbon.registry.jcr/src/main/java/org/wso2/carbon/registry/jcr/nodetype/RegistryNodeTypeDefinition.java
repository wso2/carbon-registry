/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.jcr.nodetype;

import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.PropertyDefinition;

public class RegistryNodeTypeDefinition implements NodeTypeDefinition {


    public String getName() {

        return null;
    }

    public String[] getDeclaredSupertypeNames() {

        return new String[0];
    }

    public boolean isAbstract() {

        return false;
    }

    public boolean isMixin() {

        return false;
    }

    public boolean hasOrderableChildNodes() {

        return false;
    }

    public boolean isQueryable() {

        return false;
    }

    public String getPrimaryItemName() {


        return null;

    }

    public PropertyDefinition[] getDeclaredPropertyDefinitions() {

        return new PropertyDefinition[0];
    }

    public NodeDefinition[] getDeclaredChildNodeDefinitions() {

        return new NodeDefinition[0];
    }
}
