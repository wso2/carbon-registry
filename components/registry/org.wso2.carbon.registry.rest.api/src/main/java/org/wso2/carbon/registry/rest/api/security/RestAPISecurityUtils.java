/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.rest.api.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class RestAPISecurityUtils {
	private static Log log = LogFactory.getLog(RestAPISecurityUtils.class);
	
	public static RestAPIAuthContext getAuthContext(PrivilegedCarbonContext context, String JWTToken) {
		RestAPIAuthContext authContext = new RestAPIAuthContext();
		if (context.getUsername() != null && 
				context.getTenantId() != org.wso2.carbon.base.MultitenantConstants.INVALID_TENANT_ID) {
			authContext.setUserName(context.getUsername());
			authContext.setTenantId(context.getTenantId());
			authContext.setAuthorized(true);
			
		} else if (JWTToken != null){
			String jWTTokenString = getTokenStringJWTToken(JWTToken);
			try {
				authContext.setUserName(getUserNameFromJWTTokenString(jWTTokenString));
				authContext.setTenantId(getTenantIdFromJWTTokenString(jWTTokenString));
				authContext.setAuthorized(true);
			} catch (Exception e) {
				log.error("Error retrieving UserName and TenantID" , e);
				authContext.setAuthorized(false);
			}
		} else {
			authContext.setAuthorized(false);
		}
		return authContext;
	}
	
	private static String getTokenStringJWTToken(String JWTToken) {
		String token = JWTToken.substring(JWTToken.indexOf(".") + 1, JWTToken.lastIndexOf("."));
		
		//decode the jwt token and convert it to string
		byte[] jwtBytes = token.getBytes();
		byte[] decodedBytes = Base64.decodeBase64(jwtBytes);

        return new String(decodedBytes);
		
	}
	
	private static String getUserNameFromJWTTokenString(String JWTTokenString) throws Exception{
		String endUserClaimUri = "http://wso2.org/claims/enduser";
		int endUserIndex = JWTTokenString.indexOf(endUserClaimUri);
		JWTTokenString = JWTTokenString.substring(endUserIndex + endUserClaimUri.length() + 1);	
		String endUsername = JWTTokenString.substring(JWTTokenString.indexOf('"')+1); 
		endUsername = endUsername.substring(0, endUsername.indexOf('"'));
		
		return endUsername;
	}
	
	private static int getTenantIdFromJWTTokenString(String JWTTokenString) throws Exception{
		String endUserTenantIdClaimUri = "http://wso2.org/claims/enduserTenantId";
		int endUserIndex = JWTTokenString.indexOf(endUserTenantIdClaimUri);
		JWTTokenString = JWTTokenString.substring(endUserIndex + endUserTenantIdClaimUri.length() + 1);
		String endUserTenantId = JWTTokenString.substring(JWTTokenString.indexOf('"')+1);
        endUserTenantId = endUserTenantId.substring(0, endUserTenantId.indexOf('"'));
		
		return Integer.parseInt(endUserTenantId);
	}
	
}
