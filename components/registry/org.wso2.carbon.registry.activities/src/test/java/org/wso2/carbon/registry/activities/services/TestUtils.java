/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.registry.activities.services;

import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.LogEntry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestUtils {
    private static final String cvsSplitBy = ",";
    private static final String OS_NAME_KEY = "os.name";
    private static final String WINDOWS_PARAM = "indow";

    public static Path getResourcePath(String... resourcePaths) {
        URL resourceURL = TestUtils.class.getClassLoader().getResource("");
        if (resourceURL != null) {
            String resourcePath = resourceURL.getPath();
            if (resourcePath != null) {
                resourcePath = System.getProperty(OS_NAME_KEY).contains(WINDOWS_PARAM) ?
                        resourcePath.substring(1) : resourcePath;
                return Paths.get(resourcePath, resourcePaths);
            }
        }
        return null; // Resource do not exist
    }

    public static LogEntry[] readLogEntries(String logFile) throws RegistryException {
        Path csvFilePath = TestUtils.getResourcePath(logFile);
        assert csvFilePath != null;
        LogEntry[] resultEntries = new LogEntry[0];
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath.toFile()))) {
            List<LogEntry> logEntries = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] logData = line.split(cvsSplitBy);
                LogEntry logEntry = new LogEntry();
                logEntry.setResourcePath(logData[0]);
                logEntry.setUserName(logData[1]);
                logEntry.setDate(new Date(Long.parseLong(logData[2])));
                logEntry.setAction(Integer.parseInt(logData[3]));
                logEntry.setActionData(logData[4]);
                logEntries.add(logEntry);
            }
            resultEntries = logEntries.toArray(new LogEntry[logEntries.size()]);
        } catch (IOException e) {
            throw new RegistryException("Error while reading the log entry file", e);
        }
        return resultEntries;
    }

}
