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
package org.wso2.carbon.registry.extensions.handlers.scm;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.File;
import java.io.IOException;

public class FilesystemManager {

    private File baseDir;

    public FilesystemManager(String basePath) {
        this.baseDir = new File(basePath);
    }

    public File getBaseDir() {
        return baseDir;
    }

    public byte[] getFileContent(String path) throws RegistryException {
        File file = new File(baseDir, path);
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            throw new RegistryException("Unable to read file at path: " +
                    file.getAbsolutePath(), e);
        }
    }

    public String[] getDirectoryContent(String path) throws RegistryException {
        File directory = new File(baseDir, path);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new RegistryException("A directory does not exist at path: " +
                    directory.getAbsolutePath());
        }
        return directory.list(new AndFileFilter(HiddenFileFilter.VISIBLE,
                new OrFileFilter(DirectoryFileFilter.INSTANCE, FileFileFilter.FILE)));
    }

    public void createOrUpdateFile(String path, byte[] content) throws RegistryException {
        File file = new File(baseDir, path);
        try {
            createDirectory(file.getParentFile().getAbsolutePath().substring(
                    baseDir.getAbsolutePath().length()));
            FileUtils.writeByteArrayToFile(file, content);
        } catch (IOException e) {
            throw new RegistryException("Unable to write content to file at path: " +
                    file.getAbsolutePath(), e);
        }
    }

    public void createDirectory(String path) throws RegistryException {
        File directory = new File(baseDir, path);
        if (directory.exists()) {
            return;
        }
        try {
            FileUtils.forceMkdir(directory);
        } catch (IOException e) {
            throw new RegistryException("Unable to create directory at path: " +
                    directory.getAbsolutePath(), e);
        }
    }

    public void delete(String path) throws RegistryException {
        File file = new File(baseDir, path);
        if (!file.exists()) {
            return;
        }
        try {
            FileUtils.forceDelete(file);
        } catch (IOException e) {
            throw new RegistryException("Unable to delete file at path: " +
                    file.getAbsolutePath(), e);
        }
    }

    public void copy(String source, String destination) throws RegistryException {
        File sourceFile = new File(baseDir, source);
        File destinationFile = new File(baseDir, destination);

        try {
            FileUtils.copyFile(sourceFile, destinationFile);
        } catch (IOException e) {
            throw new RegistryException("Unable to copy file at path: " +
                    sourceFile.getAbsolutePath() + " to path: " +
                    destinationFile.getAbsolutePath(),e);
        }
    }

}
