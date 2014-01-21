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

<%@ page import="org.wso2.carbon.registry.properties.stub.beans.xsd.RetentionBean" %>
<%@ page import="org.wso2.carbon.registry.properties.ui.clients.PropertiesServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>


<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<carbon:jsi18n resourceBundle="org.wso2.carbon.registry.properties.ui.i18n.JSResources"
		request="<%=request%>" namespace="org.wso2.carbon.registry.properties.ui"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../properties/js/properties.js"></script>
<link rel="stylesheet" type="text/css"
      href="../resources/css/registry.css"/>

<%
    String path = request.getParameter("path");
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    PropertiesServiceClient client = new PropertiesServiceClient(config, session);
    ResourceServiceClient res_client = new ResourceServiceClient(cookie, config, session);
    boolean writeAllowed = res_client.getResourceData(new String[]{path})[0].getPutAllowed();
    RetentionBean bean;
    boolean readOnly;
    try {
        bean = client.getRetentionProperties(request);
        readOnly = (bean == null) ? false : bean.getReadOnly();
        // Fixing REGISTRY-789  - disallowing setting retention properties for versioned resources
        if(path.matches(".*;version:\\d+$")) {
           readOnly = true;
        }
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
    <jsp:include page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }
%>
<% if(!readOnly) {%>
<script type="text/javascript">
    function initDatePickers() {
        jQuery("#fromDate").datepicker();
        jQuery("#toDate").datepicker();
    }
    jQuery(document).ready(initDatePickers);
</script>
<fmt:bundle basename="org.wso2.carbon.registry.properties.ui.i18n.Resources">
    <div class="box1-head" style="height:auto;margin-top:10px;">
        
        <table cellspacing="0" cellpadding="0" border="0" style="width:100%">
        <tr>
            <td valign="top"><h2 class="sub-headding-comments"><fmt:message key="retention"/></h2></td>

            <td align="right" valign="top" class="expanIconCell">

                <a onclick="showRetention()">
                    <img src="images/icon-expanded.gif" border="0" align="top"
                         id="retentionIconExpanded" <% if (bean == null) { %> style="display:none;" <% } %>/>
                    <img src="images/icon-minimized.gif" border="0" align="top"
                         id="retentionIconMinimized" <% if (bean != null) { %> style="display:none;" <% } %>/>
                </a>

            </td>

        </tr>
    	</table>
    </div>
    <div class="box1-mid-fill" id="retentionMinimized" <% if (bean != null) { %> style="display:none;" <% } %>></div>
    <!--div class="box1-mid-fill" id="searchMinimized" style="display:none"></div-->
    <div class="box1-mid" id="retentionExpanded" <% if (bean == null) { %> style="display:none;height:auto;" <% } else { %> style="height:auto;" <% } %>>
    <!--div class="box1-mid" id="searchExpanded"-->
        <form action="" method="put" style="display:inline;" name="retentionForm">
            <input type="hidden" id="resourcePathId" name="resourcePath" value="<%=path%>"/>
            <table cellspacing="0" cellpadding="0" border="0" style="width: 100%;"
                   class="styledLeft">
                <tbody>
                <tr>
                    <td>
                        <table class="normal" cellpadding="0" cellspacing="0" border="0">
                            <tr>
                                <td><fmt:message key="from"/> :</td>
                                <td>
                                    <a class="icon-link" style="background-image: url( ../admin/images/calendar.gif);"  onclick="jQuery('#fromDate').datepicker( 'show' );" href="javascript:void(0)"></a>
                                    <input type="text" name="fromDate"
                                           value="<%= (bean != null) ? bean.getFromDate() : "" %>"
                                           style="width:140px;" id="fromDate"
                                           />
                                </td>
                            </tr>
                            <tr>
                                <td><fmt:message key="to"/> :</td>
                                <td>
                                    <a class="icon-link" style="background-image: url( ../admin/images/calendar.gif);" onclick="jQuery('#toDate').datepicker( 'show' );" href="javascript:void(0)"></a>
                                    <input type="text" name="toDate"
                                           value="<%= (bean != null) ? bean.getToDate() : "" %>"
                                           style="width:140px;" id="toDate"
                                           />
                                </td>
                            </tr>
                        </table>
                        <fmt:message key="activities"/><br/>
                                &nbsp;&nbsp;
                        <input type="checkbox" name="write" value="true" id="writeBoxId"
                               <%=(bean != null && bean.getWriteLocked()) ? "checked=\"true\"" : ""%>
                               />
                                <fmt:message key="write"/><br/>
                                &nbsp;&nbsp;
                        <input type="checkbox" name="delete" value="true" id="readBoxId"
                               <%=(bean != null && bean.getDeleteLocked()) ? "checked=\"true\"" : ""%>
                               />
                                <fmt:message key="delete"/>
                    </td>
                </tr>

                <tr>
                    <td class="buttonRow">
                        <input type="button" <%=writeAllowed ? "":"disabled=\"disabled\"" %> id="#_0" value="<fmt:message key="lock"/>"
                               class="button" onclick="setRetentionProperties()"
                               <%=readOnly ? "style=\"display:none\"" : ""%>/>
                        <input type="button" <%=writeAllowed ? "" : "disabled=\"disabled\""  %> id="#_1" value="<fmt:message key="reset"/>"
                               class="button" onclick="resetRetentionProperties()"
                               <%=readOnly ? "style=\"display:none\"" : ""%>/>
                    </td>
                </tr>
                </tbody>
            </table>
        </form>
    </div>
</fmt:bundle>
<% }%>
