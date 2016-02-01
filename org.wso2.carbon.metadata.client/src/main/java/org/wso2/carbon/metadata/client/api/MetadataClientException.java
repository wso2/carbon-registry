/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.metadata.client.api;

/**
 * This exception will be thrown if any operation on the client side is failed
 */
public class MetadataClientException extends Exception {

    /**
     * The basic constructor to create an exception with message
     *
     * @param message Message to be passed if the operation failed
     */
    public MetadataClientException(String message) {
       super(message);
    }

    /**
     * Constructor to create an exception with message and a throwable
     *
     * @param message   Message to be passed if the operation failed
     * @param cause     Cause for the exception as a throwable
     */
    public MetadataClientException(String message, Throwable cause) {
        super(message,cause);
    }

}
