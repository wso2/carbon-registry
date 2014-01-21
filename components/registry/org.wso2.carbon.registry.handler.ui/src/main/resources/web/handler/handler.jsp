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
<carbon:jsi18n
		resourceBundle="org.wso2.carbon.registry.handler.ui.i18n.JSResources"
		request="<%=request%>" namespace="org.wso2.carbon.registry.handler.ui"/>
<script type="text/javascript" src="js/handler.js"></script>
<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String[] temp = null;
    try{
        HandlerManagementServiceClient client =
                new HandlerManagementServiceClient(cookie, config, session);
        temp = client.getHandlerList(request);
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
        label="registry.handler.menu"
        resourceBundle="org.wso2.carbon.registry.handler.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
<div id="middle">

      <h2>
         <fmt:message key="registry.handler.menu"/>
      </h2>
      <div id="workArea">
<%
    if (temp == null || temp.length == 0 || temp[0] == null) {
%>
        <div class="registryWriteOperation" id="noHandlerDiv">
            <fmt:message
                    key="no.handlers.are.currently.defined.click.add.handler.to.create.a.new.handler"/>
        </div>
        <div class="registryNonWriteOperation" id="noHandlerDiv">
            <fmt:message
                    key="no.handlers.are.currently.defined"/>
        </div>
<%
    } else {
%>
          <table class="styledLeft" cellspacing="1" id="handlerTable">
              <thead>
                  <tr>
                      <th>
                          <fmt:message key="name"/>
                      </th>
                      <th>
                          <fmt:message key="actions"/>
                      </th>
                  </tr>
              </thead>
              <tbody>

            <%
                for(String next:temp){
            %>
                  <tr>
                      <td>
                          <%=next%>
                      </td>
                                      <td>
                              <a href="#" onclick="editHandler('<%=next%>')" class="icon-link" style="background-image:url(../admin/images/edit.gif);"><fmt:message key="edit"/></a>
				<a href="#" onclick="deleteHandler('<%=next%>')" class="icon-link registryWriteOperation" style="background-image:url(../admin/images/delete.gif);"><fmt:message key="delete"/></a>
                                   </td>

                  </tr>
          <%
              }
          %>
              </tbody>
          </table>
<%
    }
%>
          <script type="text/javascript">
              alternateTableRows('handlerTable', 'tableEvenRow', 'tableOddRow');
          </script>

          <div class="registryWriteOperation" style="height:50px;">
              <a class="icon-link registryWriteOperation" style="background-image: url(../admin/images/add.gif);" href="source_handler.jsp"><fmt:message key="add.handler"/></a><br /><br />
              <a class="icon-link registryWriteOperation" style="background-image: url(../handler/images/simulate.png);" href="simulate_handler.jsp"><fmt:message key="simulate.handlers"/></a>
          </div>

          <div class="registryNonWriteOperation" style="height:25px;">
              <a class="icon-link registryNonWriteOperation" style="background-image: url(../handler/images/simulate.png);" href="simulate_handler.jsp"><fmt:message key="simulate.handlers"/></a>
          </div>

      </div>


  </div>
</fmt:bundle>
