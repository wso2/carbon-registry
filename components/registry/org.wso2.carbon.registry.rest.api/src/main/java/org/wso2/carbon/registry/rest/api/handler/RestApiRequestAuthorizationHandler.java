
/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.rest.api.handler;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
 
public class RestApiRequestAuthorizationHandler implements RequestHandler{
	
	protected Log log = LogFactory.getLog(RestApiRequestAuthorizationHandler.class);
	/**
	  * This method handles the request received at the registry endpoint. The method decodes the extracted 
	  * JWT token and extract the enduser's username and tenantID which is appended to the original rest request 
	  * as query param and forwarded to the respective resource class. This method returns 
	  * a null value which indicates that the request to be processed. 
	  */
	@Override
	public Response handleRequest(Message message, ClassResourceInfo resourceInfo) {
		if(log.isDebugEnabled()){
			log.debug("request has been received at REST API handle request method");
		}
		String requestUrl = message.get(Message.REQUEST_URL).toString();
		String queryParam = (message.get(Message.QUERY_STRING) != null)?message.get(Message.QUERY_STRING).toString():"";
		//extracting all the headers received along with the request.
		String header = message.get(Message.PROTOCOL_HEADERS).toString();
		String userName = getUsernameFromJwtToken(header);
		String tenantID = null;
		try {
			tenantID = String.valueOf(IdentityUtil.getTenantIdOFUser(userName));  
		} 
		catch (IdentityException e) {
			log.error(e.getMessage(),e);
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		String queryParamAppender = (queryParam.length() == 0 ? "" : "&");
		queryParam += queryParamAppender + "username="+userName+"&tenantid="+tenantID;
		message.put(Message.REQUEST_URL, requestUrl);
		message.put(Message.QUERY_STRING, queryParam);			
		return null;
	}
	/**
	 * this method extract the jwt token header among the request headers, extract the end user's username and returns.
	 * sample jwt token is given as follows,
	 * 
	 * eyJ0eXAiOiJKV1QiLCJhbGciOiJTSEEyNTZ3aXRoUlNBIiwieDV0IjoiTm1KbU9HVXh
	 * NelpsWWpNMlpEUmhOVFpsWVRBMVl6ZGhaVFJpT1dFME5XSTJNMkptT1RjMVpBPT0ifQ==.eyJpc3MiOiJ3c28yLm9yZy9wcm9kd
	 * WN0cy9hbSIsImV4cCI6MTM2MjcyNDczMzc4NiwiaHR0cDovL3dzbzIub3JnL2NsYWltcy9zdWJzY3JpYmVyIjoiYWRtaW4iLCJo
	 * dHRwOi8vd3NvMi5vcmcvY2xhaW1zL2FwcGxpY2F0aW9ubmFtZSI6IkRlZmF1bHRBcHBsaWNhdGlvbiIsaHR0cDovL3dzbzIu
	 * b3JnL2NsYWltcy9hcGljb250ZXh0IjoiL0FBQSIsImh0dHA6Ly93c28yLm9yZy9jbGFpbXMvdmVyc2lvbiI6IjEuMC4wIiwiaHR
	 * 0cDovL3dzbzIub3JnL2NsYWltcy90aWVyIjoiVW5saW1pdGVkIiwiaHR0cDovL3dzbzIub3JnL2NsYWltcy9lbmR1c2VyIjoi
	 * YWRtaW4ifQ.Q4Q1ET1SECUT1+OT3AEkNXuUnRg3ssUnWWyOt2Us8boBwjA9AYjnKvDnMqqaOJUjRzWqGdZjoYXycTlTmqFBVdN
	 * Nq+V4Ol4FMcL5zA3mat4JvYQlvhtqD/3zP0pM7SrLCPQ8uCTWWVlX/y+bUg1F1MoKUGvpmACDbgdLtRT9Btc=
	 * 
	 * from the above token, extract the part in between two dots(.) and decode that to string variable. 
	 * the decoded string has the informations such as app name, subscriber's username and 
	 * enduser's username ..etc. Then extract the enduser's username and returns to the caller. 
	 * 
	 * After decoded the  extracted portion will be as follows:
	 * {"iss":"wso2.org/products/am","exp":1367660211994,"http://wso2.org/claims/subscriber":"admin",
	 * "http://wso2.org/claims/applicationid":"10","http://wso2.org/claims/applicationname":"rest",
	 * "http://wso2.org/claims/applicationtier":"Unlimited","http://wso2.org/claims/apicontext":"/api",
	 * "http://wso2.org/claims/version":"1.0.0","http://wso2.org/claims/tier":"Gold",
	 * "http://wso2.org/claims/keytype":"PRODUCTION","http://wso2.org/claims/usertype":"APPLICATION",
	 * "http://wso2.org/claims/enduser":"admin","http://wso2.org/claims/enduserTenantId":"-1234"}
	 * 
	 */
	private String getUsernameFromJwtToken(String header){
		if(log.isDebugEnabled()){
			log.debug("Extracting the username of the enduser from the JWT token");
		}
		header = header.substring(header.indexOf("x-jwt-assertion"));
		header = header.substring(header.indexOf(".")+1, header.lastIndexOf("."));
		
		//decode the jwt token and convert it to string
		byte[] jwtBytes = header.getBytes();
		byte[] decodedBytes = Base64.decodeBase64(jwtBytes);
		String jwtStr = new String(decodedBytes);
		
		//extract the enduser's username and returns
		int endUserIndex = jwtStr.indexOf("http://wso2.org/claims/enduser");
		jwtStr = jwtStr.substring(endUserIndex+"http://wso2.org/claims/enduser".length()+1);	
		String endUsername = jwtStr.substring(jwtStr.indexOf('"')+1); 
		endUsername = endUsername.substring(0, endUsername.indexOf('"'));	
		return endUsername;
	}
}
