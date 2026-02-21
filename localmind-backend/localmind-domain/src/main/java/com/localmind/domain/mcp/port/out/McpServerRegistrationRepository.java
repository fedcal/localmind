package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.McpServerRegistration;
import java.util.List;
import java.util.Optional;

public interface McpServerRegistrationRepository {
    McpServerRegistration save(McpServerRegistration server);
    void delete(String serverId);
    Optional<McpServerRegistration> findById(String serverId);
    List<McpServerRegistration> findAll();
}
