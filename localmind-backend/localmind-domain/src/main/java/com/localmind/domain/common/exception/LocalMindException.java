package com.localmind.domain.common.exception;

public class LocalMindException extends RuntimeException {
    public LocalMindException(String message) {
        super(message);
    }

    public LocalMindException(String message, Throwable cause) {
        super(message, cause);
    }
}
