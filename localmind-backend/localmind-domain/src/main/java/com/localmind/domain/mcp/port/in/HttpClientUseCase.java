package com.localmind.domain.mcp.port.in;

import java.util.Map;

/**
 * Use case interface for the http-client tool group.
 * Provides HTTP request execution, response comparison, and cURL command generation.
 */
public interface HttpClientUseCase {

    /**
     * Sends an HTTP request and returns the result as a structured map.
     *
     * @param url         the target URL
     * @param method      HTTP method (GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS)
     * @param headers     optional request headers
     * @param body        optional request body
     * @param queryParams optional query parameters to append to the URL
     * @param timeoutMs   request timeout in milliseconds
     * @return a map containing url, method, statusCode, responseHeaders, responseBody, responseTimeMs, contentType
     */
    Map<String, Object> sendRequest(String url, String method, Map<String, String> headers, String body, Map<String, String> queryParams, int timeoutMs);

    /**
     * Compares the responses of two HTTP requests (baseline vs current).
     *
     * @param baselineUrl the baseline URL
     * @param currentUrl  the current URL to compare against baseline
     * @param method      HTTP method to use for both requests
     * @return a map with baseline, current, and comparison details
     */
    Map<String, Object> compareResponses(String baselineUrl, String currentUrl, String method);

    /**
     * Generates a cURL command equivalent to the given HTTP request parameters.
     *
     * @param method  HTTP method
     * @param url     the target URL
     * @param headers optional headers
     * @param body    optional request body
     * @return a map containing the generated cURL command string
     */
    Map<String, Object> generateCurl(String method, String url, Map<String, String> headers, String body);
}
