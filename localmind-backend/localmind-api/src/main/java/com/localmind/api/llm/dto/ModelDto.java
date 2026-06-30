package com.localmind.api.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelDto {
    private String id;
    private String name;
    private String provider;
    private int contextWindow;
    private boolean available;
}
