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

<%@ page import="org.wso2.carbon.registry.common.utils.RegistryUtil" %>
<%@ page import="org.wso2.carbon.registry.common.ui.UIConstants" %>
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.registry.search.ui.i18n.JSResources"
        request="<%=request%>" namespace="org.wso2.carbon.registry.search.ui"/>
<script type="text/javascript" src="../search/js/search.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>

<link rel="stylesheet" type="text/css"
      href="../resources/css/registry.css"/>
<%
    /*String searchType = (String) request.getSession().getAttribute(UIConstants.SEARCH_TYPE);
    if (searchType == null) searchType = "content";*/
%>

<fmt:bundle basename="org.wso2.carbon.registry.search.ui.i18n.Resources">
    <div class="box1-head" style="height:auto;">
        <table cellspacing="0" cellpadding="0" border="0" style="width:100%">
        <tr>


            <td valign="top"><h2 class="sub-headding-search"><fmt:message key="search"/></h2></td>


            <td align="right" valign="top" class="expanIconCell">


                <a onclick="showSimpleSearch();">
                    <img src="images/icon-expanded.gif" border="0" align="top"
                         id="searchIconExpanded" />
                    <img src="images/icon-minimized.gif" border="0" align="top"
                         id="searchIconMinimized" style="display:none;"/>
                </a>

            </td>

        </tr>
    </table>
    </div>
    <div class="box1-mid-fill" id="searchMinimized" style="display:none"></div>
    <div class="box1-mid" id="searchExpanded">
        <form action="../search/search.jsp" method="get" style="display:inline;" name="searchForm" onsubmit="return validateSimpleSearch()">
            <table cellspacing="0" cellpadding="0" border="0" style="width: 100%;"
                   class="styledLeft">
                <tbody>
                <tr>
                    <td>
                        <input type="hidden" name="region" id="region" value="region3"/>
                        <input type="hidden" name="item" id="item" value="registry_search_menu"/>
                        <input type="text" name="criteria" id="criteria" class="input-text" />
                        <select name="searchType" id="searchType" style="display:none">
                            <option value="Content">
                                <fmt:message key="content"/>
                            </option>
                            <option value="Tag" selected="selected">
                                <fmt:message key="tag"/>
                            </option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <input type="submit" class="button"
                               value="<fmt:message key="search"/>"
                               onclick="javascript:return validateSimpleSearch() ;"/>
                        <input type="button" class="button"
                               value="<fmt:message key="clear"/>"
                               onclick="$('criteria').value = '';"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </form>
    </div>
</fmt:bundle>