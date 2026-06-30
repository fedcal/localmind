package com.localmind.api.settings.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDefaultModelDto {
    @NotBlank(message = "Model name is required")
    private String model;
}
