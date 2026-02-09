package com.localmind.infrastructure.mcp.server;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)
public class LocalMindMcpResources {

    // MCP Resources will be registered programmatically via McpConfiguration
    // as @McpResource annotation support depends on Spring AI version

    public String getProviderConfig() {
        return """
                {
                  "providers": [
                    {"name": "OLLAMA", "local": true, "defaultModel": "llama3.2"},
                    {"name": "OPENAI", "local": false, "defaultModel": "gpt-4o"},
                    {"name": "ANTHROPIC", "local": false, "defaultModel": "claude-sonnet-4-20250514"},
                    {"name": "GOOGLE", "local": false, "defaultModel": "gemini-pro"}
                  ],
                  "defaultProvider": "OLLAMA"
                }
                """;
    }
}
