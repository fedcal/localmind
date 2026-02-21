package com.localmind.plugin;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * Classe base per tutti i plugin LocalMind.
 * I plugin devono estendere questa classe e registrarsi tramite il manifest PF4J.
 *
 * Base class for all LocalMind plugins.
 * Plugins must extend this class and register via the PF4J manifest.
 */
public class LocalMindPlugin extends Plugin {

    public LocalMindPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        log.info("Plugin avviato: {} v{}", getWrapper().getPluginId(), getWrapper().getDescriptor().getVersion());
    }

    @Override
    public void stop() {
        log.info("Plugin fermato: {} v{}", getWrapper().getPluginId(), getWrapper().getDescriptor().getVersion());
    }

    @Override
    public void delete() {
        log.info("Plugin eliminato: {}", getWrapper().getPluginId());
    }
}
