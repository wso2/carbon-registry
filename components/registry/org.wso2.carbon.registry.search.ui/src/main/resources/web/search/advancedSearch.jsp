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

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>

<%--Jquery autocomplete includesw--%>
<link rel="stylesheet" type="text/css" href="../yui/build/fonts/fonts-min.css" />
<link rel="stylesheet" type="text/css" href="../yui/build/autocomplete/assets/skins/sam/autocomplete.css" />
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>

<script type="text/javascript" src="../yui/build/animation/animation-min.js"></script>
<script type="text/javascript" src="../yui/build/datasource/datasource-min.js"></script>
<script type="text/javascript" src="../yui/build/autocomplete/autocomplete-min.js"></script>

<carbon:jsi18n
        resourceBundle="org.wso2.carbon.registry.search.ui.i18n.JSResources"
        request="<%=request%>" namespace="org.wso2.carbon.registry.search.ui"/>
<script type="text/javascript" src="../search/js/search.js"></script>
<link rel="stylesheet" type="text/css" href="../search/css/search.css" />

<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>

<script type="text/javascript">
    function initDatePickers() {
        jQuery("#cfromDate").datepicker();
        jQuery("#ctoDate").datepicker();
        jQuery("#ufromDate").datepicker();
        jQuery("#utoDate").datepicker();
        fillMediaTypesForSearch();
    }
    jQuery(document).ready(function(){
        initDatePickers();
        new Ajax.Updater('savedSearchFilterListDiv', '../search/getSavedSearchFilters-ajaxprocessor.jsp',{evalScripts:true});
    });
</script>
<fmt:bundle basename="org.wso2.carbon.registry.search.ui.i18n.Resources">
<carbon:breadcrumb label="search"
                   resourceBundle="org.wso2.carbon.registry.search.ui.i18n.Resources"
                   topPage="true" request="<%=request%>"/>

<%

//    String[] tempArray = new String[]{"Moderator", "Last Edited", "Date", "Organization"};
//
//    //has parameters is to identify the parameters which are already set by the previous search
//
    boolean hasParameters = false;
//    String[][] customPramValues = new String[tempArray.length][2]; // this array holds the parameter names and values.
//
//    //initialise the customPramValues  array
//
//    for (int i = 0; i < customPramValues.length; i++) {
//        customPramValues[i][0] = tempArray[i].replace(" ", "");
//
//    }

    String createdAfter = request.getParameter("createdAfter");
    if (createdAfter == null) {
        createdAfter = "";
    } else {
        hasParameters = true;
    }
    String createdBefore = request.getParameter("createdBefore");
    if (createdBefore == null) {
        createdBefore = "";
    } else {
        hasParameters = true;
    }
    String updatedAfter = request.getParameter("updatedAfter");
    if (updatedAfter == null) {
        updatedAfter = "";
    } else {
        hasParameters = true;
    }
    String updatedBefore = request.getParameter("updatedBefore");
    if (updatedBefore == null) {
        updatedBefore = "";
    } else {
        hasParameters = true;
    }
    String resourcePath = request.getParameter("resourcePath");
    if (resourcePath == null) {
        resourcePath = "";
    } else {
        hasParameters = true;
    }
    String author = request.getParameter("author");
    if (author == null) {
        author = "";
    } else {
        hasParameters = true;
    }
    String updater = request.getParameter("updater");
    if (updater == null) {
        updater = "";
    } else {
        hasParameters = true;
    }
    String tags = request.getParameter("tags");
    if (tags == null) {
        tags = "";
    } else {
        hasParameters = true;
    }
    String commentWords = request.getParameter("commentWords");
    if (commentWords == null) {
        commentWords = "";
    } else {
        hasParameters = true;
    }
    String propertyName = request.getParameter("propertyName");
    if (propertyName == null) {
        propertyName = "";
    } else {
        hasParameters = true;
    }
    String leftPropertyValue = request.getParameter("leftPropertyValue");
    String rightPropertyValue = request.getParameter("rightPropertyValue");

    if (leftPropertyValue == null) {
        leftPropertyValue = "";
    } else {
        hasParameters = true;
    }
    if (rightPropertyValue == null) {
        rightPropertyValue = "";
    }
    String associationType = request.getParameter("associationType");
    if (associationType == null) {
        associationType = "";
    } else {
        hasParameters = true;
    }
    String associationDest = request.getParameter("associationDest");
    if (associationDest == null) {
        associationDest = "";
    } else {
        hasParameters = true;
    }
    String content = request.getParameter("content");
    if (content == null) {
        content = "";
    } else {
        hasParameters = true;
    }
    String authorNameNegate = request.getParameter("authorNameNegate");
    if(authorNameNegate == null)
     	authorNameNegate = "";
    
    String updaterNameNegate = request.getParameter("updaterNameNegate");
    if(updaterNameNegate == null)
    	updaterNameNegate = "";
    
    String createdRangeNegate = request.getParameter("createdRangeNegate");
    if(createdRangeNegate == null)
    	createdRangeNegate = "";
    
    String updatedRangeNegate = request.getParameter("updatedRangeNegate");
    if(updatedRangeNegate == null)
    	updatedRangeNegate = ""; 
    
    String mediaTypeNegate = request.getParameter("mediaTypeNegate");
    if(mediaTypeNegate == null)
    	mediaTypeNegate = "";

    String mediaType = request.getParameter("mediaType");
    if(mediaType == null)
       	mediaType = "";
    
    String leftOp = request.getParameter("leftOp");
    String rightOp = request.getParameter("rightOp");
    if(rightOp == null) rightOp = "";
    if(leftOp == null) leftOp = "";
    
    
    //this was added so that the custom parameters are also checked.


//    for (int i = 0; i < tempArray.length; i++) {
//        String paramValue = request.getParameter(tempArray[i]);
//        if (paramValue == null) {
//            customPramValues[i][1] = "";
//        } else {
//            customPramValues[i][1] = paramValue;
//            hasParameters = true;
//        }
//    }


    String searchPath = "";
    String advancedFormPath = "../search/advancedSearchForm-ajaxprocessor.jsp";
    if (hasParameters) {

        searchPath = "../search/advancedSearch-ajaxprocessor.jsp?" +
                     "region=region3&item=registry_search_menu&parameterList=" +
                     "createdAfter^" + createdAfter + "|createdBefore^" + createdBefore +
                     "|updatedAfter^" + updatedAfter + "|updatedBefore^" + updatedBefore +
                     "|resourcePath^" + resourcePath + "|author^" + author + "|associationType^" + associationType +
                     "|associationDest^" + associationDest +
                     "|updater^" + updater + "|tags^" + tags +"|content^"+content+
                     "|commentWords^" + commentWords + "|propertyName^" + propertyName +
                     "|leftPropertyValue^" + leftPropertyValue + "|rightPropertyValue^" + rightPropertyValue + 
                     "|leftOp^" + leftOp + "|rightOp^" + rightOp + "|authorNameNegate^" + authorNameNegate + 
                     "|updaterNameNegate^" + updaterNameNegate + "|createdRangeNegate^" + createdRangeNegate +
                     "|updatedRangeNegate^" + updatedRangeNegate + "|mediaTypeNegate^" + mediaTypeNegate +
                     "|mediaType^" + mediaType;
        searchPath = searchPath.replace("^|", "^null|");
        if (searchPath.charAt(searchPath.length() - 1) == ':') {
            searchPath = searchPath + "null";
        }


        advancedFormPath = "../search/advancedSearchForm-ajaxprocessor.jsp?" +
                           "region=region3&item=registry_search_menu" +
                           "&createdAfter=" + createdAfter + "&createdBefore=" + createdBefore +
                           "&updatedAfter=" + updatedAfter + "&updatedBefore=" + updatedBefore +
                           "&resourcePath=" + resourcePath + "&author=" + author +
                            "&associationType=" + associationType+
                            "&associationDest=" + associationDest+
                           "&updater=" + updater + "&tags=" + tags +"&content"+content+
                           "&commentWords=" + commentWords + "&propertyName=" + propertyName +
                           "&leftPropertyValue=" + leftPropertyValue + "&rightPropertyValue=" + rightPropertyValue +
                           "&leftOp=" + leftOp + "&rightOp=" + rightOp + "&authorNameNegate=" + authorNameNegate + 
                           "&updaterNameNegate=" + updaterNameNegate + "&createdRangeNegate=" + createdRangeNegate +
                           "&updatedRangeNegate=" + updatedRangeNegate + "&mediaTypeNegate=" + mediaTypeNegate +
                           "|mediaType^" + mediaType;
    }
%>

<div id="middle">

<h2><fmt:message key="search"/></h2>

<div id="workArea">

<!-- start: load search filter -->
    <div id="loadSearchFilterArea" <%=hasParameters ? "style=\"display:none\"" : ""%>>

        <table class="styledLeft">
            <thead>
                <tr><th><fmt:message key="search.registry" /></th></tr>
            </thead>
            <tbody>
                <tr>
                    <td style="padding-left:0px !important;">
                        <div class="search-subtitle" style="padding-left:10px;padding-bottom:10px"><fmt:message key="load.search.filter"/></div>
                        <div style="padding-left:10px;color:#666666;font-style:italic;"><fmt:message key="search.help.txt"/></div>
                        <form method="get" action="">
                        <table class="normal">
                            <tr>
                                <td  class="leftCol-small"><fmt:message key="filter.name"/></td>
                                <td>
                                    <div id="savedSearchFilterListDiv">
                                        <select name="searchFilterList" id="searchFilterList">
                                            <option value=""><fmt:message key="loading"/></option>
                                        </select>
                                    </div>
                                </td>
                                <td><input type="button" onclick="loadSearchFilter()" value="<fmt:message key='load'/>"
                                           class="button" id="filterLoadButton"></td>
                                <td><input type="button" onclick="deleteSearchFilter(document.getElementById('savedSearchFilterList').options[document.getElementById('savedSearchFilterList').selectedIndex].value)" value="<fmt:message key='delete.filter'/>"
                                           class="button" id="filterDeleteButton"></td>
                            </tr>
                        </table>
                        </form>
                    </td>
                </tr>
                <tr>
                    <!-- start: advanced search form -->
                    <td id="advancedSearchFormDiv" style="padding-left:0px !important;">
                            <jsp:include page="<%= advancedFormPath%>"/>
                    </td>
                    <!-- end: advanced search form-->
                </tr>
            </tbody>
        </table>
        <%--<h2 class="sub-headding-prop"></h2>--%>

    </div>
    <!-- end: load search filter-->

<div id="advSearchReason" class="validationError"
     style="display: none;margin-bottom:20px;line-height:30px;"></div>




<!-- Search results starts here -->
<%
    if (hasParameters) {
%>
<div style="margin-bottom:30px;" id="searchResuts">
    <jsp:include page="<%=searchPath%>"/>

    <div style="margin-top:10px;margin-bottom:50px;<%=hasParameters ? "" : "display:none"%>">
        <a href="./advancedSearch.jsp?region=region3&item=registry_search_menu" class="icon-link"
           style="background-image:url(./images/search.gif);">
            <fmt:message key="try.advanced.search"/>
        </a>
    </div>
</div>
<%
} else {
%>
<div style="margin-top:30px;" id="searchResuts"></div>
<%
    }
%>



</div>

</div>
</fmt:bundle>
