package com.localmind.domain.email.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.email.model.EmailDraft;
import com.localmind.domain.email.model.EmailMessage;
import com.localmind.domain.email.port.in.EmailUseCase;
import com.localmind.domain.email.port.out.EmailPort;
import com.localmind.domain.llm.model.ChatMessage;
import com.localmind.domain.llm.model.LlmRequest;
import com.localmind.domain.llm.model.LlmResponse;
import com.localmind.domain.llm.port.out.LlmClient;

import java.util.List;

public class EmailService implements EmailUseCase {

    private final EmailPort emailPort;
    private final LlmClient llmClient;

    public EmailService(EmailPort emailPort, LlmClient llmClient) {
        this.emailPort = emailPort;
        this.llmClient = llmClient;
    }

    @Override
    public List<EmailMessage> listInbox(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }
        return emailPort.listInbox(limit);
    }

    @Override
    public void sendEmail(EmailDraft draft) {
        if (draft.getTo() == null || draft.getTo().isEmpty()) {
            throw new IllegalArgumentException("At least one recipient is required");
        }
        if (draft.getSubject() == null || draft.getSubject().isBlank()) {
            throw new IllegalArgumentException("Subject is required");
        }
        emailPort.sendEmail(draft);
    }

    @Override
    public void markAsRead(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("Message ID is required");
        }
        emailPort.markAsRead(messageId);
    }

    @Override
    public String generateDraftReply(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("Message ID is required");
        }

        EmailMessage original = emailPort.findById(messageId);
        if (original == null) {
            throw new ResourceNotFoundException("Email not found: " + messageId);
        }

        String prompt = buildReplyPrompt(original);
        LlmRequest request = LlmRequest.builder()
                .messages(List.of(ChatMessage.builder()
                        .role(ChatMessage.Role.USER)
                        .content(prompt)
                        .build()))
                .temperature(0.7)
                .build();

        LlmResponse response = llmClient.call(request);
        return response.getContent();
    }

    @Override
    public boolean isAvailable() {
        return emailPort.isAvailable();
    }

    private String buildReplyPrompt(EmailMessage original) {
        return String.format(
                "Generate a professional reply to the following email.\n\n" +
                "From: %s\n" +
                "Subject: %s\n" +
                "Body:\n%s\n\n" +
                "Write only the reply body text, without any email headers.",
                original.getFrom(),
                original.getSubject(),
                original.getBody()
        );
    }
}
