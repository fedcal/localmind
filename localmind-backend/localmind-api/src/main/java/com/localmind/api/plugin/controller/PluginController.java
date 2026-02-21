package com.localmind.api.plugin.controller;

import com.localmind.api.plugin.dto.PluginInfoDto;
import com.localmind.domain.plugin.model.PluginInfo;
import com.localmind.domain.plugin.service.PluginManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/plugins")
@Tag(name = "Plugins", description = "Plugin management")
public class PluginController {

    private final PluginManagementService pluginManagementService;

    public PluginController(PluginManagementService pluginManagementService) {
        this.pluginManagementService = pluginManagementService;
    }

    @GetMapping
    @Operation(summary = "Lista plugin / List plugins")
    @ApiResponse(responseCode = "200", description = "Lista plugin / Plugin list")
    public ResponseEntity<List<PluginInfoDto>> listPlugins() {
        List<PluginInfoDto> plugins = pluginManagementService.listPlugins().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(plugins);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio plugin / Get plugin detail")
    @ApiResponse(responseCode = "200", description = "Plugin trovato / Plugin found")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<PluginInfoDto> getPlugin(@PathVariable String id) {
        PluginInfo plugin = pluginManagementService.getPlugin(id);
        return ResponseEntity.ok(toDto(plugin));
    }

    @PostMapping("/{id}/enable")
    @Operation(summary = "Abilita plugin / Enable plugin")
    @ApiResponse(responseCode = "200", description = "Plugin abilitato / Plugin enabled")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<PluginInfoDto> enablePlugin(@PathVariable String id) {
        PluginInfo plugin = pluginManagementService.enablePlugin(id);
        return ResponseEntity.ok(toDto(plugin));
    }

    @PostMapping("/{id}/disable")
    @Operation(summary = "Disabilita plugin / Disable plugin")
    @ApiResponse(responseCode = "200", description = "Plugin disabilitato / Plugin disabled")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<PluginInfoDto> disablePlugin(@PathVariable String id) {
        PluginInfo plugin = pluginManagementService.disablePlugin(id);
        return ResponseEntity.ok(toDto(plugin));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina plugin / Delete plugin")
    @ApiResponse(responseCode = "204", description = "Plugin eliminato / Plugin deleted")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Void> deletePlugin(@PathVariable String id) {
        pluginManagementService.unloadPlugin(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload")
    @Operation(summary = "Carica plugin / Upload plugin")
    @ApiResponse(responseCode = "200", description = "Plugin caricato / Plugin uploaded")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<PluginInfoDto> uploadPlugin(@RequestParam("file") MultipartFile file) throws IOException {
        PluginInfo plugin = pluginManagementService.uploadPlugin(
                file.getOriginalFilename(),
                file.getInputStream()
        );
        return ResponseEntity.ok(toDto(plugin));
    }

    private PluginInfoDto toDto(PluginInfo plugin) {
        return PluginInfoDto.builder()
                .id(plugin.getId())
                .name(plugin.getName())
                .version(plugin.getVersion())
                .description(plugin.getDescription())
                .author(plugin.getAuthor())
                .license(plugin.getLicense())
                .state(plugin.getState().name())
                .build();
    }
}
