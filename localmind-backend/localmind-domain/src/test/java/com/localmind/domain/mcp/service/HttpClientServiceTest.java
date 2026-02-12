package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.HttpRequestResult;
import com.localmind.domain.mcp.port.out.HttpRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpClientServiceTest {

    @Mock
    private HttpRequestRepository httpRequestRepository;

    @Mock
    private HttpClient httpClient;

    private HttpClientService httpClientService;

    @BeforeEach
    void setUp() {
        httpClientService = new HttpClientService(httpRequestRepository, httpClient);
    }

    // --- sendRequest tests ---

    @Test
    @SuppressWarnings("unchecked")
    void sendRequest_withGetMethod_returnsResponse() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"message\":\"ok\"}");

        Map<String, List<String>> headerMap = new HashMap<>();
        headerMap.put("content-type", List.of("application/json"));
        headerMap.put("x-request-id", List.of("abc-123"));
        HttpHeaders httpHeaders = HttpHeaders.of(headerMap, (k, v) -> true);
        when(mockResponse.headers()).thenReturn(httpHeaders);

        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockResponse);
        when(httpRequestRepository.save(any(HttpRequestResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = httpClientService.sendRequest(
                "https://api.example.com/data", "GET", null, null, null, 5000);

        assertThat(result.get("url")).isEqualTo("https://api.example.com/data");
        assertThat(result.get("method")).isEqualTo("GET");
        assertThat(result.get("statusCode")).isEqualTo(200);
        assertThat(result.get("responseBody")).isEqualTo("{\"message\":\"ok\"}");
        assertThat(result.get("contentType")).isEqualTo("application/json");
        assertThat(result.get("responseTimeMs")).isNotNull();
        assertThat((Map<String, String>) result.get("responseHeaders")).containsKey("content-type");

        verify(httpRequestRepository).save(any(HttpRequestResult.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendRequest_withPostMethodAndHeaders_sendsCorrectRequest() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn("{\"id\":1}");

        Map<String, List<String>> headerMap = new HashMap<>();
        headerMap.put("content-type", List.of("application/json"));
        HttpHeaders httpHeaders = HttpHeaders.of(headerMap, (k, v) -> true);
        when(mockResponse.headers()).thenReturn(httpHeaders);

        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockResponse);
        when(httpRequestRepository.save(any(HttpRequestResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, String> headers = Map.of("Content-Type", "application/json", "Authorization", "Bearer token123");
        String body = "{\"name\":\"test\"}";

        Map<String, Object> result = httpClientService.sendRequest(
                "https://api.example.com/items", "POST", headers, body, null, 5000);

        assertThat(result.get("method")).isEqualTo("POST");
        assertThat(result.get("statusCode")).isEqualTo(201);
        assertThat(result.get("responseBody")).isEqualTo("{\"id\":1}");

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), eq(HttpResponse.BodyHandlers.ofString()));

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.method()).isEqualTo("POST");
        assertThat(capturedRequest.uri().toString()).isEqualTo("https://api.example.com/items");
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendRequest_withQueryParams_appendsToUrl() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("ok");

        Map<String, List<String>> headerMap = new HashMap<>();
        HttpHeaders httpHeaders = HttpHeaders.of(headerMap, (k, v) -> true);
        when(mockResponse.headers()).thenReturn(httpHeaders);

        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockResponse);
        when(httpRequestRepository.save(any(HttpRequestResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "10");

        Map<String, Object> result = httpClientService.sendRequest(
                "https://api.example.com/items", "GET", null, null, queryParams, 5000);

        String url = (String) result.get("url");
        assertThat(url).startsWith("https://api.example.com/items?");
        assertThat(url).contains("page=1");
        assertThat(url).contains("size=10");
    }

    @Test
    void sendRequest_withError_returnsErrorMap() throws Exception {
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenThrow(new IOException("Connection refused"));

        Map<String, Object> result = httpClientService.sendRequest(
                "https://api.example.com/fail", "GET", null, null, null, 5000);

        assertThat(result.get("url")).isEqualTo("https://api.example.com/fail");
        assertThat(result.get("method")).isEqualTo("GET");
        assertThat(result.get("statusCode")).isEqualTo(-1);
        assertThat((String) result.get("error")).contains("IOException");
        assertThat((String) result.get("error")).contains("Connection refused");

        verify(httpRequestRepository, never()).save(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendRequest_withLongBody_truncatesResponseBody() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        String longBody = "A".repeat(15000);
        when(mockResponse.body()).thenReturn(longBody);

        Map<String, List<String>> headerMap = new HashMap<>();
        HttpHeaders httpHeaders = HttpHeaders.of(headerMap, (k, v) -> true);
        when(mockResponse.headers()).thenReturn(httpHeaders);

        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockResponse);
        when(httpRequestRepository.save(any(HttpRequestResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = httpClientService.sendRequest(
                "https://api.example.com/large", "GET", null, null, null, 5000);

        String responseBody = (String) result.get("responseBody");
        assertThat(responseBody.length()).isLessThan(longBody.length());
        assertThat(responseBody).endsWith("... [truncated]");
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendRequest_savesResultToRepository() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("response content");

        Map<String, List<String>> headerMap = new HashMap<>();
        headerMap.put("content-type", List.of("text/plain"));
        HttpHeaders httpHeaders = HttpHeaders.of(headerMap, (k, v) -> true);
        when(mockResponse.headers()).thenReturn(httpHeaders);

        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockResponse);
        when(httpRequestRepository.save(any(HttpRequestResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        httpClientService.sendRequest("https://api.example.com/test", "GET", null, null, null, 5000);

        ArgumentCaptor<HttpRequestResult> captor = ArgumentCaptor.forClass(HttpRequestResult.class);
        verify(httpRequestRepository).save(captor.capture());

        HttpRequestResult saved = captor.getValue();
        assertThat(saved.getUrl()).isEqualTo("https://api.example.com/test");
        assertThat(saved.getMethod()).isEqualTo("GET");
        assertThat(saved.getStatusCode()).isEqualTo(200);
        assertThat(saved.getResponseBody()).isEqualTo("response content");
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // --- generateCurl tests ---

    @Test
    void generateCurl_withGetNoBody_returnsSimpleCommand() {
        Map<String, Object> result = httpClientService.generateCurl("GET", "https://api.example.com/data", null, null);

        assertThat(result.get("command")).isEqualTo("curl 'https://api.example.com/data'");
    }

    @Test
    void generateCurl_withPostAndHeaders_returnsFullCommand() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer token123");

        Map<String, Object> result = httpClientService.generateCurl(
                "POST", "https://api.example.com/items", headers, "{\"name\":\"test\"}");

        String command = (String) result.get("command");
        assertThat(command).startsWith("curl -X POST");
        assertThat(command).contains("-H 'Content-Type: application/json'");
        assertThat(command).contains("-H 'Authorization: Bearer token123'");
        assertThat(command).contains("-d '{\"name\":\"test\"}'");
        assertThat(command).contains("'https://api.example.com/items'");
    }

    @Test
    void generateCurl_withBody_includesDataFlag() {
        String body = "{\"key\":\"value\"}";

        Map<String, Object> result = httpClientService.generateCurl(
                "PUT", "https://api.example.com/resource/1", null, body);

        String command = (String) result.get("command");
        assertThat(command).contains("-X PUT");
        assertThat(command).contains("-d '{\"key\":\"value\"}'");
        assertThat(command).contains("'https://api.example.com/resource/1'");
    }

    @Test
    void generateCurl_withDeleteMethod_includesMethodFlag() {
        Map<String, Object> result = httpClientService.generateCurl(
                "DELETE", "https://api.example.com/resource/1", null, null);

        String command = (String) result.get("command");
        assertThat(command).isEqualTo("curl -X DELETE 'https://api.example.com/resource/1'");
    }

    @Test
    void generateCurl_withNullMethod_defaultsToGet() {
        Map<String, Object> result = httpClientService.generateCurl(
                null, "https://api.example.com/data", null, null);

        String command = (String) result.get("command");
        assertThat(command).isEqualTo("curl 'https://api.example.com/data'");
    }

    @Test
    void generateCurl_withBodyContainingSingleQuotes_escapesQuotes() {
        String body = "it's a test";

        Map<String, Object> result = httpClientService.generateCurl(
                "POST", "https://api.example.com/test", null, body);

        String command = (String) result.get("command");
        assertThat(command).contains("-d 'it'\\''s a test'");
    }

    // --- compareResponses tests ---

    @Test
    @SuppressWarnings("unchecked")
    void compareResponses_withIdenticalResponses_returnsMatchingComparison() throws Exception {
        HttpResponse<String> mockResponse1 = mock(HttpResponse.class);
        when(mockResponse1.statusCode()).thenReturn(200);
        when(mockResponse1.body()).thenReturn("{\"data\":\"same\"}");
        Map<String, List<String>> headerMap1 = new HashMap<>();
        headerMap1.put("content-type", List.of("application/json"));
        HttpHeaders headers1 = HttpHeaders.of(headerMap1, (k, v) -> true);
        when(mockResponse1.headers()).thenReturn(headers1);

        HttpResponse<String> mockResponse2 = mock(HttpResponse.class);
        when(mockResponse2.statusCode()).thenReturn(200);
        when(mockResponse2.body()).thenReturn("{\"data\":\"same\"}");
        Map<String, List<String>> headerMap2 = new HashMap<>();
        headerMap2.put("content-type", List.of("application/json"));
        HttpHeaders headers2 = HttpHeaders.of(headerMap2, (k, v) -> true);
        when(mockResponse2.headers()).thenReturn(headers2);

        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockResponse1)
                .thenReturn(mockResponse2);
        when(httpRequestRepository.save(any(HttpRequestResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = httpClientService.compareResponses(
                "https://api.example.com/v1/data", "https://api.example.com/v2/data", "GET");

        Map<String, Object> baseline = (Map<String, Object>) result.get("baseline");
        Map<String, Object> current = (Map<String, Object>) result.get("current");
        Map<String, Object> comparison = (Map<String, Object>) result.get("comparison");

        assertThat(baseline.get("statusCode")).isEqualTo(200);
        assertThat(current.get("statusCode")).isEqualTo(200);
        assertThat(comparison.get("statusMatch")).isEqualTo(true);
        assertThat(comparison.get("bodyIdentical")).isEqualTo(true);
        assertThat(comparison).doesNotContainKey("firstDifference");
    }

    @Test
    @SuppressWarnings("unchecked")
    void compareResponses_withDifferentResponses_returnsDifferences() throws Exception {
        HttpResponse<String> mockResponse1 = mock(HttpResponse.class);
        when(mockResponse1.statusCode()).thenReturn(200);
        when(mockResponse1.body()).thenReturn("{\"version\":\"1.0\"}");
        Map<String, List<String>> headerMap1 = new HashMap<>();
        headerMap1.put("content-type", List.of("application/json"));
        HttpHeaders headers1 = HttpHeaders.of(headerMap1, (k, v) -> true);
        when(mockResponse1.headers()).thenReturn(headers1);

        HttpResponse<String> mockResponse2 = mock(HttpResponse.class);
        when(mockResponse2.statusCode()).thenReturn(404);
        when(mockResponse2.body()).thenReturn("{\"error\":\"not found\"}");
        Map<String, List<String>> headerMap2 = new HashMap<>();
        headerMap2.put("content-type", List.of("text/plain"));
        HttpHeaders headers2 = HttpHeaders.of(headerMap2, (k, v) -> true);
        when(mockResponse2.headers()).thenReturn(headers2);

        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockResponse1)
                .thenReturn(mockResponse2);
        when(httpRequestRepository.save(any(HttpRequestResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = httpClientService.compareResponses(
                "https://api.example.com/v1/data", "https://api.example.com/v2/data", "GET");

        Map<String, Object> baseline = (Map<String, Object>) result.get("baseline");
        Map<String, Object> current = (Map<String, Object>) result.get("current");
        Map<String, Object> comparison = (Map<String, Object>) result.get("comparison");

        assertThat(baseline.get("statusCode")).isEqualTo(200);
        assertThat(current.get("statusCode")).isEqualTo(404);
        assertThat(comparison.get("statusMatch")).isEqualTo(false);
        assertThat(comparison.get("bodyIdentical")).isEqualTo(false);
        assertThat(comparison).containsKey("firstDifference");
    }
}
