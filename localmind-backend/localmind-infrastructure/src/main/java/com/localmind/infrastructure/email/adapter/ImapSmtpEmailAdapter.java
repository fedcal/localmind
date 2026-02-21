package com.localmind.infrastructure.email.adapter;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.email.model.EmailDraft;
import com.localmind.domain.email.model.EmailMessage;
import com.localmind.domain.email.port.out.EmailPort;
import com.localmind.infrastructure.email.config.EmailProperties;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@ConditionalOnProperty(name = "localmind.email.enabled", havingValue = "true")
@EnableConfigurationProperties(EmailProperties.class)
public class ImapSmtpEmailAdapter implements EmailPort {

    private static final Logger log = LoggerFactory.getLogger(ImapSmtpEmailAdapter.class);

    private final EmailProperties properties;

    public ImapSmtpEmailAdapter(EmailProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<EmailMessage> listInbox(int limit) {
        List<EmailMessage> messages = new ArrayList<>();
        try {
            Store store = getImapStore();
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            int messageCount = inbox.getMessageCount();
            int start = Math.max(1, messageCount - limit + 1);
            Message[] msgs = inbox.getMessages(start, messageCount);

            for (int i = msgs.length - 1; i >= 0; i--) {
                messages.add(toEmailMessage(msgs[i]));
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            log.error("Error listing inbox", e);
        }
        return messages;
    }

    @Override
    public void sendEmail(EmailDraft draft) {
        try {
            Session session = getSmtpSession();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(properties.getUsername()));

            for (String to : draft.getTo()) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            }

            message.setSubject(draft.getSubject());
            message.setText(draft.getBody(), "UTF-8");
            message.setSentDate(new Date());

            Transport.send(message);
            log.info("Email sent to {}", draft.getTo());
        } catch (Exception e) {
            throw new RuntimeException("Error sending email", e);
        }
    }

    @Override
    public void markAsRead(String messageId) {
        try {
            Store store = getImapStore();
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages();
            for (Message msg : messages) {
                if (getMessageId(msg).equals(messageId)) {
                    msg.setFlag(Flags.Flag.SEEN, true);
                    log.info("Marked message as read: {}", messageId);
                    break;
                }
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            log.error("Error marking message as read: {}", messageId, e);
        }
    }

    @Override
    public EmailMessage findById(String messageId) {
        try {
            Store store = getImapStore();
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            for (Message msg : messages) {
                if (getMessageId(msg).equals(messageId)) {
                    EmailMessage result = toEmailMessage(msg);
                    inbox.close(false);
                    store.close();
                    return result;
                }
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            log.error("Error finding message by id: {}", messageId, e);
        }
        throw new ResourceNotFoundException("Email not found: " + messageId);
    }

    @Override
    public boolean isAvailable() {
        try {
            Store store = getImapStore();
            boolean connected = store.isConnected();
            store.close();
            return connected;
        } catch (Exception e) {
            log.debug("Email service not available: {}", e.getMessage());
            return false;
        }
    }

    private Store getImapStore() throws MessagingException {
        Properties props = new Properties();
        props.put("mail.imap.host", properties.getImap().getHost());
        props.put("mail.imap.port", String.valueOf(properties.getImap().getPort()));
        props.put("mail.imap.ssl.enable", "true");
        props.put("mail.imap.connectiontimeout", "5000");
        props.put("mail.imap.timeout", "5000");

        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        store.connect(properties.getImap().getHost(), properties.getUsername(), properties.getPassword());
        return store;
    }

    private Session getSmtpSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", properties.getSmtp().getHost());
        props.put("mail.smtp.port", String.valueOf(properties.getSmtp().getPort()));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getUsername(), properties.getPassword());
            }
        });
    }

    private EmailMessage toEmailMessage(Message message) throws MessagingException {
        String body = "";
        try {
            Object content = message.getContent();
            if (content instanceof String) {
                body = (String) content;
            } else if (content instanceof Multipart multipart) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart part = multipart.getBodyPart(i);
                    if (part.isMimeType("text/plain")) {
                        sb.append(part.getContent().toString());
                    }
                }
                body = sb.toString();
            }
        } catch (Exception e) {
            log.warn("Could not read message body", e);
        }

        Address[] toAddresses = message.getRecipients(Message.RecipientType.TO);
        List<String> toList = new ArrayList<>();
        if (toAddresses != null) {
            for (Address addr : toAddresses) {
                toList.add(addr.toString());
            }
        }

        return EmailMessage.builder()
                .id(getMessageId(message))
                .from(message.getFrom() != null && message.getFrom().length > 0
                        ? message.getFrom()[0].toString() : "")
                .to(toList)
                .subject(message.getSubject() != null ? message.getSubject() : "")
                .body(body)
                .receivedAt(message.getReceivedDate() != null
                        ? message.getReceivedDate().toInstant() : Instant.now())
                .read(message.isSet(Flags.Flag.SEEN))
                .build();
    }

    private String getMessageId(Message message) throws MessagingException {
        String[] headers = message.getHeader("Message-ID");
        if (headers != null && headers.length > 0) {
            return headers[0];
        }
        return String.valueOf(message.getMessageNumber());
    }
}
