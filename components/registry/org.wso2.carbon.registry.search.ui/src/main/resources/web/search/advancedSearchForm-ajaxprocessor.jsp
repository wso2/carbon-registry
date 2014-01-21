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
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.search.ui.clients.SearchServiceClient" %>
<%@ page import="org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean" %>
<%@ page import="org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString" %>
<%@ page import="org.wso2.carbon.utils.CarbonUtils" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.registry.search.ui.i18n.JSResources"
        request="<%=request%>" namespace="org.wso2.carbon.registry.search.ui"/>
<script type="text/javascript" src="../search/js/search.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<link rel="stylesheet" type="text/css"
      href="../resources/css/registry.css"/>
<style type="text/css">
    table tbody tr td {
        border: 0 none;
        padding: 0;
        text-align: left;
        vertical-align: top;
    }

</style>

<fmt:bundle basename="org.wso2.carbon.registry.search.ui.i18n.Resources">

<%
    String createdAfter = null, createdBefore = null, updatedAfter = null, updatedBefore = null,
            resourcePath = null, author = null, updater = null, tags = null, content = null, commentWords = null,
            propertyName = null, rightPropertyValue = null, leftPropertyValue = null, mediaType = null, associationType = null, associationDest = null,
            createdRangeNegate = null, updatedRangeNegate = null, authorNameNegate = null, updaterNameNegate = null,
            rightOp = null, leftOp = null, mediaTypeNegate = null, lblPropName = null;

    String filterName = request.getParameter("filterName");
    boolean hasParameters = false;
    StringBuilder sb = new StringBuilder();

    if (filterName != null) {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        try {
            SearchServiceClient client = new SearchServiceClient(cookie, config, session);
            CustomSearchParameterBean bean = client.getAdvancedSearchFilter(filterName);
            if (bean.getParameterValues() != null) {
                ArrayOfString[] arrayOfStrings = bean.getParameterValues();
                for (ArrayOfString arrayOfString : arrayOfStrings) {
                    String[] array = arrayOfString.getArray();
                    if (array != null && array.length == 2) {
                        sb.append(array[0]).append("=").append(array[1]).append("&");
                        if ("createdAfter".equals(array[0])) {
                            createdAfter = array[1];
                            if(createdAfter==null){
                                createdAfter = "";
                            }
                        } else if ("createdBefore".equals(array[0])) {
                            createdBefore = array[1];
                            if(createdBefore==null){
                                createdBefore = "";
                            }
                        } else if ("updatedAfter".equals(array[0])) {
                            updatedAfter = array[1];
                            if(updatedAfter==null){
                                updatedAfter = "";
                            }
                        } else if ("updatedBefore".equals(array[0])) {
                            updatedBefore = array[1];
                            if(updatedBefore==null){
                                updatedBefore = "";
                            }
                        } else if ("resourcePath".equals(array[0])) {
                            resourcePath = array[1];
                            if(resourcePath==null){
                                resourcePath = "";
                            }
                        } else if ("author".equals(array[0])) {
                            author = array[1];
                            if(author==null){
                                author = "";
                            }
                        } else if ("updater".equals(array[0])) {
                            updater = array[1];
                            if(updater==null){
                                updater = "";
                            }
                        } else if ("tags".equals(array[0])) {
                            tags = array[1];
                            if(tags==null){
                                tags = "";
                            }
                        } else if ("content".equals(array[0])) {
                            content = array[1];
                            if(content==null){
                                content = "";
                            }
                        } else if ("commentWords".equals(array[0])) {
                            commentWords = array[1];
                            if(commentWords==null){
                                commentWords = "";
                            }
                        } else if ("associationType".equals(array[0])) {
                            associationType = array[1];
                            if(associationType==null){
                                associationType = "";
                            }
                        } else if ("associationDest".equals(array[0])) {
                            associationDest = array[1];
                            if(associationDest==null){
                                associationDest = "";
                            }
                        } else if ("propertyName".equals(array[0])) {
                            propertyName = array[1];
                            lblPropName = array[1];
                            if(propertyName==null){
                                propertyName = "";
                                lblPropName = "-";
                            }
                        } else if ("leftPropertyValue".equals(array[0])) {
                            leftPropertyValue = array[1];
                            if(leftPropertyValue==null){
                                leftPropertyValue = "";
                            }
                        } else if ("rightPropertyValue".equals(array[0])) {
                            rightPropertyValue = array[1];
                            if(rightPropertyValue==null){
                                rightPropertyValue = "";
                            }
                        } else if ("mediaType".equals(array[0])) {
                            mediaType = array[1];
                            if(mediaType==null){
                                mediaType = "";
                            }
                        } else if ("createdRangeNegate".equals(array[0])) {
                        	createdRangeNegate = array[1];
                            if(createdRangeNegate==null){
                                createdRangeNegate = "";
                            }
                        } else if ("updatedRangeNegate".equals(array[0])) {
                        	updatedRangeNegate = array[1];
                            if(updatedRangeNegate==null){
                                updatedRangeNegate = "";
                            }
                        } else if ("authorNameNegate".equals(array[0])) {
                        	authorNameNegate = array[1];
                            if(authorNameNegate==null){
                                authorNameNegate = "";
                            }
                        } else if ("updaterNameNegate".equals(array[0])) {
                        	updaterNameNegate = array[1];
                            if(updaterNameNegate==null){
                                updaterNameNegate = "";
                            }
                        } else if ("rightOp".equals(array[0])) {
                            rightOp = array[1];
                            if(rightOp == null) {
                                rightOp = "";
                            }
                        } else if ("leftOp".equals(array[0])) {
                            leftOp = array[1];
                        	if (leftOp == null) {
                                leftOp = "";
                            }
                        } else if ("mediaTypeNegate".equals(array[0])) {
                        	mediaTypeNegate = array[1];
                            if(mediaTypeNegate==null){
                                mediaTypeNegate = "";
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            response.setStatus(500);
%>
<script type="text/javascript">
    CARBON.showErrorDialog("<%=e.getMessage()%>");
</script>
<%
            return;
        }
    } else {

        createdAfter = request.getParameter("createdAfter");
        if (createdAfter == null) {
            createdAfter = "";
        } else {
            hasParameters = true;
        }
        createdBefore = request.getParameter("createdBefore");
        if (createdBefore == null) {
            createdBefore = "";
        } else {
            hasParameters = true;
        }
        updatedAfter = request.getParameter("updatedAfter");
        if (updatedAfter == null) {
            updatedAfter = "";
        } else {
            hasParameters = true;
        }
        updatedBefore = request.getParameter("updatedBefore");
        if (updatedBefore == null) {
            updatedBefore = "";
        } else {
            hasParameters = true;
        }
        resourcePath = request.getParameter("resourcePath");
        if (resourcePath == null) {
            resourcePath = "";
        } else {
            hasParameters = true;
        }
        author = request.getParameter("author");
        if (author == null) {
            author = "";
        } else {
            hasParameters = true;
        }
        updater = request.getParameter("updater");
        if (updater == null) {
            updater = "";
        } else {
            hasParameters = true;
        }
        tags = request.getParameter("tags");
        if (tags == null) {
            tags = "";
        } else {
            hasParameters = true;
        }
        content = request.getParameter("content");
        if (content == null) {
            content = "";
        } else {
        hasParameters = true;
        }
        commentWords = request.getParameter("commentWords");
        if (commentWords == null) {
            commentWords = "";
        } else {
            hasParameters = true;
        }
        associationType = request.getParameter("associationType");
        if (associationType == null) {
            associationType = "";
        } else {
            hasParameters = true;
        }
        associationDest = request.getParameter("associationDest");
        if (associationDest == null) {
            associationDest = "";
        } else {
            hasParameters = true;
        }
        propertyName = request.getParameter("propertyName");
        if (propertyName == null) {
            propertyName = "";
            lblPropName = "-";
        } else {
            hasParameters = true;
            lblPropName = propertyName;
        }
        leftPropertyValue = request.getParameter("leftPropertyValue");
        rightPropertyValue = request.getParameter("rightPropertyValue");
        if (leftPropertyValue == null) {
            leftPropertyValue = "";
        }
        if (rightPropertyValue == null) {
            rightPropertyValue = "";
        }
        
        authorNameNegate = request.getParameter("authorNameNegate"); 
        updaterNameNegate = request.getParameter("updaterNameNegate"); 
        
        if(authorNameNegate == null)
        	authorNameNegate = "";
        
        if(updaterNameNegate == null)
        	updaterNameNegate = "";
        
        mediaType = request.getParameter("mediaType");        
        if (mediaType == null) {
            mediaType = "";
        } else {
            hasParameters = true;
        }
        
        mediaTypeNegate = request.getParameter("mediaTypeNegate");
        if(mediaTypeNegate == null)
        	mediaTypeNegate = "";
        
        leftOp = request.getParameter("leftOp");
        rightOp = request.getParameter("rightOp");
        if(rightOp == null) rightOp = "";
        if(leftOp == null) leftOp = "";
    }
%>

<form id="advancedSearchForm" name="advancedSearch" onsubmit="return submitAdvSearchForm()"
      action="advancedSearch-ajaxprocessor.jsp" method="get"
        <%=hasParameters ? "style=\"display:none\"" : ""%>>
         <input type="hidden" value="<%=leftOp%>" id="hiddenOpLeft"/>
         <input type="hidden" value="<%=rightOp%>" id="hiddenOpRight"/>

            <div class="search-subtitle" style="padding-left:10px;"><fmt:message key="search.for.resources"/></div>
            <table class="normal" id="customTable" style="width:100%;">
                <tr>
                    <td class="leftCol-small"><fmt:message key="resource.name"/></td>
                    <td>
                        <input type="text" name="resourcePath" value="<%=resourcePath%>" id="#_resourceName"
                               onkeypress="handletextBoxKeyPress(event)"/>
                    </td>
                </tr>
                <% for(String serverRole : CarbonUtils.getServerConfiguration().getProperties("ServerRoles.Role")){
                    if(serverRole.equalsIgnoreCase("GovernanceRegistry")){%>
                        <tr>
                            <td class="leftCol-small"><fmt:message key="content.name"/></td>
                            <td>
                                <input type="text" name="content" value="<%=content%>" id="#_content"
                                       onkeypress="handletextBoxKeyPress(event)"/>
                            </td>
                        </tr>
                    <%}%>
                <%}%>				
                <tr>
                    <td valign="top"><fmt:message key="created"/></td>
                    <td>
                        <table cellpadding="0" cellspacing="0" border="0">
                            <tr>
                                <td>
                                    <fmt:message key="from"/> :
                                </td>
                                <td>
                                    <a class="icon-link" style="background-image: url( ../admin/images/calendar.gif);" onclick="jQuery('#cfromDate').datepicker( 'show' );" href="javascript:void(0)"></a>

                                    <input type="text" name="createdAfter" value="<%=createdAfter%>"
                                           id="cfromDate"
                                           style="width:140px;" onkeypress="handletextBoxKeyPress(event)"/>
                                </td>
                                <td>
                                    <fmt:message key="to"/> :
                                </td>
                                <td>
                                    <a class="icon-link" style="background-image: url( ../admin/images/calendar.gif);" onclick="jQuery('#ctoDate').datepicker( 'show' );" href="javascript:void(0)"></a>
                                    <input type="text" name="createdBefore" value="<%=createdBefore%>"
                                           id="ctoDate"
                                           style="width:140px;" onkeypress="handletextBoxKeyPress(event)"/>

                                    <% if (filterName != null && !createdRangeNegate.equals("")) { %>
                                        <input type="checkbox" name="createdRangeNegate" checked="checked" /> not
                                    <% } else {%>
                                        <input type="checkbox" name="createdRangeNegate" /> not
                                    <% } %>
                                </td>
                            </tr>
                            <tr>
                                <td></td>
                                <td class="helpTextTop" style="margin:0px;padding:0px;">
                                    <fmt:message key="mm.dd.yyyy"/>
                                </td>
                                <td></td>
                                <td class="helpTextTop" style="margin:0px;padding:0px;">
                                    <fmt:message key="mm.dd.yyyy"/>
                                </td>
                            </tr>
                        </table>
                    </td>

                </tr>
                <tr>
                    <td valign="top"><fmt:message key="updated"/></td>
                    <td>
                        <table cellpadding="0" cellspacing="0" border="0">
                            <tr>
                                <td>
                                    <fmt:message key="from"/> :
                                </td>
                                <td>
                                     <a class="icon-link" style="background-image: url( ../admin/images/calendar.gif);" onclick="jQuery('#ufromDate').datepicker( 'show' );" href="javascript:void(0)"></a>
                                    <input type="text" name="updatedAfter" value="<%=updatedAfter%>"
                                           id="ufromDate"
                                           style="width:140px;" onkeypress="handletextBoxKeyPress(event)"/>                                         
                                </td>
                                <td>
                                    <fmt:message key="to"/> :
                                </td>
                                <td>
                                    <a class="icon-link" style="background-image: url( ../admin/images/calendar.gif);" onclick="jQuery('#utoDate').datepicker( 'show' );" href="javascript:void(0)"></a>
                                    <input type="text" name="updatedBefore" value="<%=updatedBefore%>"
                                           id="utoDate"
                                           style="width:140px;" onkeypress="handletextBoxKeyPress(event)"/>

                                    <% if (filterName != null && !updatedRangeNegate.equals("")) { %>
                                        <input type="checkbox" name="updatedRangeNegate" checked="checked" /> not
                                    <% } else { %>
                                        <input type="checkbox" name="updatedRangeNegate" /> not
                                    <% } %>
                                </td>
                            </tr>
                            <tr>
                                <td></td>
                                <td class="helpTextTop" style="margin:0px;padding:0px;">
                                    <fmt:message key="mm.dd.yyyy"/>
                                </td>
                                <td></td>
                                <td class="helpTextTop" style="margin:0px;padding:0px;">
                                    <fmt:message key="mm.dd.yyyy"/>
                                </td>
                            </tr>
                        </table>
                    </td>

                </tr>
                <tr>
                    <td><fmt:message key="created.by"/></td>
                    <td>
                        <input type="text" name="author" value="<%=author%>" id="#_author" onkeypress="handletextBoxKeyPress(event)"/>

                        <% if (filterName != null && !authorNameNegate.equals("")) { %>
                            <input type="checkbox" name="authorNameNegate" checked="checked" /> not
                        <% } else { %>
                            <input type="checkbox" name="authorNameNegate" /> not
                        <% } %>

                    </td>

                </tr>
                <tr>
                    <td><fmt:message key="updated.by"/></td>
                    <td>
                        <input type="text" name="updater" value="<%=updater%>" id="#_updater" onkeypress="handletextBoxKeyPress(event)"/>

                        <% if (filterName != null && !updaterNameNegate.equals("")) { %>
                            <input type="checkbox" name="updaterNameNegate" checked="checked" /> not
                        <% } else { %>
                            <input type="checkbox" name="updaterNameNegate" /> not
                        <% } %>
                    </td>

                </tr>
                <tr>
                    <td><fmt:message key="tags"/></td>
                    <td>
                        <input type="text" name="tags" value="<%=tags%>" id="#_tags" onkeypress="handletextBoxKeyPress(event)"/>
                    </td>
                </tr>
                <tr>

                    <td><fmt:message key="comments"/></td>
                    <td>
                        <input type="text" name="commentWords" value="<%=commentWords%>" id="#_comments" onkeypress="handletextBoxKeyPress(event)"/>
                    </td>
                </tr>
                <tr>

                    <td><fmt:message key="associationType"/></td>
                    <td>
                        <input type="text" name="associationType" value="<%=associationType%>" id="#_associationType" onkeypress="handletextBoxKeyPress(event)"/>
                    </td>
                </tr>
                <tr>

                    <td><fmt:message key="associationDest"/></td>
                    <td>
                        <input type="text" name="associationDest" value="<%=associationDest%>" id="#_associationDest" onkeypress="handletextBoxKeyPress(event)"/>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="property.name"/></td>
                    <td>
                        <input type="text" name="propertyName" value="<%=propertyName%>"
                               id="#_propertyName" onkeypress="handletextBoxKeyPress(event)" onblur="setPropertyName()"/>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="property.value"/></td>
                    <td>
                       <%-- <input type="text" name="propertyValue" value="<%=propertyValue%>"
                               id="#_propertyValue" onkeypress="handletextBoxKeyPress(event)"/> --%>
						<input type="text" onkeypress="return isNumberKey(event)" id="valueLeft" name="leftPropertyValue" value="<%=leftPropertyValue%>"/>

						<select id="opLeft" name="leftOp" >
						    <option value="na"></option>
                          	<option value="gt">&#60;</option>
                          	<option value="ge">&#8804;</option>
                        </select>

                        <label>&#123;</label>
                        <label id="lblPropName"><%=lblPropName%></label>
                        <label>&#125;</label>

						<select onChange="adjustAllOpInput();" id="opRight" name="rightOp">
						    <option value="na"></option>
  							<option value="lt">&#60;</option>
  							<option value="le">&#8804;</option>
  							<option value="eq">&#61;</option>
						</select>
						<input type="text" onkeypress="return isNumberKey(event)" id="valueRight" name="rightPropertyValue" value="<%=rightPropertyValue%>"/>

                        <% if (filterName == null) { %>

                        <script type="text/javascript">
                            setIndexForOp("opLeft", "<%=leftOp%>");
                            setIndexForOp("opRight", "<%=rightOp%>");
                            adjustPropertyInput();
                        </script>
                        <% } %>
                    </td>
                </tr>
               <%-- <tr>
                    <td><fmt:message key="including.content"/></td>
                    <td>
                        <input type="text" name="content" value="" id="#_content" onkeypress="handletextBoxKeyPress(event)"/>
                    </td>
                </tr>--%>

                <%
                    String isFilter = "0";
                    String filterMediaType = "";
                    if (request.getParameter("filterName") != null && mediaType != null && mediaType.length() != 0) {
                        isFilter = "1";
                        filterMediaType = mediaType;
                    }
                %>

                <tr>
                    <td style="vertical-align:center !important"><fmt:message key="media.type"/></td>
                    <td style="vertical-align:top !important">
                        <div class="yui-skin-sam" style="width:200px;margin-left:0px;float:left">


                            <div id="mediaTypeAutoComplete">
                                <input id="#_mediaType" name="mediaType" type="text" value="<%=mediaType%>" style="width:auto" onblur='adjustMediaTypeNegate("<%=isFilter%>", "<%=filterMediaType%>")' />


                                                                                                                         
                                <div id="customUIButtonDiv">
                                    <%
                                        if (isFilter.equals("1")) {
                                    %><a href="javascript:collapseCustomUI()" class="loadMediaTypeButton"><fmt:message key="fewer"/> <img src="../search/images/arrow-up.png" /></a><%
                                        }
                                    %>
                                </div>
                                <div id="mediaTypeContainer"></div>

                            </div>

                        </div>

                        <div id="mediaTypeNegateDiv" style="float:left;margin-left:20px" >
                            <script type="text/javascript">

                                   var mediaTypeNegateObj = document.getElementById('mediaTypeNegateDiv');

                                   if(isCustomUIDivEmpty()) {
                                          mediaTypeNegateObj.style.marginLeft = "160px";
                                   }
                            </script>

                            <% if (filterName != null && !mediaTypeNegate.equals("")) { %>
                                <input name="mediaTypeNegate" id="mediaTypeNegate" type="checkbox" checked="checked" /> not
                            <% } else { %>
                                <input name="mediaTypeNegate" id="mediaTypeNegate" type="checkbox" /> not
                            <% } %>
                        </div>
					</td>

                        <%--<input type="text" name="mediaType" value="<%= mediaType%>" id="#_mediaType" onkeypress="handleMediaTypeKeypress(event)"></td>
                        --%>
                </tr>
                <tr>
                    <td colspan="2" style="padding:0px !important;margin:0px !important" id="customDiv">
                        <%
                            if (request.getParameter("filterName") != null && mediaType != null &&
                                    mediaType.length() > 0) {
                                String includePath = "customSearch-ajaxprocessor.jsp?mediaType=" + mediaType +
                                                     "&" + sb.toString().trim();
                        %>
                                <jsp:include page="<%=includePath%>"/>
                        <%
                            }
                        %>
                    </td>
                </tr>

                <tr>
                    <td class="buttonRow" colspan="2">
                        <input type="button" id="#_0" value="<fmt:message key="search"/>" class="button"
                               onclick="submitAdvSearchForm()"/>
                         <input type="button" id="#_1" value="<fmt:message key="clear"/>" class="button"
                               onclick="clearAll()"/>
                    </td>
                </tr>
            </table>



<div id="commentsForm" style="display:none;">
    <table cellpadding="0" cellspacing=10 border="0" style="width:100%" class="form-table">
        <tr>
            <td class="leftCol-small"><fmt:message key="commented.user"/></td>
            <td>
                <input type="text" name="commentedUser" value="" id="#_commentedUser"
                       style="width:100px;"/>
            </td>
        </tr>
        <tr>
            <td valign="top"><fmt:message key="date"/></td>
            <td>
                <div style="width:200px;font-style:italic;"><fmt:message
                        key="only.find.results.updated"/></div>
                <fmt:message key="from"/>: <input type="text" name="fromDate" value=""
                                                  id="#_fromDate"
                                                  style="width:100px;"/>
                <fmt:message key="to"/>: <input type="text" name="toDate" value="" id="#_toDate"
                                                style="width:100px;"/>
            </td>
        </tr>
        <tr>
            <td valign="top"><fmt:message key="resource.name"/></td>
            <td>
                <input type="text" name="resourceName" value="" id="#_resourceName2"/>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="commented.text"/></td>
            <td>
                <input type="text" name="commentedText" value="" id="#_commentedText"/>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="created.by1"/></td>
            <td>
                <input type="text" name="createdBy" value="" id="#_createdBy"/>
            </td>
        </tr>
    </table>
</div>


</form>
</fmt:bundle>
