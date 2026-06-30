package com.localmind.infrastructure.llm.adapter;

import com.localmind.domain.llm.model.*;
import com.localmind.domain.llm.port.out.StreamingLlmClient;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Component
@ConditionalOnProperty(name = "localmind.llm.mistral.enabled", havingValue = "true")
public class MistralStreamingLlmAdapter implements StreamingLlmClient {

    private final MistralAiChatModel chatModel;

    public MistralStreamingLlmAdapter(MistralAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public void streamResponse(LlmRequest request,
                                Consumer<String> onToken,
                                Consumer<TokenUsage> onComplete,
                                Consumer<Exception> onError) {
        try {
            List<Message> messages = request.getMessages().stream()
                    .map(this::toSpringAiMessage)
                    .toList();

            MistralAiChatOptions options = MistralAiChatOptions.builder()
                    .model(request.getModel())
                    .temperature(request.getTemperature())
                    .build();

            Prompt prompt = new Prompt(messages, options);
            AtomicReference<TokenUsage> lastUsage = new AtomicReference<>();

            chatModel.stream(prompt)
                    .doOnNext((ChatResponse response) -> {
                        if (response.getResult() != null
                                && response.getResult().getOutput() != null
                                && response.getResult().getOutput().getText() != null) {
                            onToken.accept(response.getResult().getOutput().getText());
                        }
                        if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                            var usage = response.getMetadata().getUsage();
                            if (usage.getTotalTokens() > 0) {
                                lastUsage.set(TokenUsage.builder()
                                        .promptTokens((int) usage.getPromptTokens())
                                        .completionTokens((int) usage.getCompletionTokens())
                                        .totalTokens((int) usage.getTotalTokens())
                                        .build());
                            }
                        }
                    })
                    .doOnError(error -> onError.accept(
                            error instanceof Exception ? (Exception) error : new RuntimeException(error)))
                    .doOnComplete(() -> onComplete.accept(lastUsage.get()))
                    .subscribe();
        } catch (Exception e) {
            onError.accept(e);
        }
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
}
