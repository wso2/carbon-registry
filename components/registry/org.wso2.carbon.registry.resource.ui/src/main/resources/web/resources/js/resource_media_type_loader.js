/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
/*load media types function is defined in the resource_util.js
  since resource_util.js is used even the user is not login, by placing
  the following function inside the resource_util.js gives
  user not authorized errors. so we are separating the media type loader
  to a separate js file.*/
sessionAwareFunction(function() {
    loadMediaTypes();
}, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]);
