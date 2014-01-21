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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.registry.extensions.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.common.ui.UIConstants" %>
<%@ page import="org.wso2.carbon.registry.extensions.ui.clients.ExtensionsUIUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="js/paginate.js"
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.registry.extensions.ui.i18n.JSResources"
        request="<%=request%>" namespace="org.wso2.carbon.registry.extensions.ui"/>
<%
    int pageNumber = 0;
    int numberOfPages = 1;
    String[] paginatedExtensions = new  String[0];

    String requestedPage = request.getParameter(UIConstants.REQUESTED_PAGE);
    String[] extensions;
    try {
        ResourceServiceClient client = new ResourceServiceClient(config, session);
        extensions = client.listExtensions();
    } catch (Exception e) {

%>
<script type="text/javascript">
      CARBON.showErrorDialog("<%=e.getMessage()%>",function(){
          location.href="../admin/index.jsp";
          return;
      });

</script>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.registry.extensions.ui.i18n.Resources">
<script type="text/javascript">

    function deleteExtension(extensionName, redirectPath) {
        sessionAwareFunction(function() {
            CARBON.showConfirmationDialog(org_wso2_carbon_registry_extensions_ui_jsi18n["are.you.sure.you.want.to.delete"] + "<strong>'" +
                                          extensionName + "'</strong> " + org_wso2_carbon_registry_extensions_ui_jsi18n["permanently"],
                    function() {
                        var addSuccess = true;
                        new Ajax.Request('../extensions/deleteExtension-ajaxprocessor.jsp', {
                            method:'post',
                            parameters: {extensionName: extensionName},

                            onSuccess: function() {
                                location.href=redirectPath;
                            },

                            onFailure: function() {
                                addSuccess = false;
                            }
                        });

                    }, null);

        }, org_wso2_carbon_registry_extensions_ui_jsi18n["session.timed.out"]);
    }
</script>



<carbon:breadcrumb
            label="list.schemas.menu.text"
            resourceBundle="org.wso2.carbon.registry.extensions.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>" />
<br/>
<div id="middle">
    <h2><fmt:message key="extension.list"/></h2>
<%
           if (extensions != null && extensions.length != 0) {
            int start;
            int end;
            int itemsPerPage = (int)(RegistryConstants.ITEMS_PER_PAGE * 1.5);

            if (requestedPage != null && requestedPage.length() > 0) {
                pageNumber = new Integer(requestedPage);
            } else {
                pageNumber = 1;
            }

            if (extensions.length % itemsPerPage == 0) {
                numberOfPages = extensions.length / itemsPerPage;
            } else {
                numberOfPages = extensions.length / itemsPerPage + 1;
            }

            if (extensions.length < itemsPerPage) {
                start = 0;
                end = extensions.length;
            } else {
                start = (pageNumber - 1) * itemsPerPage;
                end = (pageNumber - 1) * itemsPerPage + itemsPerPage;
            }
        paginatedExtensions = ExtensionsUIUtils.getPaginatedExtension(start,itemsPerPage,extensions);
       }
%>  <div id="workArea">
    <form id="profilesEditForm">
    <table class="styledLeft" id="customTable">
               <%if (paginatedExtensions == null || paginatedExtensions.length == 0 ||
                       paginatedExtensions[0] == null){%>
                <thead>
                    <tr>
                        <th><fmt:message key="no.extensions"/></th>
                    </tr>
                </thead>
        <%}
        else{%>
            <thead>
            <tr>
                    <th><fmt:message key="extension.name"/></th>
                    <th><fmt:message key="actions"/></th>
                </tr>
            </thead>
            <tbody>
                    <%
              for(int i=0;i<paginatedExtensions.length;i++) {

                 %>
                <tr>
                    <td><%=paginatedExtensions[i]%></td>
                    <% if (CarbonUIUtil.isUserAuthorized(request,
                            "/permission/admin/manage/extensions/add")) {%>
                    <td><a title="<fmt:message key="delete"/>" onclick="deleteExtension('<%=extensions[i]%>','../extensions/list_extensions.jsp?region=region3&item=list_extensions_menu')" href="#" class="icon-link registryWriteOperation" style="background-image:url(../admin/images/delete.gif);"><fmt:message key="delete"/></a></td>
                    <% } else { %>
                    <td></td>
                    <% } %>
                </tr>

                    <%
             }
             %>
            </tbody>
        <%}%>
        </table>
    </form>
    </div>
    </div>
   <carbon:resourcePaginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                  resourceBundle="org.wso2.carbon.registry.extensions.ui.i18n.Resources"
                                  nextKey="next" prevKey="prev"
                                  paginationFunction="submitExtension(1,{0})"/>
        <script type="text/javascript">
        alternateTableRows('customTable','tableEvenRow','tableOddRow');
</script>
</fmt:bundle>
