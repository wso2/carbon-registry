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
    boolean hasAssociations = false;
    try {
        bean = client.getDependencies(request);
        for (AssociationBean association : bean.getAssociationBeans()) {
            if (association.getSourcePath().equals(bean.getPathWithVersion()) && !association.getAssociationType().equals("depends")) {
                hasAssociations = true;
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
                        <h2 class="sub-headding-associations"><fmt:message key="associations"/></h2>
                    </td>
                    <td align="right" valign="top" class="expanIconCell">

                        <a onclick="showAssociations()">
                            <img src="images/icon-expanded.gif" border="0" align="top"
                                id="associationsIconExpanded" <% if (!hasAssociations) { %> style="display:none;" <% } %>/>
                            <img src="images/icon-minimized.gif" border="0" align="top"
                                id="associationsIconMinimized" <% if (hasAssociations) { %> style="display:none;" <% } %>/>
                        </a>

                    </td>

                </tr>
            </tbody>
        </table>
    </div>
    <div class="box1-mid-fill" id="associationsMinimized" <% if (hasAssociations) { %> style="display:none;" <% } %>></div>
    <div class="box1-mid" id="associationsExpanded" <% if (!hasAssociations) { %> style="display:none;height:auto;" <% } else { %> style="height:auto;" <% } %>>
        <% if (bean.getLoggedIn() && !bean.getVersionView() && bean.getPutAllowed()) { %>
        <div class="icon-link-ouside registryWriteOperation">
            <a class="icon-link registryWriteOperation" style="background-image:url(../admin/images/add.gif);"
               onclick="javascript:showHideCommon('associationsAddDiv');expandIfNot('associations');if($('associationsAddDiv').style.display!='none')document.forms.assoForm.type.focus()">Add
                <fmt:message key="association"/>
            </a>
        </div>
        <% } %>
        <div id="associationsSum"></div>
        <div class="registryWriteOperation" id="associationsAddDiv" style="display:none;">
            <form onsubmit="return addAssociation('assoForm');" name="assoForm">
                <input type="hidden" name="resourcePath" id="resourcePath"
                       value="<%=bean.getPathWithVersion()%>"/>
                <table cellpadding="0" cellspacing="0" border="0" class="styledLeft">
                    <thead>
                        <tr>
                            <th colspan="2"><fmt:message key="add.new.association"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td valign="top"><fmt:message key="type"/> <span class="required">*</span></td>
                            <td><select id="associationOptionList" onchange="changeTextVisibility()">
                                     <option value="0">-SELECT-</option>
                                     <option value="usedBy">usedBy</option>
                                     <option value="ownedBy">ownedBy</option>
                                     <option value="other">other</option>
                                </select>
                                <input type="text" name="type" id="type"
                                       style="width:130px;margin-bottom:5px;visibility:hidden;"/>
                            </td>
                        </tr>
                        <tr>
                            <td valign="top"><fmt:message key="path"/> <span class="required">*</span></td>
                            <td nowrap="nowrap"><input type="text" id="associationPaths" name="associationPaths"/>
                                <input type="hidden" id="associationPathsHidden" name="associationPathsHidden"/>
                                <input type="button" class="button"
                                       value=".."  title="<fmt:message key="resource.tree"/>" 
                                       onclick="setResourceTreeExpansionPath(dependencyTreeExpansionPath, showResourceTreeWithLoadFunction(false, 'associationPathsHidden', appendAssociation));"/>
                            </td>
                        </tr>
                        <tr>

                            <td colspan="2" class="buttonRow"><input type="button" class="button"
                                                                     value="<fmt:message key="add"/>"
                                                                     onclick="addAssociation('assoForm');"/><input
                                    style="margin-left:5px;" type="button"
                                    class="button" value="<fmt:message key="cancel"/>"
                                    onclick="showHideCommon('associationsAddDiv');"/>
                            </td>
                        </tr>
                    </tbody>
                </table>

            </form>
        </div>


        <% String url = "../relations/association-list-ajaxprocessor.jsp?type=asso&path=" + request.getParameter("path").replaceAll("&","%26");%>
        <div id="associationsDiv">

            <jsp:include page="<%=url%>"/>
        </div>
    </div>
</fmt:bundle>
