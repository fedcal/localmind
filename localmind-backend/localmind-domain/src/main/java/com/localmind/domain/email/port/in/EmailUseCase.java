package com.localmind.domain.email.port.in;

import com.localmind.domain.email.model.EmailDraft;
import com.localmind.domain.email.model.EmailMessage;

import java.util.List;

public interface EmailUseCase {
    List<EmailMessage> listInbox(int limit);
    void sendEmail(EmailDraft draft);
    void markAsRead(String messageId);
    String generateDraftReply(String messageId);
    boolean isAvailable();
}
