package com.localmind.domain.email.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {
    private String id;
    private String from;
    private List<String> to;
    private String subject;
    private String body;
    private Instant receivedAt;
    private boolean read;
}
