package com.localmind.api.llm.controller;

import com.localmind.api.llm.dto.ChatRequestDto;
import com.localmind.api.llm.dto.TokenUsageDto;
import com.localmind.domain.document.model.SearchResult;
import com.localmind.domain.document.port.in.DocumentSearchUseCase;
import com.localmind.domain.llm.model.*;
import com.localmind.domain.llm.port.in.StreamingChatUseCase;
import com.localmind.domain.llm.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat Streaming", description = "SSE streaming chat endpoint")
public class StreamingChatController {

    private static final int RAG_TOP_K = 5;
    private static final long SSE_TIMEOUT = 300_000L; // 5 minuti

    private final StreamingChatUseCase streamingChatUseCase;
    private final ConversationService conversationService;
    private final DocumentSearchUseCase documentSearchUseCase;
    private final int defaultMaxContextMessages;
    private final ExecutorService streamExecutor = Executors.newCachedThreadPool();

    public StreamingChatController(StreamingChatUseCase streamingChatUseCase,
                                    ConversationService conversationService,
                                    DocumentSearchUseCase documentSearchUseCase,
                                    @Value("${localmind.chat.default-max-context-messages:50}") int defaultMaxContextMessages) {
        this.streamingChatUseCase = streamingChatUseCase;
        this.conversationService = conversationService;
        this.documentSearchUseCase = documentSearchUseCase;
        this.defaultMaxContextMessages = defaultMaxContextMessages;
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Chat in streaming SSE / SSE streaming chat")
    @ApiResponse(responseCode = "200", description = "Stream SSE avviato / SSE stream started")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public SseEmitter chatStream(@Valid @RequestBody ChatRequestDto request) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        streamExecutor.execute(() -> {
            try {
                // 1. Crea/recupera conversazione e aggiungi messaggio utente
                ConversationContext conversation = conversationService
                        .getOrCreateForChat(request.getConversationId(), request.getMessage(), request.getSystemPrompt());

                // 2. Invia evento conversation con ID
                emitter.send(SseEmitter.event()
                        .name("conversation")
                        .data(Map.of("conversationId", conversation.getId())));

                // 3. RAG context (preparato PRIMA dello stream)
                List<SearchResult> ragResults = null;
                if (request.isEnableRag()) {
                    ragResults = documentSearchUseCase.search(request.getMessage(), RAG_TOP_K);
                }

                // 4. Costruisci messaggi
                List<ChatMessage> allMessages = buildMessageList(conversation, ragResults);

                LlmRequest llmRequest = LlmRequest.builder()
                        .messages(allMessages)
                        .provider(request.getProvider() != null ? LlmProvider.valueOf(request.getProvider()) : null)
                        .model(request.getModel())
                        .temperature(request.getTemperature())
                        .maxTokens(request.getMaxTokens())
                        .conversationId(conversation.getId())
                        .stream(true)
                        .build();

                // 5. Buffer per accumulare risposta completa
                StringBuilder fullResponse = new StringBuilder();

                // 6. Stream!
                streamingChatUseCase.streamChat(llmRequest,
                        // onToken
                        token -> {
                            try {
                                fullResponse.append(token);
                                emitter.send(SseEmitter.event()
                                        .name("token")
                                        .data(Map.of("content", token)));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        // onComplete
                        response -> {
                            try {
                                // Persisti risposta completa
                                String content = fullResponse.toString();
                                conversationService.addAssistantMessage(conversation.getId(), content);

                                // Aggiorna metadata conversazione
                                updateConversationMetadata(conversation, response);

                                // Invia metadata
                                TokenUsageDto usageDto = response.getTokenUsage() != null
                                        ? TokenUsageDto.builder()
                                        .promptTokens(response.getTokenUsage().getPromptTokens())
                                        .completionTokens(response.getTokenUsage().getCompletionTokens())
                                        .totalTokens(response.getTokenUsage().getTotalTokens())
                                        .build()
                                        : null;

                                emitter.send(SseEmitter.event()
                                        .name("metadata")
                                        .data(Map.of(
                                                "model", response.getModel() != null ? response.getModel() : "",
                                                "provider", response.getProvider() != null ? response.getProvider().name() : "",
                                                "latencyMs", response.getLatencyMs(),
                                                "tokenUsage", usageDto != null ? usageDto : Map.of()
                                        )));

                                // Invia done
                                emitter.send(SseEmitter.event().name("done").data(Map.of()));
                                emitter.complete();
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        // onError
                        error -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("error")
                                        .data(Map.of("message", error.getMessage() != null ? error.getMessage() : "Unknown error")));
                                emitter.complete();
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        }
                );
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Map.of("message", e.getMessage() != null ? e.getMessage() : "Unknown error")));
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        return emitter;
    }

    private List<ChatMessage> buildMessageList(ConversationContext conversation,
                                                List<SearchResult> ragResults) {
        List<ChatMessage> allMessages = new java.util.ArrayList<>();

        if (conversation.getSystemPrompt() != null && !conversation.getSystemPrompt().isBlank()) {
            allMessages.add(ChatMessage.builder()
                    .role(ChatMessage.Role.SYSTEM)
                    .content(conversation.getSystemPrompt())
                    .build());
        }

        if (ragResults != null && !ragResults.isEmpty()) {
            StringBuilder ragContext = new StringBuilder("[Contesto dai tuoi documenti]\n\n");
            for (SearchResult result : ragResults) {
                ragContext.append("--- Fonte: ")
                        .append(result.getFilename() != null ? result.getFilename() : "sconosciuto")
                        .append(" (chunk ").append(result.getChunkIndex())
                        .append(", score: ").append(String.format("%.2f", result.getScore()))
                        .append(") ---\n")
                        .append(result.getContent())
                        .append("\n\n");
            }
            allMessages.add(ChatMessage.builder()
                    .role(ChatMessage.Role.SYSTEM)
                    .content(ragContext.toString())
                    .build());
        }

        allMessages.addAll(conversation.getContextWindow(defaultMaxContextMessages).stream()
                .map(m -> {
                    if (m.getRole() == ChatMessage.Role.TOOL) {
                        String toolName = m.getMetadata() != null
                                ? String.valueOf(m.getMetadata().getOrDefault("toolName", "unknown"))
                                : "unknown";
                        return ChatMessage.builder()
                                .role(ChatMessage.Role.SYSTEM)
                                .content("[Tool Result: " + toolName + "]\n" + m.getContent())
                                .build();
                    }
                    return ChatMessage.builder()
                            .role(m.getRole())
                            .content(m.getContent())
                            .build();
                })
                .collect(Collectors.toList()));

        return allMessages;
    }

    private void updateConversationMetadata(ConversationContext conversation, LlmResponse response) {
        try {
            Map<String, Object> metadata = conversation.getMetadata() != null
                    ? new java.util.HashMap<>(conversation.getMetadata())
                    : new java.util.HashMap<>();

            if (response.getProvider() != null) {
                metadata.put("lastProvider", response.getProvider().name());
            }
            if (response.getModel() != null) {
                metadata.put("lastModel", response.getModel());
            }

            if (response.getTokenUsage() != null) {
                long previousTokens = metadata.containsKey("totalTokens")
                        ? ((Number) metadata.get("totalTokens")).longValue()
                        : 0L;
                metadata.put("totalTokens", previousTokens + response.getTokenUsage().getTotalTokens());
            }

            conversationService.updateMetadata(conversation.getId(), metadata);
        } catch (Exception ignored) {
        }
    }
}
