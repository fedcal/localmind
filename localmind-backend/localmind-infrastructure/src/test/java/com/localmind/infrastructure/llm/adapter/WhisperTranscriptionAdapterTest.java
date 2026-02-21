package com.localmind.infrastructure.llm.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhisperTranscriptionAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    private WhisperTranscriptionAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WhisperTranscriptionAdapter("sk-test-key");
        ReflectionTestUtils.setField(adapter, "restTemplate", restTemplate);
    }

    @Test
    void isAvailable_withApiKey_shouldReturnTrue() {
        assertThat(adapter.isAvailable()).isTrue();
    }

    @Test
    void isAvailable_withEmptyApiKey_shouldReturnFalse() {
        WhisperTranscriptionAdapter emptyAdapter = new WhisperTranscriptionAdapter("");
        assertThat(emptyAdapter.isAvailable()).isFalse();
    }

    @Test
    void isAvailable_withNullApiKey_shouldReturnFalse() {
        WhisperTranscriptionAdapter nullAdapter = new WhisperTranscriptionAdapter(null);
        assertThat(nullAdapter.isAvailable()).isFalse();
    }

    @Test
    void transcribe_shouldReturnTranscribedText() {
        // given
        InputStream audioStream = new ByteArrayInputStream("fake audio data".getBytes(StandardCharsets.UTF_8));
        Map<String, Object> responseBody = Map.of("text", "Hello, this is a transcription.");

        when(restTemplate.postForObject(
                eq("https://api.openai.com/v1/audio/transcriptions"),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseBody);

        // when
        String result = adapter.transcribe(audioStream, "audio/mp3");

        // then
        assertThat(result).isEqualTo("Hello, this is a transcription.");
    }

    @Test
    void transcribe_withNullResponse_shouldReturnEmpty() {
        // given
        InputStream audioStream = new ByteArrayInputStream("fake".getBytes(StandardCharsets.UTF_8));

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(null);

        // when
        String result = adapter.transcribe(audioStream, "audio/wav");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void transcribe_withResponseWithoutText_shouldReturnEmpty() {
        // given
        InputStream audioStream = new ByteArrayInputStream("fake".getBytes(StandardCharsets.UTF_8));
        Map<String, Object> responseBody = Map.of("error", "something went wrong");

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseBody);

        // when
        String result = adapter.transcribe(audioStream, "audio/wav");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void transcribe_shouldSendMultipartRequest() {
        // given
        InputStream audioStream = new ByteArrayInputStream("audio data".getBytes(StandardCharsets.UTF_8));
        Map<String, Object> responseBody = Map.of("text", "transcribed");

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseBody);

        // when
        adapter.transcribe(audioStream, "audio/mpeg");

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<MultiValueMap<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(
                eq("https://api.openai.com/v1/audio/transcriptions"),
                captor.capture(),
                eq(Map.class)
        );

        HttpEntity<MultiValueMap<String, Object>> entity = captor.getValue();
        assertThat(entity.getHeaders().getFirst("Authorization")).isEqualTo("Bearer sk-test-key");
    }

    @Test
    void transcribe_withWavMimeType_shouldUseWavExtension() {
        // given
        InputStream audioStream = new ByteArrayInputStream("wav data".getBytes(StandardCharsets.UTF_8));
        Map<String, Object> responseBody = Map.of("text", "wav transcription");

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseBody);

        // when
        String result = adapter.transcribe(audioStream, "audio/wav");

        // then
        assertThat(result).isEqualTo("wav transcription");
    }

    @Test
    void transcribe_withNullMimeType_shouldDefaultToWav() {
        // given
        InputStream audioStream = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
        Map<String, Object> responseBody = Map.of("text", "default transcription");

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseBody);

        // when
        String result = adapter.transcribe(audioStream, null);

        // then
        assertThat(result).isEqualTo("default transcription");
    }
}
