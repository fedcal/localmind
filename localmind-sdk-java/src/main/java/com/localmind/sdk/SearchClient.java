package com.localmind.sdk;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

/**
 * Client for semantic document search.
 *
 * <pre>{@code
 * SearchClient search = client.search();
 * List<SearchResult> results = search.query("deployment procedure", 10);
 * }</pre>
 */
public class SearchClient {

    private final LocalMindClient client;

    SearchClient(LocalMindClient client) {
        this.client = client;
    }

    /**
     * Search documents semantically with default topK of 5.
     *
     * @param query natural language search query
     * @return list of matching document chunks ranked by relevance
     */
    public List<SearchResult> query(String query) {
        return query(query, 5);
    }

    /**
     * Search documents semantically.
     *
     * @param query natural language search query
     * @param topK  maximum number of results to return
     * @return list of matching document chunks ranked by relevance
     */
    public List<SearchResult> query(String query, int topK) {
        Map<String, Object> body = Map.of("query", query, "topK", topK);
        String json = client.post("/documents/search", body);
        return client.parseJson(json, new TypeReference<>() {});
    }
}
