package com.localmind.domain.email.port.out;

import com.localmind.domain.email.model.EmailDraft;
import com.localmind.domain.email.model.EmailMessage;

import java.util.List;

public interface EmailPort {
    List<EmailMessage> listInbox(int limit);
    void sendEmail(EmailDraft draft);
    void markAsRead(String messageId);
    EmailMessage findById(String messageId);
    boolean isAvailable();
}
