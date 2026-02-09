package com.localmind.infrastructure.mcp.config;

import com.localmind.domain.mcp.port.out.McpClientPort;
import com.localmind.domain.mcp.port.out.McpServerRegistrationRepository;
import com.localmind.domain.mcp.service.McpServerManagementService;
import com.localmind.domain.mcp.service.McpToolOrchestratorService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfiguration {

    @Bean
    @ConditionalOnProperty(name = "localmind.mcp.client.enabled", havingValue = "true")
    public McpServerManagementService mcpServerManagementService(
            McpServerRegistrationRepository repository,
            McpClientPort clientPort) {
        return new McpServerManagementService(repository, clientPort);
    }

    @Bean
    @ConditionalOnProperty(name = "localmind.mcp.client.enabled", havingValue = "true")
    public McpToolOrchestratorService mcpToolOrchestratorService(
            McpServerRegistrationRepository repository,
            McpClientPort clientPort) {
        return new McpToolOrchestratorService(repository, clientPort);
    }
}
