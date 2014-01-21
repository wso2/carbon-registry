/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.extensions.internal;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.embed.Embedder;
import org.wso2.carbon.registry.extensions.handlers.scm.ExternalContentHandler;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SCMUpdateTask implements Runnable {

    private static final Log log = LogFactory.getLog(SCMUpdateTask.class);

    private String checkOutURL;
    private String checkInURL;
    private String username;
    private String password;
    private File workingDir;
    private boolean readOnly;
    private ExternalContentHandler handler;

    public SCMUpdateTask(File workingDir, String checkOutURL, String checkInURL, boolean readOnly,
                         ExternalContentHandler handler, String username, String password) {
        this.workingDir = workingDir;
        this.checkOutURL = checkOutURL;
        this.checkInURL = checkInURL;
        this.readOnly = readOnly;
        this.handler = handler;
        this.username = username;
        this.password = password;
    }

    public void run() {
        Embedder plexus = null;

        try {
            plexus = new Embedder();
            plexus.start();
            handler.setBusy(true);
            ScmManager scmManager = (ScmManager) plexus.lookup(ScmManager.ROLE);
            ScmRepository scmRepository = scmManager.makeScmRepository(checkOutURL);
            if (username != null && password != null) {
                scmRepository.getProviderRepository().setUser(username);
                scmRepository.getProviderRepository().setPassword(password);
            }
            StatusScmResult status =
                    scmManager.status(scmRepository, new ScmFileSet(workingDir));
            List<ScmFile> changedFiles = status.getChangedFiles();
            List<File> toDelete = new LinkedList<File>();
            for (ScmFile file : changedFiles) {
                if (file.getStatus() == ScmFileStatus.MISSING) {
                    toDelete.add(new File(file.getPath()));
                }
            }
            if (workingDir.list() == null) {
                log.error("A directory was not found in the given path: " +
                        workingDir.getAbsolutePath());
                return;
            } else if (workingDir.list().length == 0) {
                scmManager.checkOut(scmRepository, new ScmFileSet(workingDir));
            } else {
                scmManager.update(scmRepository, new ScmFileSet(workingDir));
            }
            status = scmManager.status(scmRepository, new ScmFileSet(workingDir));
            changedFiles = status.getChangedFiles();
            List<File> toAdd = new LinkedList<File>();
            List<File> conflicts = new LinkedList<File>();
            List<File> toCheckIn = new LinkedList<File>();
            for (ScmFile file : changedFiles) {
                if (file.getStatus() == ScmFileStatus.UNKNOWN) {
                    File fileToAdd = new File(file.getPath());
                    toAdd.add(fileToAdd);
                    toCheckIn.add(fileToAdd);
                } else if  (file.getStatus() == ScmFileStatus.CONFLICT) {
                    conflicts.add(new File(file.getPath()));
                } else if (file.getStatus() == ScmFileStatus.MODIFIED ||
                        file.getStatus() == ScmFileStatus.ADDED ||
                        file.getStatus() == ScmFileStatus.DELETED) {
                    toCheckIn.add(new File(file.getPath()));
                }
            }
            if (conflicts.size() > 0) {
                for (File conflict : conflicts) {
                    try {
                        log.warn("Resolving conflict: " + conflict.getAbsolutePath());
                        FileUtils.forceDelete(conflict);
                    } catch (IOException e) {
                        log.error("Unable to resolve conflict", e);
                    }
                }
                scmManager.update(scmRepository, new ScmFileSet(workingDir, conflicts));
            }
            if (toDelete.size() > 0) {
                for (File file : toDelete) {
                    try {
                        if (file.exists()) {
                            FileUtils.forceDelete(file);
                        }
                        toCheckIn.add(file);
                    } catch (IOException e) {
                        log.error("Unable to remove file to delete", e);
                    }
                }
                scmManager.update(scmRepository, new ScmFileSet(workingDir, conflicts));
            }
            if (!readOnly) {
                if (checkInURL == null) {
                    makeChanges(scmManager, scmRepository, toCheckIn, toDelete, toAdd);
                } else {
                    scmRepository = scmManager.makeScmRepository(checkInURL);
                    if (username != null && password != null) {
                        scmRepository.getProviderRepository().setUser(username);
                        scmRepository.getProviderRepository().setPassword(password);
                    }
                    makeChanges(scmManager, scmRepository, toCheckIn, toDelete, toAdd);
                }
            }
        } catch (PlexusContainerException e) {
            log.error("Unable to start Plexus Container", e);
        } catch (ComponentLookupException e) {
            log.error("Unable to obtain instance of SCM Manager", e);
        } catch (ScmRepositoryException e) {
            log.error("Unable to create an instance of a SCM repository", e);
        } catch (NoSuchScmProviderException e) {
            log.error("A provider was not found for the specified SCM URL", e);
        } catch (ScmException e) {
            log.error("The SCM operation failed", e);
        } finally {
            handler.setBusy(false);
        }


        try {
            plexus.stop();
        } catch (RuntimeException ignore) {
            // Exceptions can be ignored as in the example from Maven.
        }
    }

    private void makeChanges(ScmManager scmManager, ScmRepository scmRepository,
                             List<File> toCheckIn, List<File> toDelete, List<File> toAdd)
            throws ScmException {
        if (toAdd.size() > 0) {
            scmManager.add(scmRepository, new ScmFileSet(workingDir, toAdd), "");
        }
        if (toDelete.size() > 0) {
            scmManager.remove(scmRepository, new ScmFileSet(workingDir, toDelete), "");
        }
        if (toCheckIn.size() > 0) {
            scmManager.checkIn(scmRepository, new ScmFileSet(workingDir, toCheckIn), "");
        }
    }
}
