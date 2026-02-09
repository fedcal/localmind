package com.localmind.domain.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpServerRegistration {
    private String id;
    private String name;
    private String description;
    private McpServerType type;
    private McpServerConfig config;
    private McpServerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastConnectedAt;
}
