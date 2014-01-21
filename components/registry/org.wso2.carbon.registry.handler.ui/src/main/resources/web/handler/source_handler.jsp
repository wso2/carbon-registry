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
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.handler.ui.clients.HandlerManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>

<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<!--EditArea javascript syntax hylighter -->
<script language="javascript" type="text/javascript" src="../editarea/edit_area_full.js"></script>

<carbon:jsi18n
		resourceBundle="org.wso2.carbon.registry.handler.ui.i18n.JSResources"
		request="<%=request%>" namespace="org.wso2.carbon.registry.handler.ui"/>
<script type="text/javascript" src="js/handler.js"></script>
<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String temp = "";
    boolean isNew = true;
    try{
        HandlerManagementServiceClient client = new HandlerManagementServiceClient(cookie, config, session);
        if (request.getParameter("handlerName") != null) {
            temp = client.getHandlerConfiguration(request);
            isNew = false;
        } else {
            temp = "<!-- Following is a sample Handler. Please edit it according to your requirements -->\n" +
            "<handler class=\"org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler\">\n" +
            "\t<filter class=\"org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher\">\n" +
            "\t\t<property name=\"mediaType\">application/vnd.wso2-service+xml</property>\n" +
            "\t</filter>\n" +
            "</handler>\n";
        }
    } catch (Exception e){
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
        String errorMsg = e.getMessage().replaceAll(">","&gt;").replaceAll("<","&lt;");
%>
        <jsp:include page="../admin/error.jsp?<%=errorMsg%>"/>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.registry.handler.ui.i18n.Resources">
<carbon:breadcrumb
        label="handler.source"
        resourceBundle="org.wso2.carbon.registry.handler.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<script type="text/javascript">

    function cancelSaveHandler() {
        document.location.href = "handler.jsp?region=region3&item=registry_handler_menu";
    }
    YAHOO.util.Event.onDOMReady(function() {
        editAreaLoader.init({
            id : "payload"        // textarea id
            ,syntax: "xml"            // syntax to be uses for highgliting
            ,start_highlight: true        // to display with highlight mode on start-up
            ,allow_resize: "both"
            ,min_height:250
        });
    })

</script>
<div id="middle">
    <h2><fmt:message key="handler.source"/></h2>
    <div id="workArea">
        <form id="handler.source.form" method="post" action="save_handler-ajaxprocessor.jsp">
            <table class="styledLeft" cellspacing="0" cellpadding="0">
                <thead>
                <tr>
                    <th>
                        <span style="float: left; position: relative; margin-top: 2px;"><fmt:message key="handler"/></span>
                    </th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <textarea id="payload" style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 275px; margin-top: 5px;" name="payload" rows="30"><%=temp%></textarea>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <input class="button registryWriteOperation" type="button" onclick="saveHandler('<%=request.getParameter("handlerName")%>', '<%=Boolean.toString(isNew)%>')" value="<fmt:message key="save"/>"/>
                        <input disabled="disabled" class="button registryNonWriteOperation" type="button" value="<fmt:message key="save"/>"/>
                        <input class="button" type="button" value="<fmt:message key="cancel"/>" onclick="cancelSaveHandler(); return false;"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </form>
    </div>
</div>
</fmt:bundle>
<script>
$('payload').innerHTML = format_xml($('payload').value);
</script>
