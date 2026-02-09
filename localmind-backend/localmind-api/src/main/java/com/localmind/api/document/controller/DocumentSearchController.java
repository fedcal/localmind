package com.localmind.api.document.controller;

import com.localmind.api.document.dto.SearchRequestDto;
import com.localmind.api.document.dto.SearchResultDto;
import com.localmind.domain.document.model.SearchResult;
import com.localmind.domain.document.port.in.DocumentSearchUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents/search")
public class DocumentSearchController {

    private final DocumentSearchUseCase searchUseCase;

    public DocumentSearchController(DocumentSearchUseCase searchUseCase) {
        this.searchUseCase = searchUseCase;
    }

    @PostMapping
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
