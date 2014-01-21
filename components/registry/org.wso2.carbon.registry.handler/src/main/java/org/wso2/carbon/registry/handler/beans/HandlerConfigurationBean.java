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

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.axiom.om.OMElement;

import java.util.*;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class HandlerConfigurationBean {

    private String handlerClass = null;
    private String[] methods = new String[0];
    private String tenant = null;
    private Map<String, OMElement> xmlProperties = new HashMap<String, OMElement>();
    private Map<String, String> nonXmlProperties = new HashMap<String, String>();
    private List<String> propertyList = new LinkedList<String>();
    private FilterConfigurationBean filter = new FilterConfigurationBean();

    public String getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(String handlerClass) {
        this.handlerClass = handlerClass;
    }

    public String[] getMethods() {
        return methods;
    }

    public void setMethods(String[] methods) {
        this.methods = methods;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
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

    public FilterConfigurationBean getFilter() {
        return filter;
    }

    public void setFilter(FilterConfigurationBean filter) {
        this.filter = filter;
    }
}
