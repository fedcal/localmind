package com.localmind.infrastructure.llm.adapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.domain.llm.model.OllamaStatus;
import com.localmind.domain.llm.model.PullProgress;
import com.localmind.domain.llm.port.out.OllamaModelPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class OllamaModelAdapter implements OllamaModelPort {

    private static final Logger log = LoggerFactory.getLogger(OllamaModelAdapter.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OllamaModelAdapter() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<String> listAvailableModels(String baseUrl) {
        String url = normalizeUrl(baseUrl) + "/api/tags";
        try {
            OllamaTagsResponse response = restTemplate.getForObject(url, OllamaTagsResponse.class);
            if (response != null && response.models != null) {
                return response.models.stream()
                        .map(m -> m.name)
                        .toList();
            }
            return Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Unable to contact Ollama at " + baseUrl + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void pullModel(String baseUrl, String modelName) {
        pullModelStreaming(baseUrl, modelName, progress -> { });
    }

    @Override
    public void pullModelStreaming(String baseUrl, String modelName, Consumer<PullProgress> progressCallback) {
        String url = normalizeUrl(baseUrl) + "/api/pull";
        try {
            restTemplate.execute(url, HttpMethod.POST,
                    request -> {
                        request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        objectMapper.writeValue(request.getBody(),
                                Map.of("name", modelName, "stream", true));
                    },
                    response -> {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.getBody()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.isBlank()) continue;
                                PullProgress progress = parseProgressLine(line);
                                progressCallback.accept(progress);
                            }
                        }
                        return null;
                    });
        } catch (Exception e) {
            PullProgress errorProgress = PullProgress.builder()
                    .status("error")
                    .error(true)
                    .errorMessage("Failed to pull model " + modelName + ": " + e.getMessage())
                    .done(true)
                    .build();
            progressCallback.accept(errorProgress);
        }
    }

    private PullProgress parseProgressLine(String line) {
        try {
            JsonNode node = objectMapper.readTree(line);
            String status = node.has("status") ? node.get("status").asText() : "";
            String digest = node.has("digest") ? node.get("digest").asText() : null;
            long total = node.has("total") ? node.get("total").asLong() : 0;
            long completed = node.has("completed") ? node.get("completed").asLong() : 0;
            boolean isDone = "success".equals(status);
            boolean isError = node.has("error");
            String errorMsg = isError ? node.get("error").asText() : null;

            return PullProgress.builder()
                    .status(status)
                    .digest(digest)
                    .total(total)
                    .completed(completed)
                    .done(isDone)
                    .error(isError)
                    .errorMessage(errorMsg)
                    .build();
        } catch (Exception e) {
            return PullProgress.builder()
                    .status("parse_error")
                    .error(true)
                    .errorMessage("Failed to parse progress: " + e.getMessage())
                    .done(true)
                    .build();
        }
    }

    @Override
    public void deleteModel(String baseUrl, String modelName) {
        String url = normalizeUrl(baseUrl) + "/api/delete";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(
                    Map.of("name", modelName), headers);
            restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete model " + modelName + ": " + e.getMessage(), e);
        }
    }

    @Override
    public OllamaStatus checkStatus(String baseUrl) {
        String normalizedUrl = normalizeUrl(baseUrl);
        try {
            OllamaVersionResponse versionResp = restTemplate.getForObject(
                    normalizedUrl + "/api/version", OllamaVersionResponse.class);
            String version = versionResp != null ? versionResp.version : "unknown";

            OllamaTagsResponse tagsResp = restTemplate.getForObject(
                    normalizedUrl + "/api/tags", OllamaTagsResponse.class);

            List<OllamaStatus.OllamaModelInfo> models = Collections.emptyList();
            if (tagsResp != null && tagsResp.models != null) {
                models = tagsResp.models.stream()
                        .map(m -> OllamaStatus.OllamaModelInfo.builder()
                                .name(m.name)
                                .sizeBytes(m.size)
                                .modifiedAt(m.modified_at != null ? m.modified_at : "")
                                .build())
                        .toList();
            }

            return OllamaStatus.builder()
                    .online(true)
                    .version(version)
                    .models(models)
                    .build();
        } catch (Exception e) {
            log.warn("Ollama non raggiungibile su {}: {}", baseUrl, e.getMessage());
            return OllamaStatus.builder()
                    .online(false)
                    .errorMessage(e.getMessage())
                    .models(Collections.emptyList())
                    .build();
        }
    }

    @Override
    public void startOllama() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ollama", "serve");
            pb.redirectErrorStream(true);
            pb.start();
            log.info("Processo 'ollama serve' avviato");
        } catch (IOException e) {
            throw new RuntimeException("Impossibile avviare Ollama: " + e.getMessage(), e);
        }
    }

    private String normalizeUrl(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OllamaTagsResponse {
        public List<OllamaModelInfo> models;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OllamaModelInfo {
        public String name;
        public String model;
        public long size;
        public String modified_at;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OllamaVersionResponse {
        public String version;
    }
}
