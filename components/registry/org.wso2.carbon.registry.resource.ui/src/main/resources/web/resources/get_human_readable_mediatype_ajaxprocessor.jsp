<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%
    try {
        ResourceServiceClient client = new ResourceServiceClient(config, session);

        String mimeMappings = client.getHumanReadableMediaType();
%>
<%=mimeMappings%>
<%
    } catch (Exception e) {
        response.setStatus(500);
    }
%>