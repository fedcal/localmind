package com.localmind.infrastructure.llm.adapter;

import com.localmind.domain.llm.port.out.AudioTranscriptionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "localmind.llm.whisper.enabled", havingValue = "true")
public class WhisperTranscriptionAdapter implements AudioTranscriptionPort {

    private static final Logger log = LoggerFactory.getLogger(WhisperTranscriptionAdapter.class);
    private static final String WHISPER_URL = "https://api.openai.com/v1/audio/transcriptions";

    private final String apiKey;
    private final RestTemplate restTemplate;

    public WhisperTranscriptionAdapter(
            @Value("${spring.ai.openai.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String transcribe(InputStream audio, String mimeType) {
        log.debug("Avvio trascrizione audio con Whisper, mimeType: {}", mimeType);

        String extension = mapMimeTypeToExtension(mimeType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(apiKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new InputStreamResource(audio) {
            @Override
            public String getFilename() {
                return "audio" + extension;
            }

            @Override
            public long contentLength() {
                return -1;
            }
        });
        body.add("model", "whisper-1");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(WHISPER_URL, requestEntity, Map.class);

        if (response != null && response.containsKey("text")) {
            String text = String.valueOf(response.get("text"));
            log.debug("Trascrizione completata, lunghezza testo: {}", text.length());
            return text;
        }

        log.warn("Risposta Whisper senza campo 'text'");
        return "";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    private String mapMimeTypeToExtension(String mimeType) {
        if (mimeType == null) {
            return ".wav";
        }
        return switch (mimeType.toLowerCase()) {
            case "audio/mp3", "audio/mpeg" -> ".mp3";
            case "audio/mp4", "audio/m4a" -> ".m4a";
            case "audio/wav", "audio/wave", "audio/x-wav" -> ".wav";
            case "audio/webm" -> ".webm";
            case "audio/ogg" -> ".ogg";
            case "audio/flac" -> ".flac";
            default -> ".wav";
        };
    }
}
