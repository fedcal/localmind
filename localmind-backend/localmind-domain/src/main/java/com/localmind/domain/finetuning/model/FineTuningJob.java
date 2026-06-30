package com.localmind.domain.finetuning.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FineTuningJob {
    private String id;
    private String baseModel;
    private TrainingTechnique technique;
    private FineTuningStatus status;
    private int progress;
    private String datasetId;
    private String outputModelName;
    private Map<String, String> hyperparameters;
    private Instant startedAt;
    private Instant completedAt;
    private String error;
    private Instant createdAt;
}
