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
import org.springframework.ai.mistralai.MistralAiChatModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MistralLlmAdapterTest {

    @Mock
    private MistralAiChatModel chatModel;

    private MistralLlmAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MistralLlmAdapter(chatModel);
    }

    @Test
    void getProvider_shouldReturnMistral() {
        // when
        LlmProvider provider = adapter.getProvider();

        // then
        assertThat(provider).isEqualTo(LlmProvider.MISTRAL);
    }

    @Test
    void isAvailable_shouldReturnTrue() {
        // when
        boolean available = adapter.isAvailable();

        // then
        assertThat(available).isTrue();
    }

    @Test
    void call_shouldInvokeChatModelAndReturnCorrectResponse() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Hello Mistral")
                                .build()
                ))
                .model("mistral-large-latest")
                .temperature(0.7)
                .build();

        ChatResponse chatResponse = buildMockChatResponse("Bonjour!", "mistral-large-latest", 10, 20, 30);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // when
        LlmResponse response = adapter.call(request);

        // then
        verify(chatModel).call(any(Prompt.class));
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("Bonjour!");
        assertThat(response.getModel()).isEqualTo("mistral-large-latest");
        assertThat(response.getProvider()).isEqualTo(LlmProvider.MISTRAL);
    }

    @Test
    void call_shouldMapTokenUsageCorrectly() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Count tokens")
                                .build()
                ))
                .model("mistral-large-latest")
                .temperature(0.5)
                .build();

        ChatResponse chatResponse = buildMockChatResponse("Done", "mistral-large-latest", 15, 25, 40);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // when
        LlmResponse response = adapter.call(request);

        // then
        assertThat(response.getTokenUsage()).isNotNull();
        assertThat(response.getTokenUsage().getPromptTokens()).isEqualTo(15);
        assertThat(response.getTokenUsage().getCompletionTokens()).isEqualTo(25);
        assertThat(response.getTokenUsage().getTotalTokens()).isEqualTo(40);
    }

    @Test
    void call_shouldConvertMessageRolesCorrectly() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.SYSTEM)
                                .content("You are helpful")
                                .build(),
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Hello")
                                .build(),
                        ChatMessage.builder()
                                .role(ChatMessage.Role.ASSISTANT)
                                .content("Hi there")
                                .build()
                ))
                .model("mistral-large-latest")
                .temperature(0.7)
                .build();

        ChatResponse chatResponse = buildMockChatResponse("Response", "mistral-large-latest", 5, 10, 15);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // when
        adapter.call(request);

        // then
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        Prompt captured = promptCaptor.getValue();
        assertThat(captured.getInstructions()).hasSize(3);
        assertThat(captured.getInstructions().get(0)).isInstanceOf(SystemMessage.class);
        assertThat(captured.getInstructions().get(1)).isInstanceOf(UserMessage.class);
        assertThat(captured.getInstructions().get(2)).isInstanceOf(AssistantMessage.class);
    }

    @Test
    void call_shouldHandleNullMetadataGracefully() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Hello")
                                .build()
                ))
                .model("mistral-large-latest")
                .temperature(0.7)
                .build();

        AssistantMessage assistantMsg = new AssistantMessage("Reply");
        Generation generation = new Generation(assistantMsg);
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // when
        LlmResponse response = adapter.call(request);

        // then
        assertThat(response.getContent()).isEqualTo("Reply");
        assertThat(response.getProvider()).isEqualTo(LlmProvider.MISTRAL);
    }

    private ChatResponse buildMockChatResponse(String content, String model,
                                                int promptTokens, int completionTokens, int totalTokens) {
        AssistantMessage assistantMsg = new AssistantMessage(content);
        Generation generation = new Generation(assistantMsg);

        Usage usage = mock(Usage.class);
        when(usage.getPromptTokens()).thenReturn(promptTokens);
        when(usage.getCompletionTokens()).thenReturn(completionTokens);
        when(usage.getTotalTokens()).thenReturn(totalTokens);

        ChatResponseMetadata metadata = mock(ChatResponseMetadata.class);
        when(metadata.getModel()).thenReturn(model);
        when(metadata.getUsage()).thenReturn(usage);

        return new ChatResponse(List.of(generation), metadata);
    }
}
