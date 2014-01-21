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
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.registry.common.beans.RatingBean" %>

<carbon:jsi18n resourceBundle="org.wso2.carbon.registry.info.ui.i18n.JSResources"
		request="<%=request%>" namespace="org.wso2.carbon.registry.info.ui" />
<script type="text/javascript" src="../info/js/info.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<%
    RatingBean ratingBean;
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    boolean displayPlainError = true;
    try{
        InfoServiceClient client = null;
        if (request.getParameter("rating") != null) {
            client = new InfoServiceClient(cookie, config, session);
            client.rateResource(request);
        } else {
            displayPlainError = false;
            client = new InfoServiceClient(cookie, config, session);
        }
        ratingBean = client.getRatings(request);
    } catch (Exception e){
        response.setStatus(500);
        if (displayPlainError) {
            %><%=e.getMessage()%><%
            return;
        }
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
        <jsp:include page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }
    float averageRating = ratingBean.getAverageRating();

    String[] userStars = ratingBean.getUserStars();
    String[] averageStars = ratingBean.getAverageStars();
%>
<fmt:bundle basename="org.wso2.carbon.registry.info.ui.i18n.Resources">

    <table cellspacing="10" border="0">
        <%
            if (ratingBean.isLoggedIn()) {
                if (ratingBean.isVersionView()) {
        %>

        <tr>
            <th valign="top"><fmt:message key="my.rating"/>:</th>
            <td nowrap="nowrap">
                <div style="width:120px;">
                    <img class="registryWriteOperation" src="../resources/images/spacer.gif" style="width:16px"/>
                    <img class="registryNonWriteOperation" src="../resources/images/spacer.gif" style="width:16px"/>
                    <img class="registryWriteOperation" src="../info/images/r<%=userStars[0]%>.gif"/>
                    <img class="registryNonWriteOperation" src="../info/images/r<%=userStars[0]%>.gif"/>
                    <img class="registryWriteOperation" src="../info/images/r<%=userStars[1]%>.gif"/>
                    <img class="registryNonWriteOperation" src="../info/images/r<%=userStars[1]%>.gif"/>
                    <img class="registryWriteOperation" src="../info/images/r<%=userStars[2]%>.gif"/>
                    <img class="registryNonWriteOperation" src="../info/images/r<%=userStars[2]%>.gif"/>
                    <img class="registryWriteOperation" src="../info/images/r<%=userStars[3]%>.gif"/>
                    <img class="registryNonWriteOperation" src="../info/images/r<%=userStars[3]%>.gif"/>
                    <img class="registryWriteOperation" src="../info/images/r<%=userStars[4]%>.gif"/>
                    <img class="registryNonWriteOperation" src="../info/images/r<%=userStars[4]%>.gif"/>
                    <br/>

                </div>
            </td>
            <th></th>
            <td></td>
        </tr>

        <%
        } else {
        %>
        <tr>
            <th valign="top"><fmt:message key="my.rating"/>:</th>
            <td nowrap="nowrap">
                <div id="ratingDivUser" style="width:120px;">
                    <span initialState="<%=userStars[0]%> <%=userStars[1]%> <%=userStars[2]%> <%=userStars[3]%> <%=userStars[4]%>"/>
                    <img class="registryWriteOperation"
                         src="../resources/images/spacer.gif" style="width:16px"
                         onclick="setRating('<%=ratingBean.getPathWithVersion()%>', 0)"
                         onmouseover="previewRating('ratingDivUser', 0)"
                         onmouseout="clearPreview('ratingDivUser')"/>
                    <img class="registryNonWriteOperation"
                         src="../resources/images/spacer.gif" style="width:16px" />
                    <img class="registryWriteOperation"
                         src="../info/images/r<%=userStars[0]%>.gif"
                         onclick="setRating('<%=ratingBean.getPathWithVersion()%>', 1)"
                         onmouseover="previewRating('ratingDivUser', 1)"
                         onmouseout="clearPreview('ratingDivUser')"/>
                    <img class="registryNonWriteOperation"
                         src="../info/images/r<%=userStars[0]%>.gif"/>
                    <img class="registryWriteOperation"
                         src="../info/images/r<%=userStars[1]%>.gif"
                         onclick="setRating('<%=ratingBean.getPathWithVersion()%>', 2)"
                         onmouseover="previewRating('ratingDivUser', 2)"
                         onmouseout="clearPreview('ratingDivUser')"/>
                    <img class="registryNonWriteOperation"
                         src="../info/images/r<%=userStars[1]%>.gif"/>
                    <img class="registryWriteOperation"
                         src="../info/images/r<%=userStars[2]%>.gif"
                         onclick="setRating('<%=ratingBean.getPathWithVersion()%>', 3)"
                         onmouseover="previewRating('ratingDivUser', 3)"
                         onmouseout="clearPreview('ratingDivUser')"/>
                    <img class="registryNonWriteOperation"
                         src="../info/images/r<%=userStars[2]%>.gif"/>
                    <img class="registryWriteOperation"
                         src="../info/images/r<%=userStars[3]%>.gif"
                         onclick="setRating('<%=ratingBean.getPathWithVersion()%>', 4)"
                         onmouseover="previewRating('ratingDivUser', 4)"
                         onmouseout="clearPreview('ratingDivUser')"/>
                    <img class="registryNonWriteOperation"
                         src="../info/images/r<%=userStars[3]%>.gif"/>
                    <img class="registryWriteOperation"
                         src="../info/images/r<%=userStars[4]%>.gif"
                         onclick="setRating('<%=ratingBean.getPathWithVersion()%>', 5)"
                         onmouseover="previewRating('ratingDivUser', 5)"
                         onmouseout="clearPreview('ratingDivUser')"/>
                    <img class="registryNonWriteOperation"
                         src="../info/images/r<%=userStars[4]%>.gif"/>
                </div>
            </td>
            <th></th>
            <td></td>
        </tr>
        <% }
        } %>
        <tr>
            <th valign="top"><fmt:message key="rating"/>:</th>
            <td nowrap="nowrap">
                <div id="ratingDivAvg" style="width:120px;">
                    <img class="registryWriteOperation" src="../resources/images/spacer.gif" style="width:16px"/>
                    <img class="registryNonWriteOperation" src="../resources/images/spacer.gif" style="width:16px"/>
                    <img class="registryWriteOperation" src="../info/images/r<%=averageStars[0]%>.gif"/>
                    <img class="registryNonWriteOperation" src="../info/images/r<%=averageStars[0]%>.gif"/>
                    <img class="registryWriteOperation" src="../info/images/r<%=averageStars[1]%>.gif"/>
                    <img class="registryNonWriteOperation" src="../info/images/r<%=averageStars[1]%>.gif"/>
                    <img class="registryWriteOperation" src="../info/images/r<%=averageStars[2]%>.gif"/>
                    <img class="registryNonWriteOperation" src="../info/images/r<%=averageStars[2]%>.gif"/>
                    <img class="registryWriteOperation" src="../info/images/r<%=averageStars[3]%>.gif"/>
                    <img class="registryNonWriteOperation" src="../info/images/r<%=averageStars[3]%>.gif"/>
                    <img class="registryWriteOperation" src="../info/images/r<%=averageStars[4]%>.gif"/>
                    <img class="registryNonWriteOperation" src="../info/images/r<%=averageStars[4]%>.gif"/>
                    <br/>

                </div>
            </td>
            <td>(<%=averageRating%>)</td>
            <td></td>
        </tr>
    </table>
</fmt:bundle>
