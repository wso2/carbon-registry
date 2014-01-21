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
<%@ page import="org.wso2.carbon.registry.common.beans.CommentBean" %>
<%@ page import="org.wso2.carbon.registry.common.beans.utils.Comment" %>
<carbon:jsi18n resourceBundle="org.wso2.carbon.registry.info.ui.i18n.JSResources"
		request="<%=request%>" namespace="org.wso2.carbon.registry.info.ui" />
<script type="text/javascript" src="../info/js/info.js"></script>

    <div>
        <%
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            InfoServiceClient client = new InfoServiceClient(cookie, config, session);
            CommentBean comment;
            try {
                client.removeComment(request);
                comment = client.getComments(request);
            } catch (Exception e) {
                response.setStatus(500);
                %><%="----commentDeleteFailed----"%><%
                return;
            }
            Comment[] comments = comment.getComments();

            String content = " ";
        %>
    <fmt:bundle basename="org.wso2.carbon.registry.info.ui.i18n.Resources">
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
        <div <% if (comments.length >= 5) {%>style="overflow-y:auto;overflow-x:hidden;height:300px" <% } %> >
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
                    	
                        <div valign="top" style="padding-top:10px;padding-bottom:10px;">
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
    </fmt:bundle>
</div>
<script type="text/javascript">
alternateTableRows('commentsTable', 'tableEvenRow', 'tableOddRow');
</script>

