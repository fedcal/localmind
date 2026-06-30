package com.localmind.infrastructure.llm.adapter;

import com.localmind.domain.llm.model.*;
import com.localmind.domain.llm.port.out.LlmClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "localmind.llm.xai.enabled", havingValue = "true")
public class XaiLlmAdapter implements LlmClient {

    private final OpenAiChatModel chatModel;

    public XaiLlmAdapter(
            @Value("${localmind.llm.xai.api-key:}") String apiKey,
            @Value("${localmind.llm.xai.base-url:https://api.x.ai/v1}") String baseUrl) {
        OpenAiApi api = OpenAiApi.builder().apiKey(apiKey).baseUrl(baseUrl).build();
        this.chatModel = OpenAiChatModel.builder().openAiApi(api).build();
    }

    @Override
    public LlmResponse call(LlmRequest request) {
        List<Message> messages = request.getMessages().stream()
                .map(this::toSpringAiMessage)
                .toList();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(request.getModel())
                .temperature(request.getTemperature())
                .build();

        ChatResponse response = chatModel.call(new Prompt(messages, options));

        return LlmResponse.builder()
                .content(response.getResult().getOutput().getText())
                .model(response.getMetadata().getModel())
                .provider(LlmProvider.XAI)
                .tokenUsage(mapTokenUsage(response))
                .build();
    }

    @Override
    public LlmProvider getProvider() {
        return LlmProvider.XAI;
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
