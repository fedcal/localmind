package com.localmind.domain.mcp.port.in;

import com.localmind.domain.mcp.model.McpExternalTool;
import java.util.List;

public interface McpToolDiscoveryUseCase {
    List<McpExternalTool> listExternalTools(String serverId);
    List<McpExternalTool> listAllExternalTools();
}
