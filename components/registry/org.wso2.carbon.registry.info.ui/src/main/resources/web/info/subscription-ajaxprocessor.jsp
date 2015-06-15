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
<%@ page import="org.wso2.carbon.registry.common.beans.utils.SubscriptionInstance" %>
<%@ page import="org.wso2.carbon.registry.common.beans.SubscriptionBean" %>
<%@ page import="org.wso2.carbon.registry.common.beans.EventTypeBean" %>
<%@ page import="org.wso2.carbon.registry.common.beans.utils.EventType" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.ResourceTreeEntryBean" %>
<%@ page import="org.wso2.carbon.context.CarbonContext" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantConstants" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.common.ui.UIConstants" %>
<%@ page import="org.wso2.carbon.registry.core.pagination.PaginationContext" %>

<carbon:jsi18n resourceBundle="org.wso2.carbon.registry.info.ui.i18n.JSResources"
		request="<%=request%>" namespace="org.wso2.carbon.registry.info.ui"/>
<script type="text/javascript" src="../info/js/info.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String[] events = null;
    String[] resourceEventNames = null;
    String[] collectionEventNames = null;
    boolean[] isEventVisible = null;
    SubscriptionInstance[] subscriptions = null;
    boolean isResource = true;
    boolean canUnsubscribe = false;
    boolean canSubscribeOthers = false;
    boolean canSubscribeOtherRoles = false;
    boolean canAdd = true;
    String username = null;
    String[] roles = null;
    boolean displayPlainError = true;
    String url = null;
    boolean isCollection=true;
    ResourceServiceClient resourceServiceClient=null;
    ResourceTreeEntryBean resourceTreeEntryBean = null;
    boolean isSuperTenant = false;
    resourceServiceClient = new ResourceServiceClient(config, session);
    String path = request.getParameter("path");
    String requestedPage = request.getParameter("page");
    if ((resourceTreeEntryBean = resourceServiceClient.getResourceTreeEntry(path)) != null) {
        isCollection = resourceTreeEntryBean.getCollection();
    }

     if(CarbonContext.getThreadLocalCarbonContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
         isSuperTenant = true;
     }

    try{
        InfoServiceClient client = null;
        if (request.getParameter("endpoint") != null && request.getParameter("eventName") != null) {
            client = new InfoServiceClient(cookie, config, session);
            if (request.getParameter("doRest") == null) {
                client.subscribe(request);
            } else {
                client.subscribeREST(request);
            }
        } else if (request.getParameter("id") != null) {
            client = new InfoServiceClient(cookie, config, session);
            client.unsubscribe(request);
        } else {
            displayPlainError = false;
            client = new InfoServiceClient(cookie, config, session);
        }
        if (request.getParameter("path") != null) {
             url = client.getRemoteURL(request);
        }
        if (url == null) {
            int start;
            int count = (int) (RegistryConstants.ITEMS_PER_PAGE * 1.5);
            if (requestedPage != null) {
                start = (int) ((Integer.parseInt(requestedPage) - 1) * (RegistryConstants.ITEMS_PER_PAGE * 1.5));
            } else {
                start = 1;
            }
            PaginationContext.init(start, count, "", "", 1500);
            SubscriptionBean subscriptionBean = client.getSubscriptions(request);
            if (!subscriptionBean.getLoggedIn() || subscriptionBean.getVersionView()) {
                canAdd = false;
            }
            subscriptions = subscriptionBean.getSubscriptionInstances();
            EventTypeBean eventTypeBean = client.getEventTypes(request);
            EventType[] eventTypes = eventTypeBean.getEventTypes();
            events = new String[eventTypes.length];
            resourceEventNames = new String[eventTypes.length];
            collectionEventNames = new String[eventTypes.length];
            isEventVisible = new boolean[eventTypes.length];
            for (int i = 0; i < eventTypes.length; i++) {
                if (eventTypes[i] != null) {
                    events[i] = eventTypes[i].getId();
                    resourceEventNames[i] = eventTypes[i].getResourceEvent();
                    collectionEventNames[i] = eventTypes[i].getCollectionEvent();
                    if (eventTypes[i].getId().startsWith("publisher") || eventTypes[i].getId().startsWith("store")){
                        isEventVisible[i] = false;
                    } else{
                        isEventVisible[i] = true;
                    }
                }
            }
            isResource = client.isResource(request);
            canUnsubscribe = (subscriptionBean.getUserAccessLevel() > 0);
            canSubscribeOthers = (subscriptionBean.getUserAccessLevel() > 2);
            canSubscribeOtherRoles = (subscriptionBean.getRoleAccessLevel() > 0);
            username = subscriptionBean.getUserName();
            roles = subscriptionBean.getRoles();
        }
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
    if (url != null) {
        if (canAdd) {
%>
<fmt:bundle basename="org.wso2.carbon.registry.info.ui.i18n.Resources">
<div class="icon-link-ouside registryWriteOperation" id="add-subscription-outer-div">
    <a class="icon-link registryWriteOperation" target="_blank" style="background-image:url(../admin/images/add.gif);" href="<%=url%>">
        <fmt:message key="add.remote.subscription"/></a>
</div>
<div id="subscriptionsSummary" class="summeryStyle registryWriteOperation"><fmt:message key="click.to.add.subscription.to.remote.resource"/></div>
</fmt:bundle>
<%
        }
    } else {
%>
<fmt:bundle basename="org.wso2.carbon.registry.info.ui.i18n.Resources">
<%
    if (canAdd) {
%>
<div class="icon-link-ouside registryWriteOperation" id="add-subscription-outer-div" style="*padding-right:10px;">
    <a class="icon-link registryWriteOperation" style="background-image:url(../admin/images/add.gif);" href="javascript:void(0)"
       onclick="javascript:showHideCommon('add-subscription-div');if($('add-subscription-div').style.display!='none')$('eventList').focus();">
        <fmt:message key="add.subscription"/></a>
</div>
<%
    }
%>
<div class="registryWriteOperation" id="add-subscription-div" style="display:none;padding-bottom:10px;">
    <table cellpadding="0" cellspacing=10 border="0" width="100%">
        <tr>
            <td valign="middle" style="width:30px;text-align:left"><fmt:message key="event"/>&nbsp;<span class="required">*</span></td>
            <td valign="top" style="width:70px;text-align:left;">
                <select id="eventList" onchange="changeVisibility()">
                    <option value="0"><fmt:message key="select"/></option>
<%
    for (int i = 0; i < events.length; i++) {
        if (isResource) {
            if (resourceEventNames[i] != null && isEventVisible[i]) {
%>
                    <option value="<%=resourceEventNames[i]%>">
                        <% if (events[i].startsWith("custom:")) { %><%=events[i].substring("custom:".length())%>
                        <%} else {%><fmt:message key="<%=events[i]%>"/><%}%>
                    </option>
<%
            }
        } else if (collectionEventNames[i] != null && isEventVisible[i]) {
%>
                    <option value="<%=collectionEventNames[i]%>">
                        <% if (events[i].startsWith("custom:")) { %><%=events[i].substring("custom:".length())%>
                        <%} else {%><fmt:message key="<%=events[i]%>"/><%}%>
                    </option>
<%
        }        
    }
%>
                </select>
            </td>
        </tr>
        <tr>
            <td valign="middle" style="width:30px;text-align:left"><fmt:message key="notification.method"/>&nbsp;<span class="required">*</span></td>
            <td valign="top" style="width:70px;text-align:left;">
                <select id="notificationMethodList" disabled="disabled" onchange="changeVisibility()">
                    <option value="0"><fmt:message key="select"/></option>
                    <option value="1"><fmt:message key="email"/></option>
                    <option value="2"><fmt:message key="rest"/></option>
                    <option value="3"><fmt:message key="soap"/></option>
                    <option value="4"><fmt:message key="username"/></option>
                    <option value="5"><fmt:message key="role"/></option>
                    <% if(isSuperTenant) {%>
                    <option value="6"><fmt:message key="management.console"/></option>
                    <option value="7"><fmt:message key="jmx"/></option>
                    <%}%>
                </select>
            </td>
        </tr>
        <tr id="subscriptionDataInputRecord" style="display:none">
            <td colspan="2">
                <div id="subscriptionDataEmail" style="display:none">
                    <p style="padding:0 0 5px 0"><fmt:message key="enter.email"/></p>
                    <p>
                        <fmt:message key="enter.email.prompt"/>&nbsp;<span class="required">*</span>&nbsp;&nbsp;<input type="text" id="subscriptionEmail" />
                    </p>
                    <p style="padding:5px 0 5px 0"><fmt:message key="select.digest.delivery"/></p>
                    <p>
                        <fmt:message key="digest.delivery"/>&nbsp;<select
                            id="digestDeliveryEmail" disabled="disabled">
                            <option value="0"><fmt:message key="digest.none"/></option>
                            <option value="1"><fmt:message key="digest.hourly"/></option>
                            <option value="2"><fmt:message key="digest.daily"/></option>
                            <option value="3"><fmt:message key="digest.weekly"/></option>
                            <option value="4"><fmt:message key="digest.fortnightly"/></option>
                            <option value="5"><fmt:message key="digest.monthly"/></option>
                        </select>
                    </p>
                </div>
                <div id="subscriptionDataREST" style="display:none">
                    <p style="padding:0 0 5px 0"><fmt:message key="enter.url"/></p>
                    <p>
                        <fmt:message key="enter.url.prompt"/>&nbsp;<span class="required">*</span>&nbsp;&nbsp;<input type="text" id="subscriptionREST" />
                    </p>
                </div>
                <div id="subscriptionDataSOAP" style="display:none">
                    <p style="padding:0 0 5px 0"><fmt:message key="enter.endpoint"/></p>
                    <p>
                        <fmt:message key="enter.endpoint.prompt"/>&nbsp;<span class="required">*</span>&nbsp;&nbsp;<input type="text" id="subscriptionSOAP" />
                    </p>
                </div>
                <div id="subscriptionDataUserProfile" style="display:none">
<%
    if (canSubscribeOthers) {
%>
                    <p style="padding:0 0 5px 0"><fmt:message key="enter.username"/></p>
                    <p>
                        <fmt:message key="enter.username.prompt"/>&nbsp;<span class="required">*</span>&nbsp;&nbsp;<input type="text" id="subscriptionUserProfile" />
                    </p>
<%
    } else {
%>
                    <p style="padding:0 0 5px 0"><fmt:message key="enter.username.me"/></p>
                    <p>
                        <fmt:message key="enter.username.prompt"/>:&nbsp;&nbsp;<%=username%>
                        <input type="hidden" id="subscriptionUserProfile" value="<%=username%>" /> 
                    </p>
<%
    }
%>
                    <p style="padding:5px 0 5px 0"><fmt:message key="select.digest.delivery"/></p>
                    <p>
                        <fmt:message key="digest.delivery"/>&nbsp;<select
                            id="digestDeliveryUser" disabled="disabled">
                            <option value="0"><fmt:message key="digest.none"/></option>
                            <option value="1"><fmt:message key="digest.hourly"/></option>
                            <option value="2"><fmt:message key="digest.daily"/></option>
                            <option value="3"><fmt:message key="digest.weekly"/></option>
                            <option value="4"><fmt:message key="digest.fortnightly"/></option>
                            <option value="5"><fmt:message key="digest.monthly"/></option>
                        </select>
                    </p>
                </div>
                <div id="subscriptionDataRoleProfile" style="display:none">
<%
    if (canSubscribeOtherRoles) {
%>
                    <p style="padding:0 0 5px 0"><fmt:message key="enter.role"/></p>
                    <p>
                        <fmt:message key="enter.role.prompt"/>&nbsp;<span class="required">*</span>&nbsp;&nbsp;<input type="text" id="subscriptionRoleProfile" />
                    </p>
<%
    } else {
%>
                    <p style="padding:0 0 5px 0"><fmt:message key="enter.role.me"/></p>
                    <p>
                        <fmt:message key="enter.role.prompt"/>&nbsp;<span class="required">*</span>&nbsp;&nbsp;<select id="subscriptionRoleProfile">
                            <%
                                for (String role: roles) {
                            %>
                            <option value="<%=role%>"><%=role%></option>
                            <%
                                }
                            %>
                        </select>
                    </p>
<%
    }
%>
                    <p style="padding:5px 0 5px 0"><fmt:message key="select.digest.delivery"/></p>
                    <p>
                        <fmt:message key="digest.delivery"/>&nbsp;<select
                            id="digestDeliveryRole" disabled="disabled">
                            <option value="0"><fmt:message key="digest.none"/></option>
                            <option value="1"><fmt:message key="digest.hourly"/></option>
                            <option value="2"><fmt:message key="digest.daily"/></option>
                            <option value="3"><fmt:message key="digest.weekly"/></option>
                            <option value="4"><fmt:message key="digest.fortnightly"/></option>
                            <option value="5"><fmt:message key="digest.monthly"/></option>
                        </select>
                    </p>
                </div>
                <div id="subscriptionDataWorkList" style="display:none">
                    <%
                        if (canSubscribeOtherRoles) {
                    %>
                    <p style="padding:0 0 5px 0"><fmt:message key="enter.role"/></p>
                    <p>
                        <fmt:message key="enter.role.prompt"/>&nbsp;<span class="required">*</span>&nbsp;&nbsp;<input type="text" id="subscriptionWorkList" />
                    </p>
                    <%
                    } else {
                    %>
                    <p style="padding:0 0 5px 0"><fmt:message key="enter.role.me.work"/></p>
                    <p>
                        <fmt:message key="enter.role.prompt"/>&nbsp;<span class="required">*</span>&nbsp;&nbsp;<select id="subscriptionWorkList">
                        <%
                            for (String role: roles) {
                        %>
                        <option value="<%=role%>"><%=role%></option>
                        <%
                            }
                        %>
                    </select>
                    </p>
                    <%
                        }
                    %>
                </div>
                <div id="subscriptionDataJMX" style="display:none"/>
            </td>
        </tr>
        <%
          if(isCollection){
        %>
        <div id="hierarchicalSubscriptionInfo" style="display:1">
        <tr>
            <td valign="middle" style="width:30px;text-align:left"><fmt:message key="hierarchical.subcription"/>&nbsp;</td>
            <td valign="top" style="width:70px;text-align:left;">
                <select id="hierarchicalSubscriptionList">
                    <option value="none"><fmt:message key="collection.only"/></option>
                    <option value="*"><fmt:message key="immediate.child"/></option>
                    <option value="#"><fmt:message key="all.child"/></option>
                </select>
            </td>
        </tr>
        </div>
        <%
            }
        %>
        <tr>
            <td colspan="2">
                <input type="button" id="subscribeButton" class="button" value="<fmt:message key="subscribe"/>"
                        onclick="subscribe('<%=request.getParameter("path")%>');" disabled="disabled"/>&nbsp;
                <input type="button"
                        class="button"
                        value="<fmt:message key="cancel"/>"
                        onclick="showHideCommon('add-subscription-div');"/>
            </td>
        </tr>
    </table>
    <div style="font-style:italic;margin-top:5px;">
        <img src="../info/images/help-small.jpg" style="margin-right:5px;"/>
            <fmt:message key="select.event.type.and.notification.method"/>
    </div>
</div>

<%
    if (subscriptions.length == 0 || subscriptions[0] == null) {
%>
<div id="subscriptionsSummary" class="summeryStyle"><fmt:message key="no.subscriptions"/></div>
<%
    } else {
        if (subscriptions.length == 1) {
%>
<div id="subscriptionsSummary" class="summeryStyle"><fmt:message key="one.subscription"/></div>
<%
        } else {
%>
<div id="subscriptionsSummary" class="summeryStyle"><fmt:message key="no.of.subscriptions"><fmt:param><%=Integer.parseInt(session.getAttribute("row_count").toString())%></fmt:param></fmt:message></div>
<%
        }
%>
<div id="subscriptionsList">
    <table cellpadding="0" cellspacing=0 border="0" width="100%" class="styledLeft" id="subscriptionsTable">
        <tr>
            <td valign="top" style="text-align:left;width:65%;"><fmt:message key="subscriptions"/></td>
            <td valign="top" style="text-align:left;width:35%;"><fmt:message key="actions"/></td>
        </tr>
<%
    if (subscriptions != null && subscriptions.length > 0) {

        int itemsPerPage = (int) (RegistryConstants.ITEMS_PER_PAGE * 1.5);
        int pageNumber;
        if (requestedPage != null && requestedPage.length() > 0) {
            pageNumber = new Integer(requestedPage);
        } else {
            pageNumber = 1;
        }

        int rowCount = Integer.parseInt(session.getAttribute("row_count").toString());
        int numberOfPages;
        if (rowCount % itemsPerPage == 0) {
            numberOfPages = rowCount / itemsPerPage;
        } else {
            numberOfPages = rowCount / itemsPerPage + 1;
        }

        for (SubscriptionInstance subscription : subscriptions) {
            if (subscription == null) {
                continue;
            }
            String address = null;
            String notificationMethod = subscription.getNotificationMethod();
            boolean isDigest = subscription.getDigestType() != null &&
                    !subscription.getDigestType().equals("");
            if (notificationMethod.equals("email") || notificationMethod.equals("username") ||
                    notificationMethod.equals("role")) {
                address = subscription.getAddress().substring(7);
                if (isDigest) {
                    address = address.substring(11);
                }
            } else if (!notificationMethod.equals("jmx") && !notificationMethod.equals("work")) {
                address = subscription.getAddress();
                if(notificationMethod.equals("html.plain.text")) {
                    notificationMethod = "rest";
                } else if(notificationMethod.equals("soap")) {
                    notificationMethod = "soap";
                }
            }
            String notificationMethodPrompt = "enter." + notificationMethod + ".prompt";
            String eventName = subscription.getEventName();
            String eventId = null;
            for (int i = 0; i < events.length; i++) {
                if (eventName.equals(resourceEventNames[i]) || eventName.equals(collectionEventNames[i])) {
                    eventId = events[i];
                    break;
                }
            }
            if (eventId != null) {
%>
        <tr>
            <td valign="top" style="text-align:left;width:65%;padding:0 8px 0 2px;">
                <fmt:message key="subscription">
                    <fmt:param>
                        <% if (eventId.startsWith("custom:")) { %><%=eventId.substring("custom:".length())%>
                        <%} else {%><fmt:message key="<%=eventId%>"/><%}%>
                    </fmt:param>
                    <fmt:param><abbr <% if (address != null) { %> title='<fmt:message key="<%=notificationMethodPrompt%>"/>: <%=address%>' <% } %>><fmt:message key="<%=notificationMethod%>"/></abbr></fmt:param>
                </fmt:message>
            </td>
<%
                if (canUnsubscribe) {
%>
            <td valign="top" style="text-align:center;width:35%">
                <a class="registryWriteOperation" href="javascript:void(0)" onclick="unsubscribe('<%=request.getParameter("path")%>','<%=subscription.getId()%>');"><img class="registryWriteOperation" src="../admin/images/delete.gif" /></a>&nbsp;
                <a class="registryWriteOperation" href="javascript:void(0)" onclick="unsubscribe('<%=request.getParameter("path")%>','<%=subscription.getId()%>');"><fmt:message key="delete"/></a>
            </td>
<%
                } else {
%>
            <td valign="top" style="text-align:right;width:35%"></td>
<%
                }
%>
        </tr>
<%
            }
        }
 %>
        <carbon:resourcePaginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                  resourceBundle="org.wso2.carbon.registry.info.ui.i18n.Resources"
                                  nextKey="next" prevKey="prev"
                                  paginationFunction="<%="loadSubscriptionDiv('" + request.getParameter("path").replaceAll("&","%26") + "', {0})"%>" />
    </table>
</div>

<%
    }
%>

    <script type="text/javascript">
        alternateTableRows('subscriptionsTable', 'tableEvenRow', 'tableOddRow');
    </script>
<%
    }
%>
    </fmt:bundle>
<%
    }
%>