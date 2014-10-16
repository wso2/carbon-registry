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

<%@ page import="org.wso2.carbon.registry.common.utils.CommonUtil" %>
<%@ page import="org.wso2.carbon.registry.info.ui.clients.InfoServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.info.ui.Utils" %>
<%@ page import="org.wso2.carbon.registry.common.beans.CommentBean" %>
<%@ page import="org.wso2.carbon.registry.common.beans.utils.Comment" %>
<%@ page import="org.wso2.carbon.registry.common.ui.utils.UIUtil" %>

<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<carbon:jsi18n resourceBundle="org.wso2.carbon.registry.info.ui.i18n.JSResources"
		request="<%=request%>" namespace="org.wso2.carbon.registry.info.ui" />
<script type="text/javascript" src="../info/js/info.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    InfoServiceClient client = new InfoServiceClient(cookie, config, session);
    CommentBean comment;
    try {
        comment = client.getComments(request);
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
    Comment[] comments = comment.getComments();
%>
<fmt:bundle basename="org.wso2.carbon.registry.info.ui.i18n.Resources">

<div class="box1-head" style="height:auto;">
    <table cellspacing="0" cellpadding="0" border="0" style="width:100%">
        <tr>


            <td valign="top"><h2 class="sub-headding-comments"><fmt:message
                    key="comments"/></h2></td>


            <td align="right" valign="top" class="expanIconCell">


                <a onclick="showComments()">
                    <img src="images/icon-expanded.gif" border="0" align="top"
                         id="commentsIconExpanded" <% if (comments.length == 0) { %> style="display:none;" <% } %>/>
                    <img src="images/icon-minimized.gif" border="0" align="top"
                         id="commentsIconMinimized" <% if (comments.length > 0) { %> style="display:none;" <% } %>/>
                </a>

            </td>

        </tr>
    </table>


</div>
<div class="box1-mid-fill" id="commentsMinimized" <% if (comments.length > 0) { %> style="display:none;" <% } %>></div>
<div class="box1-mid" id="commentsExpanded" <% if (comments.length == 0) { %> style="display:none;height:auto;" <% } else { %> style="height:auto;" <% } %>>
<div style="height:15px;">
    <% if (!comment.isVersionView()) {
        String url = UIUtil.getAtomURL(config, request, request.getParameter("path")) + ";comments"; %>
    <a
            style="float:right;background-image:url(images/icon-feed-small.gif);"
            href="../../registry/atom<%=request.getParameter("path")%>;comments"
            target="_blank"
            class="icon-link"
            title="<fmt:message key="subscribe.to.the.comment.feed"/>">
            <fmt:message key="feed"/> 
    </a>
  <% } %>

</div>
<% if (comment.isLoggedIn() && !comment.isVersionView()) { %>
<div class="icon-link-ouside registryWriteOperation">
    <a class="icon-link registryWriteOperation" style="background-image:url(../admin/images/add.gif);"
       onclick="javascript:showHideCommon('add-comment-div');if($('add-comment-div').style.display!='none')$('comment').focus();">
        <fmt:message key="add.comment"/></a>
</div>
<% } %>

<div class="registryWriteOperation" id="add-comment-div" style="display:none;padding-bottom:10px;">
    <form onsubmit="return addComment('<%=request.getParameter("path")%>');">
        <table cellspacing="0" cellpadding="0" border="0" style="width:100%"
               class="styledLeft">
            <thead>
                <tr>
                    <th><fmt:message key="add.new.comment"/></th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><textarea id="comment" name="comment" style="width:100%"></textarea>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <input type="button" class="button" value="<fmt:message key="add"/>"
                               onclick="addComment('<%=comment.getPathWithVersion()%>');"/>
                        <input type="button"
                               class="button"
                               value="<fmt:message key="cancel"/>"
                               onclick="showHideCommon('add-comment-div');"/></td>
                </tr>
            </tbody>
        </table>
    </form>
</div>

<div id="commentsList">
    <div id="commentsSum" class="summeryStyle">
        <% if (comments.length > 0) {
        %>
       
                <fmt:message key="no.of.comments">
                    <fmt:param
                            value="<%=comments.length%>"/>
                </fmt:message>
                <%

                } else {

                %>
                <fmt:message key="no.comments.on.this.entry.yet"/>
                <%

                    }

                %>
    </div>
    <div <% if (comments.length >= 5) {%>style="overflow-y:auto;overflow-x:hidden;height:300px" <% } %>>
    <table cellpadding="0" cellspacing="0" class="styledLeft" id="commentsTable">
        <tbody>
            <%
                for (int i = 0; i < comments.length; i++) {
                    Comment comment1 = comments[i];
                    String commentString = comment1.getText();
                    String commentedTime = CommonUtil.formatDate(comment1.getCreatedTime().getTime());
                    String commentedUser = comment1.getUser();
					String commentPath = comment1.getCommentPath();

            %>


            <tr>
                <td>
                    <div valign="top"
                         style="padding-top:10px;padding-bottom:10px;">
                        <% if (!comment.isVersionView()) { %>
	                    <a class="closeButton icon-link registryWriteOperation" onclick="delComment('<%=request.getParameter("path")%>','<%=commentPath%>')" id="closeC<%=i%>" title="<fmt:message key="delete"/>" style="background-image: url(../admin/images/delete.gif);position:relative;float:right">&nbsp;</a>
                        <% } %>
	                    <fmt:message key="comment">
	                        <fmt:param value="<%=commentString%>"/>
	                    </fmt:message>
	                    <br/>
	                    <fmt:message key="posted.on.by">
	                        <fmt:param value="<%=commentedTime%>"/>
	                        <fmt:param value="<%=commentedUser%>"/>
	                    </fmt:message>
	                    <div style="clear:both;"></div>
                    </div>
                </td>
            </tr>


            <%

                }

            %>
        </tbody>
    </table>
    </div>
</div>
<!-- Page resource path prints here -->


</div>
<script type="text/javascript">
    alternateTableRows('commentsTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>
