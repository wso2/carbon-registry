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
<%
    String parentPath = request.getParameter("parentPath");
%>
<br/>
<fmt:bundle basename="org.wso2.carbon.registry.profiles.ui.i18n.Resources">
<%-- Read more on Ajax.Updater, evalScripts (used by resource_util.js, when adding custom content)
     and defining functions at http://www.prototypejs.org/api/ajax/updater --%>
<script type="text/javascript">
    submitAddProfile = function (parentPath) {
        var rForm = document.forms["customUIForm"];
        /* Validate the form before submit */

        var reason = "";
        reason += validateEmpty(rForm.addProfileUser,
                '<fmt:message key="username"/>');
        if (reason != "") {
            CARBON.showWarningDialog(reason);
            return false;
        }
        sessionAwareFunction(function() {
            var username = document.getElementById('addProfileUser').value;
            window.location = "../userprofiles/profiles_handler_ajaxprocessor.jsp?username=" +
                              username + "&parentPath=" + parentPath;
            /*new Ajax.Request('../userprofiles/profiles_handler_ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {username: username, parentPath: parentPath}
            });*/
        }, '<fmt:message key="session.timed.out"/>');
        return true;
    };
</script>
<form id="customUIForm" action="/wso2registry/system/addProfile" method="post">
    <table cellspacing="0" cellpadding="0" border="0" style="width:100%" class="styledLeft">
        <thead>
        <tr>
            <th colspan="2"><fmt:message key="input.username.to.add"/></th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td><fmt:message key="username"/> <span class="required">*</span></td>
            <td><input type="text" id="addProfileUser" name="addProfileUser" style="width:100%"/></td>
        </tr>
        <tr>
            <td class="buttonRow" colspan="2">
                <input type="button" onclick="submitAddProfile('<%=parentPath%>');" class="button registryWriteOperation" value="<fmt:message key="submit"/>" />
                <input type="button" disabled="disabled" class="button registryNonWriteOperation" value="<fmt:message key="submit"/>" />
            </td>
        </tr>
        </tbody>
    </table>
</form>
    </fmt:bundle>
<br/>
