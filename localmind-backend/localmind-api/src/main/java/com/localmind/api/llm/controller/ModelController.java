package com.localmind.api.llm.controller;

import com.localmind.api.llm.dto.ModelDto;
import com.localmind.domain.llm.model.LlmModel;
import com.localmind.domain.llm.port.in.ModelManagementUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/models")
public class ModelController {

    private final ModelManagementUseCase modelManagementUseCase;

    public ModelController(ModelManagementUseCase modelManagementUseCase) {
        this.modelManagementUseCase = modelManagementUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ModelDto>> listModels() {
        List<ModelDto> models = modelManagementUseCase.listModels().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(models);
    }

    @GetMapping("/{id}")
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
