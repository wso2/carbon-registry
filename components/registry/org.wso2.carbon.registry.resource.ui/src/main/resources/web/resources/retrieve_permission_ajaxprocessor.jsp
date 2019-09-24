<%--
 ~ Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 --%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.registry.common.utils.RegistryUtil" %>

<fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">
    <div class="box1-head">
        <table cellspacing="0" cellpadding="0" border="0" style="width:100%">
            <tr>
                <td valign="top">
                    <h2 class="sub-headding-permisions"><fmt:message key="permissions"/></h2>
                </td>
                <td align="right" valign="middle" style="padding-top:3px;" class="box1-head-right">
                    <a onclick="showHideCommon('perIconMinimized');
                            refreshPermissionsSection('<%=RegistryUtil.getPath(request).replaceAll("&","%26")%>');">
                        <img src="../resources/images/icon-minimized.gif" border="0" align="top"
                             id="perIconMinimized"/>
                    </a>
                </td>
            </tr>
        </table>
    </div>
</fmt:bundle>
