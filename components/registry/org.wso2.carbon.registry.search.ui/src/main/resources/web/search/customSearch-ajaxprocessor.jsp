<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ page import="org.wso2.carbon.registry.common.ui.UIConstants" %>
<%@ page import="org.wso2.carbon.registry.search.ui.clients.SearchServiceClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.search.stub.beans.xsd.MediaTypeValueList" %>


<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    MediaTypeValueList mediaTypeValueList;
    String requestedPage = request.getParameter(UIConstants.REQUESTED_PAGE);
    try {
        SearchServiceClient client = new SearchServiceClient(cookie, config, session);
        if (requestedPage != null && session.getAttribute("mediaTypeValueList") != null) {
            mediaTypeValueList = (MediaTypeValueList) session.getAttribute("mediaTypeValueList");
        } else {
            mediaTypeValueList = client.getMediaTypeParameterList(request);
            session.setAttribute("mediaTypeValueList", mediaTypeValueList);
        }
    } catch (Exception ignore) {
        // Ignore exception.
        return;
    }
    String[] medeaTypeParameter = mediaTypeValueList.getSearchFields();

//String[][] customPramValues = new String[medeaTypeParameter.length][2];
%>
<table class="normal" name="customTable" style="margin-left:-2px !important;padding:0px !important;">
    <%
        for (String parameter : medeaTypeParameter) {
            String val = "";
            if (parameter != null) {
                if (request.getParameter(parameter) != null) {
                    val = request.getParameter(parameter);
                }

    %>
    <tr>
        <td class="leftCol-small"><%=parameter%>
        </td>
        <td>
            <input type="text" name="<%=parameter%>"
                   value="<%=val%>"
                   id="#_<%=parameter.replace(" ","")%>"
                   onkeypress="handletextBoxKeyPress(event)"/>
        </td>
    </tr>
    <%
            }
        }

    %>
</table>
<%--</td>--%>
<%--</tr>--%>