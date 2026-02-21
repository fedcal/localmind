package com.localmind.api.settings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProviderRequestDto {
    @NotBlank(message = "Provider name is required")
    private String name;
    @NotNull(message = "Provider type is required")
    private String type;
    @NotBlank(message = "Base URL is required")
    private String baseUrl;
    private String apiKey;
    private String defaultModel;
}
