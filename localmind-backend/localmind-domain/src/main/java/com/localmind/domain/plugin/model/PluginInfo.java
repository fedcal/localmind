package com.localmind.domain.plugin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginInfo {
    private String id;
    private String name;
    private String version;
    private String description;
    private String author;
    private String license;
    @Builder.Default
    private PluginState state = PluginState.CREATED;
}
