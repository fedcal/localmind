package com.localmind.infrastructure.llm.adapter;

import com.localmind.domain.llm.model.*;
import com.localmind.domain.llm.port.out.StreamingLlmClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Component
@ConditionalOnProperty(name = "localmind.llm.ollama.enabled", havingValue = "true")
public class OllamaStreamingLlmAdapter implements StreamingLlmClient {

    private final OllamaChatModel chatModel;
    private final String baseUrl;
    private final RestTemplate restTemplate;

    public OllamaStreamingLlmAdapter(OllamaChatModel chatModel,
                                      @Value("${spring.ai.ollama.base-url:http://localhost:11434}") String baseUrl) {
        this.chatModel = chatModel;
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
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

            OllamaOptions options = OllamaOptions.builder()
                    .temperature(request.getTemperature())
                    .build();

            if (request.getModel() != null) {
                options = OllamaOptions.builder()
                        .model(request.getModel())
                        .temperature(request.getTemperature())
                        .build();
            }

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
        return LlmProvider.OLLAMA;
    }

    @Override
    public boolean isAvailable() {
        try {
            String url = baseUrl.endsWith("/") ? baseUrl + "api/tags" : baseUrl + "/api/tags";
            restTemplate.getForObject(url, String.class);
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
            case TOOL -> new UserMessage(msg.getContent());
        };
    }
}
