package com.localmind.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Main entry point for the LocalMind Java SDK.
 *
 * <p>Use the builder pattern to create an instance:
 * <pre>{@code
 * LocalMindClient client = LocalMindClient.builder()
 *     .baseUrl("http://localhost:8080/api/v1")
 *     .build();
 *
 * // Simple chat
 * ChatResponse response = client.chat("Riassumi il documento...");
 *
 * // Chat with options
 * ChatResponse response = client.chat("Explain this code", ChatOptions.builder()
 *     .provider("OLLAMA")
 *     .model("llama3")
 *     .enableRag(true)
 *     .build());
 *
 * // Authenticate (if auth is configured)
 * client.login("your-password");
 *
 * // Sub-clients
 * client.documents().list();
 * client.search().query("deployment guide");
 * client.conversations().list();
 * }</pre>
 */
public class LocalMindClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration requestTimeout;
    private String authToken;

    private DocumentClient documentClient;
    private SearchClient searchClient;
    private ConversationClient conversationClient;

    private LocalMindClient(Builder builder) {
        this.baseUrl = builder.baseUrl.replaceAll("/+$", "");
        this.authToken = builder.authToken;
        this.requestTimeout = builder.requestTimeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(builder.connectTimeout)
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // ---- Authentication ----

    /**
     * Authenticate with the LocalMind instance using a password.
     * The token is stored internally and used for subsequent requests.
     *
     * @param password the configured password
     * @return the JWT token
     */
    public String login(String password) {
        Map<String, String> body = Map.of("password", password);
        String json = post("/auth/login", body);
        Map<String, Object> response = parseJson(json, new TypeReference<>() {});
        this.authToken = (String) response.get("token");
        return this.authToken;
    }

    // ---- Chat ----

    /**
     * Send a simple chat message using default provider and settings.
     */
    public ChatResponse chat(String message) {
        return chat(message, (ChatOptions) null);
    }

    /**
     * Send a chat message with provider and model overrides.
     */
    public ChatResponse chat(String message, String provider, String model) {
        return chat(message, ChatOptions.builder()
                .provider(provider)
                .model(model)
                .build());
    }

    /**
     * Send a chat message with full options.
     */
    public ChatResponse chat(String message, ChatOptions options) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        if (options != null) {
            if (options.getProvider() != null) body.put("provider", options.getProvider());
            if (options.getModel() != null) body.put("model", options.getModel());
            if (options.getConversationId() != null) body.put("conversationId", options.getConversationId());
            if (options.getSystemPrompt() != null) body.put("systemPrompt", options.getSystemPrompt());
            body.put("temperature", options.getTemperature());
            if (options.getMaxTokens() > 0) body.put("maxTokens", options.getMaxTokens());
            body.put("enableToolCalling", options.isEnableToolCalling());
            body.put("enableRag", options.isEnableRag());
        }
        String json = post("/chat", body);
        return parseJson(json, ChatResponse.class);
    }

    // ---- Health ----

    /**
     * Check the health status of the LocalMind platform.
     */
    public HealthStatus health() {
        String json = get("/dashboard/health");
        return parseJson(json, HealthStatus.class);
    }

    // ---- Sub-clients ----

    /**
     * Access document management operations.
     */
    public DocumentClient documents() {
        if (documentClient == null) {
            documentClient = new DocumentClient(this);
        }
        return documentClient;
    }

    /**
     * Access semantic search operations.
     */
    public SearchClient search() {
        if (searchClient == null) {
            searchClient = new SearchClient(this);
        }
        return searchClient;
    }

    /**
     * Access conversation management operations.
     */
    public ConversationClient conversations() {
        if (conversationClient == null) {
            conversationClient = new ConversationClient(this);
        }
        return conversationClient;
    }

    // ---- Internal HTTP methods ----

    HttpRequest.Builder requestBuilder(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(requestTimeout);
        if (authToken != null && !authToken.isBlank()) {
            builder.header("Authorization", "Bearer " + authToken);
        }
        return builder;
    }

    String get(String path) {
        HttpRequest request = requestBuilder(path)
                .GET()
                .build();
        return executeRequest(request);
    }

    byte[] getBytes(String path) {
        HttpRequest request = requestBuilder(path)
                .GET()
                .build();
        return executeRequestBytes(request);
    }

    String post(String path, Object body) {
        HttpRequest request = requestBuilder(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(body)))
                .build();
        return executeRequest(request);
    }

    String patch(String path, Object body) {
        HttpRequest request = requestBuilder(path)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(toJson(body)))
                .build();
        return executeRequest(request);
    }

    String delete(String path) {
        HttpRequest request = requestBuilder(path)
                .DELETE()
                .build();
        return executeRequest(request);
    }

    String executeRequest(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new LocalMindException(
                        "HTTP " + response.statusCode() + " " + request.method() + " " + request.uri(),
                        response.statusCode(),
                        response.body());
            }
            return response.body();
        } catch (LocalMindException e) {
            throw e;
        } catch (IOException e) {
            throw new LocalMindException("Connection error: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LocalMindException("Request interrupted", e);
        }
    }

    private byte[] executeRequestBytes(HttpRequest request) {
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() >= 400) {
                throw new LocalMindException(
                        "HTTP " + response.statusCode() + " " + request.method() + " " + request.uri(),
                        response.statusCode(),
                        new String(response.body()));
            }
            return response.body();
        } catch (LocalMindException e) {
            throw e;
        } catch (IOException e) {
            throw new LocalMindException("Connection error: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LocalMindException("Request interrupted", e);
        }
    }

    <T> T parseJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new LocalMindException("Failed to parse response: " + e.getMessage(), e);
        }
    }

    <T> T parseJson(String json, TypeReference<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new LocalMindException("Failed to parse response: " + e.getMessage(), e);
        }
    }

    private String toJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new LocalMindException("Failed to serialize request body: " + e.getMessage(), e);
        }
    }

    // ---- Builder ----

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String baseUrl = "http://localhost:8080/api/v1";
        private String authToken;
        private Duration connectTimeout = Duration.ofSeconds(30);
        private Duration requestTimeout = Duration.ofSeconds(120);

        /**
         * Base URL of the LocalMind API (default: http://localhost:8080/api/v1).
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Pre-set an authentication token (JWT). Alternatively, call {@link LocalMindClient#login(String)}.
         */
        public Builder authToken(String token) {
            this.authToken = token;
            return this;
        }

        /**
         * Connection timeout (default: 30 seconds).
         */
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * Per-request timeout (default: 120 seconds). LLM responses can be slow.
         */
        public Builder requestTimeout(Duration requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        public LocalMindClient build() {
            return new LocalMindClient(this);
        }
    }
}
