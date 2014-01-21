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

<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.registry.common.utils.CommonUtil" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.Utils" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.VersionsBean" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath" %>
<%@ page import="java.net.URLEncoder" %>
<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String viewMode = Utils.getResourceViewMode(request);
    String resourceConsumer = Utils.getResourceConsumer(request);
    String targetDivID = Utils.getTargetDivID(request);
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
%>

<fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">

<div id="middle">
    <table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
        <thead>
            <tr>
                <th colspan="3"><fmt:message key="registry.browser"/></th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>
                    <h2><fmt:message key="versions.of"/> <a href="#"
                                                            onclick="loadResourcePage('<%=versionBean.getResourcePath()%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')">
                    </a></h2>

                    <div id="workArea">

                        <!-- all the content goes here -->
                        <table id="versionsTable" class="styledLeft">
                            <thead>
                                <tr>
                                    <th><fmt:message key="version"/></th>
                                    <th><fmt:message key="last.modified.date"/></th>
                                    <th><fmt:message key="last.modified.by"/></th>
                                    <th><fmt:message key="actions"/></th>
                                </tr>
                            </thead>
                            <tbody>

                                <%
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
                                        <a onclick="loadResourcePage('<%=vpath%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"
                                           class="details-icon-link"><fmt:message
                                                key="details"/></a>
                                        <% if (versionBean.getLoggedIn() && versionBean.getPutAllowed()) { %>
                                        <a onclick="restoreVersion('../resources/restore_version_ajaxprocessor.jsp?versionPath=<%=vpath%>&path=<%=path%>&resourceViewMode=<%=viewMode%>&resourcePathConsumer=<%=resourceConsumer%>&targetDivID=<%=targetDivID%>','<%=viewMode%>','<%=targetDivID%>')"
                                           class="restore-icon-link registryWriteOperation"><fmt:message key="restore"/></a>
                                        <% } %>
                                    </td>
                                </tr>
                                <%
                                    }
                                %>
                            </tbody>
                        </table>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="buttonRow" colspan="3">
                    <input id="cancelNSButton" class="button" name="cancelNSButton" type="button"
                           onclick="hideInLinedRegistryBrowser('<%=targetDivID%>')"
                           href="#"
                           value="Close"/>
                </td>
            </tr>
        </tbody>
    </table>
</div>

<script type="text/javascript">
    alternateTableRows('versionsTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>