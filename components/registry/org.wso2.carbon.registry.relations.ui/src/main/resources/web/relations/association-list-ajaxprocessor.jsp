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
<%@ page import="org.wso2.carbon.registry.relations.ui.clients.RelationServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean" %>
<%@ page import="org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationBean" %>
<%@ page import="org.wso2.carbon.registry.common.ui.utils.UIUtil" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="java.net.URLEncoder" %>

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
    try {
        if (request.getParameter("todo") != null) {
            client.addAssociation(request);
        }
        bean = client.getDependencies(request);
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        response.setStatus(500);
%><%=e.getMessage()%><%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.registry.relations.ui.i18n.Resources">
<%
    String type = request.getParameter("type");
    AssociationBean[] depList = bean.getAssociationBeans();
    AssociationBean[] assoList = bean.getAssociationBeans();

    int count = 0;

    boolean hasAssociations = false;
    boolean hasDependencies = false;

    for (int i = 0; i < depList.length; i++) {
        AssociationBean association = depList[i];
        if (association.getSourcePath().equals(bean.getPathWithVersion()) && association.getAssociationType().equals("depends"))
            hasDependencies = true;
        if (association.getSourcePath().equals(bean.getPathWithVersion()) && !association.getAssociationType().equals("depends"))
            hasAssociations = true;
    }

    if (type != null && type.equals("depends")) {
        List<AssociationBean> beansList = new LinkedList<AssociationBean>();
        for (int i = 0; i < depList.length; i++) {
            AssociationBean association = depList[i];
            if (association.getSourcePath().equals(bean.getPathWithVersion()) && association.getAssociationType().equals("depends")) {
                beansList.add(association);
            }
        }
        count = beansList.size();
        depList = beansList.toArray(new AssociationBean[count]);

%>
<div id="dependenciesSum" class="summeryStyle">
    <% if (count > 1) { %>
    <%=count%> dependencies
    <%
    } else if (count == 1) {
    %>
    <%=count%> dependency
    <%
    } else if (count == 0) {
    %>
    No dependencies on this entry yet.
    <%
        }
        count = 0;
    %>
</div>

<div id="dependenciesList">
    <% if (hasDependencies == true) { %>
    <table cellpadding="0" cellspacing="0" border="0" style="width:100%;" class="styledLeft">
        <thead>
            <tr>
                <th align="left">
                    <fmt:message key="path"/>
                </th>
                <% if (bean.getLoggedIn() && bean.getPutAllowed() && !bean.getVersionView()) { %>
                <th style="width:90px;">
                    <fmt:message key="actions"/>
                </th>
                <% } %>
            </tr>
        </thead>
        <tbody>

            <%
                int pageNumber;
                String pageStr = request.getParameter("page");
                if (pageStr != null) {
                    pageNumber = Integer.parseInt(pageStr);
                } else {
                    pageNumber = 1;
                }
                int itemsPerPage = (int)(RegistryConstants.ITEMS_PER_PAGE * 0.7);
                int numberOfPages;
                if (depList.length % itemsPerPage == 0) {
                    numberOfPages = depList.length / itemsPerPage;
                } else {
                    numberOfPages = depList.length / itemsPerPage + 1;
                }
                for(int i=(pageNumber - 1) * itemsPerPage;i<pageNumber * itemsPerPage && i<depList.length;i++) {
                    AssociationBean association = depList[i];
            %>
            <tr>
                <td>
                    <% String destPath = association.getDestinationPath();
                        String destLink = destPath;
                        String destPathSmall;
                        if (!destPath.startsWith("http://") && !destPath.startsWith("https://")) {
                            String temp = destPath;
                            try {
                                temp = URLEncoder.encode(destPath, "UTF-8");
                            } catch (Exception ignore) {}
                            destLink = "../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=" + temp;
                            destPathSmall = RegistryUtils.getResourceName(destPath);
                            if (destPathSmall == null) {
                                destPathSmall = destPath;
                            }
                        } else {
                            destPathSmall = destPath;
                        }
                        // The dependencies portlet has more space compared to the associations
                        // portlet. Therefore, we allow up to 24, instead of 14.
                        if (destPathSmall.length() >= 32) {
                            destPathSmall = destPathSmall.substring(0, 32) + "..";
                        }
                    %>
                    <a <%if (destPath.startsWith("http://") || destPath.startsWith("https://")) {%>target="_blank"<%}%> href="<%=destLink%>" title="<%=destPath%>"><%=destPathSmall%>
                    </a>
                </td>
                <% if (bean.getLoggedIn() && bean.getPutAllowed() && !bean.getVersionView()) { %>
                <td style="width:10px;" align="left">
                    <a class="icon-link registryWriteOperation" style="background-image:url(../admin/images/delete.gif);"
                       onclick="removeAssociation('<%=bean.getPathWithVersion()%>','<%=destPath%>','<%=association.getAssociationType()%>','dependenciesDiv');">
                        <fmt:message key="delete"/>
                    </a>
                </td>
                <% } %>
            </tr>

            <%
                }


            %>
            <tr>
                    <%--This empty td is required to solve the bottom margin problem on IE. Do not remove!!--%>
                <td align="left" colspan="2" style="height:0px;border-bottom:0px;">
                </td>
            </tr>
        </tbody>
    </table>
    <table width="100%" style="text-align:center; padding-top: 10px; margin-bottom: -10px">
        <carbon:resourcePaginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                  resourceBundle="org.wso2.carbon.registry.relations.ui.i18n.Resources"
                                  nextKey="next" prevKey="prev"
                                  paginationFunction="<%="loadAssociationDiv('" + request.getParameter("path").replaceAll("&","%26") + "', '" + request.getParameter("type") + "', {0})"%>" />
    </table>

    <div style="height:30px;">
        <a class="icon-link"
           style="background-image:url(../relations/images/dep-tree.gif);padding-left:30px;height:30px;margin-top:5px;"
           onclick="showAssociationTree('depends', '<%=bean.getPathWithVersion()%>')">
            <fmt:message key="dependency.tree"/>
        </a>
    </div>
</div>
<% } %>
</div>
<% }


    if (type == null || !type.equals("depends")) {
        List<AssociationBean> beansList = new LinkedList<AssociationBean>();
        for (int i = 0; i < assoList.length; i++) {
            AssociationBean association = assoList[i];
            if (association.getSourcePath().equals(bean.getPathWithVersion()) && !association.getAssociationType().equals("depends")) {
                beansList.add(association);
            }
        }
        count = beansList.size();
        assoList = beansList.toArray(new AssociationBean[count]);
%>
<div id="associationsSum" class="summeryStyle">
    <% if (count > 1) { %>
    <%=count%> associations
    <%
    } else if (count == 1) {
    %>
    <%=count%> association
    <%
    } else if (count == 0) {
    %>
    No associations on this entry yet.
    <%
        }
        count = 0;
    %>
</div>
<div id="associationsList">
    <% if (hasAssociations == true) { %>
    <table cellpadding="0" cellspacing="0" border="0" style="width:100%;" class="styledLeft">
        <thead>
            <tr>
                <th align="left">
                    <fmt:message key="type"/>
                </th>
                <th align="left">
                    <fmt:message key="path"/>
                </th>
                <% if (bean.getLoggedIn() && bean.getPutAllowed() && !bean.getVersionView()) { %>
                <th align="left" style="width:90px;">
                    <fmt:message key="actions"/>
                </th>
                <% } %>
            </tr>
        </thead>
        <tbody>
            <%
                int pageNumber;
                String pageStr = request.getParameter("page");
                if (pageStr != null) {
                    pageNumber = Integer.parseInt(pageStr);
                } else {
                    pageNumber = 1;
                }
                int itemsPerPage = (int)(RegistryConstants.ITEMS_PER_PAGE * 0.7);
                int numberOfPages;
                if (assoList.length % itemsPerPage == 0) {
                    numberOfPages = assoList.length / itemsPerPage;
                } else {
                    numberOfPages = assoList.length / itemsPerPage + 1;
                }
                for(int i=(pageNumber - 1) * itemsPerPage;i<pageNumber * itemsPerPage && i<assoList.length;i++) {
                    AssociationBean association = assoList[i];
                    if (association.getSourcePath().equals(bean.getPathWithVersion()) && !association.getAssociationType().equals("depends")) {
                        count++;
            %>
            <tr>
                <td>
                    <%=association.getAssociationType()%>
                </td>
                <td>
                    <%  int typeLen = association.getAssociationType().length();
                        String destPath = association.getDestinationPath();
                        String destLink = destPath;
                        String destPathSmall;
                        if (!destPath.startsWith("http://") && !destPath.startsWith("https://")) {
                            String temp = destPath;
                            try {
                                temp = URLEncoder.encode(destPath, "UTF-8");
                            } catch (Exception ignore) {}
                            destLink = "../resources/resource.jsp?region=region3&item=resource_browser_menu&viewType=std&path=" + temp;
                            destPathSmall = RegistryUtils.getResourceName(destPath);
                            if (destPathSmall == null) {
                                destPathSmall = destPath;
                            }
                        } else {
                            destPathSmall = destPath;
                        }
                        if (destPathSmall.length() >= 14) {
                            destPathSmall = destPathSmall.substring(0, 10) + "..";
                        }
                    %>
                    <a <%if (destPath.startsWith("http://") || destPath.startsWith("https://")) {%>target="_blank"<%}%> href="<%=destLink%>" title="<%=destPath%>"><%=destPathSmall%>
                    </a>
                </td>
                <% if (bean.getLoggedIn() && bean.getPutAllowed() && !bean.getVersionView()) { %>
                <td style="width:10px;">
                    <a class="icon-link registryWriteOperation" style="background-image:url(../admin/images/delete.gif);"
                       onclick="removeAssociation('<%=bean.getPathWithVersion()%>','<%=destPath%>','<%=association.getAssociationType()%>','associationsDiv');">
                        <fmt:message key="delete"/>
                    </a>
                </td>
                <% } %>
            </tr>
            <%
                    }
                }


            %>
            <tr>
                    <%--This empty td is required to solve the bottom margin problem on IE. Do not remove!!--%>
                <td align="left" colspan="3" style="height:0px;border-bottom:0px;">
                </td>
            </tr>
        </tbody>
    </table>
    <table width="100%" style="text-align:center; padding-top: 10px; margin-bottom: -10px">
        <carbon:resourcePaginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                  resourceBundle="org.wso2.carbon.registry.relations.ui.i18n.Resources"
                                  nextKey="next" prevKey="prev"
                                  paginationFunction="<%="loadAssociationDiv('" + request.getParameter("path").replaceAll("&","%26") + "', '" + request.getParameter("type") + "', {0})"%>" />
    </table>

    <div style="height:30px;">
        <a class="icon-link"
           style="background-image:url(../relations/images/asso-tree.gif);padding-left:30px;height:30px;margin-top:5px;"
           onclick="showAssociationTree('asso', '<%=bean.getPathWithVersion()%>')">
            <fmt:message key="association.tree"/></a>
    </div>
    <% } %>
</div>

<% } %>


</fmt:bundle>
