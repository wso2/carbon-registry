/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.resource.ui.processors;

//import org.apache.commons.collections.map.StaticBucketMap;
//import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
//import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.registry.core.utils.MediaTypesUtils;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//This class used to save, modified media type of a resource.
public class AddMediaTypeProcessor {
	
	static final String wsdlMediaType = "application/wsdl+xml";
	static final String policyMediaType = "application/policy+xml";
	static final String schemaMediaType = "application/x-xsd+xml";

    public static boolean process(
            HttpServletRequest request, HttpServletResponse response, ServletConfig config)
            throws Exception, RegistryException {
        String resourcePath = request.getParameter("resourcePath");
        String mediaType = request.getParameter("mediaType");
        String cookie = (String) request.
                getSession().getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String xmlMimeType = "application/xml";
        //added to get the MIME media type for the human readable media type
        String mimeMediatype = MediaTypesUtils.getMimeTypeFromHumanReadableMediaType(mediaType);
        
        ResourceServiceClient client =
                new ResourceServiceClient(cookie, config, request.getSession());
        //client.updateMediaType(resourcePath, mediaType);
        //passing the mime media type
        client.updateMediaType(resourcePath, mimeMediatype);
        //added by ragu 19-11-2012
        //check whether the resource come from outside the _system/governance
        if(!resourcePath.contains("/_system/governance"))
        {
        	//determine the media type of the resource to be updated
        	if(TempEditMediaTypeProcessor.getMediaTypeBeforeUpdate().equals(xmlMimeType))
        	{
        		//validate the scenario/usecase/query given by user
        		if(validateTheChangeInMediaType(mimeMediatype))
        		{
        			moveSelectedResource(resourcePath, mimeMediatype,client);
        	        client.delete(resourcePath);
        	        return true;
        		}
        	}
        }
         
        return false;
        //moveSelectedResource(resourcePath, mimeMediatype,client);
        //client.delete(resourcePath);

    }
    
    public static void moveSelectedResource(String resourePath,String mimeMediaType,ResourceServiceClient client)
    {
    	
    	/*String parentPath = "";
    	String oldResourcePath = null;*/
    	String destinationPath = null;
    	/*String resourceName = null;*/
    	if(!resourePath.startsWith("/"))
    	{
    		resourePath = RegistryConstants.PATH_SEPARATOR+resourePath;
    	}
    	String parentPath = resourePath.substring(0, resourePath.lastIndexOf("/"));
    	String oldResourcePath = resourePath;
    	String resourceName = resourePath.substring(resourePath.lastIndexOf("/")+1);
    	
    	if(mimeMediaType.equals(wsdlMediaType))
    	{
    		destinationPath = "/_system/governance/trunk/wsdls";
    	}
    	else if(mimeMediaType.equals(policyMediaType))
    	{
    		destinationPath = "/_system/governance/trunk/policies";
    	}
    	else if(mimeMediaType.equals(schemaMediaType))
    	{
    		destinationPath = "/_system/governance/trunk/schemas";
    	}
    	else
    	{
    		destinationPath= oldResourcePath;
    	}
    	try {
			client.copyResource(parentPath, oldResourcePath, destinationPath, resourceName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//CarbonUIMessage.sendCarbonUIMessage(message, messageType, request)
			e.printStackTrace();
		}
    	
    }
    
    protected static boolean validateTheChangeInMediaType(String mimeMediaType)
    {
    	if(mimeMediaType.equals(wsdlMediaType))
    	{
    		return true;
    	}
    	else if(mimeMediaType.equals(policyMediaType))
    	{
    		return true;
    	}
    	else if(mimeMediaType.equals(schemaMediaType))
    	{
    		return true;
    	}
    	else
    		return false;
    }
}
