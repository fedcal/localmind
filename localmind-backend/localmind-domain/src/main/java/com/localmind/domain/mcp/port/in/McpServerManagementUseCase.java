package com.localmind.domain.mcp.port.in;

import com.localmind.domain.mcp.model.McpServerRegistration;
import java.util.List;

public interface McpServerManagementUseCase {
    McpServerRegistration register(McpServerRegistration server);
    void remove(String serverId);
    McpServerRegistration get(String serverId);
    List<McpServerRegistration> listAll();
    McpServerRegistration testConnection(String serverId);
    void reconnect(String serverId);
}
