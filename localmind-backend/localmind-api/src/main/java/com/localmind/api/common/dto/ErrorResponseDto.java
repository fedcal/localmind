package com.localmind.api.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {
    private int status;
    private String message;
    private Instant timestamp;
    private String path;

    public static ErrorResponseDto of(int status, String message, String path) {
        return ErrorResponseDto.builder()
                .status(status)
                .message(message)
                .timestamp(Instant.now())
                .path(path)
                .build();
    }
}
