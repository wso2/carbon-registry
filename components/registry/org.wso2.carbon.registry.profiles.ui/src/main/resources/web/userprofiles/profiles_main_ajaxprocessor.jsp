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
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.registry.profiles.ui.clients.ProfilesAdminServiceClient" %>
<%@ page import="org.wso2.carbon.registry.profiles.stub.beans.xsd.ProfilesBean" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.wso2.carbon.user.core.UserCoreConstants" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.profiles.ui.utils.GetProfileUtil" %>

<%
    String path =request.getParameter("path");
    Map<String,String> defaultProfile = new HashMap();
    Map<String, Map<String,String>> data = null;
    try {
        data = GetProfileUtil.getProfile(path,config,session);
        if (data != null) {
            defaultProfile = data.get(UserCoreConstants.DEFAULT_PROFILE);
        }
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
        %>
<fmt:bundle basename="org.wso2.carbon.registry.profiles.ui.i18n.Resources">
<script type="text/javascript">
    CARBON.showErrorDialog('<fmt:message key="unable.to.display.profile"/>');
</script>
</fmt:bundle>
        <%
        return;
    }
%>
<br/>

<h3>Profiles Information</h3>
<table width="50%">
 <!--   <form name="profile_names" action="profiles_select_view.jsp" method="get">
        <tr>
            <select name="profile_menu">

                <option selected>Choose One</option>
                <%
//                    String [] profiles = bean.getProfileNames();
//                    request.setAttribute("profilenames",profiles);
//                    for(String eachprofile:profiles){

                %>
                <%--<option value=<%=eachprofile%>><%=eachprofile%></option>--%>
                <%
//                    }

                %>

            </select>
        </tr>
        <input type="button" onClick="location=document.jump.menu.options[document.jump.menu.selectedIndex].value;" value="View Profile">
        </p>
    </form> -->

            <tr>
                <th>Display Name</th>
                <th>Value</th>
            </tr>
                <%
        Iterator it = defaultProfile.entrySet().iterator();
          while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                String displayName = (String)pairs.getKey();
                String value = (String)pairs.getValue();
             %>
            <tr>
                <td><%=displayName%></td>
                <td><%=value%></td>
            </tr>

                <%
         }
         %>

</table>
    <a onclick="loadViewUI('../userprofiles/profiles_edit_ajaxprocessor.jsp', '<%=path%>')">Edit Profile</a>
</br>
