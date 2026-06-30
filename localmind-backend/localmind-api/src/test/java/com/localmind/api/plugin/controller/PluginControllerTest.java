package com.localmind.api.plugin.controller;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.plugin.model.PluginInfo;
import com.localmind.domain.plugin.model.PluginState;
import com.localmind.domain.plugin.service.PluginManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;
import java.util.List;

class PluginControllerTest {

    private MockMvc mockMvc;
    private PluginManagementService pluginManagementService;

    @BeforeEach
    void setUp() {
        pluginManagementService = mock(PluginManagementService.class);
        PluginController controller = new PluginController(pluginManagementService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listPlugins_shouldReturnAll() throws Exception {
        PluginInfo p1 = PluginInfo.builder()
                .id("p1").name("Plugin A").version("1.0.0")
                .description("First plugin").author("Author A").license("MIT")
                .state(PluginState.STARTED).build();
        PluginInfo p2 = PluginInfo.builder()
                .id("p2").name("Plugin B").version("2.0.0")
                .description("Second plugin").author("Author B").license("Apache-2.0")
                .state(PluginState.STOPPED).build();

        when(pluginManagementService.listPlugins()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/v1/plugins")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("p1"))
                .andExpect(jsonPath("$[0].name").value("Plugin A"))
                .andExpect(jsonPath("$[0].version").value("1.0.0"))
                .andExpect(jsonPath("$[0].description").value("First plugin"))
                .andExpect(jsonPath("$[0].author").value("Author A"))
                .andExpect(jsonPath("$[0].license").value("MIT"))
                .andExpect(jsonPath("$[0].state").value("STARTED"))
                .andExpect(jsonPath("$[1].id").value("p2"))
                .andExpect(jsonPath("$[1].name").value("Plugin B"))
                .andExpect(jsonPath("$[1].state").value("STOPPED"));

        verify(pluginManagementService).listPlugins();
    }

    @Test
    void listPlugins_shouldReturnEmptyList() throws Exception {
        when(pluginManagementService.listPlugins()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/plugins")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getPlugin_shouldReturnPlugin() throws Exception {
        PluginInfo plugin = PluginInfo.builder()
                .id("p1").name("Plugin A").version("1.0.0")
                .description("A test plugin").author("Author").license("MIT")
                .state(PluginState.STARTED).build();

        when(pluginManagementService.getPlugin("p1")).thenReturn(plugin);

        mockMvc.perform(get("/api/v1/plugins/p1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p1"))
                .andExpect(jsonPath("$.name").value("Plugin A"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.description").value("A test plugin"))
                .andExpect(jsonPath("$.author").value("Author"))
                .andExpect(jsonPath("$.license").value("MIT"))
                .andExpect(jsonPath("$.state").value("STARTED"));

        verify(pluginManagementService).getPlugin("p1");
    }

    @Test
    void getPlugin_shouldReturn404WhenNotFound() throws Exception {
        when(pluginManagementService.getPlugin("nonexistent"))
                .thenThrow(new ResourceNotFoundException("Plugin not found: nonexistent"));

        mockMvc.perform(get("/api/v1/plugins/nonexistent")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Plugin not found: nonexistent"));
    }

    @Test
    void enablePlugin_shouldReturnEnabledPlugin() throws Exception {
        PluginInfo enabled = PluginInfo.builder()
                .id("p1").name("Plugin A").version("1.0.0")
                .description("A test plugin").author("Author").license("MIT")
                .state(PluginState.STARTED).build();

        when(pluginManagementService.enablePlugin("p1")).thenReturn(enabled);

        mockMvc.perform(post("/api/v1/plugins/p1/enable")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p1"))
                .andExpect(jsonPath("$.state").value("STARTED"));

        verify(pluginManagementService).enablePlugin("p1");
    }

    @Test
    void enablePlugin_shouldReturn404WhenNotFound() throws Exception {
        when(pluginManagementService.enablePlugin("missing"))
                .thenThrow(new ResourceNotFoundException("Plugin not found: missing"));

        mockMvc.perform(post("/api/v1/plugins/missing/enable")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Plugin not found: missing"));
    }

    @Test
    void disablePlugin_shouldReturnDisabledPlugin() throws Exception {
        PluginInfo disabled = PluginInfo.builder()
                .id("p1").name("Plugin A").version("1.0.0")
                .description("A test plugin").author("Author").license("MIT")
                .state(PluginState.DISABLED).build();

        when(pluginManagementService.disablePlugin("p1")).thenReturn(disabled);

        mockMvc.perform(post("/api/v1/plugins/p1/disable")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p1"))
                .andExpect(jsonPath("$.state").value("DISABLED"));

        verify(pluginManagementService).disablePlugin("p1");
    }

    @Test
    void disablePlugin_shouldReturn404WhenNotFound() throws Exception {
        when(pluginManagementService.disablePlugin("missing"))
                .thenThrow(new ResourceNotFoundException("Plugin not found: missing"));

        mockMvc.perform(post("/api/v1/plugins/missing/disable")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Plugin not found: missing"));
    }

    @Test
    void deletePlugin_shouldReturn204() throws Exception {
        doNothing().when(pluginManagementService).unloadPlugin("p1");

        mockMvc.perform(delete("/api/v1/plugins/p1"))
                .andExpect(status().isNoContent());

        verify(pluginManagementService).unloadPlugin("p1");
    }

    @Test
    void deletePlugin_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Plugin not found: missing"))
                .when(pluginManagementService).unloadPlugin("missing");

        mockMvc.perform(delete("/api/v1/plugins/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Plugin not found: missing"));
    }

    @Test
    void uploadPlugin_shouldReturnLoadedPlugin() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "my-plugin.jar",
                "application/java-archive",
                "JAR content bytes".getBytes()
        );

        PluginInfo loaded = PluginInfo.builder()
                .id("p-new").name("New Plugin").version("1.0.0")
                .description("Uploaded plugin").author("Author").license("MIT")
                .state(PluginState.RESOLVED).build();

        when(pluginManagementService.uploadPlugin(eq("my-plugin.jar"), any(InputStream.class)))
                .thenReturn(loaded);

        mockMvc.perform(multipart("/api/v1/plugins/upload").file(mockFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p-new"))
                .andExpect(jsonPath("$.name").value("New Plugin"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.description").value("Uploaded plugin"))
                .andExpect(jsonPath("$.author").value("Author"))
                .andExpect(jsonPath("$.license").value("MIT"))
                .andExpect(jsonPath("$.state").value("RESOLVED"));

        verify(pluginManagementService).uploadPlugin(eq("my-plugin.jar"), any(InputStream.class));
    }
}
