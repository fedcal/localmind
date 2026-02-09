package com.localmind.infrastructure.mcp.server;

import com.localmind.domain.document.model.SearchResult;
import com.localmind.domain.document.port.in.DocumentSearchUseCase;
import com.localmind.domain.llm.model.ChatMessage;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.LlmRequest;
import com.localmind.domain.llm.model.LlmResponse;
import com.localmind.domain.llm.port.in.ChatUseCase;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)
public class LocalMindMcpTools {

    private final DocumentSearchUseCase documentSearchUseCase;
    private final ChatUseCase chatUseCase;

    public LocalMindMcpTools(DocumentSearchUseCase documentSearchUseCase, ChatUseCase chatUseCase) {
        this.documentSearchUseCase = documentSearchUseCase;
        this.chatUseCase = chatUseCase;
    }

    @Tool(description = "Search documents in the LocalMind knowledge base using RAG (Retrieval-Augmented Generation). Returns relevant document chunks with similarity scores.")
    public List<Map<String, Object>> documentSearch(
            @ToolParam(description = "The search query text") String query,
            @ToolParam(description = "Number of top results to return (default 5)") int topK) {
        if (topK <= 0) topK = 5;
        List<SearchResult> results = documentSearchUseCase.search(query, topK);
        return results.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("documentId", r.getDocumentId());
                    map.put("filename", r.getFilename());
                    map.put("content", r.getContent());
                    map.put("score", r.getScore());
                    map.put("chunkIndex", r.getChunkIndex());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Tool(description = "Send a message to an LLM through LocalMind's multi-provider gateway. Supports Ollama, OpenAI, Anthropic, Google.")
    public Map<String, Object> chat(
            @ToolParam(description = "The message to send to the LLM") String message,
            @ToolParam(description = "LLM provider: OLLAMA, OPENAI, ANTHROPIC, GOOGLE (optional)") String provider,
            @ToolParam(description = "Specific model name (optional)") String model,
            @ToolParam(description = "Temperature for generation, 0.0-1.0 (optional, default 0.7)") Double temperature) {

        LlmRequest.LlmRequestBuilder requestBuilder = LlmRequest.builder()
                .messages(List.of(ChatMessage.builder()
                        .role(ChatMessage.Role.USER)
                        .content(message)
                        .build()));

        if (provider != null && !provider.isBlank()) {
            requestBuilder.provider(LlmProvider.valueOf(provider.toUpperCase()));
        }
        if (model != null && !model.isBlank()) {
            requestBuilder.model(model);
        }
        if (temperature != null) {
            requestBuilder.temperature(temperature);
        }

        LlmResponse response = chatUseCase.chat(requestBuilder.build());

        Map<String, Object> result = new HashMap<>();
        result.put("content", response.getContent());
        result.put("model", response.getModel());
        result.put("provider", response.getProvider().name());
        result.put("latencyMs", response.getLatencyMs());
        return result;
    }

    @Tool(description = "List available LLM providers and their status in LocalMind")
    public Map<String, Object> listModels() {
        Map<String, Object> result = new HashMap<>();
        result.put("providers", List.of("OLLAMA", "OPENAI", "ANTHROPIC", "GOOGLE"));
        result.put("defaultProvider", "OLLAMA");
        return result;
    }
}
