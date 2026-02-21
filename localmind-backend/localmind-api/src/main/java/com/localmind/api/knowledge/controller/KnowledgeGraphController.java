package com.localmind.api.knowledge.controller;

import com.localmind.api.knowledge.dto.*;
import com.localmind.domain.knowledge.model.KnowledgeEntity;
import com.localmind.domain.knowledge.model.KnowledgeGraphStats;
import com.localmind.domain.knowledge.model.KnowledgeRelation;
import com.localmind.domain.knowledge.model.KnowledgeSubgraph;
import com.localmind.domain.knowledge.port.in.KnowledgeGraphUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/knowledge")
@Tag(name = "Knowledge Graph", description = "Knowledge Graph entity and relation management")
public class KnowledgeGraphController {

    private final KnowledgeGraphUseCase knowledgeGraphUseCase;

    public KnowledgeGraphController(Optional<KnowledgeGraphUseCase> knowledgeGraphUseCase) {
        this.knowledgeGraphUseCase = knowledgeGraphUseCase.orElse(null);
    }

    @GetMapping("/entities")
    @Operation(summary = "Cerca entita nel grafo / Search entities in graph")
    @ApiResponse(responseCode = "200", description = "Risultati ricerca / Search results")
    public ResponseEntity<List<KnowledgeEntityDto>> searchEntities(
            @RequestParam String query) {
        if (knowledgeGraphUseCase == null) {
            return ResponseEntity.ok(List.of());
        }
        List<KnowledgeEntityDto> results = knowledgeGraphUseCase.searchEntities(query).stream()
                .map(this::toEntityDto)
                .toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/entities/{id}/subgraph")
    @Operation(summary = "Ottieni sottografo di un'entita / Get entity subgraph")
    @ApiResponse(responseCode = "200", description = "Sottografo / Subgraph data")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<KnowledgeSubgraphDto> getSubgraph(
            @PathVariable String id,
            @RequestParam(defaultValue = "2") int depth) {
        if (knowledgeGraphUseCase == null) {
            return ResponseEntity.ok(new KnowledgeSubgraphDto(List.of(), List.of()));
        }
        KnowledgeSubgraph subgraph = knowledgeGraphUseCase.getEntitySubgraph(id, depth);
        return ResponseEntity.ok(toSubgraphDto(subgraph));
    }

    @PostMapping("/index")
    @Operation(summary = "Indicizza testo nel grafo / Index text in graph")
    @ApiResponse(responseCode = "200", description = "Testo indicizzato / Text indexed")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<Void> indexText(@Valid @RequestBody IndexTextRequest request) {
        if (knowledgeGraphUseCase == null) {
            return ResponseEntity.ok().build();
        }
        knowledgeGraphUseCase.indexText(request.text(), request.sourceDocumentId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Statistiche grafo della conoscenza / Knowledge graph statistics")
    @ApiResponse(responseCode = "200", description = "Statistiche / Statistics")
    public ResponseEntity<KnowledgeGraphStatsDto> getStats() {
        if (knowledgeGraphUseCase == null) {
            return ResponseEntity.ok(new KnowledgeGraphStatsDto(0, 0, Map.of()));
        }
        KnowledgeGraphStats stats = knowledgeGraphUseCase.getStats();
        Map<String, Long> entitiesByType = stats.getEntitiesByType().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
        return ResponseEntity.ok(new KnowledgeGraphStatsDto(
                stats.getTotalEntities(),
                stats.getTotalRelations(),
                entitiesByType
        ));
    }

    @DeleteMapping("/entities/{id}")
    @Operation(summary = "Elimina entita dal grafo / Delete entity from graph")
    @ApiResponse(responseCode = "200", description = "Entita eliminata / Entity deleted")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Void> deleteEntity(@PathVariable String id) {
        if (knowledgeGraphUseCase == null) {
            return ResponseEntity.ok().build();
        }
        knowledgeGraphUseCase.deleteEntity(id);
        return ResponseEntity.ok().build();
    }

    private KnowledgeEntityDto toEntityDto(KnowledgeEntity entity) {
        return new KnowledgeEntityDto(
                entity.getId(),
                entity.getName(),
                entity.getType().name(),
                entity.getProperties()
        );
    }

    private KnowledgeRelationDto toRelationDto(KnowledgeRelation relation) {
        return new KnowledgeRelationDto(
                relation.getId(),
                relation.getFromEntityId(),
                relation.getToEntityId(),
                relation.getType().name(),
                relation.getProperties()
        );
    }

    private KnowledgeSubgraphDto toSubgraphDto(KnowledgeSubgraph subgraph) {
        List<KnowledgeEntityDto> entities = subgraph.getEntities().stream()
                .map(this::toEntityDto)
                .toList();
        List<KnowledgeRelationDto> relations = subgraph.getRelations().stream()
                .map(this::toRelationDto)
                .toList();
        return new KnowledgeSubgraphDto(entities, relations);
    }
}
