package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.port.in.McpInternalRegistryUseCase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Domain service implementing the mcp-registry tool group.
 * Standalone service with an in-memory registry (no persistence).
 * No Spring annotations - registered as a bean via DomainConfig.
 */
public class McpInternalRegistryService implements McpInternalRegistryUseCase {

    private final Map<String, Map<String, Object>> registry = new ConcurrentHashMap<>();

    // ========== 1. registerInternalServer ==========

    @Override
    public Map<String, Object> registerInternalServer(String name, String url, String transport, String capabilitiesJson) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (name == null || name.isBlank()) {
            result.put("error", "Server name is required");
            return result;
        }

        if (url == null || url.isBlank()) {
            result.put("error", "Server URL is required");
            return result;
        }

        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();

        List<String> capabilities = parseCapabilities(capabilitiesJson);

        Map<String, Object> serverEntry = new LinkedHashMap<>();
        serverEntry.put("id", id);
        serverEntry.put("name", name);
        serverEntry.put("url", url);
        serverEntry.put("transport", transport != null ? transport : "sse");
        serverEntry.put("capabilities", capabilities);
        serverEntry.put("status", "healthy");
        serverEntry.put("registeredAt", now.toString());

        registry.put(id, serverEntry);

        result.put("id", id);
        result.put("name", name);
        result.put("url", url);
        result.put("transport", transport != null ? transport : "sse");
        result.put("capabilities", capabilities);
        result.put("registeredAt", now.toString());

        return result;
    }

    // ========== 2. discoverServers ==========

    @Override
    public Map<String, Object> discoverServers(String status, String transport) {
        Map<String, Object> result = new LinkedHashMap<>();

        List<Map<String, Object>> servers = new ArrayList<>();

        for (Map<String, Object> entry : registry.values()) {
            boolean matches = true;

            if (status != null && !status.isBlank()) {
                String serverStatus = (String) entry.get("status");
                if (!status.equals(serverStatus)) {
                    matches = false;
                }
            }

            if (transport != null && !transport.isBlank()) {
                String serverTransport = (String) entry.get("transport");
                if (!transport.equals(serverTransport)) {
                    matches = false;
                }
            }

            if (matches) {
                servers.add(entry);
            }
        }

        result.put("servers", servers);
        result.put("count", servers.size());

        return result;
    }

    // ========== 3. healthCheck ==========

    @Override
    public Map<String, Object> healthCheck(String serverId) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (serverId == null || serverId.isBlank()) {
            result.put("error", "Server ID is required");
            return result;
        }

        Map<String, Object> entry = registry.get(serverId);
        if (entry == null) {
            result.put("error", "Server not found: " + serverId);
            return result;
        }

        // Simulate health check with random response time between 50-200ms
        long responseTimeMs = 50 + (long) (Math.random() * 150);

        result.put("serverId", serverId);
        result.put("name", entry.get("name"));
        result.put("status", "healthy");
        result.put("responseTimeMs", responseTimeMs);
        result.put("checkedAt", Instant.now().toString());

        return result;
    }

    // ========== 4. getCapabilities ==========

    @Override
    public Map<String, Object> getCapabilities(String serverId) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (serverId == null || serverId.isBlank()) {
            result.put("error", "Server ID is required");
            return result;
        }

        Map<String, Object> entry = registry.get(serverId);
        if (entry == null) {
            result.put("error", "Server not found: " + serverId);
            return result;
        }

        result.put("serverId", serverId);
        result.put("name", entry.get("name"));
        result.put("capabilities", entry.get("capabilities"));

        return result;
    }

    // ========== Helpers ==========

    /**
     * Parses a JSON array of capability strings.
     * Expected format: ["tool-execution","resource-reading","prompt-completion"]
     */
    List<String> parseCapabilities(String json) {
        List<String> capabilities = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return capabilities;
        }

        String trimmed = json.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return capabilities;
        }

        String inner = trimmed.substring(1, trimmed.length() - 1).trim();
        if (inner.isEmpty()) {
            return capabilities;
        }

        String[] parts = inner.split(",");
        for (String part : parts) {
            String name = part.trim().replace("\"", "");
            if (!name.isEmpty()) {
                capabilities.add(name);
            }
        }

        return capabilities;
    }

    /**
     * Returns the internal registry (for testing purposes).
     */
    Map<String, Map<String, Object>> getRegistry() {
        return registry;
    }
}
