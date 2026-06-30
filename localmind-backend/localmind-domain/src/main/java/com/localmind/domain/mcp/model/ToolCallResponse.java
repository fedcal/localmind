package com.localmind.domain.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallResponse {
    private String toolName;
    private String result;
    private boolean success;
    private String errorMessage;
    private long executionTimeMs;
}
