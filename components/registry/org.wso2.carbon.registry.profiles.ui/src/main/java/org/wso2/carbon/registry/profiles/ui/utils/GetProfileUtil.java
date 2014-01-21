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
package org.wso2.carbon.registry.profiles.ui.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.common.ui.UIException;
import org.wso2.carbon.registry.profiles.stub.beans.xsd.ProfilesBean;
import org.wso2.carbon.registry.profiles.ui.clients.ProfilesAdminServiceClient;
import org.wso2.carbon.user.core.UserCoreConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility class used to obtain profile details of a user.
 */
public class GetProfileUtil {
    private static final Log log = LogFactory.getLog(GetProfileUtil.class);
    public static Map<String,Map<String,String>> getProfile(String path, ServletConfig config, HttpSession session) throws UIException{
        try{

            ProfilesAdminServiceClient client = new ProfilesAdminServiceClient(config,session);
            ProfilesBean bean = client.getUserProfile(path);
            if (bean == null || bean.getMainDataString() == null) {
                log.error("The profile was not found for the path " + path);
                return null;
            }
            Map <String,Map<String,String>> data = new HashMap();
            String maindatastring = bean.getMainDataString();
            String []profile = maindatastring.split("#");
            Map<String,String> pair = null;
            for(int i=0;i< profile.length;i++){
                if(!profile[i].equals("")){
                    String[]inter = profile[i].split("%");
                    if (inter == null || inter.length <= 1) {
                        continue;
                    }
                    String[] inter1 = inter[1].split(";"); //////Always Attribute name value pairs comes after second
                    pair = new HashMap();
                    for(int j=0;j<inter1.length;j=j+2){
                        pair.put(inter1[j],inter1[j+1]);
                    }
                    data.put(inter[0],pair);
                }
            }
            return data;

        }catch(Exception e){
            log.error(e.getMessage());
        }
        return null;
    }                                                             
    public static String getprofilename(Map<String,Map<String,String>> data){
        Set<String> profiles = data.keySet();
        if (data.get(UserCoreConstants.DEFAULT_PROFILE) != null) {
            return UserCoreConstants.DEFAULT_PROFILE;
        } else {
            for (String profile : profiles) {
                if (profile != null) {
                    return profile;
                }
            }
        }
        return null;
    }
    public static Map<String,String> getprofiledatatoshow(Map<String,Map<String,String>> data,String profilename){
        return data.get(profilename);
    }
}
