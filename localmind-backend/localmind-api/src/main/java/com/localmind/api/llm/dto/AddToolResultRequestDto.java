package com.localmind.api.llm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToolResultRequestDto {
    @NotBlank
    private String toolName;
    @NotBlank
    private String content;
    private Map<String, Object> metadata;
}
