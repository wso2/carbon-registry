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
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.CustomUIHandler" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.Utils" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.ContentBean" %>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>

<%
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String viewMode = Utils.getResourceViewMode(request);
        boolean isInlinedView = "inlined".equals(viewMode);
        ContentBean cb;
        boolean isRemote = false;
        boolean isLink = false;
        ResourceServiceClient client;
        boolean hasDependencies = false;

        try {
            client = new ResourceServiceClient(cookie, config, session);
            hasDependencies = client.hasAssociations(request.getParameter("path"),"depends");

            cb = client.getContent(request);
            if (client.getProperty(cb.getPathWithVersion(), "registry.realpath") != null) {
                isRemote = true;
            }
            if (client.getProperty(cb.getPathWithVersion(), "registry.link") != null) {
                isLink = true;
            }
        } catch (Exception ignored) {
            return;
        }
%>
<fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">
<%
        String cuiURL = CustomUIHandler.getCustomViewUI(cb.getMediaType(), request.getSession());
        String mode = request.getParameter("mode");
        boolean cui = false;
        if (cuiURL != null && !"standard".equals(mode)) {
            cui = true;
        }
if(!cb.getAbsent().equals("true")){
    %>

    <div class="box1-head">
        <table cellspacing="0" cellpadding="0" border="0" style="width:100%">
            <tr>
                <td valign="top">
                    <h2 class="sub-headding-entries"><%if (cb.getCollection()) { %>
                        <fmt:message key="entries"/> <% } else { %>
                        <fmt:message key="content"/> <% } %></h2>
                </td>
                <td align="right" valign="top" class="expanIconCell">

                    <a onclick="javascript: showHideCommon('entriesIconExpanded');showHideCommon('entriesIconMinimized');showHideCommon('entriesExpanded');showHideCommon('entriesMinimized');">
                        <img src="../resources/images/icon-expanded.gif" border="0" align="top"
                             id="entriesIconExpanded"/>
                        <img src="../resources/images/icon-minimized.gif" border="0" align="top"
                             id="entriesIconMinimized" style="display:none;"/>
                    </a>


                </td>

            </tr>
        </table>
    </div>
    <div class="box1-mid-fill" id="entriesMinimized" style="display:none"></div>
    <div class="box1-mid" id="entriesExpanded">
        <!-- all the content goes here -->
        <% if (cui) { %>
        <% if (!cuiURL.contains("hideStandardView=true")) {
            if (cuiURL.contains("hideEditView=true")) { %>
        <a onclick="viewStandardContentSectionWithNoEdit('<%=cb.getPathWithVersion()%>')"><fmt:message
                key="standard.view"/></a><br/>
        <%  } else {
        %>
        <a id="stdViewLink" onclick="viewStandardContentSection('<%=cb.getPathWithVersion()%>')"><fmt:message
                key="standard.view"/></a><br/>
        <% }
            }%>
        <div id="customViewUIDiv">
            <jsp:include page="<%=cuiURL%>"/>
        </div>
        <%
        } else {

            if (cuiURL != null) {
        %>
        <a id="custViewLink" onclick="viewCustomContentSection('<%=cb.getPathWithVersion()%>')"><fmt:message
                key="custom.view"/></a><br/>
        <%
            }
            if (cb.getCollection()) {
        %>

        <%
            if (!isInlinedView) {
        %>

        <% if (cb.getPutAllowed() && !cb.getVersionView() && cb.getLoggedIn() && !cui) { %>
        <div class="registryWriteOperation" style="display:block;height:30px;">
            <a class="add-resource-icon-link registryWriteOperation"
               onclick="showHide('add-resource-div');resetResourceForms()"><fmt:message
                    key="add.resource"/></a>
        </div>


        <div class="registryWriteOperation" style="display:block;height:30px;">
            <a class="add-collection-icon-link registryWriteOperation"
               onclick="javascript: showHide('add-folder-div');expandIfNot('entries');if($('add-folder-div').style.display!='none')$('collectionName').focus();">
                <fmt:message key="add.collection"/></a>
        </div>
        <% if (!isRemote & !isLink) { %>
        <div class="registryWriteOperation" style="display:block;height:30px;">
            <a class="add-link-icon-link registryWriteOperation"
               onclick="javascript: showHide('add-link-div');resetLinkForms()">
                <fmt:message key="add.link"/></a>
        </div>
        <% } %>
        <% }
        }%>

        <jsp:include page="raw-collection-content.jsp"/>

        <% } else {
        %>

        <table style="*width:430px !important;">
        <tr>
        <% if (cb.getMediaType() != null) {
            String type = null;
            if (cb.getMediaType().equals("application/wsdl+xml")) {
                type = "wsdl";
            } else if (cb.getMediaType().equals("application/x-xsd+xml")) {
                type = "xsd";
            }
            if (type != null) {
        %>
        <td>
        <a onclick="visualizeXML('<%=cb.getPathWithVersion()%>', '<%=type%>')"><img src="../resources/images/visualize.png" alt="" align="top"> <fmt:message
                key="visualize"/></a>

        </td>
        <td style="vertical-align:middle;padding-left:5px;padding-right:5px;">|</td>
        <%
            }
        %>
        <%
            if(!(cb.getMediaType().matches("image/.*")
                    || cb.getMediaType().matches("audio/.*")
                    || cb.getMediaType().matches("chemical/.*")
                    || cb.getMediaType().matches("video/.*")
                    || cb.getMediaType().matches("inode/.*")
                    || cb.getMediaType().matches("model/.*")
                    || cb.getMediaType().matches("multipart/.*"))){
        %>
        <td>
        <a onclick="displayContentAsText('<%=cb.getPathWithVersion()%>')"><img src="../admin/images/view.gif" alt="" align="top"> <fmt:message
                key="display.as.text"/></a>

        </td>
        <% if (cb.getPutAllowed() && !cb.getVersionView() && !"true".equals(request.getParameter("hideEdit")) && !cb.getMediaType().equals("application/vnd.wso2.registry-ext-type+xml")) { %>
        <td class="registryWriteOperation" style="vertical-align:middle;padding-left:5px;padding-right:5px;">|</td>
        <td>
        <a onclick="displayEditContentAsText('<%=cb.getPathWithVersion()%>')" class="registryWriteOperation"><img src="../admin/images/edit.gif" alt="" align="top"> <fmt:message key="edit.as.text"/></a>
        </td>
        <% } %>
        <% if (cb.getPutAllowed() && !cb.getVersionView() && !"true".equals(request.getParameter("hideEdit")) && !cb.getMediaType().equals("application/vnd.wso2.registry-ext-type+xml")) { %>
        <td class="registryWriteOperation" style="vertical-align:middle;padding-left:5px;padding-right:5px;">|</td>
        <% } %><% } %>
        <% if (cb.getPutAllowed() && !cb.getVersionView() && !"true".equals(request.getParameter("hideEdit")) && !cb.getMediaType().equals("application/vnd.wso2.registry-ext-type+xml")) { %>
        <td>
        <a onclick="displayUploadContent('<%=cb.getPathWithVersion()%>')" class="registryWriteOperation">
            <img src="../resources/images/icon-upload.jpg" alt="" align="top"> <fmt:message key="upload"/></a>
        </td>
        <% } %>
        <td style="vertical-align:middle;padding-left:5px;padding-right:5px;">|</td>
        <% } %>
        <td>
            <%
                String path;
            if (cb.getRealPath() != null) {
                path = cb.getRealPath();
            } else {
                path = cb.getPathWithVersion();
            }
            if (path.startsWith("http")) {
                %>
                <a class="icon-link" style="background-image:url(../resources/images/icon-download.jpg);"
		           href="<%=path%>"
		           target="_blank"><fmt:message key="download"/></a>
       <%
            } else {
        %>
	    <a onclick="javascript:downloadWithDependencies('<%=Utils.getResourceDownloadURL(request, path)%>',<%=String.valueOf(hasDependencies)%>)"
		           class="registryWriteOperation"><img src="../resources/images/icon-download.jpg" alt="" align="top"> <fmt:message key="download"/></a>
	    <% }
            %>
        </td>
        </tr>
        </table>
        <br/>
        <br/>

        <div id="generalContentDiv" style="display:none;">
        </div>
        
        <% }
        } %>

    </div>
 <%}%>
</fmt:bundle>
