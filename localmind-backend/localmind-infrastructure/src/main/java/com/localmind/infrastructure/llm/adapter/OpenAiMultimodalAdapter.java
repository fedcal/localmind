package com.localmind.infrastructure.llm.adapter;

import com.localmind.domain.llm.model.*;
import com.localmind.domain.llm.port.out.MultimodalLlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@ConditionalOnProperty(name = "localmind.llm.openai.multimodal.enabled", havingValue = "true")
public class OpenAiMultimodalAdapter implements MultimodalLlmClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiMultimodalAdapter.class);

    private final String apiKey;
    private final String defaultModel;
    private final RestTemplate restTemplate;

    public OpenAiMultimodalAdapter(
            @Value("${spring.ai.openai.api-key:}") String apiKey,
            @Value("${localmind.llm.openai.multimodal.model:gpt-4o}") String defaultModel) {
        this.apiKey = apiKey;
        this.defaultModel = defaultModel;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public boolean supportsMultimodal() {
        return true;
    }

    @Override
    public LlmResponse call(LlmRequest request) {
        long start = System.currentTimeMillis();

        String model = request.getModel() != null ? request.getModel() : defaultModel;

        List<Map<String, Object>> openAiMessages = new ArrayList<>();
        for (ChatMessage msg : request.getMessages()) {
            Map<String, Object> openAiMsg = new LinkedHashMap<>();
            openAiMsg.put("role", mapRole(msg.getRole()));

            // Se ci sono content parts multimodali, usa il formato array
            if (msg.getParts() != null && !msg.getParts().isEmpty()) {
                List<Map<String, Object>> contentArray = new ArrayList<>();

                // Aggiungi il testo principale se presente
                if (msg.getContent() != null && !msg.getContent().isBlank()) {
                    Map<String, Object> textPart = new LinkedHashMap<>();
                    textPart.put("type", "text");
                    textPart.put("text", msg.getContent());
                    contentArray.add(textPart);
                }

                for (ContentPart part : msg.getParts()) {
                    if (part.getType() == ContentPart.ContentType.TEXT && part.getContent() != null) {
                        Map<String, Object> textPart = new LinkedHashMap<>();
                        textPart.put("type", "text");
                        textPart.put("text", part.getContent());
                        contentArray.add(textPart);
                    } else if (part.getType() == ContentPart.ContentType.IMAGE && part.getContent() != null) {
                        Map<String, Object> imagePart = new LinkedHashMap<>();
                        imagePart.put("type", "image_url");

                        String imageUrl = part.getContent();
                        // Se non e' gia' un data URL, costruiscilo
                        if (!imageUrl.startsWith("data:")) {
                            String mimeType = part.getMimeType() != null ? part.getMimeType() : "image/jpeg";
                            imageUrl = "data:" + mimeType + ";base64," + imageUrl;
                        }

                        Map<String, Object> imageUrlObj = new LinkedHashMap<>();
                        imageUrlObj.put("url", imageUrl);
                        imagePart.put("image_url", imageUrlObj);
                        contentArray.add(imagePart);
                    }
                }

                openAiMsg.put("content", contentArray);
            } else {
                openAiMsg.put("content", msg.getContent() != null ? msg.getContent() : "");
            }

            openAiMessages.add(openAiMsg);
        }

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", openAiMessages);
        requestBody.put("temperature", request.getTemperature());
        if (request.getMaxTokens() > 0) {
            requestBody.put("max_tokens", request.getMaxTokens());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String url = "https://api.openai.com/v1/chat/completions";

        log.debug("Invio richiesta multimodale OpenAI con modello {}", model);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

        long latency = System.currentTimeMillis() - start;

        String content = "";
        TokenUsage tokenUsage = null;

        if (response != null) {
            // Estrai contenuto dalla risposta
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null) {
                    content = String.valueOf(message.getOrDefault("content", ""));
                }
            }

            // Estrai token usage
            @SuppressWarnings("unchecked")
            Map<String, Object> usage = (Map<String, Object>) response.get("usage");
            if (usage != null) {
                int promptTokens = toInt(usage.get("prompt_tokens"));
                int completionTokens = toInt(usage.get("completion_tokens"));
                tokenUsage = TokenUsage.builder()
                        .promptTokens(promptTokens)
                        .completionTokens(completionTokens)
                        .totalTokens(promptTokens + completionTokens)
                        .build();
            }
        }

        return LlmResponse.builder()
                .content(content)
                .model(model)
                .provider(LlmProvider.OPENAI)
                .tokenUsage(tokenUsage)
                .latencyMs(latency)
                .build();
    }

    @Override
    public LlmProvider getProvider() {
        return LlmProvider.OPENAI;
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    private String mapRole(ChatMessage.Role role) {
        return switch (role) {
            case SYSTEM -> "system";
            case USER -> "user";
            case ASSISTANT -> "assistant";
            case TOOL -> "user";
        };
    }

    private int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }
}
