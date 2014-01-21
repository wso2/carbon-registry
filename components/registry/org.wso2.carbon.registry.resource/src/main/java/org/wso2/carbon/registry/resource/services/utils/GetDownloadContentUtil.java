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

package org.wso2.carbon.registry.resource.services.utils;

import org.wso2.carbon.registry.resource.beans.ContentDownloadBean;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.sql.SQLException;
import java.util.Calendar;

public class GetDownloadContentUtil {

    private static final Log log = LogFactory.getLog(GetDownloadContentUtil.class);

    public static ContentDownloadBean getContentDownloadBean(String path, UserRegistry userRegistry) throws RegistryException {

        try {
            ContentDownloadBean bean = new ContentDownloadBean();

            Resource resource = userRegistry.get(path);

            if (resource instanceof Collection) {
                String msg = "Could not get the resource content. Path " + path + " refers to a collection.";
                log.error(msg);
                throw new RegistryException(msg);
            }

            bean.setMediatype(resource.getMediaType());
            Calendar lastModified = Calendar.getInstance();
            lastModified.setTime(resource.getLastModified());
            bean.setLastUpdatedTime(lastModified);
            bean.setResourceName(RegistryUtils.getResourceName(path));
            if (resource.getContent() != null) {
                DataSource contentSource = new InputStreamBasedDataSource(new ResourceContentInputStream(resource));
                DataHandler content = new DataHandler(contentSource);
                bean.setContent(content);
            } else {
                bean.setContent(null);
            }
            return bean;

        } catch (RegistryException e) {
            String msg = "Failed to get content of the resource for downloading. " +
                    ((e.getCause() instanceof SQLException) ?
                    "" : e.getMessage());
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }
}
