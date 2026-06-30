package com.localmind.domain.plugin.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.plugin.model.PluginInfo;
import com.localmind.domain.plugin.port.out.PluginRegistryPort;

import java.io.InputStream;
import java.util.List;

public class PluginManagementService {

    private final PluginRegistryPort pluginRegistryPort;

    public PluginManagementService(PluginRegistryPort pluginRegistryPort) {
        this.pluginRegistryPort = pluginRegistryPort;
    }

    public List<PluginInfo> listPlugins() {
        return pluginRegistryPort.listPlugins();
    }

    public PluginInfo getPlugin(String pluginId) {
        return pluginRegistryPort.getPlugin(pluginId)
                .orElseThrow(() -> new ResourceNotFoundException("Plugin not found: " + pluginId));
    }

    public PluginInfo enablePlugin(String pluginId) {
        pluginRegistryPort.getPlugin(pluginId)
                .orElseThrow(() -> new ResourceNotFoundException("Plugin not found: " + pluginId));
        return pluginRegistryPort.enablePlugin(pluginId);
    }

    public PluginInfo disablePlugin(String pluginId) {
        pluginRegistryPort.getPlugin(pluginId)
                .orElseThrow(() -> new ResourceNotFoundException("Plugin not found: " + pluginId));
        return pluginRegistryPort.disablePlugin(pluginId);
    }

    public void unloadPlugin(String pluginId) {
        pluginRegistryPort.getPlugin(pluginId)
                .orElseThrow(() -> new ResourceNotFoundException("Plugin not found: " + pluginId));
        pluginRegistryPort.unloadPlugin(pluginId);
    }

    public PluginInfo uploadPlugin(String filename, InputStream jarStream) {
        return pluginRegistryPort.loadPlugin(filename, jarStream);
    }
}
