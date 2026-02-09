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
public class ProviderConfigDto {
    private String id;
    private String name;
    private String type;
    private String baseUrl;
    private boolean enabled;
    private String defaultModel;
    private List<String> models;
}
