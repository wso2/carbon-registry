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
<%@ page import="org.wso2.carbon.registry.resource.ui.Utils" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.common.xsd.WebResourcePath" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.MetadataBean" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.processors.TempEditMediaTypeProcessor"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.common.ui.utils.UIUtil" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.RegistryAdminServiceClient" %>
<%@ page import="org.wso2.carbon.registry.core.utils.MediaTypesUtils" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.net.URLDecoder" %>

<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String viewMode = Utils.getResourceViewMode(request);
    String resourceConsumer = Utils.getResourceConsumer(request);
    String targetDivID = Utils.getTargetDivID(request);
    String httpPermalink = "#";
    String httpsPermalink = "#";
    MetadataBean metadata;
    try {
/*        if(request.getParameter("item") != null){   //recognize the request which comes when user click resource link
            ResourceServiceClient client = new ResourceServiceClient(cookie, config, session);
            metadata = client.getMetadata(request,request.getParameter("item"));
//            metadata = client.getMetadata(request);
        }
        else{*/

            ResourceServiceClient client = new ResourceServiceClient(cookie, config, session);
            metadata = client.getMetadata(request);
            RegistryAdminServiceClient regAdminClient = new RegistryAdminServiceClient(cookie, config, session);
            httpPermalink = regAdminClient.getHTTPPermalink(metadata.getPath());
            httpsPermalink = regAdminClient.getHTTPSPermalink(metadata.getPath());
        /*}*/
    }catch (Exception ignored) {
%>
<fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">
<script type="text/javascript">
    CARBON.showErrorDialog('<fmt:message key="unable.to.load.resource"/>', function() {
        window.location = "../admin/index.jsp";
    }, function() {
        window.location = "../admin/index.jsp";
    });
</script>
</fmt:bundle>
<%
        return;
    }
    String versionRestoreFullpath = "";
    if(session.getAttribute( "versionRestoreFullpath" )!=null){
		versionRestoreFullpath = (String)session.getAttribute( "versionRestoreFullpath" );
    }
    String versionRestoreActivepath = "";
    if(session.getAttribute( "versionRestoreActivepath" )!=null){
		versionRestoreActivepath = (String)session.getAttribute( "versionRestoreActivepath" );
    }
%>

<fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">

<script type="text/javascript">
   function retentionError(){
       CARBON.showWarningDialog('<fmt:message key="retention.warn"/>' );
       return;
   }
</script>

<div class="box1-head">
    <table cellspacing="0" cellpadding="0" border="0" style="width:100%">
        <tr>
            <td>
                <h2>
                    <fmt:message key="metadata"/>
                    <%--<%
                        WebResourcePath[] iNavPaths = metadata.getNavigatablePaths();
                        boolean found = false;
                        if (iNavPaths.length > 0) {
                            WebResourcePath rootPath = iNavPaths[0];

                            if (iNavPaths.length > 2) {
                                for (int i = 2; i < iNavPaths.length; i++) {
                                    WebResourcePath resourcePath = iNavPaths[i];

                                    if (i == iNavPaths.length - 1) {
                                        found = true;
                    %>
                    <%=resourcePath.getNavigateName()%>
                    <%
                                }
                            }
                        }
                        if (iNavPaths.length > 1 && !found) {
                            WebResourcePath childPath = iNavPaths[1];
                            found = true;
                    %>
                    <%=childPath.getNavigateName()%>
                    <%
                            }
                        }
                        if (!found) {
                    %>
                    root /
                    <%
                        }
                    %>--%>
                </h2>

            </td>
            <td align="right" valign="top" class="box1-head-right">
                <table cellpadding="0" cellspacing="0" border="0" style="margin-top:5px;">
                    <tr>
                        <td>
                        </td>
                        <td>
                            <a onclick="showHideCommon('mainDetailsIconExpanded');showHideCommon('mainDetailsIconMinimized');showHideCommon('resourceMainDetails');showHideCommon('resourceMainDetailsMin');">
                                <img src="../resources/images/icon-expanded.gif" border="0"
                                     align="top"
                                     id="mainDetailsIconExpanded" style="display:none;"/>
                                <img src="../resources/images/icon-minimized.gif" border="0"
                                     align="top"
                                     id="mainDetailsIconMinimized"/>
                            </a>
                        </td>
                    </tr>
                </table>
            </td>

        </tr>
    </table>
</div>


<div class="box1-mid" id="resourceMainDetails" style="display:none">
    <table class="normal" style="width:100%;">
        <tr>

            <% if (metadata.getVersionView()) { %>
            <td>
                <a
                        style="background-image:url(../resources/images/back.gif)"
                        class="icon-link"
                        onclick="document.location.href = '../resources/versions.jsp?path=' + '<%=metadata.getPath()%>'.replace('&', '%26')+ '&ordinal=1&screenWidth=' + screen.width;">
                    <fmt:message key="back.to.versions.of"/> <%=metadata.getPath()%>
                </a>

                <% if (metadata.getPutAllowed()) {
                    String decodedPath = versionRestoreFullpath;
                    try {
                        decodedPath = URLDecoder.decode(decodedPath, "UTF-8");
                    } catch (Exception ignore) {}
                    String encodedActivePath = versionRestoreActivepath;
//                    try {
//                        encodedActivePath = URLEncoder.encode(encodedActivePath, "UTF-8");
//                    } catch (Exception ignore) {}
                %>
                <%
                    if(Boolean.valueOf(metadata.getDeleteLocked()) || Boolean.valueOf(metadata.getWriteLocked())) {
                %>
                <a
                        style="background-image:url(../resources/images/icon-restore.gif)"
                        class="icon-link registryWriteOperation"
                        onclick="retentionError();"
                        >
                    <fmt:message key="restore.to.this.version"/> (<span style="text-decoration:italic"><%=decodedPath%></span>) </a>
            </td>
            <% } else { %>
            <a
                    style="background-image:url(../resources/images/icon-restore.gif)"
                    class="icon-link registryWriteOperation"
                    href="./restore_version_ajaxprocessor.jsp?versionPath=<%=versionRestoreFullpath%>&path=<%=encodedActivePath%>"
                    >
                <fmt:message key="restore.to.this.version"/> (<span style="text-decoration:italic"><%=decodedPath%></span>) </a>
           </td>
            <% } %>
            <% } %>
            <% } %>
            <td align="right" style="text-align:right !important;">
                <% if(!metadata.getVersionView()) { %>
                <%
                    String atomPath = metadata.getPath();
                    // If this is an entry, can't subscribe directly, so use logs feed
                    if (!metadata.getCollection()) {
                        atomPath += ";logs";
                    }
                %>
                <a
                        style="float:right;background-image:url(images/icon-feed-small.gif);"
                        href="<%=UIUtil.getAtomURL(config, request, atomPath)%>"
                        target="_blank"
                        class="icon-link"
                        title="Subscribe to the resource feed">
                    <fmt:message key="feed"/>
                </a>
                <% } %>
            </td>
        </tr>
    </table>


    <table cellpadding="0" cellspacing="0" border="0" class="styledLeft" id="mainDetails">
        <tbody>
        <tr>
            <td style="width:140px;"><fmt:message key="created"/>:</td>
            <td colspan="2">
                <fmt:message
                        key="by"/> <%=metadata.getAuthor()%> <%=metadata.getFormattedCreatedOn()%>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="last.updated"/>:</td>
            <td colspan="2">
                <fmt:message
                        key="by"/> <%=metadata.getLastUpdater()%> <%=metadata.getFormattedLastModified()%>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="media.type1"/>:</td>
            <td colspan="2">
                     <div style="width:100%">
                    <div id="toggleSaveMediaType_view" style="float:left;line-height: 25px;"><% if (metadata.getMediaType() != null && metadata.getMediaType().length() != 0) { String mediaType = MediaTypesUtils.getHumanReadableMediaTypeFromMimeType(metadata.getMediaType());
TempEditMediaTypeProcessor.setMediaTypeBeforeUpdate(mediaType);%><%=mediaType%><% } else { %>
                            <fmt:message key="unknown"/><% } %>
                            </div>
                            <input style="display:none;float:left" id="toggleSaveMediaType_edit" value="<% if (metadata.getMediaType() != null && metadata.getMediaType().length() != 0) { %><%=MediaTypesUtils.getHumanReadableMediaTypeFromMimeType(metadata.getMediaType())%><% } else { %><fmt:message key="unknown"/><% } %>" />
                            &nbsp;
                            &nbsp;
                            &nbsp;
                            <%
                            if (metadata.getPutAllowed() && !metadata.getVersionView()) {
                                if(Boolean.parseBoolean(metadata.getWriteLocked())) {
                            %>
                            <a  id="toggleSaveMediaType_editBtn" class="icon-link registryWriteOperation" style="background-image:url(../admin/images/edit.gif);"
                               onclick="retentionError();">
                                <fmt:message key="edit"/>
                            </a>
                            <%}else {%>
                            <a id="toggleSaveMediaType_editBtn"  class="icon-link registryWriteOperation" style="background-image:url(../admin/images/edit.gif);"
                               onclick="toggleSaveMediaType()">
                                <fmt:message key="edit"/>
                            </a>
                            <%}
                            }%>

                            <a  class="icon-link" style="background-image:url(../properties/images/save-button.gif);display:none" id="toggleSaveMediaType_saveBtn" onclick="updateMediaType('<%=metadata.getPath()%>' ,document.getElementById('toggleSaveMediaType_edit').value);">
                                Save
                            </a>
                            &nbsp;
                            &nbsp;
                            <a class="icon-link" style="background-image:url(../admin/images/cancel.gif); display:none;"  id="toggleSaveMediaType_cancelBtn" onclick="toggleSaveMediaType()">
                                Cancel
                            </a>
                         </div>

            </td>

        </tr>

        <%  String permaLink = metadata.getPermalink();
            boolean hide = false;
            if (permaLink != null) {
                if (permaLink.equals("hide")) {
                    hide = true;
                }
            }
            if (!hide) { %>
        <tr>
            <td valign="top"><fmt:message key="checkpoint"/>:</td>
            <td colspan="2">
                <div id="checkpointDiv">
                    <% if (!metadata.getVersionView()) { %>
                    <ul>
                        <li>
                            <% if (metadata.getPutAllowed()) { %>
                            <a class="checkpoint-icon-link registryWriteOperation"
                               style="background-image:url(../admin/images/create-checkpoint.gif);"
                               href="javascript:createVersion('<%=metadata.getPath()%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')">
                                <fmt:message key="create.checkpoint"/></a>
                            <% } %>
                        </li>
                        <li>
                        </li>
                    </ul>

                    <% } else { %>

                    <%=metadata.getPermalink()%>

                    <% } %>
                </div>
                <div id="checkpointWhileUpload" style="display:none;padding-top:0px;font-size:12px !important;margin-left:10px !important;margin-top:0 !important;" class="ajax-loading-message">
                    <img align="top" src="../resources/images/ajax-loader.gif"/>
                    <span>Process may take some time. Please wait..</span>
                </div>
            </td>
        </tr>
        <% } %>
        <% if (!hide && !metadata.getVersionView()) { %>
        <tr>
            <td><fmt:message key="versions"/>:</td>
            <td colspan="2"><a href="#"
                               onclick="loadVersionPage('<%=metadata.getPath()%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"
                               class="view-icon-link"><fmt:message key="view.versions"/></a></td>
        </tr>
        <% if (!metadata.getCollection()) { %>
        <tr>
            <td><fmt:message key="permalink"/>:</td>
            <td><div id="linkButtons" style="display:block;"><a class="checkpoint-icon-link" target="_blank"
                               style="background-image:url(../resources/images/goto_url.gif);"
                               href="<%=httpPermalink%>">
                                <fmt:message key="http"/></a><a class="icon-link"  target="_blank"
                               style="background-image:url(../resources/images/goto_url.gif);"
                               href="<%=httpsPermalink%>">
                                <fmt:message key="https"/></a></div></td>
        </tr>
        <% } %>
        <% } else if (!metadata.getCollection()) {
            String permalink = metadata.getPermalink();
            if (permalink != null && permalink.indexOf(";version:") > 0) {
                if (httpPermalink.indexOf(";version:") > 0) {
                    httpPermalink = httpPermalink.substring(0, httpPermalink.lastIndexOf(";version:")) +
                            permalink.substring(permalink.lastIndexOf(";version:"));
                }
                if (httpsPermalink.indexOf(";version:") > 0) {
                    httpsPermalink = httpsPermalink.substring(0, httpsPermalink.lastIndexOf(";version:")) +
                            permalink.substring(permalink.lastIndexOf(";version:"));
                }
            }
        %>
        <tr>
            <td><fmt:message key="permalink"/>:</td>
            <td><div id="linkButtonsVersion" style="display:block;"><a class="checkpoint-icon-link" target="_blank"
                               style="background-image:url(../resources/images/goto_url.gif);"
                               href="<%=httpPermalink%>">
                                <fmt:message key="http"/></a><a class="icon-link"  target="_blank"
                               style="background-image:url(../resources/images/goto_url.gif);"
                               href="<%=httpsPermalink%>">
                                <fmt:message key="https"/></a></div></td>
        </tr>
        <% } %>

        <tr>
            <td style="width:120px; padding-bottom:10px; " valign="top"><fmt:message
                    key="description"/>:
            </td>
            <td valign="top" colspan="2">
                <div id="descView" style="display:block;"><% if (metadata.getDescription() != null) { %><%=metadata.getDescription()%><% } %></div>
                <% if (metadata.getPutAllowed() && !metadata.getVersionView()) { %>
                <div id="editButton" class="registryWriteOperation" style="display:inline;">
                    <% if (!Boolean.parseBoolean(metadata.getWriteLocked())) {
                    %>
                    <a name="editButtonLink"
                       onclick="processDescription('<%=metadata.getPathWithVersion()%>','editing');"
                       class="edit-icon-link registryWriteOperation"><fmt:message key="edit"/></a>
                    <% } else {%>
                    <a name="editButtonLink"
                       onclick="retentionError();"
                       class="edit-icon-link registryWriteOperation"><fmt:message key="edit"/></a>

                    <%
                        }
                    %>
                </div>
                <div class="yui-skin-sam">
                    <textarea class="resource-content" id="descEdit" name="descEdit"
                              style="display:none;"></textarea>
                </div>
                <div id="saveButton" style="display:none;">
                    <a name="saveButtonLink"
                       onclick="processDescription('<%=metadata.getPathWithVersion()%>','saving');"
                       class="save-icon-link"><fmt:message key="save"/></a>
                    <a name="cancelButtonLink"
                       onclick="processDescription('','cancel');"
                       class="cancel-save-icon-link"><fmt:message key="cancel"/></a>
                </div>
                <% } %>
            </td>
        </tr>
        </tbody>
    </table>

</div>
<div class="box1-mid-fill" id="resourceMainDetailsMin">
</div>
<script type="text/javascript">
    alternateTableRows('mainDetails', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>
