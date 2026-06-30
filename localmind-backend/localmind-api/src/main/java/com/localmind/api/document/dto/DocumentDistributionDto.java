package com.localmind.api.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO per la distribuzione dei documenti per tipo e stato (CQRS).
 * DTO for document distribution by type and status (CQRS).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDistributionDto {
    private Map<String, Long> typeDistribution;
    private Map<String, Long> statusDistribution;
}
