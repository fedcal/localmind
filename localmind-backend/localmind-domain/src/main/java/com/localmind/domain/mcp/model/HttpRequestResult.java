package com.localmind.domain.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Domain model representing the result of an HTTP request executed by the http-client tool group.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpRequestResult {
    private String id;
    private String url;
    private String method;
    private int statusCode;
    private Map<String, String> responseHeaders;
    private String responseBody;
    private long responseTimeMs;
    private Instant createdAt;
}
