package com.localmind.api.email.controller;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.api.email.dto.SendEmailRequest;
import com.localmind.domain.email.model.EmailDraft;
import com.localmind.domain.email.model.EmailMessage;
import com.localmind.domain.email.port.in.EmailUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

class EmailControllerTest {

    private MockMvc mockMvc;
    private EmailUseCase emailUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        emailUseCase = mock(EmailUseCase.class);
        EmailController controller = new EmailController(emailUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void listInbox_shouldReturnMessages() throws Exception {
        EmailMessage msg = EmailMessage.builder()
                .id("msg-1")
                .from("sender@test.com")
                .to(List.of("me@test.com"))
                .subject("Hello")
                .body("World")
                .receivedAt(Instant.parse("2026-02-20T10:00:00Z"))
                .read(false)
                .build();

        when(emailUseCase.listInbox(20)).thenReturn(List.of(msg));

        mockMvc.perform(get("/api/v1/email/inbox")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("msg-1"))
                .andExpect(jsonPath("$[0].from").value("sender@test.com"))
                .andExpect(jsonPath("$[0].subject").value("Hello"))
                .andExpect(jsonPath("$[0].read").value(false));

        verify(emailUseCase).listInbox(20);
    }

    @Test
    void listInbox_shouldAcceptCustomLimit() throws Exception {
        when(emailUseCase.listInbox(5)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/email/inbox")
                        .param("limit", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(emailUseCase).listInbox(5);
    }

    @Test
    void sendEmail_shouldReturn200() throws Exception {
        SendEmailRequest request = SendEmailRequest.builder()
                .to(List.of("recipient@test.com"))
                .subject("Test Subject")
                .body("Test body content")
                .build();

        doNothing().when(emailUseCase).sendEmail(any(EmailDraft.class));

        mockMvc.perform(post("/api/v1/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(emailUseCase).sendEmail(any(EmailDraft.class));
    }

    @Test
    void sendEmail_shouldReturn400WhenNoRecipients() throws Exception {
        SendEmailRequest request = SendEmailRequest.builder()
                .to(List.of())
                .subject("Test")
                .body("Body")
                .build();

        mockMvc.perform(post("/api/v1/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(emailUseCase, never()).sendEmail(any());
    }

    @Test
    void sendEmail_shouldReturn400WhenSubjectIsBlank() throws Exception {
        SendEmailRequest request = SendEmailRequest.builder()
                .to(List.of("recipient@test.com"))
                .subject("")
                .body("Body")
                .build();

        mockMvc.perform(post("/api/v1/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(emailUseCase, never()).sendEmail(any());
    }

    @Test
    void markAsRead_shouldReturn200() throws Exception {
        doNothing().when(emailUseCase).markAsRead("msg-1");

        mockMvc.perform(patch("/api/v1/email/msg-1/read"))
                .andExpect(status().isOk());

        verify(emailUseCase).markAsRead("msg-1");
    }

    @Test
    void generateDraftReply_shouldReturnDraftBody() throws Exception {
        when(emailUseCase.generateDraftReply("msg-1"))
                .thenReturn("Thank you for your message. I will review and respond shortly.");

        mockMvc.perform(post("/api/v1/email/msg-1/draft-reply")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.draftBody")
                        .value("Thank you for your message. I will review and respond shortly."));

        verify(emailUseCase).generateDraftReply("msg-1");
    }

    @Test
    void listInbox_shouldReturnMultipleMessages() throws Exception {
        EmailMessage msg1 = EmailMessage.builder()
                .id("msg-1").from("a@test.com").to(List.of("me@test.com"))
                .subject("First").body("Body 1")
                .receivedAt(Instant.parse("2026-02-20T10:00:00Z")).read(true)
                .build();
        EmailMessage msg2 = EmailMessage.builder()
                .id("msg-2").from("b@test.com").to(List.of("me@test.com"))
                .subject("Second").body("Body 2")
                .receivedAt(Instant.parse("2026-02-20T11:00:00Z")).read(false)
                .build();

        when(emailUseCase.listInbox(20)).thenReturn(List.of(msg1, msg2));

        mockMvc.perform(get("/api/v1/email/inbox")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].subject").value("First"))
                .andExpect(jsonPath("$[0].read").value(true))
                .andExpect(jsonPath("$[1].subject").value("Second"))
                .andExpect(jsonPath("$[1].read").value(false));
    }
}
