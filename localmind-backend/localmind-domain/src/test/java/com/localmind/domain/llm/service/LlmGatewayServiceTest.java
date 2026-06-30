package com.localmind.domain.llm.service;

import com.localmind.domain.common.exception.LlmProviderException;
import com.localmind.domain.llm.model.*;
import com.localmind.domain.llm.port.out.LlmClient;
import com.localmind.domain.llm.port.out.LlmUsageRepository;
import com.localmind.domain.llm.port.out.ProviderConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LlmGatewayServiceTest {

    @Mock
    private LlmClient ollamaClient;

    @Mock
    private LlmClient openaiClient;

    @Mock
    private LlmUsageRepository usageRepository;

    @Mock
    private ProviderConfigRepository providerConfigRepository;

    private LlmGatewayService service;

    private final List<LlmProvider> fallbackOrder = List.of(
            LlmProvider.OLLAMA, LlmProvider.OPENAI, LlmProvider.ANTHROPIC, LlmProvider.GOOGLE,
            LlmProvider.DEEPSEEK, LlmProvider.MISTRAL, LlmProvider.XAI);

    @BeforeEach
    void setUp() {
        lenient().when(ollamaClient.getProvider()).thenReturn(LlmProvider.OLLAMA);
        lenient().when(openaiClient.getProvider()).thenReturn(LlmProvider.OPENAI);

        service = new LlmGatewayService(
                List.of(ollamaClient, openaiClient),
                List.of(),
                usageRepository,
                providerConfigRepository,
                fallbackOrder,
                LlmProvider.OLLAMA
        );
    }

    @Test
    void chat_withExplicitProvider_shouldUseIt() {
        LlmRequest request = LlmRequest.builder()
                .provider(LlmProvider.OLLAMA)
                .model("llama3")
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER).content("hello").build()))
                .build();

        LlmResponse expectedResponse = LlmResponse.builder()
                .content("Hi there!")
                .model("llama3")
                .build();

        when(ollamaClient.isAvailable()).thenReturn(true);
        when(ollamaClient.call(any(LlmRequest.class))).thenReturn(expectedResponse);

        LlmResponse result = service.chat(request);

        assertThat(result.getContent()).isEqualTo("Hi there!");
        assertThat(result.getProvider()).isEqualTo(LlmProvider.OLLAMA);
        verify(ollamaClient).call(any(LlmRequest.class));
        verify(openaiClient, never()).call(any(LlmRequest.class));
    }

    @Test
    void chat_withNoProvider_shouldUseDefault() {
        LlmRequest request = LlmRequest.builder()
                .provider(null)
                .model("llama3")
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER).content("hello").build()))
                .build();

        LlmResponse expectedResponse = LlmResponse.builder()
                .content("Default response")
                .model("llama3")
                .build();

        when(ollamaClient.isAvailable()).thenReturn(true);
        when(ollamaClient.call(any(LlmRequest.class))).thenReturn(expectedResponse);

        LlmResponse result = service.chat(request);

        assertThat(result.getContent()).isEqualTo("Default response");
        assertThat(result.getProvider()).isEqualTo(LlmProvider.OLLAMA);
        verify(ollamaClient).call(any(LlmRequest.class));
    }

    @Test
    void chat_shouldFallbackToNextProvider() {
        LlmRequest request = LlmRequest.builder()
                .provider(LlmProvider.OLLAMA)
                .model("gpt-4")
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER).content("hello").build()))
                .build();

        LlmResponse fallbackResponse = LlmResponse.builder()
                .content("OpenAI response")
                .model("gpt-4")
                .build();

        when(ollamaClient.isAvailable()).thenReturn(true);
        when(ollamaClient.call(any(LlmRequest.class))).thenThrow(new RuntimeException("Ollama down"));
        when(openaiClient.isAvailable()).thenReturn(true);
        when(openaiClient.call(any(LlmRequest.class))).thenReturn(fallbackResponse);

        LlmResponse result = service.chat(request);

        assertThat(result.getContent()).isEqualTo("OpenAI response");
        assertThat(result.getProvider()).isEqualTo(LlmProvider.OPENAI);
        verify(ollamaClient).call(any(LlmRequest.class));
        verify(openaiClient).call(any(LlmRequest.class));
    }

    @Test
    void chat_shouldSkipUnavailableClient() {
        LlmRequest request = LlmRequest.builder()
                .provider(LlmProvider.OLLAMA)
                .model("gpt-4")
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER).content("hello").build()))
                .build();

        LlmResponse expectedResponse = LlmResponse.builder()
                .content("OpenAI fallback")
                .model("gpt-4")
                .build();

        when(ollamaClient.isAvailable()).thenReturn(false);
        when(openaiClient.isAvailable()).thenReturn(true);
        when(openaiClient.call(any(LlmRequest.class))).thenReturn(expectedResponse);

        LlmResponse result = service.chat(request);

        assertThat(result.getContent()).isEqualTo("OpenAI fallback");
        assertThat(result.getProvider()).isEqualTo(LlmProvider.OPENAI);
        verify(ollamaClient, never()).call(any(LlmRequest.class));
        verify(openaiClient).call(any(LlmRequest.class));
    }

    @Test
    void chat_shouldThrowWhenNoProviderAvailable() {
        LlmRequest request = LlmRequest.builder()
                .provider(LlmProvider.OLLAMA)
                .model("llama3")
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER).content("hello").build()))
                .build();

        when(ollamaClient.isAvailable()).thenReturn(false);
        when(openaiClient.isAvailable()).thenReturn(false);

        assertThatThrownBy(() -> service.chat(request))
                .isInstanceOf(LlmProviderException.class)
                .hasMessageContaining("No LLM provider available");
    }

    @Test
    void chat_shouldTrackUsageOnSuccess() {
        TokenUsage tokenUsage = TokenUsage.builder()
                .promptTokens(10)
                .completionTokens(20)
                .totalTokens(30)
                .build();

        LlmRequest request = LlmRequest.builder()
                .provider(LlmProvider.OLLAMA)
                .model("llama3")
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER).content("hello").build()))
                .build();

        LlmResponse responseWithUsage = LlmResponse.builder()
                .content("response")
                .model("llama3")
                .tokenUsage(tokenUsage)
                .build();

        when(ollamaClient.isAvailable()).thenReturn(true);
        when(ollamaClient.call(any(LlmRequest.class))).thenReturn(responseWithUsage);

        service.chat(request);

        ArgumentCaptor<UsageRecord> captor = ArgumentCaptor.forClass(UsageRecord.class);
        verify(usageRepository).save(captor.capture());

        UsageRecord saved = captor.getValue();
        assertThat(saved.getProvider()).isEqualTo(LlmProvider.OLLAMA);
        assertThat(saved.getModel()).isEqualTo("llama3");
        assertThat(saved.getTokenUsage()).isEqualTo(tokenUsage);
        assertThat(saved.getLatencyMs()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void chat_shouldNotTrackUsageWhenNoTokenUsage() {
        LlmRequest request = LlmRequest.builder()
                .provider(LlmProvider.OLLAMA)
                .model("llama3")
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER).content("hello").build()))
                .build();

        LlmResponse responseNoUsage = LlmResponse.builder()
                .content("response")
                .model("llama3")
                .tokenUsage(null)
                .build();

        when(ollamaClient.isAvailable()).thenReturn(true);
        when(ollamaClient.call(any(LlmRequest.class))).thenReturn(responseNoUsage);

        service.chat(request);

        verify(usageRepository, never()).save(any(UsageRecord.class));
    }

    @Test
    void chat_shouldResolveModelFromConfig() {
        LlmRequest request = LlmRequest.builder()
                .provider(LlmProvider.OLLAMA)
                .model(null)
                .messages(List.of(ChatMessage.builder().role(ChatMessage.Role.USER).content("hello").build()))
                .build();

        LlmProviderConfig config = LlmProviderConfig.builder()
                .type(LlmProvider.OLLAMA)
                .defaultModel("llama3:latest")
                .enabled(true)
                .build();

        when(providerConfigRepository.findByType(LlmProvider.OLLAMA)).thenReturn(List.of(config));

        LlmResponse expectedResponse = LlmResponse.builder()
                .content("resolved model response")
                .model("llama3:latest")
                .build();

        when(ollamaClient.isAvailable()).thenReturn(true);
        when(ollamaClient.call(any(LlmRequest.class))).thenReturn(expectedResponse);

        LlmResponse result = service.chat(request);

        assertThat(result.getContent()).isEqualTo("resolved model response");

        ArgumentCaptor<LlmRequest> requestCaptor = ArgumentCaptor.forClass(LlmRequest.class);
        verify(ollamaClient).call(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getModel()).isEqualTo("llama3:latest");
    }
}
