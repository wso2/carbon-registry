<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"  %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData"%>
<%@ page import="org.wso2.carbon.registry.common.ui.UIConstants" %>
<%
    ResourceServiceClient client = new ResourceServiceClient(config, session);
    boolean resourceExists = false;
    try {
        if (client.getResourceTreeEntry(request.getParameter("pickedPath")) != null) {
            resourceExists = true;
        }
    } catch (Exception e) {
        resourceExists = false;
    }
    if (resourceExists) {
        if(request.getParameter("differentiate") != null) {
            ResourceData[] resourceData = client.getResourceData(new String[]{request.getParameter("pickedPath")});
            if (resourceData != null && resourceData[0].getResourceType().equals(UIConstants.COLLECTION)) {
                %>----CollectionExists----<%
            } else {
                %>----ResourceExists----<%
            }
        } else {
            %>----ResourceExists----<%
        }
    }
%>