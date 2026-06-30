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
class OpenAiMultimodalAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    private OpenAiMultimodalAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OpenAiMultimodalAdapter("sk-test-key", "gpt-4o");
        ReflectionTestUtils.setField(adapter, "restTemplate", restTemplate);
    }

    @Test
    void supportsMultimodal_shouldReturnTrue() {
        assertThat(adapter.supportsMultimodal()).isTrue();
    }

    @Test
    void getProvider_shouldReturnOpenAi() {
        assertThat(adapter.getProvider()).isEqualTo(LlmProvider.OPENAI);
    }

    @Test
    void isAvailable_withApiKey_shouldReturnTrue() {
        assertThat(adapter.isAvailable()).isTrue();
    }

    @Test
    void isAvailable_withEmptyApiKey_shouldReturnFalse() {
        OpenAiMultimodalAdapter emptyAdapter = new OpenAiMultimodalAdapter("", "gpt-4o");
        assertThat(emptyAdapter.isAvailable()).isFalse();
    }

    @Test
    void isAvailable_withNullApiKey_shouldReturnFalse() {
        OpenAiMultimodalAdapter nullAdapter = new OpenAiMultimodalAdapter(null, "gpt-4o");
        assertThat(nullAdapter.isAvailable()).isFalse();
    }

    @Test
    void call_withTextOnly_shouldSendStringContent() {
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

        Map<String, Object> responseBody = Map.of(
                "choices", List.of(Map.of(
                        "message", Map.of("content", "Hi there")
                )),
                "usage", Map.of(
                        "prompt_tokens", 5,
                        "completion_tokens", 3
                )
        );

        when(restTemplate.postForObject(
                eq("https://api.openai.com/v1/chat/completions"),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseBody);

        // when
        LlmResponse response = adapter.call(request);

        // then
        assertThat(response.getContent()).isEqualTo("Hi there");
        assertThat(response.getProvider()).isEqualTo(LlmProvider.OPENAI);
        assertThat(response.getModel()).isEqualTo("gpt-4o");
        assertThat(response.getTokenUsage()).isNotNull();
        assertThat(response.getTokenUsage().getPromptTokens()).isEqualTo(5);
        assertThat(response.getTokenUsage().getCompletionTokens()).isEqualTo(3);
    }

    @Test
    void call_withImageParts_shouldSendContentArray() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("What is in this image?")
                                .parts(List.of(
                                        ContentPart.builder()
                                                .type(ContentPart.ContentType.IMAGE)
                                                .content("iVBORw0KGgo=")
                                                .mimeType("image/png")
                                                .build()
                                ))
                                .build()
                ))
                .temperature(0.5)
                .build();

        Map<String, Object> responseBody = Map.of(
                "choices", List.of(Map.of(
                        "message", Map.of("content", "I see a cat")
                )),
                "usage", Map.of(
                        "prompt_tokens", 100,
                        "completion_tokens", 10
                )
        );

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseBody);

        // when
        LlmResponse response = adapter.call(request);

        // then
        assertThat(response.getContent()).isEqualTo("I see a cat");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(any(String.class), captor.capture(), eq(Map.class));

        Map<String, Object> body = captor.getValue().getBody();
        assertThat(body).isNotNull();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("messages");
        assertThat(messages).hasSize(1);

        Map<String, Object> userMsg = messages.get(0);
        // Content should be an array when parts are present
        assertThat(userMsg.get("content")).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> contentArray = (List<Map<String, Object>>) userMsg.get("content");
        assertThat(contentArray).hasSize(2); // text + image

        // First element: text
        assertThat(contentArray.get(0).get("type")).isEqualTo("text");
        assertThat(contentArray.get(0).get("text")).isEqualTo("What is in this image?");

        // Second element: image_url
        assertThat(contentArray.get(1).get("type")).isEqualTo("image_url");
        @SuppressWarnings("unchecked")
        Map<String, Object> imageUrl = (Map<String, Object>) contentArray.get(1).get("image_url");
        assertThat(imageUrl.get("url").toString()).startsWith("data:image/png;base64,");
    }

    @Test
    void call_withDataUrlImage_shouldNotDuplicatePrefix() {
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
                "choices", List.of(Map.of("message", Map.of("content", "OK")))
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
        List<Map<String, Object>> content = (List<Map<String, Object>>) messages.get(0).get("content");
        @SuppressWarnings("unchecked")
        Map<String, Object> imageUrl = (Map<String, Object>) content.get(1).get("image_url");

        // Should keep original data URL, not add another prefix
        assertThat(imageUrl.get("url")).isEqualTo("data:image/jpeg;base64,/9j/4AAQ");
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
                .model("gpt-4o-mini")
                .temperature(0.7)
                .build();

        Map<String, Object> responseBody = Map.of(
                "choices", List.of(Map.of("message", Map.of("content", "Hi")))
        );

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseBody);

        // when
        LlmResponse response = adapter.call(request);

        // then
        assertThat(response.getModel()).isEqualTo("gpt-4o-mini");
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
    void call_withMaxTokens_shouldIncludeInRequest() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Hello")
                                .build()
                ))
                .temperature(0.7)
                .maxTokens(500)
                .build();

        Map<String, Object> responseBody = Map.of(
                "choices", List.of(Map.of("message", Map.of("content", "Hi")))
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
        assertThat(body.get("max_tokens")).isEqualTo(500);
    }

    @Test
    void call_shouldIncludeBearerAuth() {
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

        Map<String, Object> responseBody = Map.of(
                "choices", List.of(Map.of("message", Map.of("content", "Hi")))
        );

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseBody);

        // when
        adapter.call(request);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(any(String.class), captor.capture(), eq(Map.class));

        String authHeader = captor.getValue().getHeaders().getFirst("Authorization");
        assertThat(authHeader).isEqualTo("Bearer sk-test-key");
    }
}
