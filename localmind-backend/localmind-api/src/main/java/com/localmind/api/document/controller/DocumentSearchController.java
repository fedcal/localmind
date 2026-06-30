package com.localmind.api.document.controller;

import com.localmind.api.document.dto.SearchRequestDto;
import com.localmind.api.document.dto.SearchResultDto;
import com.localmind.domain.document.model.SearchResult;
import com.localmind.domain.document.port.in.DocumentSearchUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents/search")
@Tag(name = "Document Search", description = "Ricerca semantica documenti / Semantic document search")
public class DocumentSearchController {

    private final DocumentSearchUseCase searchUseCase;

    public DocumentSearchController(DocumentSearchUseCase searchUseCase) {
        this.searchUseCase = searchUseCase;
    }

    @PostMapping
    @Operation(summary = "Ricerca semantica nei documenti / Semantic search in documents")
    @ApiResponse(responseCode = "200", description = "Risultati ricerca / Search results")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<List<SearchResultDto>> search(@Valid @RequestBody SearchRequestDto request) {
        List<SearchResultDto> results = searchUseCase.search(request.getQuery(), request.getTopK()).stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(results);
    }

    private SearchResultDto toDto(SearchResult result) {
        return SearchResultDto.builder()
                .documentId(result.getDocumentId())
                .filename(result.getFilename())
                .content(result.getContent())
                .score(result.getScore())
                .chunkIndex(result.getChunkIndex())
                .build();
    }
}
