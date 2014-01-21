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
package org.wso2.carbon.registry.social.api.utils;



import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Utility class for OpenSocial enums.
 */
public final class EnumUtil {

    /**
     * This is a utility class and can't be constructed.
     */
    private EnumUtil() {

    }

    /**
     * @param vals array of enums
     * @return a set of the names for a list of Enum values defined by toString
     */
  
    public static Set<String> getEnumStrings(java.lang.Enum<?>... vals) {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (java.lang.Enum<?> v : vals) {
            builder.add(v.toString());
        }
        Set<String> result = builder.build();

        if (result.size() != vals.length) {
            throw new IllegalArgumentException("Enum names are not disjoint set");
        }

        return result;
    }
}
