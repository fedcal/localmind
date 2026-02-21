package com.localmind.api.mcp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpToolExecutionRequestDto {
    @NotBlank(message = "Tool name is required")
    private String toolName;
    @NotBlank(message = "Server ID is required")
    private String serverId;
    private Map<String, Object> arguments;
}
