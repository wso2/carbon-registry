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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.registry.common.utils.CommonUtil" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.VersionsBean" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath" %>
<%@ page import="java.net.URLEncoder" %>
<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    VersionsBean versionBean;
    try {
        ResourceServiceClient client = new ResourceServiceClient(cookie, config, session);
        String path = request.getParameter("path");
        versionBean = client.getVersionsBean(path);
    } catch (Exception e) {
%>
<jsp:forward page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }
    String tempPath = versionBean.getResourcePath();
    try {
        tempPath = URLEncoder.encode(tempPath, "UTF-8");
    } catch (Exception ignore) {}
%>
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.registry.resource.ui.i18n.JSResources"
        request="<%=request%>" namespace="org.wso2.carbon.registry.resource.ui"/>

<script lanuage="text/javascript">
function setAndGo(fullpath,activepath){
	var random = Math.floor(Math.random() * 2000);
	new Ajax.Request('../resources/set_version_restore_ajaxprocessor.jsp',
	{
	    method:'get',
	    parameters: {fullpath: fullpath,activepath:activepath,random:random},
	
	    onSuccess: function() {
	        window.location = "./resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path="+fullpath.replace(/&/g,"%26");
	    },
	
	    onFailure: function() {
	    }
	});
}

function submitDelete(path, snapshotId, screenWidth){
        CARBON.showConfirmationDialog(
                   org_wso2_carbon_registry_resource_ui_jsi18n["are.you.sure.you.want.to.delete.version"] +
                   " <strong> " + snapshotId + " </strong> " +
                   org_wso2_carbon_registry_resource_ui_jsi18n["delete.version.description"], function() {

                   new Ajax.Request('../resources/delete_version_ajaxprocessor.jsp',
                   	{
                   	    method:'get',
                   	    parameters: {path: path,snapshotId:snapshotId},

                   	    onSuccess: function() {
                   	        window.location = "./versions.jsp?path=" + path + "&screenWidth=" + screenWidth;
                   	    },

                   	    onFailure: function() {
                   	        CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["could.not.delete.the.version.history"]
                   	                                + snapshotId);
                   	    }
                   	});
               }
          );


}
</script>
<script type="text/javascript">
    function retentionError() {
        CARBON.showWarningDialog("This resource currently under retention");
        return;
    }
</script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<link rel="stylesheet" type="text/css" href="css/registry.css"/>
<fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">
    <carbon:breadcrumb label="versions"
                       resourceBundle="org.wso2.carbon.registry.resource.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <div id="middle">

        <h2><fmt:message key="versions.of"/> <a style="font-size:80%;line-height:18px;"
                href="resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=<%=tempPath.replaceAll("&","%26")%>">
                <%
               		String resourcePath = versionBean.getResourcePath();
                	String firstPart = "";
                	String lastPart = "";
			String strScreenWidth = request.getParameter("screenWidth");
			
			int lastSlashIndex = 0;
			int intScreenWidth = ((strScreenWidth != null) ? Integer.parseInt(strScreenWidth) : 0);
			int maxChars = 0;
			
			if (intScreenWidth > 1024)
			{
				maxChars = 175;
			}
			else
			{
				maxChars = 100;
			}
			
			if (resourcePath.length() > maxChars)
			{
				firstPart = resourcePath.substring(0,maxChars);
				lastSlashIndex = firstPart.lastIndexOf("/");
				firstPart = resourcePath.substring(0,lastSlashIndex);
				
				lastPart = versionBean.getResourcePath().substring(lastSlashIndex);
				resourcePath = "<br/>"+firstPart+"<br/>"+lastPart;
			}
			else
			{
				resourcePath = "<br/>"+versionBean.getResourcePath();
			}
	                out.println(resourcePath);
                %>
        </a></h2>

        <div id="workArea">

            <!-- all the content goes here -->
            <table id="versionsTable" class="styledLeft">
                <%
                    if (versionBean.getVersionPaths().length > 0) {
                %>
                <thead>
                <tr>
                    <th><fmt:message key="version"/></th>
                    <th><fmt:message key="last.modified.date"/></th>
                    <th><fmt:message key="last.modified.by"/></th>
                    <th><fmt:message key="actions"/></th>
                </tr>
                </thead>

                <%
                    } else {
                %>
                <tr>
                    <td colspan="4">
                        <strong><fmt:message key="no.versions.available.for.this.resource.or.resource.collection"/>. </strong>
                    </td>
                </tr>
                <%
                    }

                    for (VersionPath versionPath : versionBean.getVersionPaths()) {
                        String vpath = versionPath.getCompleteVersionPath();
                        String path = versionPath.getActiveResourcePath();
                        try {
                            vpath = URLEncoder.encode(vpath, "UTF-8");
                            path = URLEncoder.encode(path, "UTF-8");
                        } catch (Exception ignore) {}
                %>

                <tr>
                    <td>
                        <%=versionPath.getVersionNumber()%>
                    </td>
                    <td>
                        <%=CommonUtil.formatDate(versionPath.getUpdatedOn().getTime())%>
                    </td>
                    <td>
                        <%=versionPath.getUpdater()%>
                    </td>
                    <td>
                        <a class="details-icon-link" onclick="setAndGo('<%=vpath%>','<%=path%>')"><fmt:message key="details"/></a>
                        <% if (versionBean.getLoggedIn() && versionBean.getPutAllowed() && !Boolean.parseBoolean(versionBean.getWriteLocked())) { %>
                        <a href="./restore_version_ajaxprocessor.jsp?versionPath=<%=vpath%>&path=<%=path%>"
                           class="restore-icon-link registryWriteOperation"><fmt:message key="restore"/></a>
                        <% } %>
                        <%
                           if(versionBean.getLoggedIn() && versionBean.getDeletePermissionAllowed() && !Boolean.parseBoolean(versionBean.getDeleteLocked())){
                        %>
                        <a class="delete-icon-link" onclick="submitDelete('<%=path%>','<%=versionPath.getVersionNumber()%>','<%=strScreenWidth%>')"><fmt:message key="delete.version"/></a>
                        <%
                           }
                        %>

                    </td>
                </tr>


                <%
                    }
                %>
            </table>


        </div>
    </div>

    <script type="text/javascript">
        alternateTableRows('versionsTable', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>
