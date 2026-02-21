package com.localmind.domain.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpServerConfig {
    private String command;
    private List<String> args;
    private String url;
    @Builder.Default
    private int timeoutSeconds = 30;
    @Builder.Default
    private boolean autoReconnect = true;
}
