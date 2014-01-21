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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>

<carbon:jsi18n
		resourceBundle="org.wso2.carbon.registry.activities.ui.i18n.JSResources"
		request="<%=request%>" namespace="org.wso2.carbon.registry.activities.ui" />
<script type="text/javascript" src="js/activity.js"></script>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<link rel="stylesheet" type="text/css"
      href="../resources/css/registry.css"/>
<%
    String hashData= null;
%>


<fmt:bundle basename="org.wso2.carbon.registry.activities.ui.i18n.Resources">
    <carbon:breadcrumb label="activities"
                       resourceBundle="org.wso2.carbon.registry.activities.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <style type="text/css">
        table tbody tr td {
            border: 0 none;
            padding: 0;
            text-align: left;
            vertical-align: top;
        }

    </style>
    <script type="text/javascript">
        function initDatePickers() {
            jQuery("#fromDate").datepicker();
            jQuery("#toDate").datepicker();
        }
        jQuery(document).ready(initDatePickers)
    </script>
    <div id="middle">

        <h2><fmt:message key="activities"/></h2>

        <div id="workArea">

    <script type="text/javascript">


    </script>


            <div id="activityReason" style="display: none;"></div>

            <form action="activity-ajaxprocessor.jsp" method="get" id="activityForm"
                  onsubmit="return submitActivityForm();">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="search.activities"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRow">
                            <table class="normal" id="#_innerTable">
                                <tr>
                                    <td style="width:100px;" valign="top"><fmt:message key="username"/></td>
                                    <td><input type="text" name="userName" id="user" onkeypress="handleUserNameKeyPress(event);"/></td>
                                </tr>
                                <tr>
                                    <td valign="top"><fmt:message key="path"/></td>
                                    <td>
                                        <input type="text" id="path" name="path" onkeypress="handleUserNameKeyPress(event);"/>
                                        <input type="button" class="button" value=".." title="<fmt:message key="resource.tree"/>"
                                               onclick="showResourceTree('path');"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td valign="top"><fmt:message key="date"/></td>
                                    <td>
                                        <table cellpadding="0" cellspacing="0" border="0">

                                            <tr>
                                                <td>
                                                    <fmt:message key="from"/> :
                                                </td>
                                                <td>
                                                    <a class="icon-link" style="background-image: url( ../admin/images/calendar.gif);" onclick="jQuery('#fromDate').datepicker( 'show' );" href="javascript:void(0)"></a>
                                                    <input type="text" name="fromDate" id="fromDate"
                                                           style="widht:140px;" onkeypress="handleUserNameKeyPress(event);"/>
                                                </td>
                                                <td style="padding-left:10px;">
                                                    <fmt:message key="to"/> :
                                                </td>
                                                <td>
                                                    <a class="icon-link" style="background-image: url( ../admin/images/calendar.gif);" onclick="jQuery('#toDate').datepicker( 'show' );" href="javascript:void(0)"></a>
                                                    <input type="text" name="toDate" id="toDate"
                                                           style="widht:140px;" onkeypress="handleUserNameKeyPress(event);"/>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td></td>
                                                <td class="helpTextTop"
                                                    style="margin:0px;padding:0px;"><fmt:message key="mm.dd.yyyy"/>
                                                </td>
                                                <td></td>
                                                <td class="helpTextTop"
                                                    style="margin:0px;padding:0px;"><fmt:message key="mm.dd.yyyy"/>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>

                                </tr>
                                <tr>
                                    <td valign="top"><fmt:message key="filter.by"/></td>
                                    <td>
                                        <select name="filter" id="filter">
                                            <option value="all"><fmt:message key="all"/></option>
                                            <option value="resourceAdd"><fmt:message key="resource.add"/></option>
                                            <option value="resourceUpdate"><fmt:message key="resource.updates"/></option>
                                            <option value="delete"><fmt:message key="resource.deletes"/></option>
                                            <option value="restore"><fmt:message key="resource.restores"/></option>
                                            <option value="commentings"><fmt:message key="comments"/></option>
                                            <option value="taggings"><fmt:message key="tagging"/></option>
                                            <option value="ratings"><fmt:message key="ratings"/></option>
                                            <option value="createSymbolicLink"><fmt:message key="create.symbolic.link"/></option>
                                            <% if (CarbonUIUtil.isSuperTenant(request)) {
                                                // Display the create remote link search filter only to the super-tenant, as it
                                                // does not make sense to be displayed for sub-tenants. For the super-tenant, the
                                                // tenant domain would be null.
                                            %>
                                            <option value="createRemoteLink"><fmt:message key="create.remote.link"/></option>
                                            <% } %>
                                            <option value="removeLink"><fmt:message key="remove.link"/></option>
                                            <option value="addAssociation"><fmt:message key="add.association"/></option>
                                            <option value="removeAssociation"><fmt:message key="remove.association"/></option>
                                            <option value="associateAspect"><fmt:message key="associate.aspect"/></option>
                                        </select>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input class="button" type="button" onclick="submitActivityForm(1);"
                                   value="<fmt:message key="search.activities"/>"/>
                            <input class="button" type="button" onclick="clearAll();"
                                   value="<fmt:message key="clear"/>"/>
                        </td>
                    </tr>
                </table>
            </form>
            <br/>
            <br/>
            <br/>

            <div id="activityList">
            </div>
        </div>
    </div>
</fmt:bundle>