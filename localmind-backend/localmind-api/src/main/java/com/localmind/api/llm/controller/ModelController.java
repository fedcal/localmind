package com.localmind.api.llm.controller;

import com.localmind.api.llm.dto.ModelDto;
import com.localmind.domain.llm.model.LlmModel;
import com.localmind.domain.llm.port.in.ModelManagementUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/models")
@Tag(name = "Models", description = "Modelli LLM disponibili / Available LLM models")
public class ModelController {

    private final ModelManagementUseCase modelManagementUseCase;

    public ModelController(ModelManagementUseCase modelManagementUseCase) {
        this.modelManagementUseCase = modelManagementUseCase;
    }

    @GetMapping
    @Operation(summary = "Lista modelli disponibili / List available models")
    @ApiResponse(responseCode = "200", description = "Lista modelli / Model list")
    public ResponseEntity<List<ModelDto>> listModels() {
        List<ModelDto> models = modelManagementUseCase.listModels().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(models);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio modello / Get model detail")
    @ApiResponse(responseCode = "200", description = "Modello trovato / Model found")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<ModelDto> getModel(@PathVariable String id) {
        LlmModel model = modelManagementUseCase.getModel(id);
        return ResponseEntity.ok(toDto(model));
    }

    private ModelDto toDto(LlmModel model) {
        return ModelDto.builder()
                .id(model.getId())
                .name(model.getName())
                .provider(model.getProvider().name())
                .contextWindow(model.getContextWindow())
                .available(model.isAvailable())
                .build();
    }
}
