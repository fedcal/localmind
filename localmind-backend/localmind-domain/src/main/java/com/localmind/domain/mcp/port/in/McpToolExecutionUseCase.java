package com.localmind.domain.mcp.port.in;

import com.localmind.domain.mcp.model.McpToolExecutionRequest;
import com.localmind.domain.mcp.model.McpToolExecutionResult;

public interface McpToolExecutionUseCase {
    McpToolExecutionResult executeTool(McpToolExecutionRequest request);
}
