package org.wso2.carbon.registry.extensions.handlers;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

/**
 * ArtifactCleanUpHandler can be used to clean up Artifact and their associated resources.
 * Can be customized according to requirement.
 *
 */
public class ArtifactCleanUpHandler extends Handler {
	
	private static final Log log = LogFactory.getLog(ArtifactCleanUpHandler.class);

	@Override
	public void delete(RequestContext requestContext) throws RegistryException {
		if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {

        
	        Registry registry = requestContext.getRegistry();
	        ResourcePath resourcePath = requestContext.getResourcePath();
	        if (resourcePath == null) {
	            throw new RegistryException("The resource path is not available.");
	        }
	        Set<String> associatedRes = new HashSet<String>();
	        // Collect associated path and remove the association
	        Association[] associations = registry.getAllAssociations(resourcePath.getPath());
	    	for (Association association : associations) {
	    		associatedRes.add(association.getDestinationPath());
	    		registry.removeAssociation(resourcePath.getPath(), association.getDestinationPath(), association.getAssociationType());
			}    	
	    	/*Association[] associationsDepends = registry.getAssociations(resourcePath.getPath(), CommonConstants.DEPENDS);
			for (Association association : associationsDepends) {
				if (!associatedRes.contains(association.getDestinationPath())) {
					associatedRes.add(association.getDestinationPath());
				}            			
			}
			Association[] associationsUsed = registry.getAssociations(resourcePath.getPath(), CommonConstants.USED_BY);
			for (Association association : associationsUsed) {
				if (!associatedRes.contains(association.getDestinationPath())) {
					associatedRes.add(association.getDestinationPath());
				}            			
			}*/
	    	
	    	// Remove all associated resources
	    	Set<String> allAssociatedRes = new HashSet<String>(associatedRes);
	    	for (String path : associatedRes) {
	    		Association[] allAssociations = registry.getAllAssociations(path);
		    	for (Association association : allAssociations) {
		    		allAssociatedRes.add(association.getDestinationPath());
			    	registry.removeAssociation(path, association.getDestinationPath(), association.getAssociationType());	
				}
	    	}	    	 
	    	for (String path : allAssociatedRes) {
	    		try{
	    			registry.delete(path);
	    		} catch (RegistryException ex){
	    			// There can be scenarios, resource may associated with other resources
	    			log.error("Unable to delete associated resource", ex);
	    		}	    		
			}    	

		} finally {
			CommonUtil.releaseUpdateLock();
		}
	}
	
	

}
