package com.localmind.domain.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OllamaStatus {
    private boolean online;
    private String version;
    private List<OllamaModelInfo> models;
    private String errorMessage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OllamaModelInfo {
        private String name;
        private long sizeBytes;
        private String modifiedAt;
    }
}
