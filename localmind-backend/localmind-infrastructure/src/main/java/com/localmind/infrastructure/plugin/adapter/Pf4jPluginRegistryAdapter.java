package com.localmind.infrastructure.plugin.adapter;

import com.localmind.domain.plugin.model.PluginInfo;
import com.localmind.domain.plugin.model.PluginState;
import com.localmind.domain.plugin.port.out.PluginRegistryPort;
import com.localmind.infrastructure.plugin.config.PluginProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

/**
 * Adapter infrastrutturale che implementa PluginRegistryPort utilizzando PF4J.
 * Gestisce il caricamento, l'avvio e lo scaricamento dei plugin.
 *
 * Infrastructure adapter implementing PluginRegistryPort using PF4J.
 * Manages plugin loading, starting and unloading.
 */
@Component
@ConditionalOnProperty(name = "localmind.plugins.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(PluginProperties.class)
public class Pf4jPluginRegistryAdapter implements PluginRegistryPort {

    private static final Logger log = LoggerFactory.getLogger(Pf4jPluginRegistryAdapter.class);

    private final PluginProperties pluginProperties;
    private PluginManager pluginManager;

    public Pf4jPluginRegistryAdapter(PluginProperties pluginProperties) {
        this.pluginProperties = pluginProperties;
    }

    @PostConstruct
    public void init() {
        Path pluginsDir = Paths.get(pluginProperties.getDirectory()).toAbsolutePath();
        try {
            Files.createDirectories(pluginsDir);
        } catch (Exception e) {
            log.warn("Impossibile creare la directory dei plugin: {}", pluginsDir, e);
        }

        pluginManager = new DefaultPluginManager(pluginsDir);

        if (pluginProperties.isAutoLoad()) {
            pluginManager.loadPlugins();
            pluginManager.startPlugins();
            log.info("Plugin caricati e avviati dalla directory: {}", pluginsDir);
        } else {
            log.info("Auto-load dei plugin disabilitato. Directory: {}", pluginsDir);
        }
    }

    @PreDestroy
    public void destroy() {
        if (pluginManager != null) {
            pluginManager.stopPlugins();
            log.info("Tutti i plugin sono stati fermati");
        }
    }

    @Override
    public List<PluginInfo> listPlugins() {
        return pluginManager.getPlugins().stream()
                .map(this::toPluginInfo)
                .toList();
    }

    @Override
    public Optional<PluginInfo> getPlugin(String pluginId) {
        PluginWrapper wrapper = pluginManager.getPlugin(pluginId);
        if (wrapper == null) {
            return Optional.empty();
        }
        return Optional.of(toPluginInfo(wrapper));
    }

    @Override
    public PluginInfo enablePlugin(String pluginId) {
        pluginManager.enablePlugin(pluginId);
        pluginManager.startPlugin(pluginId);
        log.info("Plugin abilitato e avviato: {}", pluginId);
        return toPluginInfo(pluginManager.getPlugin(pluginId));
    }

    @Override
    public PluginInfo disablePlugin(String pluginId) {
        pluginManager.stopPlugin(pluginId);
        pluginManager.disablePlugin(pluginId);
        log.info("Plugin fermato e disabilitato: {}", pluginId);
        return toPluginInfo(pluginManager.getPlugin(pluginId));
    }

    @Override
    public void unloadPlugin(String pluginId) {
        pluginManager.stopPlugin(pluginId);
        pluginManager.unloadPlugin(pluginId);
        log.info("Plugin scaricato: {}", pluginId);
    }

    @Override
    public PluginInfo loadPlugin(String filename, InputStream jarStream) {
        Path pluginsDir = Paths.get(pluginProperties.getDirectory()).toAbsolutePath();
        Path targetPath = pluginsDir.resolve(filename);

        try {
            Files.createDirectories(pluginsDir);
            Files.copy(jarStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile salvare il file JAR del plugin: " + filename, e);
        }

        String pluginId = pluginManager.loadPlugin(targetPath);
        if (pluginId == null) {
            throw new IllegalStateException("Impossibile caricare il plugin da: " + targetPath);
        }
        pluginManager.startPlugin(pluginId);
        log.info("Plugin caricato e avviato: {} da {}", pluginId, targetPath);

        return toPluginInfo(pluginManager.getPlugin(pluginId));
    }

    private PluginInfo toPluginInfo(PluginWrapper wrapper) {
        PluginDescriptor descriptor = wrapper.getDescriptor();
        return PluginInfo.builder()
                .id(wrapper.getPluginId())
                .name(descriptor.getPluginId())
                .version(descriptor.getVersion())
                .description(descriptor.getPluginDescription())
                .author(descriptor.getProvider())
                .license(descriptor.getLicense())
                .state(mapState(wrapper.getPluginState()))
                .build();
    }

    private PluginState mapState(org.pf4j.PluginState pf4jState) {
        return switch (pf4jState) {
            case CREATED -> PluginState.CREATED;
            case DISABLED -> PluginState.DISABLED;
            case RESOLVED -> PluginState.RESOLVED;
            case STARTED -> PluginState.STARTED;
            case STOPPED -> PluginState.STOPPED;
            case FAILED -> PluginState.FAILED;
            case UNLOADED -> PluginState.UNLOADED;
        };
    }
}
