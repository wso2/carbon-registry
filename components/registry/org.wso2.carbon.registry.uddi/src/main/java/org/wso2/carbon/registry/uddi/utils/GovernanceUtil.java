package org.wso2.carbon.registry.uddi.utils;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.service.RegistryService;

public class GovernanceUtil {

    private static final Log log = LogFactory.getLog(GovernanceUtil.class);
       private static RegistryService registryService;
       private static final String SERVICE_VERSION = "1.0.0";
       private static final String SERVICE_NAMEAPSCE = "http://uddi.com/services";


       public static RegistryService getRegistryService() {
           registryService = (RegistryService) PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(RegistryService.class);
           return registryService;
       }

       public static void acquireUDDIExternalInvokeLock() {
           CommonConstants.isExternalUDDIInvoke.set(true);
       }

       public static void releaseUDDIExternalInvokeLock() {
           CommonConstants.isExternalUDDIInvoke.set(false);
       }


}
