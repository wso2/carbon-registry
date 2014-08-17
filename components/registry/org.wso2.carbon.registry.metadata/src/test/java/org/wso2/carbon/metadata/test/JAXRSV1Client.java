package org.wso2.carbon.metadata.test;


import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.manager.BaseMetadataManager;
import org.wso2.carbon.registry.metadata.manager.MetadataManager;
import org.wso2.carbon.registry.metadata.service.HTTPServiceV1;
import org.wso2.carbon.registry.metadata.version.ServiceVersionV1;
import org.wso2.carbon.registry.metadata.version.VersionV1;

public class JAXRSV1Client {

    public static void main(String[] args) {

        MetadataManager httpServiceManager = new BaseMetadataManager("vnd.wso2.service/http+xml;version=1");
        HTTPServiceV1 httpService = (HTTPServiceV1) httpServiceManager.newInstance("foo");
        httpService.setOwner("foobar");
        httpService.setProperty("isSecured","true");
        httpServiceManager.add(httpService);
        Base resultService = httpServiceManager.getMetadata(httpService.getUUID());
        System.out.println(resultService.getUUID());

        ServiceVersionV1 httpV1 = (ServiceVersionV1) resultService.newVersion("1.0.0");
        httpV1.setEndpointUrl("http://com.rest/stockquote");
        httpServiceManager.add(httpV1);

    }
}
