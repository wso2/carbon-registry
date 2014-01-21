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
    Map<String, Map<String,String>> data = null;
    Map<String,String> defaultprofile = null;
    Iterator it = null;
    String profilename = null;
    try {
        data =GetProfileUtil.getProfile(path,config,session);
        profilename = GetProfileUtil.getprofilename(data);
        defaultprofile = GetProfileUtil.getprofiledatatoshow(data,profilename);

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

<h3><%=profilename%> Profile</h3>
<table class="styledLeft" id="customTable">
   <!--<form name="profile_names" action="profiles_edit_ajaxprocessor.jsp" method="get">
       <%--<input type="hidden" name="profilename" value="<%=path%>">--%>
        <tr>
            <select name="profile_menu">

                <option selected>Choose One</option>
                <%
//                    while(it.hasNext()){
//                    String profilename = (String)it.next();

                %>
                <%--<option value=<%=profilename%>><%=profilename%></option>--%>
                <%
//                    }

                %>

            </select>
        </tr>
        <input type="submit"  value="View Profile">
        </form> -->
        <thead>
        <tr>
                <th>Display Name</th>
                <th>Value</th>
            </tr>
        </thead>
        <tbody>
                <%
            it = defaultprofile.entrySet().iterator();
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
        </tbody>
    </table>
<script type="text/javascript">
    alternateTableRows('customTable','tableEvenRow','tableOddRow');
</script>
