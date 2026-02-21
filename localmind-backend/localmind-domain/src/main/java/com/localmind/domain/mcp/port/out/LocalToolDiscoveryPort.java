package com.localmind.domain.mcp.port.out;

import java.util.List;
import java.util.Map;

/**
 * Port for discovering local @Tool methods registered in the application.
 * The infrastructure adapter scans the Spring context for beans with @Tool annotations.
 */
public interface LocalToolDiscoveryPort {

    /**
     * Returns all discovered local @Tool methods.
     * Each map contains: name (String), description (String), local (Boolean).
     *
     * @return unmodifiable list of tool info maps
     */
    List<Map<String, Object>> discoverLocalTools();
}
