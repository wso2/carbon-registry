package org.wso2.carbon.registry.indexing.utils;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URL;
import java.util.Properties;

public class IndexingUtils {
	
	@Deprecated
	public static String getSolrUrl() throws IOException, RegistryException {

		String solrServerUrl = null;
		String carbonHome = System.getProperty("carbon.home");
		File confFile = new File(carbonHome,"repository" + File.separator + "conf" + File.separator + "solr.conf");
		if(confFile.exists()){
			FileInputStream inputStream =  new FileInputStream(confFile);
            try {
			    Properties properties = new Properties();
			    properties.load(inputStream);

			    solrServerUrl = properties.getProperty("solr.endpoint");
            } finally {
                inputStream.close();
            }
		} else {
			solrServerUrl = "https://localhost:9443/registry/resourcesolr/solr";

			boolean confCreated = confFile.createNewFile();
			if (confCreated) {
				FileOutputStream outputStream = new FileOutputStream(confFile);
				//outputStream.write("solr.endpoint=http://localhost:8080/solr\n\n".getBytes());
                try {
				    outputStream.write(RegistryUtils.encodeString("solr.endpoint=" + solrServerUrl + "\n\n"));
                } finally {
				    outputStream.close();
                }
			}
		}
		return solrServerUrl;
	}
	
//	public static String getSolrCoreUrl(int tenantId) throws IOException {
//		String baseURL = IndexingUtils.getSolrUrl();
//		return baseURL + "/admin/cores/" + tenantId;
//		
//	}

    public static boolean isAuthorized(UserRegistry registry, String resourcePath, String action)
                            throws RegistryException{
        UserRealm userRealm = registry.getUserRealm();
        String userName = registry.getUserName();
        try {
            if (!userRealm.getAuthorizationManager().isUserAuthorized(userName,
                    resourcePath, action)) {
                return false;
            }
        } catch (UserStoreException e) {
            throw new RegistryException("Error at Authorizing " + resourcePath
                    + " with user " + userName + ":" + e.getMessage(), e);
        }
        return true;
    }

    public static byte[] readBytesFromInputSteam(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int read = in.read(buf);

		while (read != -1) {
			out.write(buf, 0, read);
			read = in.read(buf);
		}
		return out.toByteArray();
	}

	public static byte[] getByteContent(Resource resource, String sourceURL)
	                        throws RegistryException {
		try {
			InputStream is = null;
			if (sourceURL != null) {
				is = new URL(sourceURL).openStream();
			} else {
                Object content = resource.getContent();
                if( null == content) {
                    //returning an empty array, rather than 'null'.
                    return new byte[0];
                }
                is = resource.getContentStream();
				if (is == null) {
                    if (content instanceof byte[]) {
                        return (byte[]) content;
                    } else if (content instanceof String) {
                        return RegistryUtils.encodeString((String) content);
                    } else {
                        throw new RegistryException("Unknown type found as content " + content);
                    }
                }
			}
			return readBytesFromInputSteam(is);
		} catch (IOException e) {
			throw new RegistryException("Error at indexing", e);
		}
	}

    public static String getLoggedInUserName(){
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
    }
}
