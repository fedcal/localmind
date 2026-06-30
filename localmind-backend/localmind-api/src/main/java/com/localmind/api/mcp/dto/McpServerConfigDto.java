package com.localmind.api.mcp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpServerConfigDto {
    private String command;
    private List<String> args;
    private String url;
    private Integer timeoutSeconds;
    private Boolean autoReconnect;
}
