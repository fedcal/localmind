package com.localmind.infrastructure.plugin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Proprieta' di configurazione per il sistema di plugin.
 * Configuration properties for the plugin system.
 */
@ConfigurationProperties(prefix = "localmind.plugins")
public class PluginProperties {

    private boolean enabled = true;
    private String directory = "./plugins";
    private boolean autoLoad = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public boolean isAutoLoad() {
        return autoLoad;
    }

    public void setAutoLoad(boolean autoLoad) {
        this.autoLoad = autoLoad;
    }
}
