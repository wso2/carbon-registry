/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.jcr.observation;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import java.util.HashMap;
import java.util.Map;


public class RegistryEvent implements Event {

    private String userdata = "";
    private int type = 0;
    private String path = "";
    private Map info = new HashMap();
    private long createdTime = 0;
    private String userId = "";

    public RegistryEvent(String userData, int type, String path, String userId, String identifier, Map info, long date) {

        this.userdata = userData;
        this.type = type;
        this.path = path;
        this.info = info;
        this.createdTime = date;
        this.userId = userId;
    }

    public int getType() {

        return type;
    }

    public String getPath() throws RepositoryException {

        return path;

    }

    public String getUserID() {

        return userId;

    }

    public String getIdentifier() throws RepositoryException {

        return userId;
    }

    public Map getInfo() throws RepositoryException {

        return info;
    }

    public String getUserData() throws RepositoryException {

        return userdata;
    }

    public long getDate() throws RepositoryException {

        return createdTime;
    }

}
