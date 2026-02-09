package com.localmind.api.mcp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpServerDto {
    private String id;
    private String name;
    private String description;
    private String type;
    private String status;
    private McpServerConfigDto config;
    private LocalDateTime createdAt;
    private LocalDateTime lastConnectedAt;
}
