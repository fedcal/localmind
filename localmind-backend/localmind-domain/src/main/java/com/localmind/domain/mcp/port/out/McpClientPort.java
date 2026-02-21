package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.*;
import java.util.List;

public interface McpClientPort {
    boolean connect(McpServerRegistration server);
    void disconnect(String serverId);
    boolean testConnection(String serverId);
    List<McpExternalTool> discoverTools(String serverId);
    McpToolExecutionResult executeTool(String serverId, McpToolExecutionRequest request);
}
