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
package org.wso2.carbon.registry.extensions.handlers;

import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;

import java.util.Date;

/**
 * This handler implements Resource Retention Locking. It checks retention lock conditions before
 * performing the operation. If resource is locked by another user and operation cannot be
 * performed, RegistryException will be thrown with an appropriate message.
 */
public class RetentionHandler extends Handler {

    public void put(RequestContext requestContext) throws RegistryException {
        checkWriteLock(requestContext.getResourcePath().getPath(), requestContext);
    }

    public void addAssociation(RequestContext requestContext) throws RegistryException {
        //verifyDependency(requestContext);
    }

    public void removeAssociation(RequestContext requestContext)
            throws RegistryException {
        //verifyDependency(requestContext);
    }
      // Adding this fix to solve REGISTRY-888
//    private void verifyDependency(RequestContext requestContext) throws RegistryException {
//        if ("depends".equals(requestContext.getAssociationType())) {
//            checkWriteLock(requestContext.getSourcePath(), requestContext);
//        }
//    }

    public void restore(RequestContext requestContext) throws RegistryException {
        checkWriteLock(requestContext.getResourcePath().getPath(), requestContext);
    }

    public void restoreVersion(RequestContext requestContext) throws RegistryException {
        checkWriteLock(new ResourcePath(requestContext.getVersionPath()).getPath(), requestContext);
    }

    public void delete(RequestContext requestContext) throws RegistryException {
        checkDeleteLock(requestContext.getResourcePath().getPath(), requestContext);
    }

    public void importResource(RequestContext requestContext) throws RegistryException {
        checkWriteLock(requestContext.getResourcePath().getPath(), requestContext);
    }

    public String rename(RequestContext requestContext) throws RegistryException {
        return move(requestContext);
    }

    public String move(RequestContext requestContext) throws RegistryException {
        checkDeleteLock(requestContext.getSourcePath(), requestContext);
        checkWriteLock(requestContext.getTargetPath(), requestContext);
        return requestContext.getTargetPath();
    }

    public String copy(RequestContext requestContext) throws RegistryException {
        checkWriteLock(requestContext.getTargetPath(), requestContext);
        return requestContext.getTargetPath();
    }

    private void checkWriteLock(String path, RequestContext requestContext)
                        throws RegistryException {
        String owner;
        if ((owner = checkRetentionLock(requestContext.getRegistry(), path,
                CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME)) != null) {
            requestContext.setProcessingComplete(true);
            throw new RegistryException("Resource Retention does not allow this operation. " +
                    "Resource at path " + path + " is write locked by " + owner);
        }
    }

    private void checkDeleteLock(String path, RequestContext requestContext)
                        throws RegistryException {
        String owner;
        if ((owner = checkRetentionLock(requestContext.getRegistry(), path,
                CommonConstants.RETENTION_DELETE_LOCKED_PROP_NAME)) != null) {
            requestContext.setProcessingComplete(true);
            throw new RegistryException("Resource Retention does not allow this operation. " +
                    "Resource at path: " + path + " is delete locked by " + owner);
        }
    }

    private String checkRetentionLock(Registry registry, String path, String retentionProperty)
            throws RegistryException {
        if (!registry.resourceExists(path)) {
            return null;
        }
        Resource existingResource = registry.get(path);
        if (CurrentSession.getUser() != null && !CurrentSession.getUser().equals(
                existingResource.getProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME))) {
            if (Boolean.parseBoolean(existingResource.getProperty(retentionProperty))) {
                Date fromDate = CommonUtil.computeDate(existingResource.getProperty(
                        CommonConstants.RETENTION_FROM_DATE_PROP_NAME));
                Date toDate = CommonUtil.computeDate(existingResource.getProperty(
                        CommonConstants.RETENTION_TO_DATE_PROP_NAME));
                Date now = new Date();
                if (now.compareTo(fromDate) > 0 && now.compareTo(toDate) < 0) {
                    //Resource is locked, return the owner's username
                    return existingResource.getProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME);
                }
            }
        }
        return null;
    }
}
