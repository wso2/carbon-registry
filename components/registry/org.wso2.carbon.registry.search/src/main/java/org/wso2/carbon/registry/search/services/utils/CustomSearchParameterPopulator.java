package org.wso2.carbon.registry.search.services.utils;

import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.search.beans.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.beans.MediaTypeValueList;

import javax.print.attribute.standard.Media;
import java.util.*;

/**
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class CustomSearchParameterPopulator {


    private static Map<String, String[]> mediatypeParameterList = new HashMap<String, String[]>();
    private static String parameterListResourcePath = "/_system/config/repository/components/org.wso2.carbon.governance/media-types/search/";

//    private static void setMediatypeParameterList(UserRegistry registry, String mediaType) throws RegistryException {
//
//
//        Resource resource = registry.get(parameterListResourcePath + mediaType.replace("/", "-"));
//        Properties property = resource.getProperties();
//        List<String> returnList = new ArrayList<String>();
//
//        for (Map.Entry<Object, Object> entry : property.entrySet()) {
//            if (((String) entry.getValue()).equals("true")) {
//                returnList.add((String) entry.getKey());
//            }
//        }
//
//    }

    public static MediaTypeValueList getMediaTypeParameterValues(UserRegistry registry, String mediaType) throws RegistryException {
//        String[] strings = {"Name", "1", "2", "3"};
//        String[] strings2 = {"4", "5", "7", "6"};
//        mediatypeParameterList.put("text", strings);
//        mediatypeParameterList.put("image",strings2);
        MediaTypeValueList bean = new MediaTypeValueList();
        try {
            List<String> returnList = new ArrayList<String>();

            if (!registry.resourceExists(parameterListResourcePath)) {
                Map<String, String[]> types = new HashMap<String, String[]>();
                types.put("application/wsdl+xml", new String[]{"WSDL Validation",
                                                                "WSI Validation"});
                types.put("application/x-xsd+xml", new String[]{"Schema Validation",
                                                                "targetNamespace"});
                for (Map.Entry<String, String[]> e : types.entrySet()) {
                    Resource resource = registry.newResource();
                    for (String value : e.getValue()) {
                        resource.setProperty(value, "true");
                    }
                    registry.put(parameterListResourcePath + e.getKey().replace("/", "-").
                            replace(".","-").replace("+","-"), resource);
                }
            }

            if (!("".equals(mediaType))) {
                Resource resource = registry.get(parameterListResourcePath + mediaType.replace("/", "-").replace(".", "-").
                        replace("+", "-"));
                Properties property = resource.getProperties();

                for (Map.Entry<Object, Object> entry : property.entrySet()) {
                    if (((ArrayList) entry.getValue()).contains("true")) {
                        returnList.add(entry.getKey().toString());
                    }
                }
            }
            bean.setMediaType(mediaType);

            if (returnList.size() > 0) {
                String[] tempString = new String[returnList.size()];
                tempString = returnList.toArray(tempString);
                bean.setSearchFields(tempString);
            }

        } catch (RegistryException e) {
            throw new RegistryException("No parameters are set for this media type", e);

        }

        return bean;
    }
}
