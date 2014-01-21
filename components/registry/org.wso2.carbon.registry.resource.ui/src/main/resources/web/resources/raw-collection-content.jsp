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
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.Utils" %>
<%@ page import="org.wso2.carbon.registry.common.ui.UIConstants" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData" %>
<%@ page import="org.wso2.carbon.registry.common.ui.utils.UIUtil" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.MetadataBean" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.registry.core.pagination.PaginationContext" %>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>

<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String viewMode = Utils.getResourceViewMode(request);
    String resourceConsumer = Utils.getResourceConsumer(request);
    String targetDivID = Utils.getTargetDivID(request);
    String requestedPage = request.getParameter(UIConstants.REQUESTED_PAGE);

    CollectionContentBean ccb;
    ResourceServiceClient client;
    String mimeMappings = null;
    String colMimeMappings = null;

    int start;
    int count = (int) (RegistryConstants.ITEMS_PER_PAGE);
    if (requestedPage != null) {
        start = (int) ((Integer.parseInt(requestedPage) - 1) * (RegistryConstants.ITEMS_PER_PAGE));
    } else {
        start = 1;
    }
    try {
        client = new ResourceServiceClient(cookie, config, session);
        PaginationContext.init(start, count, "", "", Integer.MAX_VALUE);
        ccb = client.getCollectionContent(request);
        mimeMappings = client.getCustomUIMediatypeDefinitions();
        colMimeMappings = client.getCollectionMediatypeDefinitions();
    } catch (Exception e) {
%>
<jsp:forward page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">

<div id="whileUpload" style="display:none;padding-top:0px;margin-top:20px;margin-bottom:20px;" class="ajax-loading-message">
	<img align="top" src="../resources/images/ajax-loader.gif"/>
	<span>Process may take some time. Please wait..</span>
</div>
<div class="add-resource-div registryWriteOperation" id="add-resource-div" style="display:none;">


<input type="hidden" name="path" value="<%=ccb.getPathWithVersion()%>"/>

<div class="validationError" id="resourceReason" style="display:none;"></div>

<table cellpadding="0" cellspacing="0" border="0" class="styledLeft noBorders">
<thead>
<tr>
    <th colspan="2"><strong><fmt:message key="add.resource"/></strong></th>
</tr>
</thead>
<tbody>
<tr>
    <td colspan="2">
    	<table class="normal">
    		<tr>
		    <td style="width:117px;*width:120px;"><fmt:message key="method"/></td>
		    <td>
		        <select id="addMethodSelector" onchange="viewAddResourceUI()">
		            <option value="upload" selected="selected"><fmt:message key="upload.content.from.file"/></option>
		            <option value="import"><fmt:message key="import.content.from.url"/></option>
		            <option value="text"><fmt:message key="text.content"/></option>
		            <option value="custom"><fmt:message key="create.custom.content"/></option>
                        </select>
		    </td>
		</tr>

    	</table>
    </td>
</tr>




<!-- upload file UI -->
<tr  id="uploadContentUI">
<td colspan="2">
<form onsubmit="return submitUploadContentForm();" method="post" name="resourceUploadForm"
      id="resourceUploadForm"
      action="../../fileupload/resource" enctype="multipart/form-data" target="_self">
    <input type="hidden" id="path" name="path" value="<%=ccb.getPathWithVersion()%>"/>


    <table class="styledLeft">
    <tr>
        <td class="middle-header" colspan="2"><fmt:message key="upload.content.from.file1"/></td>
   </tr>
    <tr>
        <td valign="top" style="width:120px;">
            <span><fmt:message key="file"/> <span class="required">*</span></span></td>
        <td>
            <input id="uResourceFile" type="file" name="upload" style="background-color:#cccccc" onkeypress="return blockManual(event)"
                   onchange="fillResourceUploadDetails()"/>

            <div class="helpText" id="fileHelpText">
                <fmt:message key="content.path.help.text"/>
            </div>
        </td>
    </tr>
    <tr>
        <td valign="top"><fmt:message key="name"/> <span class="required">*</span></td>

        <td><input id="uResourceName" type="text" name="filename"
                   style="margin-bottom:10px;"/></td>
    </tr>
    <tr>
        <td valign="top"><fmt:message key="media.type"/></td>

        <td>
            <input id="uResourceMediaType" type="text" name="mediaType"
                   style="margin-bottom:10px;"/>

        </td>
    </tr>
    <tr>
        <td valign="top"><fmt:message key="description"/></td>
        <td><textarea name="description" class="normal-textarea"></textarea></td>
    </tr>
    <tr>
        <td class="buttonRow" colspan="2">
            <input type="button" class="button" value="<fmt:message key="add"/>"
                   onclick="whileUpload();submitUploadContentForm();"/>
            <input type="button" class="button" value="<fmt:message key="cancel"/>"
                   onclick="showHide('add-resource-div')"/>
        </td>
    </tr>
    </table>

</form>
</td>
</tr>

<!-- import content UI -->

<tr id="importContentUI" style="display:none;">
<td colspan="2">
<form method="post" name="resourceImportForm"
      id="resourceImportForm"
      action="/wso2registry/system/fetchResource">
    <input type="hidden" id="irParentPath" name="path" value="<%=ccb.getPathWithVersion()%>"/>


    <table class="styledLeft">
    <tr>
        <td class="middle-header" colspan="2"><fmt:message key="import.content.from.url1"/></td>
    </tr>
    <tr>
        <td valign="top" style="width:120px;">
            <span><fmt:message key="url"/> <span class="required">*</span></span></td>
        <td>
            <input id="irFetchURL" type="text" name="fetchURL"
                   onchange="fillResourceImportDetails()"/>

            <div class="helpText" id="urlHelpText" style="color:#9a9a9a;">
                <fmt:message key="content.url.help.text"/>
            </div>
        </td>
    </tr>
    <tr>
        <td valign="top"><fmt:message key="name"/> <span class="required">*</span></td>

        <td><input id="irResourceName" type="text" name="resourceName"
                   style="margin-bottom:10px;"/></td>
    </tr>
    <tr>
        <td valign="top"><fmt:message key="media.type"/></td>

        <td>
            <input id="irMediaType" type="text" name="mediaType"
                   style="margin-bottom:10px;"/>

        </td>
    </tr>
    <tr>
        <td valign="top"><fmt:message key="description"/></td>
        <td><textarea id="irDescription" name="description" class="normal-textarea"></textarea></td>
    </tr>
    <tr>
        <td colspan="2" class="buttonRow"><input type="button" class="button"
                                                 value="<fmt:message key="add"/>"
                                                 onclick="whileUpload();submitImportContentForm();"/> <input
                type="button" class="button"
                value="<fmt:message key="cancel"/>"
                style="margin-top:10px;"
                onclick="showHide('add-resource-div')"/>
        </td>
    </tr>
    </table>

</form>
</td>
</tr>

<!-- text content UI -->

<tr id="textContentUI" style="display:none;">
<td colspan="2">
<form name="textContentForm" id="textContentForm" action="/wso2registry/system/addTextResource"
      method="post">
    <input type="hidden" id="trParentPath" name="path" value="<%=ccb.getPathWithVersion()%>"/>

    <table class="styledLeft">
    <tr>
        <td class="middle-header" colspan="2"><fmt:message key="text.content1"/></td>
    </tr>
    <tr>
        <td valign="top" style="width:120px;"><fmt:message key="name"/> <span
                class="required">*</span></td>
        <td><input type="text" id="trFileName" name="filename" style="margin-bottom:10px;"/></td>
    </tr>
    <tr>
        <td valign="top"><fmt:message key="media.type"/></td>
        <td><input type="text" id="trMediaType" name="mediaType" value="text/plain"
                   style="margin-bottom:10px;"/></td>
    </tr>
    <tr>
        <td valign="top"><fmt:message key="description"/></td>
        <td>
            <textarea id="trDescription" name="description"></textarea>
        </td>
    </tr>
    <tr>
        <td style="vertical-align:top !important;"><fmt:message key="content"/></td>
        <td>
            <div>
                <input type="radio" name="richText" checked="checked" value="plain" onclick="handleRichText()" /> <fmt:message key="plain.text.editor"/>
                <input type="radio" name="richText" value="rich" onclick="handleRichText()" /> <fmt:message key="rich.text.editor"/>
            </div>
            <textarea id="trPlainContent" style="width:99%;height:200px"></textarea>
            <div class="yui-skin-sam" id="textAreaPanel" style="display:none;">
                <textarea id="trContent" name="trContent" style="display:none;"></textarea>
            </div>
        </td>
    </tr>
    <tr>
        <td colspan="2" class="buttonRow">
            <input type="button" class="button" value="<fmt:message key="add"/>"
                   style="margin-top:10px;"
                   onclick="whileUpload();submitTextContentForm();"/>
            <input type="button" class="button"
                   value="<fmt:message key="cancel"/>"
                   onclick="showHide('add-resource-div')"/>
        </td>
    </tr>
    </table>

</form>
</td>
</tr>

<!-- custom content UI -->

<tr id="customContentUI" style="display:none;">
<td colspan="2">

<table class="styledLeft">
<tr>
    <td class="middle-header" colspan="2"><fmt:message key="create.custom.content1"/></td>
</tr>
<tr>
    <td colspan="2" class="special-area" style="font-style:italic;">
        <img style="margin-right: 5px;" src="../resources/images/help-small.jpg"/>
        <fmt:message key="custom.content.help.text"/>
    </td>
</tr>
<tr>
    <td style="width:80px;"><fmt:message key="media.type"/></td>
    <td><select id="customMediaTypeID" onchange="updateOther('customMediaTypeID', 'other')">
<%
            if(mimeMappings != null) {
                String[] mimeMappingArray = mimeMappings.split(",");
                for(String mimeMapping: mimeMappingArray) {
                    String[] mimeParts = mimeMapping.split(":");
                    %><option value="<%=mimeParts[1]%>"><%=mimeParts[1]%></option><%
                }
            }
%>
        <option value="other"><fmt:message key="other"/></option>
        </select>&nbsp;&nbsp;
       <span id="customMediaTypeIDOther" style="display:none">&nbsp;<fmt:message key="other.display"/>
            <input type="text" id="customMediaTypeIDOtherValue"/>
        </span>
    </td>
</tr>
<tr>
    <td colspan="2" class="buttonRow">
        <input type="button" class="button"
               value="<fmt:message key="create.content"/>"
               onclick="generateNewUI('<%=ccb.getPathWithVersion()%>')"/>
        <input type="button" class="button"
                   value="<fmt:message key="cancel"/>"
                   onclick="showHide('add-resource-div')"/>
    </td>
</tr>
<tr>
    <td colspan="2">
        <br/>

        <div id="customAddUIDiv">
        </div>
    </td>
</tr>
</table>

</td>
</tr>
</tbody>
</table>
</div>


<!-- Add link div -->
<div class="add-resource-div registryWriteOperation" id="add-link-div" style="display:none;">

<div class="validationError" id="LinkReason" style="display:none;"></div>

<table cellpadding="0" cellspacing="0" border="0" class="styledLeft noBorders">
<thead>
<tr>
    <th colspan="2"><strong><fmt:message key="add.link"/></strong></th>
</tr>
</thead>
<tbody>
<%
// remote links settings are only available to super tenant, 
// blocking the select tag will lead to show only the symlink content
if (CarbonUIUtil.isSuperTenant(request)) {
%>
<tr>
    <td colspan="2">
    	<table class="normal">
    		<tr>
		    <td style="width:120px;"><fmt:message key="method"/></td>
		    <td>
		        <select id="addLinkMethodSelector" onchange="viewAddLinkUI()">
                            <option value="symlink"><fmt:message key="add.symbolic.link"/></option>
                            <option value="remotelink"><fmt:message key="add.remote.link"/></option>
                        </select>
		    </td>
		</tr>

    	</table>
    </td>
</tr>
<%
}
%>


<!-- symlink content UI -->

<tr id="symlinkContentUI" style="display:none;">
<td colspan="2">
<form name="symlinkContentForm1" id="symlinkContentForm1" action="/wso2registry/system/addSymbolicLink"
      method="post">
    <input type="hidden" id="srParentPath" name="path" value="<%=ccb.getPathWithVersion()%>"/>

    <table class="styledLeft">
    <tr>
        <td class="middle-header" colspan="2"><fmt:message key="add.symbolic.link1"/></td>
    </tr>
    <tr>
        <td valign="top" style="width:120px;"><fmt:message key="name"/> <span
                class="required">*</span></td>
        <td><input type="text" id="srFileName" name="filename" style="margin-bottom:10px;"/></td>
    </tr>
    <tr>
        <td valign="top"><fmt:message key="path"/><span
                class="required">*</span></td>
        <td>
            <input id="srPath" name="targetpath"/>
            <input type="button" class="button"
                                       value=".." title="<fmt:message key="resource.tree"/>"
                                       onclick="showResourceTree('srPath');"/>
        </td>
    </tr>
    <tr>
        <td colspan="2" class="buttonRow">
            <input type="button" class="button" value="<fmt:message key="add"/>"
                   style="margin-top:10px;"
                   onclick="whileUpload();submitSymlinkContentForm();"/>
            <input type="button" class="button"
                   value="<fmt:message key="cancel"/>"
                   onclick="showHide('add-link-div')"/>
        </td>
    </tr>
    </table>

</form>
</td>
</tr>


<!-- remotelink content UI -->

<tr id="remotelinkContentUI" style="display:none;">
<td colspan="2">
<form name="symlinkContentForm" id="remotelinkContentForm" action="/wso2registry/system/addRemoteLink"
      method="post">
    <input type="hidden" id="rrParentPath" name="path" value="<%=ccb.getPathWithVersion()%>"/>

    <table class="styledLeft">
    <tr>
        <td class="middle-header" colspan="2"><fmt:message key="add.remote.link1"/></td>
    </tr>
    <tr>
        <td valign="top" style="width:120px;"><fmt:message key="name"/> <span
                class="required">*</span></td>
        <td><input type="text" id="rrFileName" name="filename" style="margin-bottom:10px;"/></td>
    </tr>
    <tr>
        <td valign="top"><fmt:message key="remote.instance"/><span
                class="required">*</span></td>
        <%
            String[] remoteInstances = {"No instances available"};
            String[] instances = ccb.getRemoteInstances();
            if (instances != null) {
                remoteInstances = instances;
            }
        %>
        <td>
            <select id="rrInstance"/>
            <% for(int i=0; i<remoteInstances.length; i++) {
            %>
            <option value="<%=remoteInstances[i]%>"><%=remoteInstances[i]%></option>
            <%
                }
             %>

        </td>
    </tr>
    <tr>
        <td valign="top" style="width:120px;"><fmt:message key="path"/></td>
        <td><input type="text" id="rrTargetPath" name="rrTargetPath" style="margin-bottom:10px;"/></td>
    </tr>
    <tr>
        <td colspan="2" class="buttonRow">
        <%if (instances == null) {
        %>

       <input type="button" class="button" value="<fmt:message key="add"/>"
                   style="margin-top:10px;" disabled="disabled" />
            <%
    } else {
           %>
            <input type="button" class="button" value="<fmt:message key="add"/>"
                   style="margin-top:10px;"
                   onclick="whileUpload();submitRemotelinkContentForm();"/>
   <% }
            %>
            <input type="button" class="button"
                   value="<fmt:message key="cancel"/>"
                   onclick="showHide('add-link-div')"/>
        </td>
    </tr>
    </table>

</form>
</td>
</tr>
</tbody>
</table>
</div>


<!-- Add folder div -->
<div class="add-resource-div registryWriteOperation" id="add-folder-div" style="display:none;">

    <form name="collectionForm" method="post" action="add_collection_ajaxprocessor.jsp"
          onsubmit="return submitCollectionAddForm();">
        <input id="parentPath" type="hidden" name="parentPath"
               value="<%=ccb.getPathWithVersion()%>"/>

        <div class="validationError" id="collectionReason" style="display:none;"></div>
        <table width="100%" border="0" cellspacing="0" cellpadding="0" class="styledLeft noBorders">
            <tbody>
            <tr>
                <td class="middle-header" colspan="2"><strong><fmt:message
                        key="add.collection"/></strong></td>
            </tr>
            <tr>
                <td valign="top" style="width:120px;"><fmt:message key="name"/> <span
                        class="required">*</span></td>

                <td><input type="text" id="collectionName" name="collectionName"/></td>
            </tr>
            <tr>
                <td valign="top"><fmt:message key="media.type1"/></td>
                <td>
                    <select id="collectionMediaType" onchange="updateOther('mediaType', 'other')">
                        <option value=""><fmt:message key="none"/></option>
<%
            if(colMimeMappings != null) {
                String[] mimeMappingArray = colMimeMappings.split(",");
                for(String mimeMapping: mimeMappingArray) {
                    String[] mimeParts = mimeMapping.split(":");
                    %><option value="<%=mimeParts[1]%>"><%=mimeParts[1]%></option><%
                }
            }
%>
                    <option value="other"><fmt:message key="other"/></option>
                </select>&nbsp;&nbsp;
                <span id="mediaTypeOther" style="display:none"><fmt:message key="other.display"/>&nbsp;
                    <input type="text" id="mediaTypeOtherValue"/>
                </span>
                </td>
            </tr>
            <tr>
                <td valign="top"><fmt:message key="description"/></td>
                <td><textarea name="description" id="colDesc" class="normal-textarea"></textarea>
                </td>
            </tr>
            <tr>
                <td class="buttonRow" colspan="2">
                    <input type="button" class="button"
                           value="<fmt:message key="add"/>"
                           onclick="submitCollectionAddForm()"/>
                    <input type="button" class="button"
                           value="<fmt:message key="cancel"/>"
                           onclick="showHide('add-folder-div')"/>
                </td>
            </tr>
            </tbody>
        </table>
    </form>
</div>
<div id="entryListReason" class="validationError" style="display: none;"></div>
</fmt:bundle>
<div id="entryList">
<%
    if (ccb.getChildCount() != 0) {
%>
<%
    int rowCount = Integer.parseInt(session.getAttribute("row_count").toString());
    int totalCount = rowCount;

    int itemsPerPage = RegistryConstants.ITEMS_PER_PAGE;

    int pageNumber;
    if (requestedPage != null) {
        pageNumber = new Integer(requestedPage);
    } else {
        pageNumber = 1;
    }

    int numberOfPages;
    if (rowCount % itemsPerPage == 0) {
        numberOfPages = rowCount / itemsPerPage;
    } else {
        numberOfPages = rowCount / itemsPerPage + 1;
    }

    String[] nodes = Utils.getSortedChildNodes(ccb);
    List<String> availableNodes = new LinkedList<String>();
    for (String node : nodes) {
        try {
            if (node != null && client.getResourceTreeEntry(node) != null) {
                availableNodes.add(node);
            }
        } catch (Exception ignore) {}
    }
    String[] allChildNodes = availableNodes.toArray(new String[availableNodes.size()]);
    ResourceData[] resourceDataSet;
    try {
        resourceDataSet = client.getResourceData(allChildNodes);
    } catch(Exception e) {
        %>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">
<table cellpadding="0" cellspacing="0" border="0" style="width:100%"
       class="styledLeft" id="resourceColTable">
<%
    if (resourceDataSet != null) {

%>
<thead>
<tr>
    <th><fmt:message key="name"/></th>
    <th><fmt:message key="created.date"/></th>
    <th><fmt:message key="author"/></th>
</tr>
</thead>
<tbody>
<%        
    int entryNumber = 0;

    //for (int i = start; i <= end; i++) {
    for (int ri = 0; ri < resourceDataSet.length; ri++) {
        ResourceData resourceData = resourceDataSet[ri];
        MetadataBean metadata;
        try {
            if (resourceData.getResourcePath() != null) {
                metadata = client.getMetadata(resourceData.getResourcePath());
            } else {
                metadata = null;
            }
        } catch(Exception e) {
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
        //    ResourceData resourceData = (ResourceData) collection.getResourceDataList().get(i);
        entryNumber++;
%>

<tr id="1">
   
    <td valign="top"  id="actionPaneHelper<%=entryNumber%>" class="action-pane-helper">
         <table cellpadding="0" cellspacing="0" border="0" style="width:100%">
         <tr>
         <td class="entryName-left" style="border:none !important;padding:0px !important;margin:0px !important;">
      
	        <% if (resourceData.getResourceType().equals(UIConstants.COLLECTION)) { %>
	        <a class=<% if(resourceData.getLink()){
		        	if (!resourceData.getMounted()) { 
		        	%>"folder-small-icon-link-x trimer"<%
		        	}else{
		        	%>"folder-small-icon-link-y trimer"<%
		        	}
	        	}else {
	        	 %>"folder-small-icon-link trimer"<% 
	        	 } %>
	           onclick="loadResourcePage('<%=resourceData.getResourcePath()%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"
	           id="resourceView<%=entryNumber%>"
	           title="<%=resourceData.getName()%>"><%=resourceData.getName()%>
	        </a>
	
	
	        <% } else { %>
	         <a class=<% if(resourceData.getLink()){
		        	if (!resourceData.getMounted()) { 
		        	%>"resource-icon-link-x trimer"<%
		        	}else{
		        	%>"resource-icon-link-y trimer"<%
		        	}
	        	}else {                                                            
	        	 %>"resource-icon-link trimer"<% 
	        	 } %>
	           <% if(!resourceData.getExternalLink()){ %>
               onclick="loadResourcePage('<%=resourceData.getResourcePath()%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"
             <% } else {
                client = new ResourceServiceClient(cookie, config, session);
                 String orignalPath = (String)request.getAttribute("path");
                 request.setAttribute("path", resourceData.getResourcePath());
                String url = client.getExternalURL(request);
                 request.setAttribute("path", orignalPath);
             %>
             onclick="myReg=window.open('<%=url%>')"
             <% }%>
	           id="resourceView<%=entryNumber%>" title="<%=resourceData.getName()%>"><%=resourceData.getName()%>
	        </a>
	
	        <% } %>
	 </td>
	 <td class="entryName-right" style="border:none !important;padding:0px !important;margin:0px !important;float:right;" nowrap="nowrap">
         <table cellspacing="0" cellpadding="0" border="0"><tr><td style="border:none !important;padding:0px !important;margin:0px !important;" width="100%" nowrap="nowrap">
             <a style="float:right;" id="infoLink<%=entryNumber%>" onclick="loadActionPane('<%=entryNumber%>','info')" title="<fmt:message key='actions'/>" class="entryName-contracted"><fmt:message key='info'/></a>
         </td><td style="border:none;" nowrap="nowrap">
             <a style="float:right;" id="actionLink<%=entryNumber%>" onclick="loadActionPane('<%=entryNumber%>','action')" title="<fmt:message key='actions'/>" class="entryName-contracted"><fmt:message key='actions'/></a>
         </td></tr></table>
        </td>
        </tr>
        </table>
        
    </td>

    <td valign="top">
    <div style="width:90px;">
        <% if(resourceData.getCreatedOn() != null) { %>
        <span style="cursor:default" title="<%=resourceData.getFormattedCreatedOn()%>">
        <%
            if (resourceData.getFormattedCreatedOn().length() > 12) {
                String createdOn = resourceData.getFormattedCreatedOn();
            // Full date scenario
        %><%=createdOn.substring(0, createdOn.indexOf("ago") + 3)%>
        <%
        } else { %>
        <%=resourceData.getFormattedCreatedOn()%>
        <% } %>
        </span>
        <% } %>
    </div>
    </td>
    <td valign="top">
     <span style="cursor:default" title="<%=resourceData.getAuthorUserName()%>">
    <%
            if (resourceData.getAuthorUserName().length() > 13) {
        %><%=resourceData.getAuthorUserName().subSequence(0, 11) + ".."%>
        <%
        } else { %>
        <%=resourceData.getAuthorUserName()%>
        <% } %>
    </span>
    </td>
</tr>
<tr id="actionPane<%=entryNumber%>" style="display:none" class="actionPaneSelector">
   <td colspan="3" class="action-pane">
   <div>

   <%
       if (!ccb.getVersionView()) { %>

            <% if (resourceData.getResourceType().equals(UIConstants.COLLECTION)) { %> 
            
	            <% if (resourceData.getDeleteAllowed()){
                    if (resourceData.getPutAllowed()) { %>
                   <a class="edit-icon-link registryWriteOperation"
                      onclick="javascript:showHideCommon('rename_panel<%=entryNumber%>');hideOthers(<%=entryNumber%>,'rename');if($('rename_panel<%=entryNumber%>').style.display!='none')$('resourceEdit<%=entryNumber%>').focus();">
                       <fmt:message key="rename"/></a>
                   <% } %>
	    	    <a class="move-icon-link registryWriteOperation"
	               onclick="showHideCommon('move_panel<%=entryNumber%>');hideOthers(<%=entryNumber%>,'move');">
	                <fmt:message key="move"/></a>
                <a class="delete-icon-link registryWriteOperation"
	               onclick="this.disabled = true; hideOthers(<%=entryNumber%>,'del');deleteResource('<%=resourceData.getResourcePath()%>', '<%=ccb.getPathWithVersion()%>'); this.disabled = false; "
	                    >
	                <fmt:message key="delete"/></a>
                <%}
                if(resourceData.getGetAllowed()){%>
	            <a class="copy-icon-link registryWriteOperation"
	               onclick="showHideCommon('copy_panel<%=entryNumber%>');hideOthers(<%=entryNumber%>,'copy');">
	                <fmt:message key="copy"/></a>
            <%}%>


            <% } else { %>
	            <% if(resourceData.getDeleteAllowed()){
                    if (resourceData.getPutAllowed()) { %>
                <a class="edit-icon-link registryWriteOperation"
	               onclick="javascript:showHideCommon('rename_panel<%=entryNumber%>');hideOthers(<%=entryNumber%>,'rename');if($('rename_panel<%=entryNumber%>').style.display!='none')$('resourceEdit<%=entryNumber%>').focus();">
	                <fmt:message key="rename"/></a>
                <% } %>
	            <a class="move-icon-link registryWriteOperation"
	               onclick="showHideCommon('move_panel<%=entryNumber%>');hideOthers(<%=entryNumber%>,'move');">
	                <fmt:message key="move"/></a>
                <a class="delete-icon-link registryWriteOperation" style="margin-left:5px"
	               onclick="hideOthers(<%=entryNumber%>,'del');deleteResource('<%=resourceData.getResourcePath()%>', '<%=ccb.getPathWithVersion()%>')">
	                <fmt:message key="delete"/></a>
	            <%}
                if(resourceData.getGetAllowed()){%>
                <a class="copy-icon-link registryWriteOperation"
	               onclick="showHideCommon('copy_panel<%=entryNumber%>');hideOthers(<%=entryNumber%>,'copy');">
	                <fmt:message key="copy"/></a>
       <%}%>


            <% } %>
   <% }
       if (!ccb.getVersionView() && !resourceData.getAbsent().equals("true")) { %>
	    <% if (!resourceData.getResourceType().equals(UIConstants.COLLECTION)) {
            String path;
            if (resourceData.getRealPath() != null) {
                path = resourceData.getRealPath();
            } else {
                path = resourceData.getResourcePath();
            }
            if (path.startsWith("http")) {
                %>
                <a class="download-icon-link"
		           href="<%=path%>"
		           target="_blank"><fmt:message key="download"/></a>
       <%
            } else {
        %>
	    <a class="download-icon-link"
		           href="javascript:sessionAwareFunction(function() {window.location = '<%=Utils.getResourceDownloadURL(request, path)%>'}, org_wso2_carbon_registry_resource_ui_jsi18n['session.timed.out']);"
		           target="_self"><fmt:message key="download"/></a>
	    <% } }%>
    <% } %>
   </div>
   </td>
</tr>
<tr id="infoPane<%=entryNumber%>" style="display:none" class="actionPaneSelector">
    <td colspan="3" class="action-pane">
        <div>
            <table class="normal">
                <tr>
                    <td class="info-emp"><fmt:message key="media.type1"/>:</td>
                    <td><% if (metadata != null && metadata.getMediaType() != null && metadata.getMediaType().length() != 0) { %><%=metadata.getMediaType()%><% } else { %>
                <fmt:message key="unknown"/><% } %></td>
                </tr>
                <tr>
                    <td class="info-emp"><fmt:message key="feed"/>:</td>
                    <td><a class="feed-small-res-icon-link" href="<%=UIUtil.getAtomURL(config, request, resourceData.getResourcePath() +
    ((resourceData.getResourceType().equals(UIConstants.COLLECTION)) ? "" : ";logs"))%>"
                           target="_blank">
                        <fmt:message key="feed"/>
                    </a></td>
                </tr>
                <tr>
                    <td class="info-emp"><fmt:message key="rating"/>:</td>
                    <td>
                        <div style="width:100px;">
                            <a style="cursor:default;" title="<%=resourceData.getAverageRating()%>">
                                <img src="../resources/images/r<%=resourceData.getAverageStars()[0]%>.gif"
                                     align="top"/>
                                <img src="../resources/images/r<%=resourceData.getAverageStars()[1]%>.gif"
                                     align="top"/>
                                <img src="../resources/images/r<%=resourceData.getAverageStars()[2]%>.gif"
                                     align="top"/>
                                <img src="../resources/images/r<%=resourceData.getAverageStars()[3]%>.gif"
                                     align="top"/>
                                <img src="../resources/images/r<%=resourceData.getAverageStars()[4]%>.gif"
                                     align="top"/>
                            </a>
                        </div>
                    </td>
                </tr>
            </table>
        </div>
    </td>
</tr>
<tr class="copy-move-panel registryWriteOperation" id="copy_panel<%=entryNumber%>" style="display:none;">
    <td colspan="3" align="left">
        <table cellpadding="0" cellspacing="0" class="styledLeft">
            <thead>
            <tr>
                <th colspan="2">Copy <%=resourceData.getResourceType()%>
                </th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><fmt:message key="destination.path"/><span class="required">*</span></td>
                <td><input type="text" id="copy_destination_path<%=entryNumber%>"/><input
                        type="button"
                        title="<fmt:message key="resource.tree"/>"
                        onclick="showCollectionTree('copy_destination_path<%=entryNumber%>');"
                        value=".." class="button"/></td>
            </tr>
            <tr>
                <td class="buttonRow" colspan="2">
                    <input type="button" class="button" value="<fmt:message key="copy"/>"
                           onclick="this.disabled = true; copyResource('<%=ccb.getPathWithVersion()%>', '<%=resourceData.getResourcePath()%>','copy_destination_path<%=entryNumber%>','<%=resourceData.getName()%>',<%=pageNumber%>); this.disabled = false;"/>
                    <input
                            type="button" style="margin-left:5px;" class="button"
                            value="<fmt:message key="cancel"/>"
                            onclick="showHideCommon('copy_panel<%=entryNumber%>')"/></td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>
<tr class="copy-move-panel registryWriteOperation" id="move_panel<%=entryNumber%>" style="display:none;">
    <td colspan="3" align="left">
        <table cellpadding="0" cellspacing="0" class="styledLeft">
            <thead>
            <tr>
                <th colspan="2"><fmt:message key="move"/> <%=resourceData.getResourceType()%>
                </th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><fmt:message key="destination.path"/><span class="required">*</span></td>
                <td><input type="text" id="move_destination_path<%=entryNumber%>"/> <input
                        type="button"
                        class="button"
                        title="<fmt:message key="resource.tree"/>"
                        onclick="showCollectionTree('move_destination_path<%=entryNumber%>');"
                        value=".."/></td>
            </tr>
            <tr>
                <td class="buttonRow" colspan="2">
                    <input type="button" class="button" value="<fmt:message key="move"/>"
                           onclick="this.disabled = true; moveResource('<%=ccb.getPathWithVersion()%>', '<%=resourceData.getResourcePath()%>','move_destination_path<%=entryNumber%>','<%=resourceData.getName()%>',<%=pageNumber%>); this.disabled = false;"/>
                    <input
                            type="button" style="margin-left:5px;" class="button"
                            value="<fmt:message key="cancel"/>"
                            onclick="showHideCommon('move_panel<%=entryNumber%>')"/></td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>
<tr class="copy-move-panel registryWriteOperation" id="rename_panel<%=entryNumber%>" style="display:none;">
    <td colspan="3" align="left">
        <table cellpadding="0" cellspacing="0" class="styledLeft">
            <thead>
            <tr>
                <th><fmt:message key="rename.name"/> <%=resourceData.getResourceType()%>
                </th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>New <% if (resourceData.getResourceType().equals(UIConstants.COLLECTION)) { %>
                    <fmt:message key="collection"/><% } else {%><fmt:message key="resource"/><% } %>
                    Name <span class="required">*</span>  <input value="<%=resourceData.getName()%>" type="text"
                                id="resourceEdit<%=entryNumber%>"/></td>
            </tr>
            <tr>
                <td class="buttonRow">
                    <input type="button" class="button" value="<fmt:message key="rename"/>"
                           onclick="this.disabled = true; renameResource('<%=ccb.getPathWithVersion()%>', '<%=resourceData.getResourcePath()%>', 'resourceEdit<%=entryNumber%>',<%=pageNumber%>, <% if (resourceData.getResourceType().equals(UIConstants.COLLECTION)) { %>'collection'<% } else { %>'resource'<% } %>);this.disabled = false;"/>
                    <input
                        type="button" style="margin-left:5px;" class="button"
                        value="<fmt:message key="cancel"/>"
                        onclick="showHideCommon('rename_panel<%=entryNumber%>')"/>
                </td>
            </tr>
            </tbody>
        </table>

    </td>
</tr>
<tr class="copy-move-panel registryWriteOperation" id="del_panel<%=entryNumber%>" style="display:none;">
    <td colspan="3" align="left">
        <table cellpadding="0" cellspacing="0" class="styledLeft">
            <thead>
            <tr>
                <th><fmt:message key="confirm.delete"/> <%=resourceData.getResourceType()%>
                </th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><fmt:message key="confirm.remove.resource.message"/>
                    <%=resourceData.getResourceType()%> '<%=resourceData.getName()%>'
                    <br/><strong><fmt:message key="warning"/>: </strong>
                    <fmt:message key="undo.warning.message"/>
                </td>
            </tr>
            <tr>
                <td class="buttonRow">
                    <input type="button" class="button" value="<fmt:message key="yes"/>"
                           onclick="deleteResource('<%=resourceData.getResourcePath()%>', '<%=ccb.getPathWithVersion()%>')"/>
                    <input style="margin-left:5px;" class="button" type="button"
                           value="<fmt:message key="no"/>"
                           onclick="showHideCommon('del_panel<%=entryNumber%>')"/>
                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>

<% }
    if (totalCount <= itemsPerPage) {
        //No paging
    } else {
%>
<tr>
    <td colspan="3" class="pagingRow" style="padding-top:10px; padding-bottom:10px;">

        <%
            if (pageNumber == 1) {
        %>
        <span class="disableLink">< Prev</span>
        <%
        } else {
        %>
        <a class="pageLinks" title="<fmt:message key="page.x.to.y"><fmt:param value="<%=(pageNumber - 1)%>"/><fmt:param value="<%=UIUtil.getFirstPage(pageNumber - 1, itemsPerPage, allChildNodes)%>"/><fmt:param value="<%=UIUtil.getLastPage(pageNumber - 1, itemsPerPage, allChildNodes)%>"/></fmt:message>"
           onclick="navigatePages(<%=(pageNumber-1)%>, '<%=ccb.getPathWithVersion()%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"><
            <fmt:message key="prev"/></a>
        <%
            }
            if (numberOfPages <= 10) {
                for (int pageItem = 1; pageItem <= numberOfPages; pageItem++) { %>

        <a title="<fmt:message key="page.x.to.y"><fmt:param value="<%=pageItem%>"/><fmt:param value="<%=UIUtil.getFirstPage(pageItem, itemsPerPage, allChildNodes)%>"/><fmt:param value="<%=UIUtil.getLastPage(pageItem, itemsPerPage, allChildNodes)%>"/></fmt:message>" class=<% if(pageNumber==pageItem){ %>"pageLinks-selected"<% } else { %>"pageLinks" <% } %>
        onclick="navigatePages(<%=pageItem%>, '<%=ccb.getPathWithVersion()%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')" ><%=pageItem%></a>
        <% }
        } else {
            // FIXME: The equals comparisons below looks buggy. Need to test whether the desired
            // behaviour is met, when there are more than ten pages.
            String place = "middle";
            int pageItemFrom = pageNumber - 2;
            int pageItemTo = pageNumber + 2;

            if (numberOfPages - pageNumber <= 5) place = "end";
            if (pageNumber <= 5) place = "start";

            if (place == "start") {
                pageItemFrom = 1;
                pageItemTo = 7;
            }
            if (place == "end") {
                pageItemFrom = numberOfPages - 7;
                pageItemTo = numberOfPages;
            }

            if (place == "end" || place == "middle") {


                for (int pageItem = 1; pageItem <= 2; pageItem++) { %>

        <a class="pageLinks" title="<fmt:message key="page.x.to.y"><fmt:param value="<%=pageItem%>"/><fmt:param value="<%=UIUtil.getFirstPage(pageItem, itemsPerPage, allChildNodes)%>"/><fmt:param value="<%=UIUtil.getLastPage(pageItem, itemsPerPage, allChildNodes)%>"/></fmt:message>"
           onclick="navigatePages(<%=pageItem%>, '<%=ccb.getPathWithVersion()%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"><%=pageItem%>
        </a>
        <% } %>
        ...
        <%
            }

            for (int pageItem = pageItemFrom; pageItem <= pageItemTo; pageItem++) { %>

        <a title="<fmt:message key="page.x.to.y"><fmt:param value="<%=pageItem%>"/><fmt:param value="<%=UIUtil.getFirstPage(pageItem, itemsPerPage, allChildNodes)%>"/><fmt:param value="<%=UIUtil.getLastPage(pageItem, itemsPerPage, allChildNodes)%>"/></fmt:message>" class=<% if(pageNumber==pageItem){ %>"pageLinks-selected"<% } else {%>"pageLinks"<% } %>
        onclick="navigatePages(<%=pageItem%>, '<%=ccb.getPathWithVersion()%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"><%=pageItem%></a>
        <% }

            if (place == "start" || place == "middle") {
        %>
        ...
        <%
            for (int pageItem = (numberOfPages - 1); pageItem <= numberOfPages; pageItem++) { %>

        <a class="pageLinks" title="<fmt:message key="page.x.to.y"><fmt:param value="<%=pageItem%>"/><fmt:param value="<%=UIUtil.getFirstPage(pageItem, itemsPerPage, allChildNodes)%>"/><fmt:param value="<%=UIUtil.getLastPage(pageItem, itemsPerPage, allChildNodes)%>"/></fmt:message>"
           onclick="navigatePages(<%=pageItem%>, '<%=ccb.getPathWithVersion()%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"
           style="margin-left:5px;margin-right:5px;"><%=pageItem%>
        </a>
        <% }
        }

            if (place == "middle") {

            }
            //End middle display
        }
            if (pageNumber == numberOfPages) {
        %>
        <span class="disableLink"><fmt:message key="next"/> ></span>
        <%
        } else {
        %>
        <a class="pageLinks" title="<fmt:message key="page.x.to.y"><fmt:param value="<%=(pageNumber + 1)%>"/><fmt:param value="<%=UIUtil.getFirstPage(pageNumber + 1, itemsPerPage, allChildNodes)%>"/><fmt:param value="<%=UIUtil.getLastPage(pageNumber + 1, itemsPerPage, allChildNodes)%>"/></fmt:message>"
           onclick="navigatePages(<%=(pageNumber+1)%>, '<%=ccb.getPathWithVersion()%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')">Next
            ></a>
        <%
            }
        %>
	<span id="xx<%=pageNumber%>" style="display:none" />
    </td>
</tr>
<%
        }
        }
%>
<tr>
        <%--This empty td is required to solve the bottom margin problem on IE. Do not remove!!--%>
    <td align="left" colspan="3" style="height:0px;border-bottom:0px;">
    </td>
</tr>
</tbody>
</table>
</fmt:bundle>
<%
    }

%>

</div>
