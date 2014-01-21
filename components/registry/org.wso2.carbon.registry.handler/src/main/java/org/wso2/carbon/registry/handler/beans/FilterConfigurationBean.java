/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.handler.beans;

import org.apache.axiom.om.OMElement;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FilterConfigurationBean {

    private String filterClass = null;
    private Map<String, OMElement> xmlProperties = new HashMap<String, OMElement>();
    private Map<String, String> nonXmlProperties = new HashMap<String, String>();
    private List<String> propertyList = new LinkedList<String>();

    public String getFilterClass() {
        return filterClass;
    }

    public void setFilterClass(String filterClass) {
        this.filterClass = filterClass;
    }

    public Map<String, OMElement> getXmlProperties() {
        return xmlProperties;
    }

    public Map<String, String> getNonXmlProperties() {
        return nonXmlProperties;
    }

    public List<String> getPropertyList() {
        return propertyList;
    }
}
