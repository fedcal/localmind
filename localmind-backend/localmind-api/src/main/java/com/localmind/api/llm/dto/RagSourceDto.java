package com.localmind.api.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagSourceDto {
    private String documentId;
    private String filename;
    private int chunkIndex;
    private double score;
    private String contentPreview;
}
