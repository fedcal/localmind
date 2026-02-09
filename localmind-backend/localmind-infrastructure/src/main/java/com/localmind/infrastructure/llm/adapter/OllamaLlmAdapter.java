package com.localmind.infrastructure.llm.adapter;

import com.localmind.domain.llm.model.*;
import com.localmind.domain.llm.port.out.LlmClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "localmind.llm.ollama.enabled", havingValue = "true")
public class OllamaLlmAdapter implements LlmClient {

    private final OllamaChatModel chatModel;

    public OllamaLlmAdapter(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public LlmResponse call(LlmRequest request) {
        List<Message> messages = request.getMessages().stream()
                .map(this::toSpringAiMessage)
                .toList();

        OllamaOptions options = OllamaOptions.builder()
                .temperature(request.getTemperature())
                .build();

        if (request.getModel() != null) {
            options = OllamaOptions.builder()
                    .model(request.getModel())
                    .temperature(request.getTemperature())
                    .build();
        }

        ChatResponse response = chatModel.call(new Prompt(messages, options));

        return LlmResponse.builder()
                .content(response.getResult().getOutput().getText())
                .model(response.getMetadata().getModel())
                .provider(LlmProvider.OLLAMA)
                .tokenUsage(mapTokenUsage(response))
                .build();
    }

    @Override
    public LlmProvider getProvider() {
        return LlmProvider.OLLAMA;
    }

    @Override
    public boolean isAvailable() {
        try {
            chatModel.call("ping");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Message toSpringAiMessage(ChatMessage msg) {
        return switch (msg.getRole()) {
            case SYSTEM -> new SystemMessage(msg.getContent());
            case USER -> new UserMessage(msg.getContent());
            case ASSISTANT -> new AssistantMessage(msg.getContent());
        };
    }

    private TokenUsage mapTokenUsage(ChatResponse response) {
        if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
            var usage = response.getMetadata().getUsage();
            return TokenUsage.builder()
                    .promptTokens((int) usage.getPromptTokens())
                    .completionTokens((int) usage.getCompletionTokens())
                    .totalTokens((int) usage.getTotalTokens())
                    .build();
        }
        return null;
    }
}
