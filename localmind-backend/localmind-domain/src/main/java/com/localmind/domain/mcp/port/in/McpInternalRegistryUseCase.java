package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Input port for the mcp-registry tool group.
 * Provides internal MCP server registration, discovery, health checking and capability listing.
 */
public interface McpInternalRegistryUseCase {

    /**
     * Registers an internal MCP server in the in-memory registry.
     *
     * @param name             the server name
     * @param url              the server URL
     * @param transport        the transport type (e.g. "sse", "stdio")
     * @param capabilitiesJson JSON array of capabilities
     * @return registration details including generated ID
     */
    Map<String, Object> registerInternalServer(String name, String url, String transport, String capabilitiesJson);

    /**
     * Discovers registered servers, optionally filtered by status and transport.
     *
     * @param status    optional status filter (nullable)
     * @param transport optional transport filter (nullable)
     * @return list of matching servers with count
     */
    Map<String, Object> discoverServers(String status, String transport);

    /**
     * Performs a simulated health check on a registered server.
     *
     * @param serverId the server ID to check
     * @return health check result with response time
     */
    Map<String, Object> healthCheck(String serverId);

    /**
     * Returns the capabilities of a registered server.
     *
     * @param serverId the server ID
     * @return server capabilities
     */
    Map<String, Object> getCapabilities(String serverId);
}
