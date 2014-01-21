/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.social.impl.test.people.userprofile;

import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.profile.ProfileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ClaimTestUtil {

    public static final String CLAIM_URI1 = "http://wso2.org/claims/aboutme";
    public static final String CLAIM_URI2 = "http://wso2.org/claims/givenname";
    public static final String CLAIM_URI3 = "http://wso2.org/givenname3";
        public static final String CLAIM_URI4 = "http://wso2.org/claims/emailaddress";
         public static final String CLAIM_URI5 = "http://wso2.org/claims/telephone";
    public static final String HOME_PROFILE_NAME = "HomeProfile";

    public static Map<String, ClaimMapping> getClaimTestData() {
        Map<String, ClaimMapping> claims = new HashMap<String, ClaimMapping>();
        Claim claim1 = new Claim();
        claim1.setClaimUri(CLAIM_URI1);
        claim1.setDescription("About Me");
        claim1.setDialectURI("http://wso2.org/claims");
        claim1.setDisplayTag("About Me");
        //claim1.setRegEx("ty&*RegEx");
        claim1.setRequired(true);
        claim1.setSupportedByDefault(true);
        ClaimMapping cm1 = new ClaimMapping();
        cm1.setClaim(claim1);
        cm1.setMappedAttribute("attr1");
        claims.put("http://wso2.org/claims/aboutme", cm1);

        Claim claim2 = new Claim();
        claim2.setClaimUri(CLAIM_URI2);
        claim2.setDescription("Given Name");
        claim2.setDialectURI("http://wso2.org/claims");
        claim2.setDisplayTag("Given Name");
        claim2.setRegEx("ty&*RegEx2");
        claim2.setRequired(true);
        claim2.setSupportedByDefault(true);
        ClaimMapping cm2 = new ClaimMapping();
        cm2.setClaim(claim2);
        cm2.setMappedAttribute("attr2");
        claims.put("http://wso2.org/claims/givenname", cm2);

        Claim claim4 = new Claim();
        claim4.setClaimUri(CLAIM_URI4);
        claim4.setDescription("Email");
        claim4.setDialectURI("http://wso2.org/claims");
        claim4.setDisplayTag("Email");
        //claim4.setRegEx("ty&*RegEx2");
        claim4.setRequired(true);
        claim4.setSupportedByDefault(true);
        ClaimMapping cm4 = new ClaimMapping();
        cm4.setClaim(claim4);
        cm4.setMappedAttribute("attr4");
        claims.put("http://wso2.org/claims/emailaddress", cm4);

        Claim claim5 = new Claim();
        claim5.setClaimUri(CLAIM_URI5);
        claim5.setDescription("Phone Number");
        claim5.setDialectURI("http://wso2.org/claims");
        claim5.setDisplayTag("Phone Number");
        //claim5.setRegEx("ty&*RegEx2");
        claim5.setRequired(true);
        claim5.setSupportedByDefault(true);
        ClaimMapping cm5 = new ClaimMapping();
        cm5.setClaim(claim5);
        cm5.setMappedAttribute("attr5");
        claims.put("http://wso2.org/claims/telephone", cm5);

        Claim claim3 = new Claim();
        claim3.setClaimUri(CLAIM_URI3);
        claim3.setDescription("The description is nutts3");
        claim3.setDialectURI("http://wso2.org/");
        claim3.setDisplayTag("Given Name3");
        claim3.setRegEx("ty&*RegEx3");
        claim3.setRequired(true);
        claim3.setSupportedByDefault(true);
        ClaimMapping cm3 = new ClaimMapping();
        cm3.setClaim(claim3);
        cm3.setMappedAttribute("attr3");
        claims.put("http://wso2.org/givenname3", cm3);

        return claims;

    }

    public static Map<String, ProfileConfiguration> getProfileTestData() {
        Map<String, ProfileConfiguration> map = new HashMap<String, ProfileConfiguration>();
        ProfileConfiguration profConfig = new ProfileConfiguration();
        profConfig.setProfileName(HOME_PROFILE_NAME);
        profConfig.addHiddenClaim(CLAIM_URI1);
        profConfig.addInheritedClaim(CLAIM_URI2);
        profConfig.setDialectName("http://wso2.org/claims");
        map.put(HOME_PROFILE_NAME, profConfig);
        return map;
    }

}