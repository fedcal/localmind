package com.localmind.domain.mcp.port.in;

import java.util.List;
import java.util.Map;

/**
 * Input port for the api-documentation tool group.
 * Provides endpoint extraction, OpenAPI spec generation, and undocumented code detection.
 */
public interface ApiDocumentationUseCase {

    /**
     * Extracts REST API endpoints from a source file.
     * Supports Spring (@GetMapping, @PostMapping, etc.), Express (app.get, router.post, etc.)
     * and NestJS (@Get, @Post, etc.) patterns.
     *
     * @param filePath path to the source file to scan
     * @return analysis result with filePath, endpointCount, endpoints list, scannedAt
     */
    Map<String, Object> extractEndpoints(String filePath);

    /**
     * Generates an OpenAPI 3.0.3 specification from a list of endpoint definitions.
     *
     * @param endpoints list of endpoint maps, each containing method, path, handlerName
     * @param title     the API title for the OpenAPI info section
     * @param version   the API version for the OpenAPI info section
     * @return result containing title, version, endpointCount, openApiSpec (JSON string)
     */
    Map<String, Object> generateOpenApi(List<Map<String, Object>> endpoints, String title, String version);

    /**
     * Finds undocumented exports/public declarations in a source file.
     * Checks for JavaDoc or JSDoc comments preceding public declarations.
     *
     * @param filePath path to the source file to scan
     * @return result with filePath, totalExports, documented, undocumented, coveragePercent, undocumentedItems
     */
    Map<String, Object> findUndocumented(String filePath);
}
