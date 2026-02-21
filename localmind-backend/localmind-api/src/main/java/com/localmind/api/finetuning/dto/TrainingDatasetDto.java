package com.localmind.api.finetuning.dto;

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
public class TrainingDatasetDto {

    private String id;
    private String name;
    private List<String> documentIds;
    private int sampleCount;
    private String format;
    private Instant createdAt;
}
