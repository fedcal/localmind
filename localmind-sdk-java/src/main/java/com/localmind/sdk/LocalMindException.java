package com.localmind.sdk;

/**
 * Exception thrown when a LocalMind API call fails.
 * Contains the HTTP status code and the error message from the server.
 */
public class LocalMindException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public LocalMindException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public LocalMindException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.responseBody = null;
    }

    /**
     * HTTP status code returned by the server (0 if the error occurred before receiving a response).
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Raw response body returned by the server, useful for debugging.
     */
    public String getResponseBody() {
        return responseBody;
    }
}
