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

import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;


public class RegistryPropertyDefinition implements PropertyDefinition {
    public int getRequiredType() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getValueConstraints() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Value[] getDefaultValues() {
        return new Value[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isMultiple() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getAvailableQueryOperators() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isFullTextSearchable() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isQueryOrderable() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public NodeType getDeclaringNodeType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAutoCreated() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isMandatory() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getOnParentVersion() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isProtected() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
