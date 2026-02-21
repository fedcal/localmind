package com.localmind.domain.plugin.port.out;

import com.localmind.domain.plugin.model.PluginInfo;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface PluginRegistryPort {

    List<PluginInfo> listPlugins();

    Optional<PluginInfo> getPlugin(String pluginId);

    PluginInfo enablePlugin(String pluginId);

    PluginInfo disablePlugin(String pluginId);

    void unloadPlugin(String pluginId);

    PluginInfo loadPlugin(String filename, InputStream jarStream);
}
