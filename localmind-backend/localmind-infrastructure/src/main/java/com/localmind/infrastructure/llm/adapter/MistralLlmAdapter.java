package com.localmind.infrastructure.llm.adapter;

import com.localmind.domain.llm.model.*;
import com.localmind.domain.llm.port.out.LlmClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mistralai.MistralAiChatModel;
import org.springframework.ai.mistralai.MistralAiChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "localmind.llm.mistral.enabled", havingValue = "true")
public class MistralLlmAdapter implements LlmClient {

    private final MistralAiChatModel chatModel;

    public MistralLlmAdapter(MistralAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public LlmResponse call(LlmRequest request) {
        List<Message> messages = request.getMessages().stream()
                .map(this::toSpringAiMessage)
                .toList();

        MistralAiChatOptions options = MistralAiChatOptions.builder()
                .model(request.getModel())
                .temperature(request.getTemperature())
                .build();

        ChatResponse response = chatModel.call(new Prompt(messages, options));

        return LlmResponse.builder()
                .content(response.getResult().getOutput().getText())
                .model(response.getMetadata().getModel())
                .provider(LlmProvider.MISTRAL)
                .tokenUsage(mapTokenUsage(response))
                .build();
    }

    @Override
    public LlmProvider getProvider() {
        return LlmProvider.MISTRAL;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private Message toSpringAiMessage(ChatMessage msg) {
        return switch (msg.getRole()) {
            case SYSTEM -> new SystemMessage(msg.getContent());
            case USER -> new UserMessage(msg.getContent());
            case ASSISTANT -> new AssistantMessage(msg.getContent());
            case TOOL -> new UserMessage(msg.getContent());
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
