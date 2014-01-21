package org.wso2.carbon.registry.extensions.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.UUID;

public class PeopleMediaTypeHandler extends Handler {

    private static final Log log = LogFactory.getLog(PeopleMediaTypeHandler.class);

    public void put(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            Registry registry = requestContext.getRegistry();
            Resource resource = requestContext.getResource();
            if (resource == null) {
                throw new RegistryException("The resource is not available.");
            }
            String originalPath = requestContext.getResourcePath().getPath();
            String resourceName = RegistryUtils.getResourceName(originalPath);

            OMElement artifactInfoElement;
            Object resourceContent = resource.getContent();
            String artifactInfo;
            if (resourceContent instanceof String) {
                artifactInfo = (String) resourceContent;
            } else {
                artifactInfo = RegistryUtils.decodeBytes((byte[]) resourceContent);
            }
            try {
                XMLStreamReader reader = XMLInputFactory.newInstance().
                        createXMLStreamReader(new StringReader(artifactInfo));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                artifactInfoElement = builder.getDocumentElement();
            } catch (Exception e) {
                String msg = "Error in parsing the content of the people artifact. " +
                        "The requested path to store the artifact: " + originalPath + ".";
                log.error(msg);
                throw new RegistryException(msg, e);
            }
            // derive the path where the people artifact should be saved.
            String personName = CommonUtil.getServiceName(artifactInfoElement);
            String peopleGroup = CommonUtil.getPeopleGroup(artifactInfoElement);
            String peopleType =  CommonUtil.getPeopleType(artifactInfoElement);

            //TODO: Rename RegistryConstants.GOVERNANCE_CONSUMER_PATH constant
            String artifactStorePath = RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                            RegistryConstants.GOVERNANCE_PEOPLE_PATH + "/" +
                            peopleGroup + "/" + peopleType + "/" + personName);
            // saving the artifact id.
            String peopleArtifactId = resource.getUUID();
            if (peopleArtifactId == null) {
                // generate a consumer id
                peopleArtifactId = UUID.randomUUID().toString();
                resource.setUUID(peopleArtifactId);
            }
            if (registry.resourceExists(artifactStorePath)) {
                Resource oldResource = registry.get(artifactStorePath);
                String oldContent = RegistryUtils.decodeBytes((byte[]) oldResource.getContent());
                if (artifactInfo.equals(oldContent)) {
                    /* if user is not changing anything in the people artifact we skip
                    the processing done in this handler */
                    return;
                }
            }
//            CommonUtil.addGovernanceArtifactEntryWithAbsoluteValues(
//                    CommonUtil.getUnchrootedSystemRegistry(requestContext),
//                    peopleArtifactId, artifactStorePath);

            resource.setContent(RegistryUtils.encodeString(artifactInfoElement.toString()));
            // updating the wsdl url
            ((ResourceImpl) resource).prepareContentForPut();
            registry.put(artifactStorePath, resource);

            String symLinkLocation = RegistryUtils.getAbsolutePath(requestContext.getRegistryContext(),
                    requestContext.getResource().getProperty(RegistryConstants.SYMLINK_PROPERTY_NAME));

            if (!artifactStorePath.equals(originalPath)) {
                // we are creating a sym link from service path to original service path.
                Resource serviceResource = requestContext.getRegistry().get(
                        RegistryUtils.getParentPath(originalPath));
                String isLink = serviceResource.getProperty("registry.link");
                String mountPoint = serviceResource.getProperty("registry.mountpoint");
                String targetPoint = serviceResource.getProperty("registry.targetpoint");
                String actualPath = serviceResource.getProperty("registry.actualpath");
                if (isLink != null && mountPoint != null && targetPoint != null) {
                    symLinkLocation = actualPath + RegistryConstants.PATH_SEPARATOR;
                }
                if (symLinkLocation != null) {
                    registry.createLink(symLinkLocation + resourceName, artifactStorePath);
                }
            }
            // in this flow the resource is already added. marking the process completed..
            requestContext.setProcessingComplete(true);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

}
