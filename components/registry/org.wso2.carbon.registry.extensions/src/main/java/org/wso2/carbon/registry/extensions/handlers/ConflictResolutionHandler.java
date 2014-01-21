package org.wso2.carbon.registry.extensions.handlers;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.extensions.handlers.utils.ConflictResolutionReader;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import java.io.Reader;

public class ConflictResolutionHandler extends Handler {
    @Override
    public void restore(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isRestoringLockAvailable()) {
            return;
        }
        CommonUtil.acquireRestoringLock();
        try {
            Registry registry = requestContext.getRegistry();
            Reader reader = requestContext.getDumpingReader();
            String path = requestContext.getResourcePath().getPath();

            ConflictResolutionReader conflictResolutionReader = new ConflictResolutionReader(reader, path, registry);

            registry.restore(path,conflictResolutionReader);
            requestContext.setProcessingComplete(true);
        } finally {
            CommonUtil.releaseRestoringLock();
        }
    }
}
