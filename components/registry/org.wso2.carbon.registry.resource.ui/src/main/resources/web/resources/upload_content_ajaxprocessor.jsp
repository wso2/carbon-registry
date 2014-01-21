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

<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.MetadataBean" %>

<%
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        String path = request.getParameter("path");
        String parentPath = RegistryUtils.getParentPath(path);
        String resourceName = RegistryUtils.getResourceName(path);
        String mediaType;
        String description;
        try {
            ResourceServiceClient client = new ResourceServiceClient(cookie, config, session);
            MetadataBean metadata = client.getMetadata(request);
            mediaType = metadata.getMediaType();
            if (mediaType == null) {
                mediaType = "";
            }
            description = metadata.getDescription();
            if (description == null) {
                description = "";
            }
        } catch (Exception e) {
            response.setStatus(500);
            %><%=e.getMessage()%><%
            return;
        }
    %>
<fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">
<form method="post"
      name="updateUploadForm"
      id="updateUploadForm"
      action="../../fileupload/resource"
      enctype="multipart/form-data" target="_self">
    <input type="hidden" id="uPath" name="path" value="<%=parentPath%>"/>
    <input type="hidden" id="uMediaType" name="mediaType" value="<%=mediaType%>"/>
    <input type="hidden" id="uDescription" name="description" value="<%=description%>"/>
    <input type="hidden" id="uResourceName" name="filename" value="<%=resourceName%>"/>
    <input type="hidden" id="uRedirect" name="redirect" value="resources/resource.jsp?region=region3&item=resource_browser_menu&path=<%=path.replaceAll("&","%26")%>&viewType=std"/>
    <%
        if ("application/wsdl+xml".equals(mediaType) || "application/x-xsd+xml".equals(mediaType) || "application/policy+xml".equals(mediaType)) {
            %><input type="hidden" id="uSymlinkLocation" name="symlinkLocation" value="<%=parentPath%>"/><%
        }
    %>

    <table class="normal">
        <tr>
            <td style="padding-top:5px"><fmt:message key="file"/> <span class="required">*</span></td>
            <td><input id="uResourceFile" type="file" name="upload"
                       style="background-color:#cccccc"/>
            </td>
            <td valign="middle" style="padding-left:5px;padding-top:2px">
                <input type='button' class='button' id ="uploadContentButtonID" onclick="submitUploadUpdatedContentForm()"
                       value='<fmt:message key="upload"/>'/>
            </td>
            <td valign="middle" style="padding-left:5px;padding-top:2px">
                <input type='button' class='button' id ="cancelUploadContentButtonID" onclick='cancelTextContentEdit()'
                       value='<fmt:message key="cancel"/>'/>
            </td>
        </tr>
    </table>

</form>
</fmt:bundle>
