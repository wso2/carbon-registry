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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.registry.common.utils.RegistryUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.ResourceTreeEntryBean" %>

<jsp:include page="../dialog/display_messages.jsp"/>

<!-- YUI inculudes for rich text editor -->
<link rel="stylesheet" type="text/css"
      href="../yui/build/editor/assets/skins/sam/simpleeditor.css"/>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/element/element-beta-min.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/editor/simpleeditor-min.js"></script>

<!-- other includes -->
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="js/resource_util.js"></script>
<script type="text/javascript" src="js/resource_media_type_loader.js"></script>
<!-- including the JS for properties, since JS can't be loaded via async calls. -->
<jsp:include page="../properties/properties-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../properties/js/properties.js"></script>
<link rel="stylesheet" type="text/css" href="css/registry.css"/>

<link rel="stylesheet" href="css/select2.css"/>
<script type="text/javascript" src="js/select2.min.js"></script>

<%
    // the error message is been set in <code>org.wso2.carbon.registry.resource.ui.processors.AddResourceProcessor</code>
    // and is been redirected by the java class. Previously, this was redirected to ../admin/error.jsp.
    // the change is made as a fix for REGISTRY-805
    String errorMsg = request.getParameter("errorMsg");
    if( errorMsg != null) {
%>
       <script type="text/javascript" >
            CARBON.showErrorDialog("<%=errorMsg %>", function(){
               window.history.back();
               return false;
            });
       </script>
<%
    }

    ResourceServiceClient client;
    boolean resourceExists = false;
    boolean isRoot = false;
    String location = "";
    ResourceTreeEntryBean resourceTreeEntryBean = null;
    try {
        client = new ResourceServiceClient(config, session);
        String path = request.getParameter("path");
        if (path == null || path.equals("")) {
            path = "/";
        } else {
            location = path;
        }
        if ("/".equals(path)) {
            isRoot = true;
        }
        if ((resourceTreeEntryBean = client.getResourceTreeEntry(path)) != null) {
            resourceExists = true;
        }
    } catch (Exception e) {
        resourceExists = false;
    }
%>
<fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">
<script type="text/javascript">
  <!--
  sessionAwareFunction(function() {
  <% if (!resourceExists) {
  //TODO: We should be able to distinguish the two scenarios below. An authorization failure
  //generates a AuthorizationFailedException which doesn't seem to arrive at this page.
  %>
      CARBON.showErrorDialog("<fmt:message key="unable.to.browse"/>",function(){
          location.href="../admin/index.jsp";
          return;
      });
  <% } else { %>
      loadMediaTypes();
        jQuery(document).ready(loadMediaTypes);
  <% } %>
  }, "<fmt:message key="session.timed.out"/>");
  // -->
</script>
</fmt:bundle>
<%
    if (!resourceExists) {
        return;
    }
%>
 <!--[if IE 7]>
   <link rel="stylesheet" type="text/css" href="css/registry-ie7.css" />
 <![endif]-->
 <!--[if IE 8]>
   <link rel="stylesheet" type="text/css" href="css/registry-ie8.css" />
 <![endif]-->

<fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">
<carbon:breadcrumb label="resources"
                       resourceBundle="org.wso2.carbon.registry.resource.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>

    <%
        boolean infoFound = CarbonUIUtil.isContextRegistered(config, "/info/") &&
                CarbonUIUtil.isUserAuthorized(request,
                        "/permission/admin/manage/resources/community-features");

        boolean propertiesFound = CarbonUIUtil.isContextRegistered(config, "/properties/");

        boolean relationsFound = CarbonUIUtil.isContextRegistered(config, "/relations/") &&
                CarbonUIUtil.isUserAuthorized(request,
                        "/permission/admin/manage/resources/associations");

        boolean searchFound = CarbonUIUtil.isContextRegistered(config, "/search/");

        boolean lifecyclesFound = CarbonUIUtil.isContextRegistered(config, "/lifecycles/");

        boolean retentionFound = propertiesFound;
        String contraction = "min";
        boolean enableContraction = true;
        String viewType = "tree";
        if(session.getAttribute( "contraction" )!=null){
            contraction = (String)session.getAttribute( "contraction" );
        }
        /*if(session.getAttribute( "viewType" )!=null){
            viewType = (String)session.getAttribute( "viewType" );
        }*/
        if (!isRoot || "std".equals(request.getParameter("viewType"))) {
            viewType = "std";
        } else {
            viewType = "tree";
        }
    %>
    <style type="text/css">
        .yui-skin-sam h3 {
            font-size: 10px !important;
        }

        .yui-toolbar-container .yui-toolbar-titlebar h2 a {
            font-size: 11px !important;
        }
    </style>
    <div id="middle">

        <h2><fmt:message key="registry.resources.menu"/></h2>

        <div id="workArea">
            <div class="resource-path">
                <jsp:include page="metadata_resourcepath.jsp"/>
            </div>
            <table width="100%">

                <tr>

                    <% if (!infoFound && !relationsFound) {
                        contraction = "exp";
                        enableContraction = false;
                    }
                    %>
                    <td id="resourceMain" <% if (contraction.equals("min")){ %>style="width:70%"<% } %>>
                        <table><tr><td nowrap="nowrap" style="width:100%">
                        <fmt:message key="location"/>: <input id="uLocationBar" onkeypress="if (event.keyCode == 13) {loadFromPath();}" type="text" name="locationBar" style="width:400px;margin-bottom:10px;" value="<%=location%>"/> <input type="button" class="button" value="<fmt:message key="go"/>" onclick="loadFromPath();"/>
                        </td></tr></table>
                        <div class="tabview">
                            <a id="treeView" title="<fmt:message key="tree.view"/>"
                               class="treeView-<% if(viewType.equals("std")){ %>not<% }%>Selected"
                               onclick="showHideTreeView('<%=(request.getParameter("path") == null) ? "" : request.getParameter("path")%>',this)">Tree
                                view</a>
                            <a id="stdView" title="<fmt:message key="detail.view"/>"
                               class="stdView-<% if(viewType.equals("tree")){ %>not<% }%>Selected"
                               onclick="showHideTreeView('<%=(request.getParameter("path") == null) ? "" : request.getParameter("path")%>',this)">Detail
                                view</a>

                        </div>


                        <div id="viewPanel">
                            <% if(viewType.equals("tree")){ %>
                            <jsp:include page="tree_view_ajaxprocessor.jsp"/>
                            <% } else { %>
                            <jsp:include page="standard_view_ajaxprocessor.jsp"/>
                            <% } %>
                        </div>
                    </td>

                    <td class="contraction-spacer"></td>
                    <td id="pointTds">
			
                    </td>
                    <% if (infoFound || relationsFound ||
                            (retentionFound && !resourceTreeEntryBean.getCollection())) {
                    %>
                    <td class="resource-right" id="resourceInfo" nowrap="nowrap" <% if (contraction.equals("exp")){ %>style="display:none;"<% } %>>

                        <% if (searchFound && CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/search/resources")) {
                            String searchPath = "../search/simpleSearch.jsp";
                        %>
                        <jsp:include page="<%=searchPath%>"/>
                        <% } %>

                        <% if (relationsFound) {
                        String relationsPath = "../relations/relations.jsp?path=" + RegistryUtil.getPath(request).replaceAll("&","%26");
                        %>
                        <jsp:include page="<%=relationsPath%>"/>
                        <% } %>

                        <% if (retentionFound && !resourceTreeEntryBean.getCollection()) {
                            String retentionPath = "../properties/retention_ajaxprocessor.jsp?path="
                                    + RegistryUtil.getPath(request).replaceAll("&","%26");
                        %>
                        <jsp:include page="<%=retentionPath%>"/>
                        <% } %>

                        <% if (lifecyclesFound && CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/govern/lifecycles")) {
                        String lifecyclesPath = "../lifecycles/lifecycles_ajaxprocessor.jsp?path=" + RegistryUtil.getPath(request).replaceAll("&","%26");
                        %>
                        <div id="lifecyclesDiv">
                            <jsp:include page="<%=lifecyclesPath%>"/>
                        </div>
                        <% } %>

                        <%-- Lifecycle History UI --%>
                        <% if (lifecyclesFound && CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/govern/lifecycles")) {
                            String resourcePath =
                                    "../history/lifecyclesHistory_ajaxprocessor.jsp?path=" + RegistryUtil.getPath(request).replaceAll("&","%26");
                        %>
                        <div id="lifecyclesHistoryDiv">
                            <jsp:include page="<%=resourcePath%>"/>
                        </div>
                        <% } %>
                        <%-- end of Lifecycle History UI--%>

                        <% if (infoFound) {
                            String infoPath = "../info/info_ajaxprocessor.jsp?path=" + RegistryUtil.getPath(request).replaceAll("&","%26");
                        %>
                        <div id="infoDiv">
                            <jsp:include page="<%=infoPath%>"/>
                        </div>
                        <% } %>
                    </td>

                    <% } %>
                    <td><div id="pointA" <% if (!(infoFound || relationsFound || (retentionFound && !resourceTreeEntryBean.getCollection()))) {%>style="display:none"<%} %> title="<fmt:message key="resource.show.left"/>" <% if (contraction.equals("exp")){ %>class="showToHidden"<% } else { %>class="hiddenToShow"<% } %> <% if (enableContraction) { %> onclick="showHideResources('<%=contraction%>')" <% } %> class="showToHidden"><span id="showHideId"><% if (contraction.equals("exp")){ %>Show<% } else { %>Hide<% } %> </span></div></td>
                </tr>

            </table>

        </div>
    </div>

    <!--div id="resourceTree" style="display:none" class="resourceTreePage">
        <div class="ajax-loading-message">
            <img src="/wso2registry/admin/images/ajax-loader.gif" align="top"/>
            <span><fmt:message key="resource.tree.loading.please.wait"/> ..</span>
        </div>
    </div>
    <div id="popup-main" style="display:none" class="popup-main">
    </div-->
</fmt:bundle>
