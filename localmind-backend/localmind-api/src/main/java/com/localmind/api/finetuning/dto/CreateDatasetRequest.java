package com.localmind.api.finetuning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDatasetRequest {

    @NotBlank
    private String name;

    @NotEmpty
    private List<String> documentIds;
}
