package com.localmind.domain.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmModel {
    private String id;
    private String name;
    private LlmProvider provider;
    private int contextWindow;
    private double costPerInputToken;
    private double costPerOutputToken;
    private boolean available;
}
