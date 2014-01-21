
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
package org.wso2.carbon.registry.social.impl.people.userprofile.model.impl;

import org.wso2.carbon.registry.social.api.people.userprofile.model.Url;

/**
 *  An implementation of the {@link org.wso2.carbon.registry.social.api.people.userprofile.model.Url}
 */
public class UrlImpl extends ListFieldImpl implements Url {
    private String linkText;

    public UrlImpl() {
    }

    public UrlImpl(String value, String linkText, String type) {
        super(type, value);
        this.linkText = linkText;
    }

    public String getLinkText() {
        return linkText;
    }

    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }
}
