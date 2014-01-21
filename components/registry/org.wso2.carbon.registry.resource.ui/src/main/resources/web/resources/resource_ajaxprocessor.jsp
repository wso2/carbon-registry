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

<%@ page import="org.wso2.carbon.registry.common.utils.RegistryUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.Utils" %>


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
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script src="../global-params.js" type="text/javascript"></script>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>

<%
    boolean infoFound = CarbonUIUtil.isContextRegistered(config, "/info/") &&
            CarbonUIUtil.isUserAuthorized(request,
                    "/permission/admin/manage/resources/community-features");

    boolean propertiesFound = CarbonUIUtil.isContextRegistered(config, "/properties/");

    boolean relationsFound = CarbonUIUtil.isContextRegistered(config, "/relations/") &&
            CarbonUIUtil.isUserAuthorized(request,
                    "/permission/admin/manage/resources/associations");

//    boolean searchFound = CarbonUIUtil.isContextRegistered(config, "/search/");

//    boolean lifecyclesFound = CarbonUIUtil.isContextRegistered(config, "/lifecycles/");      
    String targetDivID = Utils.getTargetDivID(request);
%>
<fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">

<style type="text/css">
    .yui-skin-sam h3 {
        font-size: 10px !important;
    }

    .yui-toolbar-container .yui-toolbar-titlebar h2 a {
        font-size: 11px !important;
    }
</style>
<div id="regEditorContent" style="margin-top:10px;">
<div id="middle">
<table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">   
    <tbody>
         <tr id="local-registry-td">
            <td>
                <div style="margin-top:10px;">
                    <h2><fmt:message key="local.registry"/></h2>
                    <div id="local-registry-workArea" style="margin-top:10px;"></div>
                    <script type="text/javascript">
                        var url = '../sequences/local_registry-ajaxprocessor.jsp?resourceConsumer='+'<%=Utils.getResourceConsumer(request)%>';
                        jQuery(document).ready(function() {
                            jQuery("#local-registry-workArea").load(url, ({}),
                                    function(data, status, t) {
                                        if (status != "success") {
                                            CARBON.showWarningDialog("Error Occurred!");
                                            document.getElementById("local-registry-td").style.display = "none";
                                        }
                                    });
                        });
                    </script>
                </div>        
            </td>        
        </tr>       
        <tr>
            <td>
                <div style="margin-top:10px;">
                    <h2><fmt:message key="remote.registry"/></h2>

                    <div id="workArea">
                        <div class="resource-path">
                            <jsp:include page="metadata_resourcepath.jsp"/>
                        </div>
                        <table width="100%">
                            <tbody>
                                <tr>

                                    <td id="resourceMain">
                                        <div id="metadataDiv">
                                            <jsp:include page="metadata_ajaxprocessor.jsp"/>
                                        </div>

                                        <% if (propertiesFound) {
                                            String propertiesPath = "../properties/properties-main-ajaxprocessor.jsp?path=" + RegistryUtil.getPath(request).replaceAll("&","%26");
                                        %>
                                        <div id="propertiesDiv">
                                            <jsp:include page="<%=propertiesPath%>"/>
                                        </div>
                                        <% } %>

                                            <%--<div id="permissionsDiv">--%>
                                            <%--<jsp:include page="permissions_ajaxprocessor.jsp"/>--%>
                                            <%--</div>--%>

                                        <div id="contentDiv">
                                            <jsp:include page="content_ajaxprocessor.jsp"/>
                                        </div>


                                    </td>
                                    <% if (infoFound || relationsFound) { %>
                                    <td id="pointLeft" style="display:none;"><a class="pointLeft"
                                                                                onclick="showHideResources()"></a>
                                    </td>
                                    <td id="pointRight"><a class="pointRight" onclick="showHideResources()"></a>
                                    </td>
                                    <td class="resource-right" id="resourceInfo" nowrap="nowrap">

                                            <%--<% if (searchFound) {--%>
                                            <%--String searchPath = "../search/simpleSearch.jsp";--%>
                                            <%--%>--%>
                                            <%--<jsp:include page="<%=searchPath%>"/>--%>
                                            <%--<% } %>--%>

                                        <% if (infoFound) {
                                            String infoPath = "../info/info_ajaxprocessor.jsp?path=" + RegistryUtil.getPath(request).replaceAll("&","%26");
                                        %>
                                        <div id="infoDiv">
                                            <jsp:include page="<%=infoPath%>"/>
                                        </div>
                                        <% } %>
                                            <%----%>
                                            <%--<% if (relationsFound) {--%>
                                            <%--String relationsPath = "../relations/relations.jsp?path=" + RegistryUtil.getPath(request);--%>
                                            <%--%>--%>
                                            <%--<jsp:include page="<%=relationsPath%>"/>--%>
                                            <%--<% } %>--%>

                                            <%--<% if (lifecyclesFound) {--%>
                                            <%--String lifecyclesPath = "../lifecycles/lifecycles_ajaxprocessor.jsp?path=" + RegistryUtil.getPath(request);--%>
                                            <%--%>--%>
                                            <%--<div id="lifecyclesDiv">--%>
                                            <%--<jsp:include page="<%=lifecyclesPath%>"/>--%>
                                            <%--</div>                                    --%>
                                            <%--<% } %>--%>
                                    </td>


                                    <% } %>
                                </tr>
                            </tbody>
                        </table>

                    </div>
                </div>
            </td>                   
        </tr>       
    </tbody>
</table>
</div>
<div id="resourceTree" style="display:none" class="resourceTreePage">
    <div class="ajax-loading-message">
        <img src="../resources/images/ajax-loader.gif" align="top"/>
        <span><fmt:message key="resource.tree.loading.please.wait"/> ..</span>
    </div>
</div>
<div id="popup-main" style="display:none" class="popup-main"></div>
</div>
</fmt:bundle>