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
package org.wso2.carbon.registry.ws.api;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class WSTaggedResourcePath {
	/**
	 * Resource path for which the tag counts are associated.
	 */
	private String resourcePath;

	/**
	 * Tag counts in the form tag -> count
	 * Where tag is a string and count is a long.
	 */

	/**
	 * This is deprecated. Included only for backward compatibility.
	 */
	private WSMap[] map;
	private long tagCount;

	public WSTaggedResourcePath() {
		// TODO Auto-generated constructor stub
	}

	public WSTaggedResourcePath(long tagcount,WSMap[] tagcounts,String resourcepath){
		resourcePath = resourcepath;
		map = tagcounts;
		tagCount = tagcount;
	}
	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public long getTagCount() {
		return tagCount;
	}

	public void setTagCount(long tagCount) {
		this.tagCount = tagCount;
	}

	public WSMap[] getTagCounts() {
		return map;
	}

	public void setTagCounts(WSMap[] tagCounts) {
		this.map = tagCounts;
	}

}