package com.localmind.infrastructure.llm.adapter;

import com.localmind.domain.llm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OllamaMultimodalAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    private OllamaMultimodalAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OllamaMultimodalAdapter("http://localhost:11434", "llava");
        ReflectionTestUtils.setField(adapter, "restTemplate", restTemplate);
    }

    @Test
    void supportsMultimodal_shouldReturnTrue() {
        assertThat(adapter.supportsMultimodal()).isTrue();
    }

    @Test
    void getProvider_shouldReturnOllama() {
        assertThat(adapter.getProvider()).isEqualTo(LlmProvider.OLLAMA);
    }

    @Test
    void call_withTextOnly_shouldSendCorrectRequest() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Describe this")
                                .build()
                ))
                .temperature(0.7)
                .build();

        Map<String, Object> responseBody = Map.of(
                "message", Map.of("content", "This is a description"),
                "prompt_eval_count", 10,
                "eval_count", 20
        );

        when(restTemplate.postForObject(
                eq("http://localhost:11434/api/chat"),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseBody);

        // when
        LlmResponse response = adapter.call(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("This is a description");
        assertThat(response.getProvider()).isEqualTo(LlmProvider.OLLAMA);
        assertThat(response.getModel()).isEqualTo("llava");
        assertThat(response.getTokenUsage()).isNotNull();
        assertThat(response.getTokenUsage().getPromptTokens()).isEqualTo(10);
        assertThat(response.getTokenUsage().getCompletionTokens()).isEqualTo(20);
        assertThat(response.getTokenUsage().getTotalTokens()).isEqualTo(30);
    }

    @Test
    void call_withImageParts_shouldIncludeImagesInRequest() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("What do you see?")
                                .parts(List.of(
                                        ContentPart.builder()
                                                .type(ContentPart.ContentType.IMAGE)
                                                .content("iVBORw0KGgoAAAANSUhEUg==")
                                                .mimeType("image/png")
                                                .build()
                                ))
                                .build()
                ))
                .temperature(0.5)
                .build();

        Map<String, Object> responseBody = Map.of(
                "message", Map.of("content", "I see an image"),
                "prompt_eval_count", 50,
                "eval_count", 15
        );

        when(restTemplate.postForObject(
                eq("http://localhost:11434/api/chat"),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseBody);

        // when
        LlmResponse response = adapter.call(request);

        // then
        assertThat(response.getContent()).isEqualTo("I see an image");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(
                eq("http://localhost:11434/api/chat"),
                captor.capture(),
                eq(Map.class)
        );

        Map<String, Object> body = captor.getValue().getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("model")).isEqualTo("llava");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("messages");
        assertThat(messages).hasSize(1);

        Map<String, Object> userMsg = messages.get(0);
        assertThat(userMsg.get("role")).isEqualTo("user");
        assertThat(userMsg.get("content")).isEqualTo("What do you see?");

        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) userMsg.get("images");
        assertThat(images).hasSize(1);
        assertThat(images.get(0)).isEqualTo("iVBORw0KGgoAAAANSUhEUg==");
    }

    @Test
    void call_withDataUrlPrefix_shouldStripPrefix() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Describe")
                                .parts(List.of(
                                        ContentPart.builder()
                                                .type(ContentPart.ContentType.IMAGE)
                                                .content("data:image/jpeg;base64,/9j/4AAQ")
                                                .mimeType("image/jpeg")
                                                .build()
                                ))
                                .build()
                ))
                .temperature(0.7)
                .build();

        Map<String, Object> responseBody = Map.of(
                "message", Map.of("content", "A photo")
        );

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseBody);

        // when
        adapter.call(request);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(any(String.class), captor.capture(), eq(Map.class));

        Map<String, Object> body = captor.getValue().getBody();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("messages");
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) messages.get(0).get("images");
        assertThat(images.get(0)).isEqualTo("/9j/4AAQ");
    }

    @Test
    void call_withCustomModel_shouldUseCustomModel() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Hello")
                                .build()
                ))
                .model("llava:13b")
                .temperature(0.7)
                .build();

        Map<String, Object> responseBody = Map.of(
                "message", Map.of("content", "Hi")
        );

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseBody);

        // when
        LlmResponse response = adapter.call(request);

        // then
        assertThat(response.getModel()).isEqualTo("llava:13b");
    }

    @Test
    void call_withSystemMessage_shouldMapCorrectly() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.SYSTEM)
                                .content("You are a helpful assistant")
                                .build(),
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Hello")
                                .build()
                ))
                .temperature(0.7)
                .build();

        Map<String, Object> responseBody = Map.of(
                "message", Map.of("content", "Hi there")
        );

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseBody);

        // when
        adapter.call(request);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(any(String.class), captor.capture(), eq(Map.class));

        Map<String, Object> body = captor.getValue().getBody();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("messages");
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).get("role")).isEqualTo("system");
        assertThat(messages.get(1).get("role")).isEqualTo("user");
    }

    @Test
    void call_withMaxTokens_shouldSetNumPredict() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Hello")
                                .build()
                ))
                .temperature(0.7)
                .maxTokens(100)
                .build();

        Map<String, Object> responseBody = Map.of(
                "message", Map.of("content", "Hi")
        );

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseBody);

        // when
        adapter.call(request);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(any(String.class), captor.capture(), eq(Map.class));

        Map<String, Object> body = captor.getValue().getBody();
        @SuppressWarnings("unchecked")
        Map<String, Object> options = (Map<String, Object>) body.get("options");
        assertThat(options.get("num_predict")).isEqualTo(100);
    }

    @Test
    void isAvailable_whenOllamaResponds_shouldReturnTrue() {
        // given
        when(restTemplate.getForObject("http://localhost:11434/api/tags", String.class))
                .thenReturn("{\"models\": []}");

        // when
        boolean available = adapter.isAvailable();

        // then
        assertThat(available).isTrue();
    }

    @Test
    void isAvailable_whenOllamaThrows_shouldReturnFalse() {
        // given
        when(restTemplate.getForObject("http://localhost:11434/api/tags", String.class))
                .thenThrow(new RuntimeException("Connection refused"));

        // when
        boolean available = adapter.isAvailable();

        // then
        assertThat(available).isFalse();
    }

    @Test
    void call_withNullResponse_shouldReturnEmptyContent() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Hello")
                                .build()
                ))
                .temperature(0.7)
                .build();

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(null);

        // when
        LlmResponse response = adapter.call(request);

        // then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTokenUsage()).isNull();
    }

    @Test
    void call_withToolRole_shouldMapToUser() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.TOOL)
                                .content("Tool result")
                                .build()
                ))
                .temperature(0.7)
                .build();

        Map<String, Object> responseBody = Map.of(
                "message", Map.of("content", "OK")
        );

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseBody);

        // when
        adapter.call(request);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(any(String.class), captor.capture(), eq(Map.class));

        Map<String, Object> body = captor.getValue().getBody();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("messages");
        assertThat(messages.get(0).get("role")).isEqualTo("user");
    }
}
