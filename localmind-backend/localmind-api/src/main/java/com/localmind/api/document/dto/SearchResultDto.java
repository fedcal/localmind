package com.localmind.api.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDto {
    private String documentId;
    private String filename;
    private String content;
    private double score;
    private int chunkIndex;
}
