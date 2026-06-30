package com.localmind.domain.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpExternalTool {
    private String name;
    private String description;
    private String inputSchema;
    private String serverId;
}
