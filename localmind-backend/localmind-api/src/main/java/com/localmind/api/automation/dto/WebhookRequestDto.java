package com.localmind.api.automation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRequestDto {

    @NotBlank
    @Size(min = 2, max = 200)
    private String name;

    @NotBlank
    @Size(max = 1000)
    private String url;

    @NotBlank
    private String eventType;

    @Builder.Default
    private boolean active = true;
}
