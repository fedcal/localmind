package com.localmind.infrastructure.mcp.server;

import com.localmind.domain.mcp.model.CodeSnippet;
import com.localmind.domain.mcp.port.in.HttpClientUseCase;
import com.localmind.domain.mcp.port.in.RegexUseCase;
import com.localmind.domain.mcp.port.in.SnippetUseCase;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)
public class LocalMindUtilityTools {

    private final RegexUseCase regexUseCase;
    private final HttpClientUseCase httpClientUseCase;
    private final SnippetUseCase snippetUseCase;

    public LocalMindUtilityTools(RegexUseCase regexUseCase,
                                 HttpClientUseCase httpClientUseCase,
                                 SnippetUseCase snippetUseCase) {
        this.regexUseCase = regexUseCase;
        this.httpClientUseCase = httpClientUseCase;
        this.snippetUseCase = snippetUseCase;
    }

    // ==================== REGEX BUILDER ====================

    @Tool(description = "Test a regex pattern against a list of test strings. Returns match results including captured groups for each string.")
    public Map<String, Object> testRegex(
            @ToolParam(description = "The regex pattern to test") String pattern,
            @ToolParam(description = "List of strings to test against the pattern") List<String> testStrings,
            @ToolParam(description = "Optional flags: i=case-insensitive, m=multiline, s=dotall, x=comments") String flags) {
        return regexUseCase.testRegex(pattern, testStrings, flags);
    }

    @Tool(description = "Explain a regex pattern in natural language, breaking it down component by component.")
    public Map<String, Object> explainRegex(
            @ToolParam(description = "The regex pattern to explain") String pattern) {
        return regexUseCase.explainRegex(pattern);
    }

    @Tool(description = "Build a regex pattern from a description or keyword. Supports: email, url, ipv4, ipv6, phone, date, time, hex-color, uuid, credit-card.")
    public Map<String, Object> buildRegex(
            @ToolParam(description = "A keyword (e.g. 'email', 'url') or natural language description") String description) {
        return regexUseCase.buildRegex(description);
    }

    @Tool(description = "Analyze a regex pattern and suggest optimizations such as redundant character classes, unnecessary capture groups, and greedy quantifiers.")
    public Map<String, Object> optimizeRegex(
            @ToolParam(description = "The regex pattern to optimize") String pattern) {
        return regexUseCase.optimizeRegex(pattern);
    }

    @Tool(description = "Convert a regex pattern notation between programming language formats: java, python, javascript, pcre.")
    public Map<String, Object> convertRegex(
            @ToolParam(description = "The regex pattern to convert") String pattern,
            @ToolParam(description = "Target format: java, python, javascript, pcre") String toFormat) {
        return regexUseCase.convertRegex(pattern, toFormat);
    }

    // ==================== HTTP CLIENT ====================

    @Tool(description = "Send an HTTP request and return the response including status code, headers, body, and response time in milliseconds.")
    public Map<String, Object> sendHttpRequest(
            @ToolParam(description = "The target URL") String url,
            @ToolParam(description = "HTTP method: GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS") String method,
            @ToolParam(description = "Optional request headers as key-value pairs") Map<String, String> headers,
            @ToolParam(description = "Optional request body") String body,
            @ToolParam(description = "Optional query parameters as key-value pairs") Map<String, String> queryParams,
            @ToolParam(description = "Request timeout in milliseconds (default 30000)") int timeoutMs) {
        if (timeoutMs <= 0) timeoutMs = 30000;
        return httpClientUseCase.sendRequest(url, method, headers, body, queryParams, timeoutMs);
    }

    @Tool(description = "Compare HTTP responses from two URLs. Shows differences in status code, response time, body content, and headers.")
    public Map<String, Object> compareHttpResponses(
            @ToolParam(description = "Baseline URL to compare against") String baselineUrl,
            @ToolParam(description = "Current URL to compare") String currentUrl,
            @ToolParam(description = "HTTP method to use for both requests (default GET)") String method) {
        if (method == null || method.isBlank()) method = "GET";
        return httpClientUseCase.compareResponses(baselineUrl, currentUrl, method);
    }

    @Tool(description = "Generate an equivalent cURL command from HTTP request parameters.")
    public Map<String, Object> generateCurl(
            @ToolParam(description = "HTTP method") String method,
            @ToolParam(description = "Target URL") String url,
            @ToolParam(description = "Optional headers as key-value pairs") Map<String, String> headers,
            @ToolParam(description = "Optional request body") String body) {
        return httpClientUseCase.generateCurl(method, url, headers, body);
    }

    // ==================== SNIPPET MANAGER ====================

    @Tool(description = "Save a reusable code snippet with metadata including title, language, description, and tags for later retrieval.")
    public Map<String, Object> saveSnippet(
            @ToolParam(description = "Title of the snippet") String title,
            @ToolParam(description = "The code content") String code,
            @ToolParam(description = "Programming language (e.g. java, python, javascript)") String language,
            @ToolParam(description = "Optional description of what the snippet does") String description,
            @ToolParam(description = "Optional tags for categorization") List<String> tags) {
        Set<String> tagSet = tags != null ? new LinkedHashSet<>(tags) : Set.of();
        CodeSnippet snippet = snippetUseCase.save(title, code, language, description, tagSet);
        return snippetToMap(snippet);
    }

    @Tool(description = "Search for code snippets by keyword (matches title and code), tag, or programming language. Filters can be combined.")
    public List<Map<String, Object>> searchSnippets(
            @ToolParam(description = "Optional keyword to search in title and code") String keyword,
            @ToolParam(description = "Optional tag to filter by") String tag,
            @ToolParam(description = "Optional programming language to filter by") String language) {
        return snippetUseCase.search(keyword, tag, language).stream()
                .map(this::snippetToMap)
                .collect(Collectors.toList());
    }

    @Tool(description = "Retrieve a specific code snippet by its ID.")
    public Map<String, Object> getSnippet(
            @ToolParam(description = "The snippet ID") String id) {
        return snippetUseCase.getById(id)
                .map(this::snippetToMap)
                .orElse(Map.of("error", "Snippet not found", "id", id));
    }

    @Tool(description = "Delete a code snippet by its ID. Returns whether the deletion was successful.")
    public Map<String, Object> deleteSnippet(
            @ToolParam(description = "The snippet ID to delete") String id) {
        boolean deleted = snippetUseCase.delete(id);
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("deleted", deleted);
        return result;
    }

    @Tool(description = "List all tags used across code snippets with their usage count.")
    public List<Map<String, Object>> listSnippetTags() {
        return snippetUseCase.listTags();
    }

    private Map<String, Object> snippetToMap(CodeSnippet snippet) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", snippet.getId());
        map.put("title", snippet.getTitle());
        map.put("code", snippet.getCode());
        map.put("language", snippet.getLanguage());
        map.put("description", snippet.getDescription());
        map.put("tags", snippet.getTags());
        map.put("createdAt", snippet.getCreatedAt() != null ? snippet.getCreatedAt().toString() : null);
        map.put("updatedAt", snippet.getUpdatedAt() != null ? snippet.getUpdatedAt().toString() : null);
        return map;
    }
}
