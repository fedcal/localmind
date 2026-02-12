package com.localmind.api.common.advice;

import com.localmind.api.common.dto.ErrorResponseDto;
import com.localmind.domain.common.exception.LlmProviderException;
import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.common.exception.DocumentProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(404).body(
                ErrorResponseDto.of(404, ex.getMessage(), req.getRequestURI())
        );
    }

    @ExceptionHandler(LlmProviderException.class)
    public ResponseEntity<ErrorResponseDto> handleLlmError(LlmProviderException ex, HttpServletRequest req) {
        log.error("LLM provider error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(502).body(
                ErrorResponseDto.of(502, "LLM provider error: " + ex.getMessage(), req.getRequestURI())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        log.warn("Validation error: {}", message);
        return ResponseEntity.status(400).body(
                ErrorResponseDto.of(400, message, req.getRequestURI())
        );
    }

    @ExceptionHandler(DocumentProcessingException.class)
    public ResponseEntity<ErrorResponseDto> handleDocumentError(DocumentProcessingException ex, HttpServletRequest req) {
        log.error("Document processing error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(500).body(
                ErrorResponseDto.of(500, ex.getMessage(), req.getRequestURI())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneral(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(500).body(
                ErrorResponseDto.of(500, "Internal server error", req.getRequestURI())
        );
    }
}
