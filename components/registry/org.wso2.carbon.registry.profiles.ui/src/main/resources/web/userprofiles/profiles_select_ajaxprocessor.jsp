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
<%@ page import="java.util.Set" %>

<%
    String path =request.getParameter("path");
    String selectedProfile = request.getParameter("profile_menu");
    if (selectedProfile == null) {
        selectedProfile = "default";
    }
    Map<String,String> defaultProfile = null;
    Map<String, Map<String,String>> data = null;
    Iterator it = null;
    try {
        data =GetProfileUtil.getProfile(path.substring(path.indexOf(RegistryConstants.PROFILES_PATH)),config,session);
        if (data != null) {
            defaultProfile = data.get(selectedProfile);
            Set<String> profileNames = data.keySet();
            if (defaultProfile == null) {
                if (data.isEmpty()) {
                    throw new Exception();
                }
                selectedProfile = profileNames.toArray(new String[data.size()])[0];
                defaultProfile = data.get(selectedProfile);
                if (defaultProfile == null) {
                    throw new Exception();
                }
            }
            it = profileNames.iterator();
        } else {
            throw new Exception();
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
<script type="text/javascript">
    function switchProfiles() {
        if ($('profile_menu').value != 0 && $('profile_menu').value != "<%=selectedProfile%>") {
            window.location = "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=<%=path%>&viewType=std&profile_menu=" +
                              $('profile_menu').value;
        }
    }
</script>
<fmt:bundle basename="org.wso2.carbon.registry.profiles.ui.i18n.Resources">
<h3><fmt:message key="view.profiles"/></h3>
<table width="50%">
   <form name="profile_names" action="profiles_select_ajaxprocessor.jsp" method="get">
        <tr>
            <select id="profile_menu">

                <option value="0" selected><fmt:message key="choose.one"/></option>
                <%
                    while(it.hasNext()){
                    String profilename = (String)it.next();

                %>
                <option value=<%=profilename%>><%=profilename%></option>
                <%
                    }

                %>

            </select>
        </tr>
        <input type="button" onClick="switchProfiles()" value="View Profile">
        </form>
    <form id="profilesEditForm">
        <tr>
                <th><fmt:message key="display.name"/></th>
                <th><fmt:message key="value"/></th>
            </tr>
        <tr>
                <td><fmt:message key="profile.name"/></td>
                <td><%=selectedProfile%></td>
            </tr>
                <%
            it = defaultProfile.entrySet().iterator();
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
        </form>
    </table>
</fmt:bundle>