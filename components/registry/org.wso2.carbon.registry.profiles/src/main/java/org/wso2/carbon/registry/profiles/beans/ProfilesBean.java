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
package org.wso2.carbon.registry.profiles.beans;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class ProfilesBean {
    
    private String username;

    private String mainDataString;

    private String[] profileNames;

   public void setMainDataString(String data){
     this.mainDataString = data;
   }
   public String getMainDataString(){
       return this.mainDataString;
   }
   public void setProfileNames(String [] profileNames){
       this.profileNames = profileNames;
   }

   public String[] getProfileNames(){
       return this.profileNames;
   }
   public void setUserName(String username){
       this.username = username;
   }
   public String getUserName(){
       return this.username;
   }
}
