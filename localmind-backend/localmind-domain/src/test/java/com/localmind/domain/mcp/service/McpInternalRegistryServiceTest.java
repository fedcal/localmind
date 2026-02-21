package com.localmind.domain.mcp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class McpInternalRegistryServiceTest {

    private McpInternalRegistryService service;

    @BeforeEach
    void setUp() {
        service = new McpInternalRegistryService();
    }

    // ========== registerInternalServer Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void registerInternalServer_validData_registersSuccessfully() {
        Map<String, Object> result = service.registerInternalServer(
                "test-server", "http://localhost:8080", "sse",
                "[\"tool-execution\",\"resource-reading\"]");

        assertThat(result.get("id")).isNotNull();
        assertThat(result.get("name")).isEqualTo("test-server");
        assertThat(result.get("url")).isEqualTo("http://localhost:8080");
        assertThat(result.get("transport")).isEqualTo("sse");
        assertThat((List<String>) result.get("capabilities")).containsExactly("tool-execution", "resource-reading");
        assertThat(result.get("registeredAt")).isNotNull();
    }

    @Test
    void registerInternalServer_defaultTransport_usesSSE() {
        Map<String, Object> result = service.registerInternalServer(
                "server", "http://localhost:8080", null, "[]");

        assertThat(result.get("transport")).isEqualTo("sse");
    }

    @Test
    void registerInternalServer_blankName_returnsError() {
        Map<String, Object> result = service.registerInternalServer(
                "", "http://localhost:8080", "sse", "[]");

        assertThat(result.get("error")).isEqualTo("Server name is required");
    }

    @Test
    void registerInternalServer_nullName_returnsError() {
        Map<String, Object> result = service.registerInternalServer(
                null, "http://localhost:8080", "sse", "[]");

        assertThat(result.get("error")).isEqualTo("Server name is required");
    }

    @Test
    void registerInternalServer_blankUrl_returnsError() {
        Map<String, Object> result = service.registerInternalServer(
                "server", "", "sse", "[]");

        assertThat(result.get("error")).isEqualTo("Server URL is required");
    }

    @Test
    void registerInternalServer_addsToRegistry() {
        service.registerInternalServer("server1", "http://localhost:8080", "sse", "[]");

        assertThat(service.getRegistry()).hasSize(1);
    }

    // ========== discoverServers Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void discoverServers_noFilter_returnsAll() {
        service.registerInternalServer("server1", "http://localhost:8080", "sse", "[]");
        service.registerInternalServer("server2", "http://localhost:9090", "stdio", "[]");

        Map<String, Object> result = service.discoverServers(null, null);

        List<Map<String, Object>> servers = (List<Map<String, Object>>) result.get("servers");
        assertThat(servers).hasSize(2);
        assertThat(result.get("count")).isEqualTo(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void discoverServers_filterByTransport_filtersCorrectly() {
        service.registerInternalServer("server1", "http://localhost:8080", "sse", "[]");
        service.registerInternalServer("server2", "http://localhost:9090", "stdio", "[]");

        Map<String, Object> result = service.discoverServers(null, "sse");

        List<Map<String, Object>> servers = (List<Map<String, Object>>) result.get("servers");
        assertThat(servers).hasSize(1);
        assertThat(servers.get(0).get("name")).isEqualTo("server1");
    }

    @Test
    @SuppressWarnings("unchecked")
    void discoverServers_emptyRegistry_returnsEmptyList() {
        Map<String, Object> result = service.discoverServers(null, null);

        List<Map<String, Object>> servers = (List<Map<String, Object>>) result.get("servers");
        assertThat(servers).isEmpty();
        assertThat(result.get("count")).isEqualTo(0);
    }

    // ========== healthCheck Tests ==========

    @Test
    void healthCheck_existingServer_returnsHealthy() {
        Map<String, Object> registered = service.registerInternalServer(
                "server1", "http://localhost:8080", "sse", "[]");
        String serverId = (String) registered.get("id");

        Map<String, Object> result = service.healthCheck(serverId);

        assertThat(result.get("serverId")).isEqualTo(serverId);
        assertThat(result.get("name")).isEqualTo("server1");
        assertThat(result.get("status")).isEqualTo("healthy");
        assertThat(result.get("responseTimeMs")).isNotNull();
        long responseTime = (long) result.get("responseTimeMs");
        assertThat(responseTime).isBetween(50L, 200L);
        assertThat(result.get("checkedAt")).isNotNull();
    }

    @Test
    void healthCheck_unknownServer_returnsError() {
        Map<String, Object> result = service.healthCheck("unknown-id");

        assertThat(result.get("error")).isEqualTo("Server not found: unknown-id");
    }

    @Test
    void healthCheck_blankServerId_returnsError() {
        Map<String, Object> result = service.healthCheck("");

        assertThat(result.get("error")).isEqualTo("Server ID is required");
    }

    // ========== getCapabilities Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void getCapabilities_existingServer_returnsCapabilities() {
        Map<String, Object> registered = service.registerInternalServer(
                "server1", "http://localhost:8080", "sse",
                "[\"tool-execution\",\"resource-reading\"]");
        String serverId = (String) registered.get("id");

        Map<String, Object> result = service.getCapabilities(serverId);

        assertThat(result.get("serverId")).isEqualTo(serverId);
        assertThat(result.get("name")).isEqualTo("server1");
        List<String> capabilities = (List<String>) result.get("capabilities");
        assertThat(capabilities).containsExactly("tool-execution", "resource-reading");
    }

    @Test
    void getCapabilities_unknownServer_returnsError() {
        Map<String, Object> result = service.getCapabilities("unknown-id");

        assertThat(result.get("error")).isEqualTo("Server not found: unknown-id");
    }

    @Test
    void getCapabilities_blankServerId_returnsError() {
        Map<String, Object> result = service.getCapabilities("");

        assertThat(result.get("error")).isEqualTo("Server ID is required");
    }

    // ========== parseCapabilities Tests ==========

    @Test
    void parseCapabilities_validArray_parsesCorrectly() {
        List<String> caps = service.parseCapabilities("[\"tool-execution\",\"resource-reading\"]");

        assertThat(caps).containsExactly("tool-execution", "resource-reading");
    }

    @Test
    void parseCapabilities_emptyArray_returnsEmpty() {
        List<String> caps = service.parseCapabilities("[]");
        assertThat(caps).isEmpty();
    }

    @Test
    void parseCapabilities_null_returnsEmpty() {
        List<String> caps = service.parseCapabilities(null);
        assertThat(caps).isEmpty();
    }
}
