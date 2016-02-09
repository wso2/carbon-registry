package org.wso2.carbon.registry.rest.api.swagger;

import com.wordnik.swagger.jaxrs.config.BeanConfig;
import org.wso2.carbon.utils.NetworkUtils;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "SwaggerJaxrsConfig", loadOnStartup = 1)
public class SwaggerJaxrsConfig extends BeanConfig {

    public SwaggerJaxrsConfig(){
        super();
    }

    public void setBasePath(String basePath)
    {
        // Hostname
        String hostName = "localhost";
        try {
            hostName = NetworkUtils.getMgtHostName();
        } catch (Exception ignored) {
        }

        super.setBasePath("https://" +
                          hostName + ":" + System.getProperty("mgt.transport.https.port") +
                          "/resource/1.0.0");
    }
}