
/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.rest.api.model;

import org.wso2.carbon.registry.core.Association;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class provides the getters and setters to model the association to a resource
 */
@XmlRootElement(name = "AssociationModel")
public class AssociationModel {

	private String type;
	private String target;
	private String source;
	
	public AssociationModel(Association association) {
		this.type = association.getAssociationType();
		this.target = association.getDestinationPath();
		this.source = association.getSourcePath();
	}

    public AssociationModel() {
    }

    public String getType() {
		return type;
	}
	
	public String getTarget() {
		return target;
	}
	
	public String getSource() {
		return source;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setTarget(String target) {
		this.target = target;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
}
