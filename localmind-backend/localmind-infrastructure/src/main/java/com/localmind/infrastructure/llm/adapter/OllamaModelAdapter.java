package com.localmind.infrastructure.llm.adapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.localmind.domain.llm.port.out.OllamaModelPort;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
public class OllamaModelAdapter implements OllamaModelPort {

    private final RestTemplate restTemplate;

    public OllamaModelAdapter() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public List<String> listAvailableModels(String baseUrl) {
        String url = baseUrl.endsWith("/") ? baseUrl + "api/tags" : baseUrl + "/api/tags";
        try {
            OllamaTagsResponse response = restTemplate.getForObject(url, OllamaTagsResponse.class);
            if (response != null && response.models != null) {
                return response.models.stream()
                        .map(m -> m.name)
                        .toList();
            }
            return Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Impossibile contattare Ollama a " + baseUrl + ": " + e.getMessage(), e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OllamaTagsResponse {
        public List<OllamaModel> models;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OllamaModel {
        public String name;
        public String model;
    }
}
