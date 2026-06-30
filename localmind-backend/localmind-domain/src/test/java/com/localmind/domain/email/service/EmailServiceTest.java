package com.localmind.domain.email.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.email.model.EmailDraft;
import com.localmind.domain.email.model.EmailMessage;
import com.localmind.domain.email.port.out.EmailPort;
import com.localmind.domain.llm.model.LlmRequest;
import com.localmind.domain.llm.model.LlmResponse;
import com.localmind.domain.llm.port.out.LlmClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private EmailPort emailPort;

    @Mock
    private LlmClient llmClient;

    @InjectMocks
    private EmailService emailService;

    @Test
    void listInbox_shouldReturnMessages() {
        EmailMessage msg = EmailMessage.builder()
                .id("msg-1")
                .from("sender@test.com")
                .to(List.of("me@test.com"))
                .subject("Hello")
                .body("World")
                .receivedAt(Instant.parse("2026-02-20T10:00:00Z"))
                .read(false)
                .build();

        when(emailPort.listInbox(10)).thenReturn(List.of(msg));

        List<EmailMessage> result = emailService.listInbox(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSubject()).isEqualTo("Hello");
        verify(emailPort).listInbox(10);
    }

    @Test
    void listInbox_shouldThrowWhenLimitIsZero() {
        assertThatThrownBy(() -> emailService.listInbox(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than 0");

        verify(emailPort, never()).listInbox(anyInt());
    }

    @Test
    void listInbox_shouldThrowWhenLimitIsNegative() {
        assertThatThrownBy(() -> emailService.listInbox(-5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than 0");

        verify(emailPort, never()).listInbox(anyInt());
    }

    @Test
    void sendEmail_shouldDelegateToPort() {
        EmailDraft draft = EmailDraft.builder()
                .to(List.of("recipient@test.com"))
                .subject("Test Subject")
                .body("Test body")
                .build();

        emailService.sendEmail(draft);

        verify(emailPort).sendEmail(draft);
    }

    @Test
    void sendEmail_shouldThrowWhenNoRecipients() {
        EmailDraft draft = EmailDraft.builder()
                .to(List.of())
                .subject("Test")
                .body("Body")
                .build();

        assertThatThrownBy(() -> emailService.sendEmail(draft))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("recipient");

        verify(emailPort, never()).sendEmail(any());
    }

    @Test
    void sendEmail_shouldThrowWhenRecipientsNull() {
        EmailDraft draft = EmailDraft.builder()
                .to(null)
                .subject("Test")
                .body("Body")
                .build();

        assertThatThrownBy(() -> emailService.sendEmail(draft))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("recipient");

        verify(emailPort, never()).sendEmail(any());
    }

    @Test
    void sendEmail_shouldThrowWhenSubjectIsBlank() {
        EmailDraft draft = EmailDraft.builder()
                .to(List.of("recipient@test.com"))
                .subject("")
                .body("Body")
                .build();

        assertThatThrownBy(() -> emailService.sendEmail(draft))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Subject");

        verify(emailPort, never()).sendEmail(any());
    }

    @Test
    void markAsRead_shouldDelegateToPort() {
        emailService.markAsRead("msg-1");

        verify(emailPort).markAsRead("msg-1");
    }

    @Test
    void markAsRead_shouldThrowWhenIdIsBlank() {
        assertThatThrownBy(() -> emailService.markAsRead(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required");

        verify(emailPort, never()).markAsRead(any());
    }

    @Test
    void markAsRead_shouldThrowWhenIdIsNull() {
        assertThatThrownBy(() -> emailService.markAsRead(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required");

        verify(emailPort, never()).markAsRead(any());
    }

    @Test
    void generateDraftReply_shouldCallLlmWithOriginalEmailContext() {
        EmailMessage original = EmailMessage.builder()
                .id("msg-1")
                .from("sender@test.com")
                .to(List.of("me@test.com"))
                .subject("Project Update")
                .body("Please send me the latest report.")
                .receivedAt(Instant.parse("2026-02-20T10:00:00Z"))
                .read(true)
                .build();

        when(emailPort.findById("msg-1")).thenReturn(original);

        LlmResponse llmResponse = LlmResponse.builder()
                .content("Thank you for your message. I will send the report shortly.")
                .build();
        when(llmClient.call(any(LlmRequest.class))).thenReturn(llmResponse);

        String reply = emailService.generateDraftReply("msg-1");

        assertThat(reply).isEqualTo("Thank you for your message. I will send the report shortly.");

        ArgumentCaptor<LlmRequest> captor = ArgumentCaptor.forClass(LlmRequest.class);
        verify(llmClient).call(captor.capture());

        LlmRequest sentRequest = captor.getValue();
        assertThat(sentRequest.getMessages()).hasSize(1);
        String prompt = sentRequest.getMessages().get(0).getContent();
        assertThat(prompt).contains("sender@test.com");
        assertThat(prompt).contains("Project Update");
        assertThat(prompt).contains("Please send me the latest report.");
    }

    @Test
    void generateDraftReply_shouldThrowWhenMessageNotFound() {
        when(emailPort.findById("nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> emailService.generateDraftReply("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("nonexistent");

        verify(llmClient, never()).call(any());
    }

    @Test
    void generateDraftReply_shouldThrowWhenIdIsBlank() {
        assertThatThrownBy(() -> emailService.generateDraftReply(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required");

        verify(emailPort, never()).findById(any());
        verify(llmClient, never()).call(any());
    }

    @Test
    void isAvailable_shouldDelegateToPort() {
        when(emailPort.isAvailable()).thenReturn(true);

        boolean result = emailService.isAvailable();

        assertThat(result).isTrue();
        verify(emailPort).isAvailable();
    }

    @Test
    void isAvailable_shouldReturnFalseWhenPortUnavailable() {
        when(emailPort.isAvailable()).thenReturn(false);

        boolean result = emailService.isAvailable();

        assertThat(result).isFalse();
    }
}
