package com.localmind.api.plugin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginInfoDto {
    private String id;
    private String name;
    private String version;
    private String description;
    private String author;
    private String license;
    private String state;
}
