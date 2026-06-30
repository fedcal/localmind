package com.localmind.infrastructure.mcp.server;

import com.localmind.domain.mcp.port.in.ApiDocumentationUseCase;
import com.localmind.domain.mcp.port.in.CodebaseKnowledgeUseCase;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)
public class LocalMindDocTools {

    private final ApiDocumentationUseCase apiDocumentationUseCase;
    private final CodebaseKnowledgeUseCase codebaseKnowledgeUseCase;

    public LocalMindDocTools(ApiDocumentationUseCase apiDocumentationUseCase,
                             CodebaseKnowledgeUseCase codebaseKnowledgeUseCase) {
        this.apiDocumentationUseCase = apiDocumentationUseCase;
        this.codebaseKnowledgeUseCase = codebaseKnowledgeUseCase;
    }

    // ==================== API DOCUMENTATION ====================

    @Tool(description = "Extract REST API endpoints from a source file. Supports Spring MVC "
            + "(@GetMapping, @PostMapping, etc.), Express (app.get, router.post, etc.) "
            + "and NestJS (@Get, @Post, etc.) patterns. Returns filePath, endpointCount, "
            + "endpoints list with method/path/framework, and scannedAt timestamp.")
    public Map<String, Object> extractEndpoints(
            @ToolParam(description = "Absolute path to the source file to scan for API endpoints") String filePath) {
        return apiDocumentationUseCase.extractEndpoints(filePath);
    }

    @Tool(description = "Generate an OpenAPI 3.0.3 specification skeleton from a list of endpoint definitions. "
            + "Converts Express :param to OpenAPI {param} format, generates operationId, tags, "
            + "path parameters, request body for POST/PUT/PATCH, and standard responses (200, 400, 404, 500). "
            + "Returns title, version, endpointCount, and the complete openApiSpec object.")
    public Map<String, Object> generateOpenApi(
            @ToolParam(description = "List of endpoint definitions. Each map should contain: "
                    + "'method' (string: GET, POST, PUT, DELETE, PATCH), 'path' (string), "
                    + "'description' (string, optional)")
            List<Map<String, Object>> endpoints,
            @ToolParam(description = "The API title for the OpenAPI info section") String title,
            @ToolParam(description = "The API version (e.g. '1.0.0')") String version) {
        return apiDocumentationUseCase.generateOpenApi(endpoints, title, version);
    }

    @Tool(description = "Find undocumented exports and public declarations in a source file. "
            + "Checks for JavaDoc or JSDoc comments preceding function, class, interface, type, "
            + "const, enum, and var declarations. Returns filePath, totalExports, documented count, "
            + "undocumented count, coveragePercent, and lists of documented/undocumented items.")
    public Map<String, Object> findUndocumented(
            @ToolParam(description = "Absolute path to the source file to check for documentation coverage")
            String filePath) {
        return apiDocumentationUseCase.findUndocumented(filePath);
    }

    // ==================== CODEBASE KNOWLEDGE ====================

    @Tool(description = "Search for a pattern (string or regex) across files in a directory. "
            + "Skips common non-source directories (node_modules, .git, dist, target, etc.). "
            + "Returns totalFilesScanned, totalMatches, matchingFiles list, and detailed matches "
            + "with file path, line number, and content.")
    public Map<String, Object> searchCode(
            @ToolParam(description = "Root directory to search in") String directory,
            @ToolParam(description = "Search pattern (string or regex)") String pattern,
            @ToolParam(description = "File extensions to filter (e.g. [\".java\", \".ts\"]). Pass null for all files")
            List<String> fileExtensions,
            @ToolParam(description = "Maximum number of results to return (default 20)") int maxResults) {
        return codebaseKnowledgeUseCase.searchCode(directory, pattern, fileExtensions, maxResults);
    }

    @Tool(description = "Analyze a source file and provide a structural summary including imports, exports, "
            + "functions, classes, interfaces, and type aliases. Returns filePath, fileName, extension, "
            + "lineCount, and categorized lists of declarations.")
    public Map<String, Object> explainModule(
            @ToolParam(description = "Absolute path to the source file to analyze") String filePath) {
        return codebaseKnowledgeUseCase.explainModule(filePath);
    }

    @Tool(description = "Generate an architecture map (text tree) of a directory with file counts and types. "
            + "Skips common non-source directories. Returns directory, tree (formatted text), "
            + "totalFiles, and extensionCounts breakdown.")
    public Map<String, Object> architectureMap(
            @ToolParam(description = "Root directory to map") String directory,
            @ToolParam(description = "Maximum traversal depth (default 3)") int maxDepth) {
        return codebaseKnowledgeUseCase.architectureMap(directory, maxDepth);
    }

    @Tool(description = "Create a dependency graph between internal modules by analyzing import/require statements. "
            + "Only includes local relative imports, excludes node_modules. Returns totalFiles, "
            + "totalDependencyEdges, adjacencyList, and a Mermaid diagram string.")
    public Map<String, Object> dependencyGraph(
            @ToolParam(description = "Root directory to scan for source files") String directory) {
        return codebaseKnowledgeUseCase.dependencyGraph(directory);
    }

    @Tool(description = "Track changes to codebase modules over time, or view change history. "
            + "If changeType is provided, records the change. Otherwise, returns history for the module. "
            + "Change types: feature, bugfix, refactor, dependency-update, performance, security.")
    public Map<String, Object> trackChanges(
            @ToolParam(description = "Module path (e.g. 'src/auth')") String modulePath,
            @ToolParam(description = "Type of change: feature, bugfix, refactor, dependency-update, etc. "
                    + "Pass null to view history instead") String changeType,
            @ToolParam(description = "Description of the change. Required when recording") String description,
            @ToolParam(description = "Number of files changed") int filesChanged,
            @ToolParam(description = "Author of the change") String author,
            @ToolParam(description = "Git commit hash or reference") String commitRef,
            @ToolParam(description = "Limit for history results (default 20)") int historyLimit) {
        return codebaseKnowledgeUseCase.trackChanges(modulePath, changeType, description,
                filesChanged, author, commitRef, historyLimit);
    }
}
