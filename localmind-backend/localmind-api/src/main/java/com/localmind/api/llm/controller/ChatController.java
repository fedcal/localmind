package com.localmind.api.llm.controller;

import com.localmind.api.llm.dto.ChatRequestDto;
import com.localmind.api.llm.dto.ChatResponseDto;
import com.localmind.api.llm.dto.TokenUsageDto;
import com.localmind.domain.llm.model.*;
import com.localmind.domain.llm.port.in.ChatUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatUseCase chatUseCase;

    public ChatController(ChatUseCase chatUseCase) {
        this.chatUseCase = chatUseCase;
    }

    @PostMapping
    public ResponseEntity<ChatResponseDto> chat(@Valid @RequestBody ChatRequestDto request) {
        LlmRequest llmRequest = LlmRequest.builder()
                .messages(List.of(ChatMessage.builder()
                        .role(ChatMessage.Role.USER)
                        .content(request.getMessage())
                        .build()))
                .provider(request.getProvider() != null ? LlmProvider.valueOf(request.getProvider()) : null)
                .model(request.getModel())
                .temperature(request.getTemperature())
                .maxTokens(request.getMaxTokens())
                .conversationId(request.getConversationId())
                .build();

        LlmResponse response = chatUseCase.chat(llmRequest);

        ChatResponseDto responseDto = ChatResponseDto.builder()
                .content(response.getContent())
                .model(response.getModel())
                .provider(response.getProvider().name())
                .latencyMs(response.getLatencyMs())
                .tokenUsage(response.getTokenUsage() != null ? TokenUsageDto.builder()
                        .promptTokens(response.getTokenUsage().getPromptTokens())
                        .completionTokens(response.getTokenUsage().getCompletionTokens())
                        .totalTokens(response.getTokenUsage().getTotalTokens())
                        .build() : null)
                .build();

        return ResponseEntity.ok(responseDto);
    }
}
