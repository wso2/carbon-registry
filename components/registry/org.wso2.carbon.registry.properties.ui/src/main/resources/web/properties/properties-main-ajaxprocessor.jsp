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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>

<%@ page import="org.wso2.carbon.registry.common.ui.UIConstants" %>
<jsp:include page="../properties/properties-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../properties/js/properties.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>

<%
    PropertiesBean propertiesBean;
    try {
        PropertiesServiceClient client = new PropertiesServiceClient(config, session);
        propertiesBean = client.getProperties(request);
        if (propertiesBean == null) {
            return;
        }
    } catch (Exception ignored) {
        return;
    }

%>
<fmt:bundle basename="org.wso2.carbon.registry.properties.ui.i18n.Resources">

    <div class="box1-head" style="height:auto;margin-top:10px;">
        <table cellspacing="0" cellpadding="0" border="0" style="width: 100%;">
            <tbody>
                <tr>
                    <td valign="top">
                        <h2 class="sub-headding-prop"><fmt:message key="properties"/></h2>
                    </td>
                    <td align="right" valign="top" class="expanIconCell">
                        <a onclick="showProperties();">
                            <img src="images/icon-expanded.gif" border="0" align="top"
                                 id="propertiesIconExpanded" style="display:none;" />
                            <img src="images/icon-minimized.gif" border="0" align="top"
                                 id="propertiesIconMinimized" />
                        </a>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
    <div class="box1-mid-fill" id="propertiesMinimized"></div>
    <div class="box1-mid" id="propertiesExpanded" style="display:none">
        <% if (propertiesBean.getLoggedIn() && propertiesBean.getPutAllowed() && !propertiesBean.getVersionView()) { %>
        <div class="icon-link-ouside registryWriteOperation">
            <a class="icon-link registryWriteOperation" style="background-image:url(../admin/images/add.gif);"
               onclick="showHideCommon('propertiesAddDiv');if($('propertiesAddDiv').style.display!='none')$('propName').focus();">
                <fmt:message key="add.new.property"/>
            </a>
        </div>
        <% } %>
        <% Boolean sysPropsAttr = (Boolean) request.getSession().getAttribute(UIConstants.SHOW_SYSPROPS_ATTR);
            boolean sysPropsEnabled = sysPropsAttr != null && sysPropsAttr; %>


        <% if (propertiesBean.getVersionView() && propertiesBean.getPutAllowed()) { %>
        <span class="helpText"><fmt:message
                key="go.to.the.current.version.to.add.or.edit.properties"/>.</span>
        <% } %>
        <div class="registryWriteOperation" id="propertiesAddDiv" style="display:none;">
            <form onsubmit="return setProperty();">

                <input type="hidden" id="propRPath"
                       value="<%=propertiesBean.getPathWithVersion()%>"/>
                <table cellpadding="0" cellspacing="0" border="0" class="styledLeft noBorders">
                    <thead>
                        <tr>
                            <th colspan="2"><fmt:message key="add.new.property"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td id="propertySizer"><fmt:message key="name"/><font color="red">*</font></td>
                            <td><input type="text" id="propName"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="value"/></td>
                            <td><input type="text" id="propValue"/></td>
                        </tr>
                        <tr>
                            <td colspan="2" class="buttonRow">
                                <input type="button" class="button"
                                       value="<fmt:message key="add"/>"
                                       onclick="setProperty();"/>
                                <input
                                        style="margin-left:5px;" type="button" class="button"
                                        value="<fmt:message key="cancel"/>"
                                        onclick="showHideCommon('propertiesAddDiv');"/>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </form>
        </div>


        <div id="resourceProperties">
            <%@ include file="../properties/properties-ajaxprocessor.jsp" %>
        </div>
    </div>
</fmt:bundle>