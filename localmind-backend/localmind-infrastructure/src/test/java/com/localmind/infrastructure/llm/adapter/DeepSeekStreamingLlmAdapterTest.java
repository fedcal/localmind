package com.localmind.infrastructure.llm.adapter;

import com.localmind.domain.llm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeepSeekStreamingLlmAdapterTest {

    @Mock
    private OpenAiChatModel chatModel;

    private DeepSeekStreamingLlmAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DeepSeekStreamingLlmAdapter("test-api-key", "https://api.deepseek.com");
        ReflectionTestUtils.setField(adapter, "chatModel", chatModel);
    }

    @Test
    void getProvider_shouldReturnDeepSeek() {
        // when
        LlmProvider provider = adapter.getProvider();

        // then
        assertThat(provider).isEqualTo(LlmProvider.DEEPSEEK);
    }

    @Test
    void isAvailable_shouldReturnTrue() {
        // when
        boolean available = adapter.isAvailable();

        // then
        assertThat(available).isTrue();
    }

    @Test
    void streamResponse_shouldStreamTokensAndComplete() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Hello DeepSeek")
                                .build()
                ))
                .model("deepseek-chat")
                .temperature(0.7)
                .build();

        ChatResponse chunk1 = buildStreamChunk("Hello", 0, 0, 0);
        ChatResponse chunk2 = buildStreamChunk(" from", 0, 0, 0);
        ChatResponse finalChunk = buildStreamChunk(" DeepSeek", 10, 20, 30);

        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(chunk1, chunk2, finalChunk));

        List<String> receivedTokens = new ArrayList<>();
        AtomicReference<TokenUsage> completedUsage = new AtomicReference<>();
        AtomicReference<Exception> receivedError = new AtomicReference<>();

        // when
        adapter.streamResponse(request, receivedTokens::add, completedUsage::set, receivedError::set);

        // then
        verify(chatModel).stream(any(Prompt.class));
        assertThat(receivedTokens).containsExactly("Hello", " from", " DeepSeek");
        assertThat(completedUsage.get()).isNotNull();
        assertThat(completedUsage.get().getPromptTokens()).isEqualTo(10);
        assertThat(completedUsage.get().getCompletionTokens()).isEqualTo(20);
        assertThat(completedUsage.get().getTotalTokens()).isEqualTo(30);
        assertThat(receivedError.get()).isNull();
    }

    @Test
    void streamResponse_shouldMapTokenUsageFromLastChunkWithNonZeroTokens() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Count")
                                .build()
                ))
                .model("deepseek-chat")
                .temperature(0.5)
                .build();

        ChatResponse chunk1 = buildStreamChunk("Token", 0, 0, 0);
        ChatResponse finalChunk = buildStreamChunk(" counted", 15, 25, 40);

        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(chunk1, finalChunk));

        AtomicReference<TokenUsage> completedUsage = new AtomicReference<>();

        // when
        adapter.streamResponse(request, token -> {}, completedUsage::set, error -> {});

        // then
        assertThat(completedUsage.get()).isNotNull();
        assertThat(completedUsage.get().getPromptTokens()).isEqualTo(15);
        assertThat(completedUsage.get().getCompletionTokens()).isEqualTo(25);
        assertThat(completedUsage.get().getTotalTokens()).isEqualTo(40);
    }

    @Test
    void streamResponse_shouldConvertMessageRolesCorrectly() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.SYSTEM)
                                .content("Be a coder")
                                .build(),
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Write code")
                                .build(),
                        ChatMessage.builder()
                                .role(ChatMessage.Role.ASSISTANT)
                                .content("Here it is")
                                .build()
                ))
                .model("deepseek-coder")
                .temperature(0.7)
                .build();

        ChatResponse chunk = buildStreamChunk("OK", 5, 10, 15);
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(chunk));

        // when
        adapter.streamResponse(request, token -> {}, usage -> {}, error -> {});

        // then
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).stream(promptCaptor.capture());
        Prompt captured = promptCaptor.getValue();
        assertThat(captured.getInstructions()).hasSize(3);
        assertThat(captured.getInstructions().get(0)).isInstanceOf(SystemMessage.class);
        assertThat(captured.getInstructions().get(1)).isInstanceOf(UserMessage.class);
        assertThat(captured.getInstructions().get(2)).isInstanceOf(AssistantMessage.class);
    }

    @Test
    void streamResponse_shouldCallOnErrorWhenFluxErrors() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Fail")
                                .build()
                ))
                .model("deepseek-chat")
                .temperature(0.7)
                .build();

        RuntimeException expectedError = new RuntimeException("DeepSeek stream error");
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.error(expectedError));

        AtomicReference<Exception> receivedError = new AtomicReference<>();

        // when
        adapter.streamResponse(request, token -> {}, usage -> {}, receivedError::set);

        // then
        assertThat(receivedError.get()).isNotNull();
        assertThat(receivedError.get().getMessage()).isEqualTo("DeepSeek stream error");
    }

    @Test
    void streamResponse_shouldCallOnErrorWhenExceptionThrownBeforeStream() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Hello")
                                .build()
                ))
                .model("deepseek-chat")
                .temperature(0.7)
                .build();

        when(chatModel.stream(any(Prompt.class))).thenThrow(new RuntimeException("Connection failed"));

        AtomicReference<Exception> receivedError = new AtomicReference<>();

        // when
        adapter.streamResponse(request, token -> {}, usage -> {}, receivedError::set);

        // then
        assertThat(receivedError.get()).isNotNull();
        assertThat(receivedError.get().getMessage()).isEqualTo("Connection failed");
    }

    @Test
    void streamResponse_shouldSkipNullOutputText() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Hello")
                                .build()
                ))
                .model("deepseek-chat")
                .temperature(0.7)
                .build();

        Generation generation = new Generation((AssistantMessage) null);
        ChatResponse nullChunk = new ChatResponse(List.of(generation));
        ChatResponse validChunk = buildStreamChunk("Valid", 5, 10, 15);

        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(nullChunk, validChunk));

        List<String> receivedTokens = new ArrayList<>();

        // when
        adapter.streamResponse(request, receivedTokens::add, usage -> {}, error -> {});

        // then
        assertThat(receivedTokens).containsExactly("Valid");
    }

    private ChatResponse buildStreamChunk(String content, int promptTokens, int completionTokens, int totalTokens) {
        AssistantMessage assistantMsg = new AssistantMessage(content);
        Generation generation = new Generation(assistantMsg);

        Usage usage = mock(Usage.class);
        lenient().when(usage.getPromptTokens()).thenReturn(promptTokens);
        lenient().when(usage.getCompletionTokens()).thenReturn(completionTokens);
        lenient().when(usage.getTotalTokens()).thenReturn(totalTokens);

        ChatResponseMetadata metadata = mock(ChatResponseMetadata.class);
        lenient().when(metadata.getUsage()).thenReturn(usage);

        return new ChatResponse(List.of(generation), metadata);
    }
}
