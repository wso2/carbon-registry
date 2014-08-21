package org.wso2.carbon.metadata.test;


import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.metadata.service.HTTPServiceV1;
import org.wso2.carbon.registry.metadata.service.ServiceV1;
import org.wso2.carbon.registry.metadata.version.HTTPServiceVersionV1;

public class JAXRSV1Client {

    public static void main(String[] args) throws RegistryException {

        HTTPServiceV1 http1 = new HTTPServiceV1("foo");
        http1.setOwner("serviceOwner");
        http1.setProperty("createdDate","12-12-2012");
        HTTPServiceV1.add(http1);

        HTTPServiceV1[] services = HTTPServiceV1.getAll();
        for(HTTPServiceV1 ht:services){
            ht.setProperty("newProp","newValue");
            HTTPServiceV1.update(ht);
        }

        HTTPServiceVersionV1 httpV1 = http1.newVersion("1.0.0");
        httpV1.setEndpointUrl("http://test.rest/stockquote");
        httpV1.setProperty("isSecured","true");
        HTTPServiceVersionV1.add(httpV1);

        HTTPServiceVersionV1 v1 = HTTPServiceVersionV1.get(httpV1.getUUID());
        HTTPServiceV1.delete(v1.getUUID());

    }
}
