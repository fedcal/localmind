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
@ConditionalOnProperty(name = "localmind.llm.ollama.multimodal.enabled", havingValue = "true")
public class OllamaMultimodalAdapter implements MultimodalLlmClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaMultimodalAdapter.class);

    private final String baseUrl;
    private final String defaultModel;
    private final RestTemplate restTemplate;

    public OllamaMultimodalAdapter(
            @Value("${spring.ai.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${localmind.llm.ollama.multimodal.model:llava}") String defaultModel) {
        this.baseUrl = baseUrl;
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

        List<Map<String, Object>> ollamaMessages = new ArrayList<>();
        for (ChatMessage msg : request.getMessages()) {
            Map<String, Object> ollamaMsg = new LinkedHashMap<>();
            ollamaMsg.put("role", mapRole(msg.getRole()));
            ollamaMsg.put("content", msg.getContent() != null ? msg.getContent() : "");

            // Aggiungi immagini base64 se presenti nelle parts
            if (msg.getParts() != null && !msg.getParts().isEmpty()) {
                List<String> images = new ArrayList<>();
                StringBuilder textContent = new StringBuilder();
                if (msg.getContent() != null) {
                    textContent.append(msg.getContent());
                }

                for (ContentPart part : msg.getParts()) {
                    if (part.getType() == ContentPart.ContentType.IMAGE && part.getContent() != null) {
                        // Rimuovi eventuale prefisso data:image/...;base64,
                        String base64Data = part.getContent();
                        if (base64Data.contains(",")) {
                            base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                        }
                        images.add(base64Data);
                    } else if (part.getType() == ContentPart.ContentType.TEXT && part.getContent() != null) {
                        if (textContent.length() > 0) {
                            textContent.append("\n");
                        }
                        textContent.append(part.getContent());
                    }
                }

                ollamaMsg.put("content", textContent.toString());
                if (!images.isEmpty()) {
                    ollamaMsg.put("images", images);
                }
            }

            ollamaMessages.add(ollamaMsg);
        }

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", ollamaMessages);
        requestBody.put("stream", false);

        Map<String, Object> options = new LinkedHashMap<>();
        options.put("temperature", request.getTemperature());
        if (request.getMaxTokens() > 0) {
            options.put("num_predict", request.getMaxTokens());
        }
        requestBody.put("options", options);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String url = baseUrl.endsWith("/") ? baseUrl + "api/chat" : baseUrl + "/api/chat";

        log.debug("Invio richiesta multimodale Ollama a {} con modello {}", url, model);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

        long latency = System.currentTimeMillis() - start;

        String content = "";
        if (response != null && response.get("message") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) response.get("message");
            content = String.valueOf(message.getOrDefault("content", ""));
        }

        TokenUsage tokenUsage = null;
        if (response != null) {
            int promptTokens = toInt(response.get("prompt_eval_count"));
            int completionTokens = toInt(response.get("eval_count"));
            tokenUsage = TokenUsage.builder()
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(promptTokens + completionTokens)
                    .build();
        }

        return LlmResponse.builder()
                .content(content)
                .model(model)
                .provider(LlmProvider.OLLAMA)
                .tokenUsage(tokenUsage)
                .latencyMs(latency)
                .build();
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
