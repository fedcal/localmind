package com.localmind.api.settings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OllamaStatusDto {
    private boolean online;
    private String version;
    private List<OllamaModelDto> models;
    private String errorMessage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OllamaModelDto {
        private String name;
        private String size;
        private String modifiedAt;
    }
}
