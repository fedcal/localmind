package com.localmind.api.email.controller;

import com.localmind.api.email.dto.DraftReplyResponse;
import com.localmind.api.email.dto.EmailMessageDto;
import com.localmind.api.email.dto.SendEmailRequest;
import com.localmind.domain.email.model.EmailDraft;
import com.localmind.domain.email.model.EmailMessage;
import com.localmind.domain.email.port.in.EmailUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/email")
@Tag(name = "Email", description = "Email management via IMAP/SMTP")
public class EmailController {

    private final EmailUseCase emailUseCase;

    public EmailController(EmailUseCase emailUseCase) {
        this.emailUseCase = emailUseCase;
    }

    @GetMapping("/inbox")
    @Operation(summary = "Lista email in arrivo / List inbox emails")
    @ApiResponse(responseCode = "200", description = "Lista email / Email list")
    public ResponseEntity<List<EmailMessageDto>> listInbox(
            @RequestParam(defaultValue = "20") int limit) {
        List<EmailMessageDto> messages = emailUseCase.listInbox(limit).stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/send")
    @Operation(summary = "Invia email / Send email")
    @ApiResponse(responseCode = "200", description = "Email inviata / Email sent")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<Void> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        EmailDraft draft = EmailDraft.builder()
                .to(request.getTo())
                .subject(request.getSubject())
                .body(request.getBody())
                .build();
        emailUseCase.sendEmail(draft);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Segna email come letta / Mark email as read")
    @ApiResponse(responseCode = "200", description = "Email segnata come letta / Email marked as read")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Void> markAsRead(@PathVariable String id) {
        emailUseCase.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/draft-reply")
    @Operation(summary = "Genera bozza di risposta AI / Generate AI draft reply")
    @ApiResponse(responseCode = "200", description = "Bozza generata / Draft generated")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<DraftReplyResponse> generateDraftReply(@PathVariable String id) {
        String draftBody = emailUseCase.generateDraftReply(id);
        return ResponseEntity.ok(DraftReplyResponse.builder()
                .draftBody(draftBody)
                .build());
    }

    private EmailMessageDto toDto(EmailMessage message) {
        return EmailMessageDto.builder()
                .id(message.getId())
                .from(message.getFrom())
                .to(message.getTo())
                .subject(message.getSubject())
                .body(message.getBody())
                .receivedAt(message.getReceivedAt())
                .read(message.isRead())
                .build();
    }
}
