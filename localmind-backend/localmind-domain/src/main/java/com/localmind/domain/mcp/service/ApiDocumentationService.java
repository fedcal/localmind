package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.ApiDocumentation;
import com.localmind.domain.mcp.port.in.ApiDocumentationUseCase;
import com.localmind.domain.mcp.port.out.ApiDocumentationRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Domain service implementing the api-documentation tool group.
 * Provides endpoint extraction, OpenAPI generation, and undocumented code detection.
 * No Spring annotations - registered as a bean via DomainConfig.
 */
public class ApiDocumentationService implements ApiDocumentationUseCase {

    private final ApiDocumentationRepository repository;

    // Spring MVC annotations
    private static final Pattern SPRING_MAPPING_PATTERN = Pattern.compile(
            "@(Get|Post|Put|Delete|Patch)Mapping\\s*\\(\\s*(?:value\\s*=\\s*)?[\"']([^\"']*)[\"']");
    private static final Pattern SPRING_CONTROLLER_PREFIX = Pattern.compile(
            "@(?:Request)Mapping\\s*\\(\\s*(?:value\\s*=\\s*)?[\"']([^\"']*)[\"']\\s*\\)\\s*(?:\\n|.)*?class");

    // Express patterns
    private static final Pattern EXPRESS_ROUTE_PATTERN = Pattern.compile(
            "(?:app|router)\\.(get|post|put|delete|patch)\\s*\\(\\s*[\"']([^\"']+)[\"']");

    // NestJS patterns
    private static final Pattern NESTJS_DECORATOR_PATTERN = Pattern.compile(
            "@(Get|Post|Put|Delete|Patch)\\s*\\(\\s*[\"']?([^\"')]*)[\"']?\\s*\\)");
    private static final Pattern NESTJS_CONTROLLER_PATTERN = Pattern.compile(
            "@Controller\\s*\\(\\s*[\"']([^\"']*)[\"']\\s*\\)");

    // Documentation patterns
    private static final Pattern JAVADOC_PATTERN = Pattern.compile("/\\*\\*[\\s\\S]*?\\*/");
    private static final Pattern JSDOC_PATTERN = Pattern.compile("/\\*\\*[\\s\\S]*?\\*/");
    private static final Pattern EXPORT_PATTERN = Pattern.compile(
            "^\\s*(?:export\\s+)?(?:public\\s+)?(?:abstract\\s+)?(?:default\\s+)?" +
                    "(function|class|interface|type|const|enum|let|var)\\s+(\\w+)");

    private static final Set<String> METHODS_WITH_BODY = Set.of("POST", "PUT", "PATCH");

    public ApiDocumentationService(ApiDocumentationRepository repository) {
        this.repository = repository;
    }

    // ========== 1. extractEndpoints ==========

    @Override
    public Map<String, Object> extractEndpoints(String filePath) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (filePath == null || filePath.isBlank()) {
            result.put("error", "filePath is empty or null");
            return result;
        }

        List<Map<String, Object>> endpoints = new ArrayList<>();
        String controllerPrefix = "";

        try {
            String content = readFileContent(filePath);
            if (content == null || content.isEmpty()) {
                result.put("filePath", filePath);
                result.put("endpointCount", 0);
                result.put("endpoints", endpoints);
                result.put("message", "File is empty or could not be read");
                saveResult("extract-endpoints", filePath, result);
                return result;
            }

            // Detect controller prefix (Spring @RequestMapping or NestJS @Controller)
            Matcher springPrefix = SPRING_CONTROLLER_PREFIX.matcher(content);
            if (springPrefix.find()) {
                controllerPrefix = springPrefix.group(1);
            }
            Matcher nestPrefix = NESTJS_CONTROLLER_PATTERN.matcher(content);
            if (nestPrefix.find()) {
                controllerPrefix = nestPrefix.group(1);
            }

            // Extract Spring MVC endpoints
            Matcher springMatcher = SPRING_MAPPING_PATTERN.matcher(content);
            while (springMatcher.find()) {
                String method = springMatcher.group(1).toUpperCase();
                if ("REQUEST".equals(method)) method = "GET";
                String path = controllerPrefix + springMatcher.group(2);
                endpoints.add(buildEndpoint(method, path, "spring"));
            }

            // Extract Express routes
            Matcher expressMatcher = EXPRESS_ROUTE_PATTERN.matcher(content);
            while (expressMatcher.find()) {
                String method = expressMatcher.group(1).toUpperCase();
                String path = expressMatcher.group(2);
                endpoints.add(buildEndpoint(method, path, "express"));
            }

            // Extract NestJS routes
            Matcher nestMatcher = NESTJS_DECORATOR_PATTERN.matcher(content);
            while (nestMatcher.find()) {
                String method = nestMatcher.group(1).toUpperCase();
                String path = controllerPrefix + "/" + nestMatcher.group(2);
                path = path.replaceAll("//+", "/");
                endpoints.add(buildEndpoint(method, path, "nestjs"));
            }

        } catch (Exception e) {
            result.put("error", "Failed to read file: " + e.getMessage());
            return result;
        }

        result.put("filePath", filePath);
        result.put("endpointCount", endpoints.size());
        result.put("endpoints", endpoints);
        result.put("scannedAt", Instant.now().toString());

        saveResult("extract-endpoints", filePath, result);
        return result;
    }

    // ========== 2. generateOpenApi ==========

    @Override
    public Map<String, Object> generateOpenApi(List<Map<String, Object>> endpoints, String title, String version) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (endpoints == null || endpoints.isEmpty()) {
            result.put("error", "No endpoints provided");
            return result;
        }
        if (title == null || title.isBlank()) {
            title = "API Documentation";
        }
        if (version == null || version.isBlank()) {
            version = "1.0.0";
        }

        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("openapi", "3.0.3");

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", title);
        info.put("version", version);
        spec.put("info", info);

        Map<String, Object> paths = new LinkedHashMap<>();

        for (Map<String, Object> ep : endpoints) {
            String method = ((String) ep.getOrDefault("method", "GET")).toLowerCase();
            String path = (String) ep.getOrDefault("path", "/");
            String description = (String) ep.getOrDefault("description", "");

            // Convert Express :param to OpenAPI {param}
            String openApiPath = path.replaceAll(":([a-zA-Z_]+)", "{$1}");

            Map<String, Object> pathItem = (Map<String, Object>) paths.getOrDefault(openApiPath, new LinkedHashMap<>());

            Map<String, Object> operation = new LinkedHashMap<>();

            // Generate operationId
            String operationId = sanitizeOperationId(method + "_" + path);
            operation.put("operationId", operationId);

            if (!description.isEmpty()) {
                operation.put("description", description);
            }

            // Tags from first path segment
            String[] segments = openApiPath.split("/");
            if (segments.length > 1 && !segments[1].isEmpty()) {
                operation.put("tags", List.of(segments[1]));
            }

            // Path parameters
            List<Map<String, Object>> parameters = extractPathParams(openApiPath);
            if (!parameters.isEmpty()) {
                operation.put("parameters", parameters);
            }

            // Request body for POST/PUT/PATCH
            if (METHODS_WITH_BODY.contains(method.toUpperCase())) {
                Map<String, Object> requestBody = new LinkedHashMap<>();
                requestBody.put("required", true);
                Map<String, Object> content = new LinkedHashMap<>();
                Map<String, Object> jsonType = new LinkedHashMap<>();
                Map<String, Object> schema = new LinkedHashMap<>();
                schema.put("type", "object");
                jsonType.put("schema", schema);
                content.put("application/json", jsonType);
                requestBody.put("content", content);
                operation.put("requestBody", requestBody);
            }

            // Standard responses
            Map<String, Object> responses = new LinkedHashMap<>();
            Map<String, Object> ok = new LinkedHashMap<>();
            ok.put("description", "Successful operation");
            responses.put("200", ok);
            Map<String, Object> badRequest = new LinkedHashMap<>();
            badRequest.put("description", "Bad request");
            responses.put("400", badRequest);
            Map<String, Object> notFound = new LinkedHashMap<>();
            notFound.put("description", "Not found");
            responses.put("404", notFound);
            Map<String, Object> serverError = new LinkedHashMap<>();
            serverError.put("description", "Internal server error");
            responses.put("500", serverError);
            operation.put("responses", responses);

            pathItem.put(method, operation);
            paths.put(openApiPath, pathItem);
        }

        spec.put("paths", paths);

        result.put("title", title);
        result.put("version", version);
        result.put("endpointCount", endpoints.size());
        result.put("openApiSpec", spec);

        saveResult("generate-openapi", null, result);
        return result;
    }

    // ========== 3. findUndocumented ==========

    @Override
    public Map<String, Object> findUndocumented(String filePath) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (filePath == null || filePath.isBlank()) {
            result.put("error", "filePath is empty or null");
            return result;
        }

        try {
            String content = readFileContent(filePath);
            if (content == null || content.isEmpty()) {
                result.put("filePath", filePath);
                result.put("totalExports", 0);
                result.put("documented", 0);
                result.put("undocumented", 0);
                result.put("coveragePercent", 100.0);
                result.put("undocumentedItems", new ArrayList<>());
                saveResult("find-undocumented", filePath, result);
                return result;
            }

            String[] lines = content.split("\n");
            List<Map<String, Object>> documentedItems = new ArrayList<>();
            List<Map<String, Object>> undocumentedItems = new ArrayList<>();

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                Matcher exportMatcher = EXPORT_PATTERN.matcher(line);

                if (exportMatcher.find()) {
                    String kind = exportMatcher.group(1);
                    String name = exportMatcher.group(2);

                    boolean hasDoc = hasDocComment(lines, i);

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", name);
                    item.put("kind", kind);
                    item.put("line", i + 1);

                    if (hasDoc) {
                        documentedItems.add(item);
                    } else {
                        undocumentedItems.add(item);
                    }
                }
            }

            int total = documentedItems.size() + undocumentedItems.size();
            double coverage = total > 0 ? (documentedItems.size() * 100.0 / total) : 100.0;

            result.put("filePath", filePath);
            result.put("totalExports", total);
            result.put("documented", documentedItems.size());
            result.put("undocumented", undocumentedItems.size());
            result.put("coveragePercent", Math.round(coverage * 100.0) / 100.0);
            result.put("documentedItems", documentedItems);
            result.put("undocumentedItems", undocumentedItems);

        } catch (Exception e) {
            result.put("error", "Failed to read file: " + e.getMessage());
            return result;
        }

        saveResult("find-undocumented", filePath, result);
        return result;
    }

    // ========== Helper Methods ==========

    private String readFileContent(String filePath) {
        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Path.of(filePath)));
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> buildEndpoint(String method, String path, String framework) {
        Map<String, Object> endpoint = new LinkedHashMap<>();
        endpoint.put("method", method);
        endpoint.put("path", path);
        endpoint.put("framework", framework);
        return endpoint;
    }

    private String sanitizeOperationId(String raw) {
        return raw.replaceAll("[^a-zA-Z0-9_]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    private List<Map<String, Object>> extractPathParams(String path) {
        List<Map<String, Object>> params = new ArrayList<>();
        Pattern paramPattern = Pattern.compile("\\{([a-zA-Z_]+)\\}");
        Matcher matcher = paramPattern.matcher(path);
        while (matcher.find()) {
            Map<String, Object> param = new LinkedHashMap<>();
            param.put("name", matcher.group(1));
            param.put("in", "path");
            param.put("required", true);
            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("type", "string");
            param.put("schema", schema);
            params.add(param);
        }
        return params;
    }

    private boolean hasDocComment(String[] lines, int declarationLine) {
        // Look backwards from the declaration line for /** ... */
        for (int i = declarationLine - 1; i >= 0; i--) {
            String trimmed = lines[i].trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.endsWith("*/")) return true;
            if (trimmed.startsWith("//")) continue;
            if (trimmed.startsWith("@")) continue; // annotations
            break;
        }
        return false;
    }

    private void saveResult(String type, String filePath, Map<String, Object> result) {
        ApiDocumentation doc = ApiDocumentation.builder()
                .id(UUID.randomUUID().toString())
                .documentationType(type)
                .filePath(filePath)
                .resultJson(mapToJson(result))
                .createdAt(Instant.now())
                .build();
        repository.save(doc);
    }

    // ========== JSON Serialization ==========

    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            sb.append(valueToJson(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escapeJson((String) value) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Map) return mapToJson((Map<String, Object>) value);
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(valueToJson(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
