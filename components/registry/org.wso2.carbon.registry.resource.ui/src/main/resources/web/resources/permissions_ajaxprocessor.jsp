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
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.PermissionBean" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.PermissionEntry" %>

<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    PermissionBean permissionBean;
    try {
        ResourceServiceClient client = new ResourceServiceClient(cookie, config, session);
        permissionBean = client.getPermissions(request);
    } catch (Exception ignored) {
%>
<fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">
<script type="text/javascript">
    CARBON.showErrorDialog('<fmt:message key="unable.to.determine.resource.permissions"/>', function() {
        window.location = "../admin/index.jsp";
    }, function() {
        window.location = "../admin/index.jsp";
    });
</script>
</fmt:bundle>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">

<!-- Hear comes the box1 table -->
<% if (permissionBean.getAuthorizeAllowed() && !permissionBean.getVersionView()) { %>
<div class="box1-head">
    <table cellspacing="0" cellpadding="0" border="0" style="width:100%">
        <tr>
            <td valign="top">
                <%--<h2 class="sub-headding-permisions"><fmt:message key="permissions"/></h2>--%>
                <h2 class="sub-headding-permisions"><fmt:message key="permissions"/></h2>
            </td>
            <td align="right" valign="middle" style="padding-top:3px;" class="box1-head-right">
                <a
                        onclick="showHideCommon('perIconExpanded');showHideCommon('perIconMinimized');showHideCommon('perExpanded');">
                    <img src="../resources/images/icon-expanded.gif" border="0" align="top" id="perIconExpanded"
                         style="display:none;"/>
                    <img src="../resources/images/icon-minimized.gif" border="0" align="top"
                         id="perIconMinimized"/>
                </a>
            </td>
        </tr>
    </table>
</div>

<div class="box1-mid" id="perExpanded" style="display:none;">
    <!-- all the content goes here -->

    <div class="success-message-pop" id="permissionSuccess" style="display:none;"></div>

    <div id="permisionReason" class="validationError" style="display: none;"></div>
    <%--<h3><fmt:message key="user.permissions"/></h3>

    <div id="userPermisionsDiv" class="userPermisionsDiv leftSpace">

        <h4><fmt:message key="add.new.permissions"/></h4>

        <form theme="simple">
            <div class="importantArea" style="margin-top:10px;">
                <table width="100%" border="0" cellpadding="5" cellspacing="0">
                    <tr id="userListRow">
                        <td><fmt:message key="user"/></td>
                        <td>
                            <select id="userToAuthorize" name="userToAuthorize"
                                    style="width:100px;">
                                <option value="-select-">-Select-
                                </option>
                                <%
                                    String[] users = permissionBean.getUserNames();
                                    if (users == null) users = new String[0];
                                    // registry UI does not allow to change the admin user
                                    for (String regUser : users) {
                                %>
                                <option value="<%=regUser%>"><%=regUser%>
                                </option>
                                <% } %>
                            </select> <!--<a href="javascript:toggleFilterRow()"><fmt:message key="filter"/></a>-->

                        </td>
                        <td align="right"><fmt:message key="action"/></td>
                        <td>
                            <select id="actionToAuthorize" name="actionToAuthorize">
                                <option value="1">-<fmt:message key="select"/>-</option>
                                <option value="2"><fmt:message key="read"/></option>
                                <option value="3"><fmt:message key="write"/></option>
                                <option value="4"><fmt:message key="delete"/></option>
                                <option value="5"><fmt:message key="authorize"/></option>
                            </select>
                        </td>
                        <td>
                            <input type="radio" id="permissionAllow" name="permissionType" value="1" checked="true"/>
                            <fmt:message key="allow"/>
                            <input type="radio" id="permissionDeny"
                                                                   name="permissionType" value="2"/>
                            <fmt:message key="deny"/>
                        </td>
                    </tr>
                    <tr id="addPermissionBtnRow">
                        <td colspan="5" align="right" class="importantAreaSeperation"
                            style="padding-top:10px;">
                            <input type="button" class="button"
                                                             value="<fmt:message key="add.permission"/>"
                                                             onclick="addUserPermission('<%=permissionBean.getPathWithVersion()%>')"/>
                        </td>
                    </tr>
                </table>
            </div>
        </form>
        <h4 style="margin-top:10px;"><fmt:message key="defined.user.permissions"/></h4>

        <form name="permissions" theme="simple" method="post">
            <input type="hidden" id="pInput" name="permissionInput" value=""/>
            <input type="hidden" name="pathToAuthorize"
                   value="<%=permissionBean.getPathWithVersion()%>"/>
            <table width="100%" class="styledLeft" border="0" cellpadding="3" cellspacing="0">
                <tbody>
                <tr class="perRow">
                    <td align="left" style="width:120px;" valign="top" class="subTH"><fmt:message key="user"/></td>
                    <td colspan="2"  align="center" class="subTH"><fmt:message key="read"/></td>
                    <td colspan="2"  align="center" class="subTH"><fmt:message key="write"/></td>
                    <td colspan="2"  align="center" class="subTH"><fmt:message key="delete"/></td>
                    <td colspan="2"  align="center" class="subTH"><fmt:message key="authorize"/></td>
                </tr>
                <tr>
                    <td align="center" class="lineSeperationRight middle-header"></td>
                    <td align="center" style="font-weight:normal;" class="middle-header"><fmt:message key="allow"/></td>
                    <td align="center" style="font-weight:normal;"
                        class="lineSeperationRight middle-header"><fmt:message key="deny"/>
                    </td>
                    <td align="center" style="font-weight:normal;" class="middle-header"><fmt:message key="allow"/></td>
                    <td align="center" style="font-weight:normal;"
                        class="lineSeperationRight middle-header"><fmt:message key="deny"/>
                    </td>
                    <td align="center" style="font-weight:normal;" class="middle-header">Allow</td>
                    <td align="center" style="font-weight:normal;"
                        class="lineSeperationRight middle-header"><fmt:message key="deny"/>
                    </td>
                    <td align="center" style="font-weight:normal;" class="middle-header"><fmt:message key="allow"/></td>
                    <td align="center" style="font-weight:normal;" class="middle-header"><fmt:message key="deny"/></td>
                </tr>


                <%
                    PermissionEntry[] userPermissions = permissionBean.getUserPermissions();
                    if (userPermissions != null && userPermissions.length != 0) {
                        for (PermissionEntry permission : userPermissions) {
                %>

                <tr>
                    <td class="lineSeperationRight"><%=permission.getUserName()%>
                    </td>
                    <td width="100"><input type="checkbox" id="<%=permission.getUserName()%>^ra"
                                           onmouseup="handlePeerCheckbox('<%=permission.getUserName()%>^ra', '<%=permission.getUserName()%>^rd')"
                                           name="<%=permission.getUserName()%>"
                                           value="ra" <% if (permission.getReadAllow()) { %>
                                           checked <% } %>/></td>
                    <td width="100" class="lineSeperationRight"><input type="checkbox"
                                                                       id="<%=permission.getUserName()%>^rd"
                                                                       onmouseup="handlePeerCheckbox('<%=permission.getUserName()%>^rd', '<%=permission.getUserName()%>^ra')"
                                                                       name="<%=permission.getUserName()%>"
                                                                       value="rd" <% if (permission.getReadDeny()) { %>
                                                                       checked <% } %>/></td>
                    <td width="100"><input type="checkbox" id="<%=permission.getUserName()%>^wa"
                                           onmouseup="handlePeerCheckbox('<%=permission.getUserName()%>^wa', '<%=permission.getUserName()%>^wd')"
                                           name="<%=permission.getUserName()%>"
                                           value="wa" <% if (permission.getWriteAllow()) { %>
                                           checked <% } %>/></td>

                    <td width="100" class="lineSeperationRight"><input type="checkbox"
                                                                       id="<%=permission.getUserName()%>^wd"
                                                                       onmouseup="handlePeerCheckbox('<%=permission.getUserName()%>^wd', '<%=permission.getUserName()%>^wa')"
                                                                       name="<%=permission.getUserName()%>"
                                                                       value="wd" <% if (permission.getWriteDeny()) { %>
                                                                       checked <% } %>/></td>
                    <td width="100"><input type="checkbox" id="<%=permission.getUserName()%>^da"
                                           onmouseup="handlePeerCheckbox('<%=permission.getUserName()%>^da', '<%=permission.getUserName()%>^dd')"
                                           name="<%=permission.getUserName()%>"
                                           value="da" <% if (permission.getDeleteAllow()) { %>
                                           checked <% } %>/></td>
                    <td width="100" class="lineSeperationRight"><input type="checkbox"
                                                                       id="<%=permission.getUserName()%>^dd"
                                                                       onmouseup="handlePeerCheckbox('<%=permission.getUserName()%>^dd', '<%=permission.getUserName()%>^da')"
                                                                       name="<%=permission.getUserName()%>"
                                                                       value="dd" <% if (permission.getDeleteDeny()) { %>
                                                                       checked <% } %>/></td>
                    <td width="100"><input type="checkbox" id="<%=permission.getUserName()%>^aa"
                                           onmouseup="handlePeerCheckbox('<%=permission.getUserName()%>^aa', '<%=permission.getUserName()%>^ad')"
                                           name="<%=permission.getUserName()%>"
                                           value="aa" <% if (permission.getAuthorizeAllow()) { %>
                                           checked <% } %>/></td>
                    <td width="100"><input type="checkbox" id="<%=permission.getUserName()%>^ad"
                                           onmouseup="handlePeerCheckbox('<%=permission.getUserName()%>^ad', '<%=permission.getUserName()%>^aa')"
                                           name="<%=permission.getUserName()%>"
                                           value="ad" <% if (permission.getAuthorizeDeny()) { %>
                                           checked <% } %>/></td>
                </tr>
                    <% }
                    }%>
                <tr>
                    <td colspan="9" align="right" class="buttonRow"><input type="button"
                                                                           class="button"
                                                                           value="<fmt:message key="apply.all.permissions"/>"
                                                                           onclick="processPermissions('<%=permissionBean.getPathWithVersion()%>');"
                                                                           style="margin-top:5px; float:right;"/><span
                            style="clear:both;"/></td>
                </tr>
                </tbody>
            </table>
        </form>
    </div>

    <h3 style="padding-top:20px;"><fmt:message key="role.permissions"/></h3>--%>

    <div id="rolePermisionsDiv" class="rolePermisionsDiv leftSpace">
        <h4><fmt:message key="new.role.permissions"/></h4>

        <form theme="simple">
            <div class="importantArea" style="margin-top:10px;">
                <table width="100%" border="0" cellpadding="5" cellspacing="0">
                    <tr>
                        <td><fmt:message key="role"/></td>
                        <td>
                            <select id="roleToAuthorize" name="roleToAuthorize">
                                <%
                                    String[] roles = permissionBean.getRoleNames();
                                    if (roles == null) roles = new String[0];
                                    for (String regRole : roles) {
                                        %>
                                        <option value="<%=regRole%>"><%=regRole%>
                                        </option>
                                        <%
                                      }
                                 %>
                            </select>

                        </td>
                        <td align="right"><fmt:message key="action"/></td>
                        <td>
                            <select id="roleActionToAuthorize" name="actionToAuthorize"
                                    style="width:100px;">
                                <option value="1">-<fmt:message key="select"/>-</option>
                                <option value="2"><fmt:message key="read"/></option>
                                <option value="3"><fmt:message key="write"/></option>
                                <option value="4"><fmt:message key="delete"/></option>
                                <option value="5"><fmt:message key="authorize"/></option>
                            </select>
                        </td>
                        <td>
                            <input type="radio" id="rolePermissionAllow" name="permissionType"
                                   value="1" checked="checked"/><fmt:message key="allow"/>
                            <input type="radio"
                                                                             id="rolePermissionDeny"
                                                                             name="permissionType"
                                                                             value="2"/><fmt:message key="deny"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="5" class="importantAreaSeperation" align="right"
                            style="padding-top:10px;"><input type="button" class="button"
                                                             value="<fmt:message key="add.permission"/>"
                                                             onclick="addRolePermission('<%=permissionBean.getPathWithVersion()%>')"/>
                        </td>
                    </tr>
                </table>
            </div>
        </form>

        <script>
            jQuery(document).ready(function() {
                makeSelectRoleSearchable();
                makeSelectActionSelect2styled();
            });
        </script>

        <h4 style="margin-top:10px;"><fmt:message key="defined.role.permissions"/></h4>

        <form name="rolePermissions" theme="simple">
            <input type="hidden" id="pRoleInput" name="permissionInput" value=""/>
            <input type="hidden" name="pathToAuthorize" value="%{path}"/>
            <table width="100%" class="styledLeft" border="0" cellpadding="3" cellspacing="0">
                <tbody>
                <tr class="perRow">
                    <td align="left" class="subTH"><fmt:message key="role"/></td>
                    <td colspan="2" align="center" class="subTH"><fmt:message key="read"/></td>
                    <td colspan="2" align="center" class="subTH"><fmt:message key="write"/></td>
                    <td colspan="2" align="center" class="subTH"><fmt:message key="delete"/></td>
                    <td colspan="2" align="center" class="subTH"><fmt:message key="authorize"/></td>
                </tr>

                <tr>
                    <td class="lineSeperationRight middle-header"></td>
                    <td align="center" style="font-weight:normal;" class="middle-header"><fmt:message key="allow"/></td>
                    <td align="center" style="font-weight:normal;"
                        class="lineSeperationRight middle-header"><fmt:message key="deny"/>
                    </td>
                    <td align="center" style="font-weight:normal;" class="middle-header"><fmt:message key="allow"/></td>
                    <td align="center" style="font-weight:normal;"
                        class="lineSeperationRight middle-header"><fmt:message key="deny"/>
                    </td>
                    <td align="center" style="font-weight:normal;" class="middle-header"><fmt:message key="allow"/></td>
                    <td align="center" style="font-weight:normal;"
                        class="lineSeperationRight middle-header"><fmt:message key="deny"/>
                    </td>
                    <td align="center" style="font-weight:normal;" class="middle-header"><fmt:message key="allow"/></td>
                    <td align="center" style="font-weight:normal;"
                        class="lineSeperationRight middle-header"><fmt:message key="deny"/>
                    </td>
                </tr>

                <%
                    PermissionEntry[] rolePermissions = permissionBean.getRolePermissions();
                    if (rolePermissions == null) rolePermissions = new PermissionEntry[0];
                    for (PermissionEntry permission : rolePermissions) {
                %>

                <tr>
                    <td class="lineSeperationRight"><%=permission.getUserName()%>
                    </td>
                    <td><input type="checkbox" id="<%=permission.getUserName()%>^rra"
                               onmouseup="handlePeerCheckbox('<%=permission.getUserName()%>^rra', '<%=permission.getUserName()%>^rrd')"
                               name="<%=permission.getUserName()%>"
                               value="ra" <% if (permission.getReadAllow()) { %> checked <% } %>/>
                    </td>
                    <td class="lineSeperationRight"><input type="checkbox"
                                                           id="<%=permission.getUserName()%>^rrd"
                                                           onmouseup="handlePeerCheckbox('<%=permission.getUserName()%>^rrd', '<%=permission.getUserName()%>^rra')"
                                                           name="<%=permission.getUserName()%>"
                                                           value="rd" <% if (permission.getReadDeny()) { %>
                                                           checked <% } %>/></td>
                    <td><input type="checkbox" id="<%=permission.getUserName()%>^rwa"
                               onclick="handlePeerCheckbox('<%=permission.getUserName()%>^rwa', '<%=permission.getUserName()%>^rwd')"
                               name="<%=permission.getUserName()%>"
                               value="wa" <% if (permission.getWriteAllow()) { %> checked <% } %>/>
                    </td>
                    <td class="lineSeperationRight"><input type="checkbox"
                                                           id="<%=permission.getUserName()%>^rwd"
                                                           onmouseup="handlePeerCheckbox('<%=permission.getUserName()%>^rwd', '<%=permission.getUserName()%>^rwa')"
                                                           name="<%=permission.getUserName()%>"
                                                           value="wd" <% if (permission.getWriteDeny()) { %>
                                                           checked <% } %>/></td>
                    <td><input type="checkbox" id="<%=permission.getUserName()%>^rda"
                               onmouseup="handlePeerCheckbox('<%=permission.getUserName()%>^rda', '<%=permission.getUserName()%>^rdd')"
                               name="<%=permission.getUserName()%>"
                               value="da" <% if (permission.getDeleteAllow()) { %> checked <% } %>/>
                    </td>
                    <td class="lineSeperationRight"><input type="checkbox"
                                                           id="<%=permission.getUserName()%>^rdd"
                                                           onmouseup="handlePeerCheckbox('<%=permission.getUserName()%>^rdd', '<%=permission.getUserName()%>^rda')"
                                                           name="<%=permission.getUserName()%>"
                                                           value="dd" <% if (permission.getDeleteDeny()) { %>
                                                           checked <% } %>/></td>
                    <td class="lineSeperationRight"><input type="checkbox" id="<%=permission.getUserName()%>^raa"
                               onchange="handlePeerCheckbox('<%=permission.getUserName()%>^raa', '<%=permission.getUserName()%>^rad')"
                               name="<%=permission.getUserName()%>"
                               value="aa" <% if (permission.getAuthorizeAllow()) { %>
                               checked <% } %>/></td>
                    <td class="lineSeperationRight"><input type="checkbox"
                                                           id="<%=permission.getUserName()%>^rad"
                                                           onmouseup="handlePeerCheckbox('<%=permission.getUserName()%>^rad', '<%=permission.getUserName()%>^raa')"
                                                           name="<%=permission.getUserName()%>"
                                                           value="ad" <% if (permission.getAuthorizeDeny()) { %>
                                                           checked <% } %>/></td>
                </tr>
                <% } %>
                <tr>
                    <td colspan="9"><input type="button" class="button"
                                                         value="<fmt:message key="apply.all.permissions"/>"
                                                         onmouseup="processRolePermissions('<%=permissionBean.getPathWithVersion()%>');"
                                                         style="margin-top:5px;"/>&nbsp;<input type="button" class="button"
                                                         value="<fmt:message key="reset"/>"
                                                         onmouseup="refreshPermissionsSection('<%=permissionBean.getPathWithVersion()%>');"
                                                         style="margin-top:5px;"/><span
                            style="clear:both;"/></td>
                </tr>
                </tbody>
            </table>


        </form>
    </div>
</div>

<div style="visibility:hidden">
    <table id="hiddenFilterBox">
        <tr id="filterRow" style="backgournd:#ffffff">
            <td><fmt:message key="filter"/></td>
            <td>
                <input name id="filterString" name="filterString" style="width:100px;"/>

            </td>
            <td align="right"><fmt:message key="limit"/></td>
            <td>
                <input name id="limit" name="limit" style="width:100px;"/>
            </td>
            <td>
                <input type="button" id="applyFilter" name="applyFilter" value="<fmt:message key="apply.filter"/>"/>
            </td>
        </tr>
    </table>
</div>

<% } %>
<div style="margin-bottom:10px"></div>
</fmt:bundle>