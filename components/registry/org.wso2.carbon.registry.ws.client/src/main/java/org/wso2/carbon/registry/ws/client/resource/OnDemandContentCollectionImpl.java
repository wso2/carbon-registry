package org.wso2.carbon.registry.ws.client.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class OnDemandContentCollectionImpl extends CollectionImpl {

    private String pathWithVersion;

	public String getPathWithVersion() {
		return pathWithVersion;
	}

	public void setPathWithVersion(String pathWithVersion) {
		this.pathWithVersion = pathWithVersion;
	}

	public OnDemandContentCollectionImpl(WSRegistryServiceClient client) {
		this.client = client;
	}
	
	public WSRegistryServiceClient getClient() {
    	return client;
    }

	public void setClient(WSRegistryServiceClient client) {
    	this.client = client;
    }

	private WSRegistryServiceClient client;

    @Override
    public int getChildCount() throws RegistryException {
        Object content = getContent();
        if (content == null) {
            return 0;
        } else {
            return super.getChildCount();
        }
    }

    @Override
    public String[] getChildren(int start, int pageLen) throws RegistryException {
        Object content = getContent();
        if (content == null) {
            return new String[0];
        } else {
            return super.getChildren(start, pageLen);
        }
    }

    @Override
    public String[] getChildren() throws RegistryException {
        Object content = getContent();
        if (content == null) {
            return new String[0];
        } else {
            return super.getChildren();
        }
    }

    @Override
    public Object getContent() throws RegistryException {
		try {
			if (content == null) { // fetch data only if it hasn't been fetched previously
                if (getPath() != null) {
                    content = getClient().getCollectionContent(getPathWithVersion());
                }
            }
			return content;
        } catch (Exception e) {
	        throw new RegistryException("Failed to get collection content", e);
        }
    }

}
