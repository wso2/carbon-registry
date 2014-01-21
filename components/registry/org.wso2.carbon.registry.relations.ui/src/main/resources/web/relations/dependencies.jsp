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

<%@ page import="org.wso2.carbon.registry.relations.ui.clients.RelationServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean" %>
<%@ page import="org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationBean" %>

<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../relations/relations-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../relations/js/relations.js"></script>
<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    RelationServiceClient client = new RelationServiceClient(cookie, config, session);
    DependenciesBean bean;
    boolean hasDependencies = false;
    try {
        bean = client.getDependencies(request);
        for (AssociationBean association : bean.getAssociationBeans()) {
            if (association.getSourcePath().equals(bean.getPathWithVersion()) && association.getAssociationType().equals("depends")) {
                hasDependencies = true;
                break;
            }
        }
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
    <jsp:include page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.registry.relations.ui.i18n.Resources">

    <div class="box1-head" style="height:auto;">
        <table cellspacing="0" cellpadding="0" border="0" style="width: 100%;">
            <tbody>
                <tr>
                    <td valign="top">
                        <h2 class="sub-headding-dependencies"><fmt:message key="dependencies"/></h2>
                    </td>
                    <td align="right" valign="top" class="expanIconCell">

                        <a onclick="showDependencies()">
                            <img src="images/icon-expanded.gif" border="0" align="top"
                                id="dependenciesIconExpanded" <% if (!hasDependencies) { %> style="display:none;" <% } %>/>
                            <img src="images/icon-minimized.gif" border="0" align="top"
                                id="dependenciesIconMinimized" <% if (hasDependencies) { %> style="display:none;" <% } %>/>
                        </a>

                    </td>
                </tr>
            </tbody>
        </table>
    </div>
    <div class="box1-mid-fill" id="dependenciesMinimized" <% if (hasDependencies) { %> style="display:none;" <% } %>></div>
    <div class="box1-mid" id="dependenciesExpanded" <% if (!hasDependencies) { %> style="display:none;height:auto;" <% } else { %> style="height:auto;" <% } %>>
        <% if (bean.getLoggedIn() && !bean.getVersionView() && bean.getPutAllowed()) { %>
        <div class="icon-link-ouside registryWriteOperation">
            <a class="icon-link registryWriteOperation" style="background-image:url(../admin/images/add.gif);"
               onclick="javascript:showHideCommon('dependenciesAddDiv');if($('dependenciesAddDiv').style.display!='none')$('depPaths').focus();">Add
                <fmt:message key="dependency"/></a>
        </div>
        <% } %>
        <div id="dependenciesSum"></div>

        <div id="dependencyReason" class="validationError" style="display: none;"></div>
        <div class="registryWriteOperation" id="dependenciesAddDiv" style="display:none;">
            <form onsubmit="return addAssociation('depForm');" name="depForm">
                <input type="hidden" id="resourcePath" name="resourcePath"
                       value="<%=bean.getPathWithVersion()%>"/>
                <input type="hidden" name="type" value="depends"/>
                <table cellpadding="0" border="0" class="styledLeft">
                    <thead>
                        <tr>
                            <th colspan="2"><fmt:message key="add.dependency"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td valign="top"><fmt:message key="path"/> <span class="required">*</span>
                            </td>
                            <td nowrap="nowrap"><input type="text" name="depPaths" id="depPaths"/>
                                <input type="button" class="button" title="<fmt:message key="resource.tree"/>" value=".."
                                       onclick="setResourceTreeExpansionPath(dependencyTreeExpansionPath, showResourceTreeWithLoadFunction(false, 'depPaths'));"/>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2" class="buttonRow">
                                <input type="button" class="button"
                                       value="<fmt:message key="add"/>"
                                       onclick="addDependency();"/>
                                <input style="margin-left:5px;" type="button"
                                       class="button" value="<fmt:message key="cancel"/>"
                                       onclick="showHideCommon('dependenciesAddDiv');"/>
                            </td>
                        </tr>
                    </tbody>
                </table>

            </form>
        </div>


        <% String url = "../relations/association-list-ajaxprocessor.jsp?type=depends&path=" + request.getParameter("path").replaceAll("&","%26"); %>
        <div id="dependenciesDiv">

            <jsp:include page="<%=url%>"/>
        </div>
    </div>
</fmt:bundle>
