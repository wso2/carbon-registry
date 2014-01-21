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
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ page import="org.wso2.carbon.registry.common.utils.CommonUtil" %>
<%@ page import="org.wso2.carbon.registry.common.utils.RegistryUtil" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.Utils" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.MetadataBean" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.VersionsBean" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.common.xsd.WebResourcePath" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String viewMode = Utils.getResourceViewMode(request);
    String synapseRegistryMetaDataRootPath =
            RegistryConstants.ROOT_PATH + "carbon" + RegistryConstants.PATH_SEPARATOR + "synapse-registries";
    String resourceConsumer = Utils.getResourceConsumer(request);
    String targetDivID = Utils.getTargetDivID(request);
    String synapseRegistryRoot = "";
    MetadataBean metadata;
    ResourceData [] resourceData;

    ResourceServiceClient client;
    try {
        /*if(request.getParameter("item") != null){
            client = new ResourceServiceClient(cookie, config, session);
            metadata = client.getMetadata(request,request.getParameter("item"));
//            metadata = client.getMetadata(request);

        }
        else{*/

            client = new ResourceServiceClient(cookie, config, session);
            metadata = client.getMetadata(request);
        /*}*/
    } catch (Exception e) {
        request.setAttribute(CarbonUIMessage.ID,new CarbonUIMessage(RegistryConstants.REGISTRY_UNAUTHORIZED_ERROR,RegistryConstants.REGISTRY_UNAUTHORIZED_ERROR,null));
%>
<jsp:forward page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }
%>

<table cellspacing="0" cellpadding="0" border="0" style="width:100%">
    <tr class="top-toolbar-back">
        <td valign="middle" style="width:35px;" nowrap="nowrap">

            <!-- Page resource path prints here -->

            <%
                WebResourcePath[] iNavPaths = metadata.getNavigatablePaths();
                String path = "";
                if (iNavPaths.length > 0) {
                    WebResourcePath rootPath = iNavPaths[0];
                    try {
                        client = new ResourceServiceClient(cookie, config, session);
                        synapseRegistryRoot = client.getProperty(synapseRegistryMetaDataRootPath, "SYNAPSE_REGISTRY_ROOT");
                        path = RegistryConstants.ROOT_PATH;
                    } catch (Exception ignored) {
                    }

            %>
            <a href="#" style="font-size:10px;font-weight:bold;position:absolute;margin-top:-10px;margin-left:5px;"
               onclick="document.location.href = '../resources/resource.jsp?region=region3&item=resource_browser_menu&path=/&viewType=std&screenWidth=' + screen.width;"
               title="Go to root resource">Root<br/><img
                    src="../resources/images/to-root.gif" border="0" align="top"/></a>
        </td>
        <td valign="middle" style="padding-left:0px;"><a class="registry-breadcrumb"
                                                         href="#"
                                                         onclick="document.location.href = '../resources/resource.jsp?region=region3&item=resource_browser_menu&path=/&viewType=std&screenWidth=' + screen.width;"
                                                         title="root">/</a><%


            if (iNavPaths.length > 1) {
                WebResourcePath childPath = iNavPaths[1];
                path = childPath.getNavigatePath();
                try {
                    client = new ResourceServiceClient(cookie, config, session);
                    String paths [] = {path};
                    resourceData = client.getResourceData(paths);
                } catch (Exception e) {
                    %>
                        <jsp:forward page="../admin/error.jsp?<%=e.getMessage()%>"/>
                    <%
                    return;
                }

                if (resourceData != null && resourceData[0] != null && (resourceData[0].getMounted() || resourceData[0].getLink())) {
                    %><a class="registry-breadcrumb" href="#"
             onclick="loadResourcePage('<%=childPath.getNavigatePath()%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"><i><%=childPath.getNavigateName()%></i></a><%
                } else {
        %><a class="registry-breadcrumb" href="#"
             onclick="loadResourcePage('<%=childPath.getNavigatePath()%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"><%=childPath.getNavigateName()%></a><%

                }
            }

            if (iNavPaths.length > 2) {
                for (int i = 2; i < iNavPaths.length; i++) {
                    WebResourcePath resourcePath = iNavPaths[i];
                    path = resourcePath.getNavigatePath();

                    try {
                    client = new ResourceServiceClient(cookie, config, session);
                    String paths [] = {path};
                    resourceData = client.getResourceData(paths);
                } catch (Exception e) {
                    %>
                        <jsp:forward page="../admin/error.jsp?<%=e.getMessage()%>"/>
                    <%
                    return;
                }

                if (resourceData != null && resourceData[0] != null && (resourceData[0].getMounted() || resourceData[0].getLink())) {
                    %>/<a class="registry-breadcrumb" href="#"
              onclick="loadResourcePage('<%=resourcePath.getNavigatePath()%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"><div style="display:inline" id=<%="pathResult"+i %>><i><%=resourcePath.getNavigateName()%></i></div></a><%
            } else {
        %>/<a class="registry-breadcrumb" href="#"
              onclick="loadResourcePage('<%=resourcePath.getNavigatePath()%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"><div style="display:inline" id=<%="pathResult"+i %>><%=resourcePath.getNavigateName()%></div></a><%
                    }
                }
            }
            if (path != null && !"".equals(path)) {
                String correctPath = Utils.resolveResourceKey(path, synapseRegistryRoot);
        %>
            <script type="text/javascript">
                setResourcePathOnConsumer('<%=resourceConsumer%>', '<%=correctPath%>',<%=iNavPaths.length%>);
            </script>
            <%
                    }
                }
            %>

            <span style="clear:both;"/>
            <input id="hidden_media_type" type="hidden" value="<%=metadata.getMediaType()%>"/>
        </td>

    </tr>
</table>