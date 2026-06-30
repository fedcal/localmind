package com.localmind.domain.plugin.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.plugin.model.PluginInfo;
import com.localmind.domain.plugin.model.PluginState;
import com.localmind.domain.plugin.port.out.PluginRegistryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PluginManagementServiceTest {

    @Mock
    private PluginRegistryPort pluginRegistryPort;

    @InjectMocks
    private PluginManagementService pluginManagementService;

    @Test
    void listPlugins_shouldReturnAllPlugins() {
        List<PluginInfo> plugins = List.of(
                PluginInfo.builder().id("p1").name("Plugin A").version("1.0.0").state(PluginState.STARTED).build(),
                PluginInfo.builder().id("p2").name("Plugin B").version("2.0.0").state(PluginState.STOPPED).build()
        );
        when(pluginRegistryPort.listPlugins()).thenReturn(plugins);

        List<PluginInfo> result = pluginManagementService.listPlugins();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Plugin A");
        assertThat(result.get(1).getName()).isEqualTo("Plugin B");
        verify(pluginRegistryPort).listPlugins();
    }

    @Test
    void listPlugins_shouldReturnEmptyList() {
        when(pluginRegistryPort.listPlugins()).thenReturn(List.of());

        List<PluginInfo> result = pluginManagementService.listPlugins();

        assertThat(result).isEmpty();
        verify(pluginRegistryPort).listPlugins();
    }

    @Test
    void getPlugin_shouldReturnPlugin() {
        PluginInfo plugin = PluginInfo.builder()
                .id("p1").name("Plugin A").version("1.0.0")
                .description("A test plugin").author("Author").license("MIT")
                .state(PluginState.STARTED).build();
        when(pluginRegistryPort.getPlugin("p1")).thenReturn(Optional.of(plugin));

        PluginInfo result = pluginManagementService.getPlugin("p1");

        assertThat(result).isEqualTo(plugin);
        assertThat(result.getName()).isEqualTo("Plugin A");
        assertThat(result.getVersion()).isEqualTo("1.0.0");
        verify(pluginRegistryPort).getPlugin("p1");
    }

    @Test
    void getPlugin_shouldThrowWhenNotFound() {
        when(pluginRegistryPort.getPlugin("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pluginManagementService.getPlugin("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    @Test
    void enablePlugin_shouldDelegateToPort() {
        PluginInfo plugin = PluginInfo.builder()
                .id("p1").name("Plugin A").version("1.0.0").state(PluginState.STOPPED).build();
        PluginInfo enabled = PluginInfo.builder()
                .id("p1").name("Plugin A").version("1.0.0").state(PluginState.STARTED).build();
        when(pluginRegistryPort.getPlugin("p1")).thenReturn(Optional.of(plugin));
        when(pluginRegistryPort.enablePlugin("p1")).thenReturn(enabled);

        PluginInfo result = pluginManagementService.enablePlugin("p1");

        assertThat(result.getState()).isEqualTo(PluginState.STARTED);
        verify(pluginRegistryPort).getPlugin("p1");
        verify(pluginRegistryPort).enablePlugin("p1");
    }

    @Test
    void enablePlugin_shouldThrowWhenNotFound() {
        when(pluginRegistryPort.getPlugin("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pluginManagementService.enablePlugin("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");

        verify(pluginRegistryPort, never()).enablePlugin(anyString());
    }

    @Test
    void disablePlugin_shouldDelegateToPort() {
        PluginInfo plugin = PluginInfo.builder()
                .id("p1").name("Plugin A").version("1.0.0").state(PluginState.STARTED).build();
        PluginInfo disabled = PluginInfo.builder()
                .id("p1").name("Plugin A").version("1.0.0").state(PluginState.DISABLED).build();
        when(pluginRegistryPort.getPlugin("p1")).thenReturn(Optional.of(plugin));
        when(pluginRegistryPort.disablePlugin("p1")).thenReturn(disabled);

        PluginInfo result = pluginManagementService.disablePlugin("p1");

        assertThat(result.getState()).isEqualTo(PluginState.DISABLED);
        verify(pluginRegistryPort).getPlugin("p1");
        verify(pluginRegistryPort).disablePlugin("p1");
    }

    @Test
    void disablePlugin_shouldThrowWhenNotFound() {
        when(pluginRegistryPort.getPlugin("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pluginManagementService.disablePlugin("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");

        verify(pluginRegistryPort, never()).disablePlugin(anyString());
    }

    @Test
    void unloadPlugin_shouldDelegateToPort() {
        PluginInfo plugin = PluginInfo.builder()
                .id("p1").name("Plugin A").version("1.0.0").state(PluginState.STARTED).build();
        when(pluginRegistryPort.getPlugin("p1")).thenReturn(Optional.of(plugin));

        pluginManagementService.unloadPlugin("p1");

        verify(pluginRegistryPort).getPlugin("p1");
        verify(pluginRegistryPort).unloadPlugin("p1");
    }

    @Test
    void unloadPlugin_shouldThrowWhenNotFound() {
        when(pluginRegistryPort.getPlugin("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pluginManagementService.unloadPlugin("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");

        verify(pluginRegistryPort, never()).unloadPlugin(anyString());
    }

    @Test
    void uploadPlugin_shouldDelegateToPort() {
        InputStream stream = new ByteArrayInputStream("jar content".getBytes());
        PluginInfo loaded = PluginInfo.builder()
                .id("p-new").name("New Plugin").version("1.0.0")
                .description("New plugin desc").author("Author").license("Apache-2.0")
                .state(PluginState.RESOLVED).build();
        when(pluginRegistryPort.loadPlugin("my-plugin.jar", stream)).thenReturn(loaded);

        PluginInfo result = pluginManagementService.uploadPlugin("my-plugin.jar", stream);

        assertThat(result.getId()).isEqualTo("p-new");
        assertThat(result.getName()).isEqualTo("New Plugin");
        assertThat(result.getState()).isEqualTo(PluginState.RESOLVED);
        verify(pluginRegistryPort).loadPlugin("my-plugin.jar", stream);
    }
}
