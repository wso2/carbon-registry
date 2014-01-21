
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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class provides getters and setters to model the rating of a resource
 */
@XmlRootElement(name = "RatingModel")
public class RatingModel {

	private int myRating;
	private float average;
	
	public RatingModel(int yourRating,float averageRating) {
		this.myRating = yourRating;
		this.average = averageRating;
	}

    public RatingModel() {
    }

    public void setAverage(float averageRating) {
		this.average = averageRating;
	}
		
	public float getAverage() {
		return average;
	}
	
	public int getMyRating() {
		return myRating;
	}

	public void setMyRating(int yourRating) {
		this.myRating = yourRating;
	}
	
}
