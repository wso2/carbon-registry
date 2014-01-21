package org.wso2.carbon.registry.jcr;

import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.app.RemoteRegistryService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.InMemoryEmbeddedRegistryService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.common.DefaultRealmService;
import org.wso2.carbon.user.core.service.RealmService;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import java.util.Map;


public class RegistryRepositoryFactory implements RepositoryFactory {
    private Repository regRepo; //  for remote registry creation only
    private RealmService realmService = null;  // for embeded registry creation only

    public Repository getRepository(Map map) throws RepositoryException {
        Repository repository = null;
        try {
            repository = getRemoteRepository(map);
        } catch (Exception e) {
            throw new RepositoryException("Unable to create realm service " + e.getMessage());
        }
        return repository;
    }

    private Repository getRemoteRepository(Map map) throws Exception {

        RemoteRegistry remoteRegistry;
        Registry registry; // an admin registry
        try {
            RemoteRegistryService remoteRegistryService = new RemoteRegistryService(
                    (String) map.get("registryURL"),
                    (String) map.get("userName"),
                    (String) map.get("password"));
            if (regRepo == null) {
                regRepo = new RegistryRepository(remoteRegistryService);
            }
        } catch (Exception e) {
            throw new Exception("Unable to create realm service " + e.getMessage());
        }
        return regRepo;
    }

    public Repository getLocalRepository(Map map) throws Exception {

        if ((map != null) && (map.get("org.wso2.registry.jcr") != null) && (map.get("org.wso2.registry.jcr").equals("greg"))) {

            InMemoryEmbeddedRegistryService embeddedRegistryService = null;

            try {
                realmService = new DefaultRealmService(null);
            } catch (Exception e) {
                throw new Exception("Unable to create realm service " + e.getMessage());
            }
            RegistryCoreServiceComponent registryComponent = new RegistryCoreServiceComponent() {
                public void setRealmService(RealmService realm) {
                    super.setRealmService(realmService);
                }
            };
            RegistryService registryService = null;

            try {
                registryService = registryComponent.buildRegistryService();
                if (regRepo == null) {
                    regRepo = new RegistryRepository((EmbeddedRegistryService) registryService);
                }
            } catch (RegistryException e) {
                throw new Exception("Unable to build registry service" + e.getMessage());
            }
            return regRepo;
        }
        return null;
    }


}







