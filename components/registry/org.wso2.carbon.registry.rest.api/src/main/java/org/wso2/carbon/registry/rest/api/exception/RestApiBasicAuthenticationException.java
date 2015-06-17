package org.wso2.carbon.registry.rest.api.exception;


public class RestApiBasicAuthenticationException extends Exception {

    public RestApiBasicAuthenticationException()
    {
    }

    public RestApiBasicAuthenticationException(String message)
    {
        super(message);
    }

    public RestApiBasicAuthenticationException(Throwable cause)
    {
        super(cause);
    }

    public RestApiBasicAuthenticationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
