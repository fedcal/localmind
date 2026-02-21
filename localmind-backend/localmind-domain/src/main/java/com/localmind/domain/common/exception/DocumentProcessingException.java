package com.localmind.domain.common.exception;

public class DocumentProcessingException extends LocalMindException {
    public DocumentProcessingException(String message) {
        super(message);
    }

    public DocumentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
