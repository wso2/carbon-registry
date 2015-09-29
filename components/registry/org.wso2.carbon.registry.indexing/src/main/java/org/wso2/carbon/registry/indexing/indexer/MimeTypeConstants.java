/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.indexing.indexer;

// This class contains all mime types supported by the content search indexing component
// Please add a constant to this class when adding support for indexing additional mime types
public class MimeTypeConstants {
	
	public static final String PLAINTEXT = "text/plain";
	
	public static final String PDF = "application/pdf";
	
	public static final String XML = "application/xml";
	
	public static final String MSWORD = "application/msword";
	
	public static final String MSEXCEL = "application/vnd.ms-excel";
	
	public static final String MSPOWERPOINT = "application/vnd.ms-powerpoint";
	
}
