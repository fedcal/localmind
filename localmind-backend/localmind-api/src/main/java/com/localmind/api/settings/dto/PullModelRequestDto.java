package com.localmind.api.settings.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PullModelRequestDto {
    @NotBlank(message = "Base URL is required")
    private String baseUrl;
    @NotBlank(message = "Model name is required")
    private String modelName;
}
