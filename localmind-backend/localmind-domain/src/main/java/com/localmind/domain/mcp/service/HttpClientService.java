package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.HttpRequestResult;
import com.localmind.domain.mcp.port.in.HttpClientUseCase;
import com.localmind.domain.mcp.port.out.HttpRequestRepository;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Domain service implementing the http-client tool group.
 * Provides HTTP request execution, response comparison, and cURL generation.
 * No Spring annotations - registered as a bean via DomainConfig.
 */
public class HttpClientService implements HttpClientUseCase {

    private static final int MAX_RESPONSE_BODY_LENGTH = 10000;
    private static final int DEFAULT_TIMEOUT_MS = 30000;

    private final HttpRequestRepository httpRequestRepository;
    private final HttpClient httpClient;

    /**
     * Constructor with injectable HttpClient for testability.
     */
    public HttpClientService(HttpRequestRepository httpRequestRepository, HttpClient httpClient) {
        this.httpRequestRepository = httpRequestRepository;
        this.httpClient = httpClient;
    }

    /**
     * Constructor that creates a default HttpClient.
     */
    public HttpClientService(HttpRequestRepository httpRequestRepository) {
        this(httpRequestRepository, HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    @Override
    public Map<String, Object> sendRequest(String url, String method, Map<String, String> headers,
                                           String body, Map<String, String> queryParams, int timeoutMs) {
        String fullUrl = buildUrlWithQueryParams(url, queryParams);
        int effectiveTimeout = timeoutMs > 0 ? timeoutMs : DEFAULT_TIMEOUT_MS;

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .timeout(Duration.ofMillis(effectiveTimeout));

        // Set headers
        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }

        // Set method and body
        String upperMethod = method != null ? method.toUpperCase() : "GET";
        HttpRequest.BodyPublisher bodyPublisher = (body != null && !body.isEmpty())
                ? HttpRequest.BodyPublishers.ofString(body)
                : HttpRequest.BodyPublishers.noBody();

        switch (upperMethod) {
            case "GET" -> requestBuilder.GET();
            case "POST" -> requestBuilder.POST(bodyPublisher);
            case "PUT" -> requestBuilder.PUT(bodyPublisher);
            case "DELETE" -> requestBuilder.DELETE();
            case "PATCH" -> requestBuilder.method("PATCH", bodyPublisher);
            case "HEAD" -> requestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
            case "OPTIONS" -> requestBuilder.method("OPTIONS", HttpRequest.BodyPublishers.noBody());
            default -> requestBuilder.method(upperMethod, bodyPublisher);
        }

        HttpRequest httpRequest = requestBuilder.build();

        try {
            long startTime = System.nanoTime();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            long responseTimeMs = (System.nanoTime() - startTime) / 1_000_000;

            Map<String, String> responseHeaders = new LinkedHashMap<>();
            response.headers().map().forEach((key, values) -> {
                if (key != null && !values.isEmpty()) {
                    responseHeaders.put(key, String.join(", ", values));
                }
            });

            String responseBody = response.body();
            String truncatedBody = truncateBody(responseBody);

            String contentType = responseHeaders.getOrDefault("content-type",
                    responseHeaders.getOrDefault("Content-Type", "unknown"));

            // Persist the result
            HttpRequestResult result = HttpRequestResult.builder()
                    .id(UUID.randomUUID().toString())
                    .url(fullUrl)
                    .method(upperMethod)
                    .statusCode(response.statusCode())
                    .responseHeaders(responseHeaders)
                    .responseBody(truncatedBody)
                    .responseTimeMs(responseTimeMs)
                    .createdAt(Instant.now())
                    .build();

            httpRequestRepository.save(result);

            // Build response map
            Map<String, Object> resultMap = new LinkedHashMap<>();
            resultMap.put("url", fullUrl);
            resultMap.put("method", upperMethod);
            resultMap.put("statusCode", response.statusCode());
            resultMap.put("responseHeaders", responseHeaders);
            resultMap.put("responseBody", truncatedBody);
            resultMap.put("responseTimeMs", responseTimeMs);
            resultMap.put("contentType", contentType);

            return resultMap;

        } catch (Exception e) {
            Map<String, Object> errorMap = new LinkedHashMap<>();
            errorMap.put("url", fullUrl);
            errorMap.put("method", upperMethod);
            errorMap.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            errorMap.put("statusCode", -1);
            return errorMap;
        }
    }

    @Override
    public Map<String, Object> compareResponses(String baselineUrl, String currentUrl, String method) {
        Map<String, Object> baselineResult = sendRequest(baselineUrl, method, null, null, null, DEFAULT_TIMEOUT_MS);
        Map<String, Object> currentResult = sendRequest(currentUrl, method, null, null, null, DEFAULT_TIMEOUT_MS);

        int baselineStatus = getIntValue(baselineResult, "statusCode");
        int currentStatus = getIntValue(currentResult, "statusCode");
        long baselineTime = getLongValue(baselineResult, "responseTimeMs");
        long currentTime = getLongValue(currentResult, "responseTimeMs");
        String baselineBody = getStringValue(baselineResult, "responseBody");
        String currentBody = getStringValue(currentResult, "responseBody");

        boolean statusMatch = baselineStatus == currentStatus;
        long timeDiffMs = currentTime - baselineTime;
        boolean bodyIdentical = baselineBody.equals(currentBody);

        // Find first difference in body
        String firstDifference = null;
        if (!bodyIdentical) {
            firstDifference = findFirstDifference(baselineBody, currentBody);
        }

        // Compare headers
        @SuppressWarnings("unchecked")
        Map<String, String> baselineHeaders = (Map<String, String>) baselineResult.getOrDefault("responseHeaders", Map.of());
        @SuppressWarnings("unchecked")
        Map<String, String> currentHeaders = (Map<String, String>) currentResult.getOrDefault("responseHeaders", Map.of());

        Map<String, Object> headersDiff = compareHeaders(baselineHeaders, currentHeaders);

        // Build baseline summary
        Map<String, Object> baselineSummary = new LinkedHashMap<>();
        baselineSummary.put("statusCode", baselineStatus);
        baselineSummary.put("responseTimeMs", baselineTime);
        baselineSummary.put("bodyLength", baselineBody.length());

        // Build current summary
        Map<String, Object> currentSummary = new LinkedHashMap<>();
        currentSummary.put("statusCode", currentStatus);
        currentSummary.put("responseTimeMs", currentTime);
        currentSummary.put("bodyLength", currentBody.length());

        // Build comparison
        Map<String, Object> comparison = new LinkedHashMap<>();
        comparison.put("statusMatch", statusMatch);
        comparison.put("timeDiffMs", timeDiffMs);
        comparison.put("bodyIdentical", bodyIdentical);
        if (firstDifference != null) {
            comparison.put("firstDifference", firstDifference);
        }
        comparison.put("headersDiff", headersDiff);

        // Build final result
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("baseline", baselineSummary);
        result.put("current", currentSummary);
        result.put("comparison", comparison);

        return result;
    }

    @Override
    public Map<String, Object> generateCurl(String method, String url, Map<String, String> headers, String body) {
        StringBuilder curl = new StringBuilder("curl");

        String upperMethod = method != null ? method.toUpperCase() : "GET";
        if (!"GET".equals(upperMethod)) {
            curl.append(" -X ").append(upperMethod);
        }

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                curl.append(" -H '").append(entry.getKey()).append(": ").append(entry.getValue()).append("'");
            }
        }

        if (body != null && !body.isEmpty()) {
            String escapedBody = body.replace("'", "'\\''");
            curl.append(" -d '").append(escapedBody).append("'");
        }

        curl.append(" '").append(url).append("'");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("command", curl.toString());
        return result;
    }

    // --- Private helper methods ---

    private String buildUrlWithQueryParams(String url, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return url;
        }

        String queryString = queryParams.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        String separator = url.contains("?") ? "&" : "?";
        return url + separator + queryString;
    }

    private String truncateBody(String body) {
        if (body == null) {
            return "";
        }
        if (body.length() > MAX_RESPONSE_BODY_LENGTH) {
            return body.substring(0, MAX_RESPONSE_BODY_LENGTH) + "... [truncated]";
        }
        return body;
    }

    private String findFirstDifference(String baseline, String current) {
        int minLen = Math.min(baseline.length(), current.length());
        for (int i = 0; i < minLen; i++) {
            if (baseline.charAt(i) != current.charAt(i)) {
                int start = Math.max(0, i - 20);
                int end = Math.min(minLen, i + 20);
                return "Position " + i + ": baseline='" + baseline.substring(start, Math.min(end, baseline.length()))
                        + "' vs current='" + current.substring(start, Math.min(end, current.length())) + "'";
            }
        }
        if (baseline.length() != current.length()) {
            return "Length difference: baseline=" + baseline.length() + " vs current=" + current.length();
        }
        return null;
    }

    private Map<String, Object> compareHeaders(Map<String, String> baseline, Map<String, String> current) {
        Map<String, Object> diff = new LinkedHashMap<>();

        Map<String, String> added = new HashMap<>();
        Map<String, String> removed = new HashMap<>();
        Map<String, Map<String, String>> changed = new HashMap<>();

        for (Map.Entry<String, String> entry : current.entrySet()) {
            if (!baseline.containsKey(entry.getKey())) {
                added.put(entry.getKey(), entry.getValue());
            } else if (!baseline.get(entry.getKey()).equals(entry.getValue())) {
                Map<String, String> change = new LinkedHashMap<>();
                change.put("baseline", baseline.get(entry.getKey()));
                change.put("current", entry.getValue());
                changed.put(entry.getKey(), change);
            }
        }

        for (Map.Entry<String, String> entry : baseline.entrySet()) {
            if (!current.containsKey(entry.getKey())) {
                removed.put(entry.getKey(), entry.getValue());
            }
        }

        if (!added.isEmpty()) diff.put("added", added);
        if (!removed.isEmpty()) diff.put("removed", removed);
        if (!changed.isEmpty()) diff.put("changed", changed);

        return diff;
    }

    private int getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        return -1;
    }

    private long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        return 0L;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
}
