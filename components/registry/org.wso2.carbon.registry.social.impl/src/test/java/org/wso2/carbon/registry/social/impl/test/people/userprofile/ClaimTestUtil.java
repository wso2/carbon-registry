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
    public static final String CLAIM_URI6 = "http://wso2.org/claims/lastname";
    public static final String CLAIM_URI7 = "http://wso2.org/claims/nickname";
    public static final String CLAIM_URI8 = "http://wso2.org/claims/displayname";
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

        Claim claim6 = new Claim();
        claim6.setClaimUri(CLAIM_URI6);
        claim6.setDescription("This is last name");
        claim6.setDialectURI("http://wso2.org/");
        claim6.setDisplayTag("Last Name");
        claim6.setRegEx("ty&*RegEx2");
        claim6.setRequired(true);
        claim6.setSupportedByDefault(true);
        ClaimMapping cm6 = new ClaimMapping();
        cm6.setClaim(claim6);
        cm6.setMappedAttribute("attr6");
        claims.put(CLAIM_URI6, cm6);

        Claim claim7 = new Claim();
        claim7.setClaimUri(CLAIM_URI7);
        claim7.setDescription("This is nick name");
        claim7.setDialectURI("http://wso2.org/");
        claim7.setDisplayTag("Nick Name");
        claim7.setRegEx("ty&*RegEx2");
        claim7.setRequired(true);
        claim7.setSupportedByDefault(true);
        ClaimMapping cm7 = new ClaimMapping();
        cm7.setClaim(claim7);
        cm7.setMappedAttribute("attr7");
        claims.put(CLAIM_URI7, cm7);

        Claim claim8 = new Claim();
        claim8.setClaimUri(CLAIM_URI8);
        claim8.setDescription("This is display name");
        claim8.setDialectURI("http://wso2.org/");
        claim8.setDisplayTag("Display Name");
        claim8.setRegEx("ty&*RegEx2");
        claim8.setRequired(true);
        claim8.setSupportedByDefault(true);
        ClaimMapping cm8 = new ClaimMapping();
        cm8.setClaim(claim8);
        cm8.setMappedAttribute("attr8");
        claims.put(CLAIM_URI8, cm8);

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