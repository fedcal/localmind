package com.localmind.api.finetuning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartTrainingRequest {

    @NotBlank
    private String datasetId;

    @NotBlank
    private String baseModel;

    @NotNull
    private String technique;

    private Map<String, String> hyperparameters;
}
