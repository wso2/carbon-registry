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

<%@ page import="org.wso2.carbon.registry.info.ui.clients.InfoServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.common.beans.TagBean" %>
<%@ page import="org.wso2.carbon.registry.common.beans.utils.Tag" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>

<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<carbon:jsi18n resourceBundle="org.wso2.carbon.registry.info.ui.i18n.JSResources"
		request="<%=request%>" namespace="org.wso2.carbon.registry.info.ui"/>
<script type="text/javascript" src="../info/js/info.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<link rel="stylesheet" type="text/css"
      href="../resources/css/registry.css"/>

<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    InfoServiceClient client = new InfoServiceClient(cookie, config, session);
    TagBean tag;
    try {
        tag = client.getTags(request);
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
    <jsp:include page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }
    Tag[] tags = tag.getTags();
%>
<fmt:bundle basename="org.wso2.carbon.registry.info.ui.i18n.Resources">

    <div class="box1-head" style="height:auto;margin-top:10px;">
        
        <table cellspacing="0" cellpadding="0" border="0" style="width:100%">
        <tr>


            <td valign="top"><h2 class="sub-headding-comments"><fmt:message key="tags"/></h2></td>


            <td align="right" valign="top" class="expanIconCell">

                <a onclick="showTags()">
                    <img src="images/icon-expanded.gif" border="0" align="top"
                         id="tagsIconExpanded" <% if (tags.length == 0) { %> style="display:none;" <% } %>/>
                    <img src="images/icon-minimized.gif" border="0" align="top"
                         id="tagsIconMinimized" <% if (tags.length > 0) { %> style="display:none;" <% } %>/>
                </a>

            </td>

        </tr>
    	</table>
    </div>
    <div class="box1-mid-fill" id="tagsMinimized" <% if (tags.length > 0) { %> style="display:none;" <% } %>></div>
    <div class="box1-mid" id="tagsExpanded" <% if (tags.length == 0) { %> style="display:none;height:auto;" <% } else { %> style="height:auto;" <% } %>>
        <% if (tag.isLoggedIn() && !tag.isVersionView()) { %>
        <div class="icon-link-ouside registryWriteOperation" id="tagAddDiv">
            <a class="icon-link registryWriteOperation" style="background-image:url(../admin/images/add.gif);"
               onclick="javascript:showHideCommon('tagAddTable');if($('tagAddTable').style.display!='none')$('tfTag').focus();">
                <fmt:message key="add.new.tag"/>
            </a>
        </div>
        <% } %>
        <!-- START add tag box -->
        <div class="registryWriteOperation" id="tagAddTable" style="margin-top:10px;display: none;">
            <input id="tfPath" type="hidden" name="resourcePath"
                   value='<%=tag.getPathWithVersion()%>'/>
            <input id="tfTag" type="text" name="tag" <%--onkeypress="applyTag(event);"--%>/>
            <br /><br />
	    <input type="button" onclick="applyTag(event);" value="<fmt:message key="add"/>" class="button"/>
            <input type="button" onclick="showHideCommon('tagAddTable');" value="<fmt:message key="cancel"/>" class="button" style="margin-left: 5px;"/>
            <div style="font-style:italic;margin-top:5px;">
                <img src="../info/images/help-small.jpg" style="margin-right:5px;"/>
                <fmt:message key="use.commas.to.add.multiple.tags"/>

            </div>
        </div>

        <div id="tagList" style="padding-top:5px">
            <%
                if (tags.length > 0) {
                    for (int i = 0; i < tags.length; i++) {
                        Tag tag1 = tags[i];
                        String tagName = tag1.getTagName();
                        String style = "cloud-x" + tag1.getCategory();

            %>

            <% if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/search/resources")) { %>
            <a href="../search/search.jsp?region=region3&item=registry_search_menu&searchType=tag&criteria=<%=tagName%>" class="<%=style%>" onmouseover="showDel('<%=i%>')"><%=tagName%>
            <% } else { %>
            <a href="javascript:void(0);" class="<%=style%>" onmouseover="showDel('<%=i%>')"><%=tagName%>
            <% } %>
            <% if (!tag.isVersionView()) { %>
            </a><a class="closeButton registryWriteOperation" onclick="delTag('<%=tagName%>','<%=tag.getPathWithVersion()%>')" id="close<%=i%>" style="display:none" title="<fmt:message key="delete"/>"><img src="../admin/images/delete.gif" style="width:8px"/></a>&nbsp;&nbsp;
            <% } %>
            <%
                }
            } else {
            %>
            <div id="noTags" class="summeryStyle">
                <fmt:message key="no.tags.on.this.entry.yet"/>
            </div>
            <%
                }
            %>
        </div>
    </div>
</fmt:bundle>