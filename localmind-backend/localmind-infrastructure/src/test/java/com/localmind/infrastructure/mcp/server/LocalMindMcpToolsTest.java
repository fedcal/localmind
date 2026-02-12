package com.localmind.infrastructure.mcp.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.localmind.domain.document.port.in.DocumentSearchUseCase;
import com.localmind.domain.llm.model.*;
import com.localmind.domain.llm.port.in.ChatUseCase;
import com.localmind.domain.llm.service.ConversationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class LocalMindMcpToolsTest {

    @Mock
    private DocumentSearchUseCase documentSearchUseCase;

    @Mock
    private ChatUseCase chatUseCase;

    @Mock
    private ConversationService conversationService;

    private LocalMindMcpTools mcpTools;

    @BeforeEach
    void setUp() {
        mcpTools = new LocalMindMcpTools(documentSearchUseCase, chatUseCase, conversationService);
    }

    @Test
    void chat_withNullConversationId_createsNewConversation() {
        ConversationContext newConv = ConversationContext.builder()
                .id("new-conv-id")
                .title("Hello AI")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Hello AI").build()
                ))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationService.getOrCreateForChat(isNull(), eq("Hello AI"), isNull()))
                .thenReturn(newConv);

        LlmResponse llmResponse = LlmResponse.builder()
                .content("Hi there!")
                .model("llama3.2")
                .provider(LlmProvider.OLLAMA)
                .latencyMs(150L)
                .build();
        when(chatUseCase.chat(any(LlmRequest.class))).thenReturn(llmResponse);

        when(conversationService.addAssistantMessage("new-conv-id", "Hi there!"))
                .thenReturn(newConv);

        Map<String, Object> result = mcpTools.chat("Hello AI", null, null, null, null);

        assertThat(result.get("content")).isEqualTo("Hi there!");
        assertThat(result.get("model")).isEqualTo("llama3.2");
        assertThat(result.get("provider")).isEqualTo("OLLAMA");
        assertThat(result.get("conversationId")).isEqualTo("new-conv-id");

        verify(conversationService).getOrCreateForChat(isNull(), eq("Hello AI"), isNull());
        verify(conversationService).addAssistantMessage("new-conv-id", "Hi there!");
    }

    @Test
    void chat_withExistingConversationId_continuesConversation() {
        ConversationContext existingConv = ConversationContext.builder()
                .id("existing-conv-id")
                .title("Previous conversation")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("First question").build(),
                        ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).content("First answer").build(),
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Follow up").build()
                ))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationService.getOrCreateForChat(eq("existing-conv-id"), eq("Follow up"), isNull()))
                .thenReturn(existingConv);

        LlmResponse llmResponse = LlmResponse.builder()
                .content("Follow up answer")
                .model("gpt-4")
                .provider(LlmProvider.OPENAI)
                .latencyMs(200L)
                .build();
        when(chatUseCase.chat(any(LlmRequest.class))).thenReturn(llmResponse);

        when(conversationService.addAssistantMessage("existing-conv-id", "Follow up answer"))
                .thenReturn(existingConv);

        Map<String, Object> result = mcpTools.chat("Follow up", "OPENAI", "gpt-4", 0.5, "existing-conv-id");

        assertThat(result.get("content")).isEqualTo("Follow up answer");
        assertThat(result.get("conversationId")).isEqualTo("existing-conv-id");

        // Verifica che tutti i messaggi della conversazione siano inviati al LLM
        ArgumentCaptor<LlmRequest> captor = ArgumentCaptor.forClass(LlmRequest.class);
        verify(chatUseCase).chat(captor.capture());
        LlmRequest captured = captor.getValue();

        assertThat(captured.getMessages()).hasSize(3);
        assertThat(captured.getMessages().get(0).getRole()).isEqualTo(ChatMessage.Role.USER);
        assertThat(captured.getMessages().get(0).getContent()).isEqualTo("First question");
        assertThat(captured.getMessages().get(1).getRole()).isEqualTo(ChatMessage.Role.ASSISTANT);
        assertThat(captured.getMessages().get(2).getRole()).isEqualTo(ChatMessage.Role.USER);
        assertThat(captured.getMessages().get(2).getContent()).isEqualTo("Follow up");

        assertThat(captured.getProvider()).isEqualTo(LlmProvider.OPENAI);
        assertThat(captured.getModel()).isEqualTo("gpt-4");
        assertThat(captured.getTemperature()).isEqualTo(0.5);
        assertThat(captured.getConversationId()).isEqualTo("existing-conv-id");
    }

    @Test
    void chat_returnsConversationIdInResponse() {
        ConversationContext conv = ConversationContext.builder()
                .id("auto-generated-id")
                .title("Test message")
                .messages(List.of(
                        ChatMessage.builder().role(ChatMessage.Role.USER).content("Test message").build()
                ))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationService.getOrCreateForChat(isNull(), eq("Test message"), isNull()))
                .thenReturn(conv);

        LlmResponse llmResponse = LlmResponse.builder()
                .content("Response")
                .model("llama3.2")
                .provider(LlmProvider.OLLAMA)
                .latencyMs(100L)
                .build();
        when(chatUseCase.chat(any(LlmRequest.class))).thenReturn(llmResponse);
        when(conversationService.addAssistantMessage(any(), any())).thenReturn(conv);

        Map<String, Object> result = mcpTools.chat("Test message", null, null, null, null);

        assertThat(result).containsKey("conversationId");
        assertThat(result.get("conversationId")).isEqualTo("auto-generated-id");
    }
}
