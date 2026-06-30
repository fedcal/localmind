package com.localmind.api.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsageDto {
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
}
