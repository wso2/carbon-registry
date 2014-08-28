package org.wso2.carbon.registry.metadata.provider.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.Constants;
import org.wso2.carbon.registry.metadata.service.HTTPServiceV1;
import org.wso2.carbon.registry.metadata.version.HTTPServiceVersionV1;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Util {

    private static final Log log = LogFactory.getLog(Util.class);

    public static OMElement getBaseContentElement(){
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement root = factory.createOMElement(new QName(Constants.CONTENT_ROOT_NAME));
        OMElement properties = factory.createOMElement(new QName(Constants.CONTENT_PROPERTY_EL_ROOT_NAME));
        OMElement attributes = factory.createOMElement(new QName(Constants.CONTENT_ATTRIBUTE_EL_ROOT_NAME));
        root.addChild(properties);
        root.addChild(attributes);
        return root;
    }

    public static OMElement getAttributeRoot(){
        OMFactory factory = OMAbstractFactory.getOMFactory();
      return factory.createOMElement(new QName(Constants.CONTENT_ATTRIBUTE_EL_ROOT_NAME));
    }

    public static OMElement getPropertyRoot(){
        OMFactory factory = OMAbstractFactory.getOMFactory();
        return factory.createOMElement(new QName(Constants.CONTENT_PROPERTY_EL_ROOT_NAME));
    }

    public static OMElement getContentRoot(){
        OMFactory factory = OMAbstractFactory.getOMFactory();
        return factory.createOMElement(new QName(Constants.CONTENT_ROOT_NAME));
    }

    public static OMElement buildOMElement(byte[] content) throws RegistryException {
        XMLStreamReader parser;
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_COALESCING, new Boolean(true));
            parser = factory.createXMLStreamReader(new StringReader(
                    RegistryUtils.decodeBytes(content)));
        } catch (Exception e) {
            String msg = "Error in initializing the parser to build the OMElement.";
            log.error(msg, e);
            throw new RegistryException("",e);
        }

        //create the builder
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        //get the root element (in this case the envelope)

        return builder.getDocumentElement();
    }


    public static Map<String,String> getPropertyBag(OMElement root){
        Map<String,String> resultMap = new HashMap<String, String>();
        OMElement properties = root.getFirstChildWithName(new QName(Constants.CONTENT_PROPERTY_EL_ROOT_NAME));
        Iterator itr = properties.getChildren();
        while (itr.hasNext()){
            OMElement el = (OMElement) itr.next();
            String key = el.getLocalName();
            String value = el.getText();
            resultMap.put(key,value);
        }
        return resultMap;
    }

}
