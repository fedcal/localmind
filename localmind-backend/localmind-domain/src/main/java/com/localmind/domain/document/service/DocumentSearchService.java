package com.localmind.domain.document.service;

import com.localmind.domain.document.model.SearchResult;
import com.localmind.domain.document.port.in.DocumentSearchUseCase;
import com.localmind.domain.document.port.out.VectorStorePort;

import java.util.List;

public class DocumentSearchService implements DocumentSearchUseCase {

    private final VectorStorePort vectorStorePort;

    public DocumentSearchService(VectorStorePort vectorStorePort) {
        this.vectorStorePort = vectorStorePort;
    }

    @Override
    public List<SearchResult> search(String query, int topK) {
        return vectorStorePort.search(query, topK);
    }
}
