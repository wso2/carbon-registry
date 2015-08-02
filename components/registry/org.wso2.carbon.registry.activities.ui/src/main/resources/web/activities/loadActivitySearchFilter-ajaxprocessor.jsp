<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.registry.activities.ui.clients.ActivityServiceClient" %>
<%@ page import="org.wso2.carbon.registry.activities.beans.xsd.CustomActivityParameterBean" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>


<fmt:bundle basename="org.wso2.carbon.registry.activities.ui.i18n.Resources">
<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String filterName = request.getParameter("filterName");
    CustomActivityParameterBean customActivityParameterBean;
    try {
        ActivityServiceClient client = new ActivityServiceClient(cookie, config, session);
        customActivityParameterBean = client.getActivitySearchFilter(filterName);
        System.out.println(customActivityParameterBean.getUserName());
    } catch (Exception e) {
        response.setStatus(500);
%>
<script type="text/javascript">
        function initDatePickers() {
            jQuery("#fromDate").datepicker();
            jQuery("#toDate").datepicker();
        }
        jQuery(document).ready(function(){
                initDatePickers();
                new Ajax.Updater('savedSearchFilterListDiv', '../activities/getSavedActivitySearchFilters-ajaxprocessor.jsp',{evalScripts:true});
            });
    </script>

<script type="text/javascript">
    CARBON.showErrorDialog("<%=e.getMessage()%>");
</script>
<%
        return;
    }
%>


<%  if (filterName != null || filterName != "") {
    System.out.println(customActivityParameterBean.getPath()); %>

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
                                        <td><input type="text" name="userName" id="user" value="<%=customActivityParameterBean.getUserName()%>" onkeypress="handleUserNameKeyPress(event);"/></td>
                                    </tr>
                                    <tr>
                                        <td valign="top"><fmt:message key="path"/></td>
                                        <td>
                                            <input type="text" id="path" name="path" value="<%=customActivityParameterBean.getPath()%>" onkeypress="handleUserNameKeyPress(event);"/>
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
                                                        <input type="text" name="fromDate" id="fromDate" value="<%=customActivityParameterBean.getFromDate()%>"
                                                               style="widht:140px;" onkeypress="handleUserNameKeyPress(event);"/>
                                                    </td>
                                                    <td style="padding-left:10px;">
                                                        <fmt:message key="to"/> :
                                                    </td>
                                                    <td>
                                                        <a class="icon-link" style="background-image: url( ../admin/images/calendar.gif);" onclick="jQuery('#toDate').datepicker( 'show' );" href="javascript:void(0)"></a>
                                                        <input type="text" name="toDate" id="toDate" value="<%=customActivityParameterBean.getToDate()%>"
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
                                                <option selected><%=customActivityParameterBean.getFilter()%></option>
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


<%
    } else { System.out.println("inside else");%>
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
    <%
    }
    %>
    </fmt:bundle>