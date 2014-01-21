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

<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

    <%
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        String path = request.getParameter("path");
        String textContent;
        String mediaType;
        boolean showPlainText = false;
        boolean hideRichText = false;
        try {
            ResourceServiceClient client = new ResourceServiceClient(cookie, config, session);
            textContent = client.getTextContent(request);
            mediaType = client.getMetadata(request).getMediaType();
            if (mediaType.equals("application/wsdl+xml") ||
            		mediaType.equals("application/wadl+xml") ||
                    mediaType.equals("application/policy+xml") ||
                    mediaType.equals("application/x-xsd+xml") ||
                    mediaType.equals("application/vnd.wso2-service+xml") ||
                    mediaType.equals("application/rdf+xml") ||
                    mediaType.equals("application/rss+xml") ||
                    mediaType.equals("application/xhtml+xml") ||
                    mediaType.equals("application/vnd.mozilla.xul+xml") ||
                    mediaType.equals("application/vnd.wap.wbxml") ||
                    mediaType.equals("application/vnd.wso2.endpoint") ||
                    mediaType.equals("application/vnd.wso2.registry-ext-type+xml") ||
                    mediaType.equals("application/vnd.sun.wadl+xml") ||
                    mediaType.startsWith("application/vnd.sun.xml") ||
                    mediaType.endsWith(".bpel") ||
                    mediaType.equals("application/xml")) {
                hideRichText = true;
            }
            showPlainText = true;
        } catch (Exception e) {
            response.setStatus(500);
            %><%=e.getMessage()%><%
            return;
        }
    %>
<fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">
    <br/>
    <div>
        <input id="editTextContentIDRichText1" type="radio" name="editTextContentIDRichText" <%=(showPlainText) ? "checked=\"checked\"" : "" %> value="plain" onclick="handleUpdateRichText()" /> <fmt:message key="plain.text.editor"/>
        <input id="editTextContentIDRichText0" type="radio" name="editTextContentIDRichText" <%=(showPlainText) ? "" : "checked=\"checked\"" %> <%=(hideRichText) ? "disabled=\"disabled\"" : "" %> value="rich" onclick="handleUpdateRichText()" /> <fmt:message key="rich.text.editor"/>
    </div>
    <textarea id="editTextContentIDPlain" style="<%=(showPlainText) ? "" : "display:none;" %>width:99%;height:200px"><%=textContent.replace("&", "&amp;")%></textarea>
    <div class="yui-skin-sam" id="editTextContentTextAreaPanel" <%=(showPlainText) ? "style=\"display:none\"" : "" %> >
        <textarea id="editTextContentID" name="editTextContentID" style="display:none;"><%=textContent.replace("&", "&amp;")%></textarea>
    </div>
    <br/>
    <input type='button' class='button' id ="saveContentButtonID" onclick='updateTextContent("<%=path%>","<%=mediaType%>","false")'
           value='<fmt:message key="save.content"/>'/>
    <input type='button' class='button' id ="cancelContentButtonID" onclick='cancelTextContentEdit()'
           value='<fmt:message key="cancel"/>'/>
    <br/>

</fmt:bundle>