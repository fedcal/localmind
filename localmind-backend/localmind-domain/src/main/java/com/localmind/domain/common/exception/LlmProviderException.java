package com.localmind.domain.common.exception;

public class LlmProviderException extends LocalMindException {
    public LlmProviderException(String message) {
        super(message);
    }

    public LlmProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
