package com.localmind.domain.document.port.in;

import com.localmind.domain.document.model.SearchResult;

import java.util.List;

public interface DocumentSearchUseCase {
    List<SearchResult> search(String query, int topK);
}
