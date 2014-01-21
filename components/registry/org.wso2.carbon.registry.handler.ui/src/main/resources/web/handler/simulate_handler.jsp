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

<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>
<link type="text/css" rel="stylesheet" href="css/handlerui.css"/>


<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<carbon:jsi18n
		resourceBundle="org.wso2.carbon.registry.handler.ui.i18n.JSResources"
		request="<%=request%>" namespace="org.wso2.carbon.registry.handler.ui"/>
<script type="text/javascript" src="js/handler.js"></script>

<fmt:bundle basename="org.wso2.carbon.registry.handler.ui.i18n.Resources">
<carbon:breadcrumb
        label="handler.simulate"
        resourceBundle="org.wso2.carbon.registry.handler.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<script type="text/javascript">
    fillMediaTypes();
    function cancelSimulateHandler() {
        document.location.href = "handler.jsp?region=region3&item=registry_handler_menu";
    }
    function fillResourceMediaTypeTimed() {
        if (mediaTypeMap['txt']) {
            fillResourceMediaType();
        } else {
            setTimeout(fillResourceMediaTypeTimed, 100);
        }
    }
</script>
<div id="middle">
    <h2><fmt:message key="handler.simulate"/></h2>
    <div id="workArea">
        <form id="handler.simulate.form" method="post" action="simulate_handler-ajaxprocessor.jsp">
            <table class="styledLeft" cellspacing="0" cellpadding="0">
                <thead>
                <tr>
                    <th>
                        <span style="float: left; position: relative; margin-top: 2px;"><fmt:message key="simulation.details"/></span>
                    </th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td style="margin: 0px ! important; padding: 0px ! important;">
                        <table class="normal" cellpadding="0" cellspacing="0" id="simulatorTable" style="width:100%">
                            <tbody>
                            <tr id="operationRow">
                                <td class="leftCol-small">
                                    <fmt:message key="operation.name"/><span class="required" id="required_operation">*</span>
                                </td>
                                <td>
                                    <select name="operation" value="" id="operation" onchange="changeOperation(this[this.selectedIndex].value)"></select>
                                </td>
                            </tr>
                            <tr style="display:none">
                                <td>
                                   <fmt:message key="path"/><span class="required" id="path_required">*</span>
                                </td>
                                <td>
                                    <input type="text" style="width:400px" name="path" value="" id="path" onchange="fillMediaTypes();fillResourceMediaTypeTimed();"/>
                                    <input id="path_browser"
                                        type="button"
                                        title="<fmt:message key="resource.tree"/>"
                                        onclick="showResourceTree('path', fillResourceMediaType);"
                                        value=".." class="button browse_button"/>
                                </td>
                            </tr>
                            <tr style="display:none">
                                <td>
                                    <fmt:message key="media.type"/><span class="required" id="mediaType_required">*</span>
                                </td>
                                <td>
                                    <input type="text" style="width:400px" name="mediaType" value="" id="mediaType"/>
                                </td>
                            </tr>
                            <tr style="display:none">
                                <td>
                                    <fmt:message key="resource.path"/><span class="required" id="resourcePath_required">*</span>
                                </td>
                                <td>
                                    <input type="text" style="width:400px" name="resourcePath" value="" id="resourcePath"/>
                                    <input id="resourcePath_browser"
                                        type="button"
                                        title="<fmt:message key="resource.tree"/>"
                                        onclick="showResourceTree('resourcePath');"
                                        value=".." class="button browse_button"/>
                                </td>
                            </tr>
                            <tr style="display:none">
                                <td>
                                    <fmt:message key="type"/><span class="required" id="type_required">*</span>
                                </td>
                                <td>
                                    <select name="type" id="type">
                                        <option value="resource"><fmt:message key="resource"/></option>
                                        <option value="collection"><fmt:message key="collection"/></option>
                                    </select>
                                </td>
                            </tr>
                            <tr style="display:none">
                                <td>
                                    <span id="param1_txt">param1</span><span class="required" id="param1_required">*</span>
                                </td>
                                <td>
                                    <input type="text" style="width:400px" name="param1" value="" id="param1"/>
                                    <input id="param1_browser"
                                        type="button"
                                        title="<fmt:message key="resource.tree"/>"
                                        onclick="showResourceTree('param1');"
                                        value=".." class="button browse_button"/>
                                </td>
                            </tr>
                            <tr style="display:none">
                                <td>
                                    <span id="param2_txt">param2</span><span class="required" id="param2_required">*</span>
                                </td>
                                <td>
                                    <input type="text" style="width:400px" name="param2" value="" id="param2"/>
                                    <input id="param2_browser"
                                        type="button"
                                        title="<fmt:message key="resource.tree"/>"
                                        onclick="showResourceTree('param2');"
                                        value=".." class="button browse_button"/>
                                </td>
                            </tr>
                            <tr style="display:none">
                                <td>
                                    <span id="param3_txt">param1</span><span class="required" id="param3_required">*</span>
                                </td>
                                <td>
                                    <input type="text" style="width:400px" name="param3" value="" id="param3"/>
                                    <input id="param3_browser"
                                        type="button"
                                        title="<fmt:message key="resource.tree"/>"
                                        onclick="showResourceTree('param3');"
                                        value=".." class="button browse_button"/>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <input class="button" type="button" onclick="simulateHandler()" value="<fmt:message key="simulate"/>"/>
                        <input class="button" type="button" value="<fmt:message key="cancel"/>" onclick="cancelSimulateHandler(); return false;"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </form>
        <div id="simulation"></div>
        <div id="simulationChart">
        </div>
    </div>
</div>
</fmt:bundle>
<script type="text/javascript">
    YAHOO.util.Event.onDOMReady(fillOperation);
</script>