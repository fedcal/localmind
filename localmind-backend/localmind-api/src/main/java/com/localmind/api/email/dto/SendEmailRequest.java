package com.localmind.api.email.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailRequest {
    @NotEmpty(message = "At least one recipient is required")
    private List<String> to;
    @NotBlank(message = "Subject is required")
    private String subject;
    private String body;
}
