package com.localmind.infrastructure.llm.adapter;

import com.localmind.domain.llm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OllamaLlmAdapterTest {

    @Mock
    private OllamaChatModel chatModel;

    private OllamaLlmAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OllamaLlmAdapter(chatModel, "http://localhost:11434");
    }

    @Test
    void getProvider_shouldReturnOllama() {
        // when
        LlmProvider provider = adapter.getProvider();

        // then
        assertThat(provider).isEqualTo(LlmProvider.OLLAMA);
    }

    @Test
    void call_shouldInvokeChatModel() {
        // given
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(
                        ChatMessage.builder()
                                .role(ChatMessage.Role.USER)
                                .content("Hello")
                                .build()
                ))
                .model("llama3")
                .temperature(0.7)
                .build();

        // Use a real ChatResponse via its constructor to avoid complex mocking
        org.springframework.ai.chat.model.Generation generation =
                new org.springframework.ai.chat.model.Generation(
                        new org.springframework.ai.chat.messages.AssistantMessage("Hello back!"));

        org.springframework.ai.chat.model.ChatResponse chatResponse =
                new org.springframework.ai.chat.model.ChatResponse(List.of(generation));

        org.mockito.Mockito.when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // when
        LlmResponse response = adapter.call(request);

        // then
        verify(chatModel).call(any(Prompt.class));
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("Hello back!");
        assertThat(response.getProvider()).isEqualTo(LlmProvider.OLLAMA);
    }
}
