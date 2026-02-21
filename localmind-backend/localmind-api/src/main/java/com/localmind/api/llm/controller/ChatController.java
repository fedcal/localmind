package com.localmind.api.llm.controller;

import com.localmind.api.llm.dto.AttachmentDto;
import com.localmind.api.llm.dto.ChatRequestDto;
import com.localmind.api.llm.dto.ChatResponseDto;
import com.localmind.api.llm.dto.RagSourceDto;
import com.localmind.api.llm.dto.TokenUsageDto;
import com.localmind.domain.document.model.SearchResult;
import com.localmind.domain.document.port.in.DocumentSearchUseCase;
import com.localmind.domain.llm.model.*;
import com.localmind.domain.llm.port.in.ChatUseCase;
import com.localmind.domain.llm.service.ConversationService;
import com.localmind.domain.mcp.model.ToolCallRequest;
import com.localmind.domain.mcp.model.ToolCallResponse;
import com.localmind.domain.mcp.port.in.ToolCallingPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat", description = "LLM Chat and conversation management")
public class ChatController {

    private static final int MAX_TOOL_ITERATIONS = 3;
    private static final int RAG_TOP_K = 5;

    private final ChatUseCase chatUseCase;
    private final ConversationService conversationService;
    private final ToolCallingPort toolCallingService;
    private final DocumentSearchUseCase documentSearchUseCase;
    private final int defaultMaxContextMessages;

    public ChatController(ChatUseCase chatUseCase, ConversationService conversationService,
                          ToolCallingPort toolCallingService,
                          DocumentSearchUseCase documentSearchUseCase,
                          @Value("${localmind.chat.default-max-context-messages:50}") int defaultMaxContextMessages) {
        this.chatUseCase = chatUseCase;
        this.conversationService = conversationService;
        this.toolCallingService = toolCallingService;
        this.documentSearchUseCase = documentSearchUseCase;
        this.defaultMaxContextMessages = defaultMaxContextMessages;
    }

    @PostMapping
    @Operation(summary = "Invia messaggio chat al LLM / Send chat message to LLM")
    @ApiResponse(responseCode = "200", description = "Risposta LLM / LLM response")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<ChatResponseDto> chat(@Valid @RequestBody ChatRequestDto request) {
        // 1. Crea o recupera conversazione, aggiunge messaggio USER e salva
        ConversationContext conversation = conversationService
                .getOrCreateForChat(request.getConversationId(), request.getMessage(), request.getSystemPrompt());

        // 2. Costruisci system prompt con tool descriptions se abilitato
        String toolsSystemPrompt = null;
        if (request.isEnableToolCalling() && toolCallingService.hasAvailableTools()) {
            toolsSystemPrompt = toolCallingService.buildToolsSystemPrompt();
        }

        // 2b. RAG: cerca documenti rilevanti se abilitato
        List<SearchResult> ragResults = null;
        if (request.isEnableRag()) {
            ragResults = documentSearchUseCase.search(request.getMessage(), RAG_TOP_K);
        }

        // 3. Loop agentico (max iterazioni)
        LlmResponse response = null;
        int iterations = 0;

        while (iterations < MAX_TOOL_ITERATIONS) {
            iterations++;

            List<ChatMessage> allMessages = buildMessageList(conversation, toolsSystemPrompt, ragResults);

            // Aggiungi ContentParts multimodali all'ultimo messaggio utente se ci sono allegati
            if (request.getAttachments() != null && !request.getAttachments().isEmpty() && iterations == 1) {
                List<ContentPart> parts = convertAttachmentsToParts(request.getAttachments());
                // Trova l'ultimo messaggio USER e aggiungi le parts
                for (int i = allMessages.size() - 1; i >= 0; i--) {
                    if (allMessages.get(i).getRole() == ChatMessage.Role.USER) {
                        allMessages.get(i).setParts(parts);
                        break;
                    }
                }
            }

            LlmRequest llmRequest = LlmRequest.builder()
                    .messages(allMessages)
                    .provider(request.getProvider() != null ? LlmProvider.valueOf(request.getProvider()) : null)
                    .model(request.getModel())
                    .temperature(request.getTemperature())
                    .maxTokens(request.getMaxTokens())
                    .conversationId(conversation.getId())
                    .build();

            response = chatUseCase.chat(llmRequest);

            // Se tool calling è abilitato, controlla se il LLM vuole chiamare un tool
            if (request.isEnableToolCalling() && toolCallingService.hasAvailableTools()) {
                Optional<ToolCallRequest> toolCall = toolCallingService.parseToolCallFromResponse(response.getContent());
                if (toolCall.isPresent()) {
                    // Esegui il tool
                    ToolCallResponse toolResponse = toolCallingService.executeToolCall(toolCall.get());

                    // Salva la risposta LLM (che contiene la richiesta tool) come assistant message
                    conversationService.addAssistantMessage(conversation.getId(), response.getContent());

                    // Salva il risultato del tool nella conversazione
                    String toolContent = toolResponse.isSuccess()
                            ? toolResponse.getResult()
                            : "Error: " + toolResponse.getErrorMessage();
                    conversationService.addToolMessage(
                            conversation.getId(),
                            toolCall.get().getToolName(),
                            toolContent,
                            Map.of("executionTimeMs", toolResponse.getExecutionTimeMs(),
                                   "success", toolResponse.isSuccess()));

                    // Ricarica la conversazione per avere i nuovi messaggi
                    conversation = conversationService.getById(conversation.getId());

                    // Continua il loop per reinviare al LLM con il risultato del tool
                    continue;
                }
            }

            // Nessun tool call trovato, il LLM ha risposto direttamente
            break;
        }

        // 4. Persisti risposta assistant finale
        conversationService.addAssistantMessage(conversation.getId(), response.getContent());

        // 5. Auto-update metadata conversazione (provider, modello, token)
        updateConversationMetadata(conversation, response);

        // 6. Restituisci con conversationId e fonti RAG
        List<RagSourceDto> ragSourceDtos = null;
        if (ragResults != null && !ragResults.isEmpty()) {
            ragSourceDtos = ragResults.stream()
                    .map(r -> RagSourceDto.builder()
                            .documentId(r.getDocumentId())
                            .filename(r.getFilename())
                            .chunkIndex(r.getChunkIndex())
                            .score(r.getScore())
                            .contentPreview(r.getContent() != null && r.getContent().length() > 200
                                    ? r.getContent().substring(0, 200) + "..."
                                    : r.getContent())
                            .build())
                    .toList();
        }

        ChatResponseDto responseDto = ChatResponseDto.builder()
                .content(response.getContent())
                .model(response.getModel())
                .provider(response.getProvider().name())
                .conversationId(conversation.getId())
                .latencyMs(response.getLatencyMs())
                .tokenUsage(response.getTokenUsage() != null ? TokenUsageDto.builder()
                        .promptTokens(response.getTokenUsage().getPromptTokens())
                        .completionTokens(response.getTokenUsage().getCompletionTokens())
                        .totalTokens(response.getTokenUsage().getTotalTokens())
                        .build() : null)
                .ragSources(ragSourceDtos)
                .build();

        return ResponseEntity.ok(responseDto);
    }

    private List<ChatMessage> buildMessageList(ConversationContext conversation,
                                               String toolsSystemPrompt,
                                               List<SearchResult> ragResults) {
        List<ChatMessage> allMessages = new java.util.ArrayList<>();

        // System prompt della conversazione
        if (conversation.getSystemPrompt() != null && !conversation.getSystemPrompt().isBlank()) {
            allMessages.add(ChatMessage.builder()
                    .role(ChatMessage.Role.SYSTEM)
                    .content(conversation.getSystemPrompt())
                    .build());
        }

        // System prompt dei tool disponibili
        if (toolsSystemPrompt != null && !toolsSystemPrompt.isBlank()) {
            allMessages.add(ChatMessage.builder()
                    .role(ChatMessage.Role.SYSTEM)
                    .content(toolsSystemPrompt)
                    .build());
        }

        // RAG context: inietta risultati ricerca documenti
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

        // Messaggi della conversazione (TOOL → SYSTEM per LLM), con context window
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

    private List<ContentPart> convertAttachmentsToParts(List<AttachmentDto> attachments) {
        List<ContentPart> parts = new ArrayList<>();
        for (AttachmentDto attachment : attachments) {
            ContentPart.ContentType contentType;
            String mimeType = attachment.getMimeType();

            if (mimeType != null && mimeType.startsWith("image/")) {
                contentType = ContentPart.ContentType.IMAGE;
            } else if (mimeType != null && mimeType.startsWith("audio/")) {
                contentType = ContentPart.ContentType.AUDIO;
            } else {
                contentType = ContentPart.ContentType.TEXT;
            }

            parts.add(ContentPart.builder()
                    .type(contentType)
                    .content(attachment.getData())
                    .mimeType(mimeType)
                    .build());
        }
        return parts;
    }

    private void updateConversationMetadata(ConversationContext conversation, LlmResponse response) {
        try {
            Map<String, Object> metadata = conversation.getMetadata() != null
                    ? new java.util.HashMap<>(conversation.getMetadata())
                    : new java.util.HashMap<>();

            metadata.put("lastProvider", response.getProvider().name());
            metadata.put("lastModel", response.getModel());

            if (response.getTokenUsage() != null) {
                long previousTokens = metadata.containsKey("totalTokens")
                        ? ((Number) metadata.get("totalTokens")).longValue()
                        : 0L;
                metadata.put("totalTokens", previousTokens + response.getTokenUsage().getTotalTokens());
            }

            conversationService.updateMetadata(conversation.getId(), metadata);
        } catch (Exception ignored) {
            // Non bloccare la risposta se l'aggiornamento metadata fallisce
        }
    }
}
