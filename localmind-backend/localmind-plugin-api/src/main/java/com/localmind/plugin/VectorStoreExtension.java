package com.localmind.plugin;

import org.pf4j.ExtensionPoint;

import java.util.List;
import java.util.Map;

/**
 * Extension point per vector store personalizzati.
 * Implementare questa interfaccia per aggiungere un nuovo vector store tramite plugin.
 *
 * Extension point for custom vector stores.
 * Implement this interface to add a new vector store via plugin.
 */
public interface VectorStoreExtension extends ExtensionPoint {

    /**
     * Restituisce il nome univoco del vector store.
     * Returns the unique vector store name.
     *
     * @return nome dello store / store name
     */
    String getStoreName();

    /**
     * Memorizza un embedding con i relativi metadati.
     * Stores an embedding with its metadata.
     *
     * @param id        l'identificativo univoco del vettore / the unique vector identifier
     * @param embedding il vettore di embedding / the embedding vector
     * @param metadata  i metadati associati / the associated metadata
     */
    void store(String id, float[] embedding, Map<String, Object> metadata);

    /**
     * Cerca i vettori piu' simili alla query fornita.
     * Searches for the vectors most similar to the given query.
     *
     * @param query il vettore di query / the query vector
     * @param topK  il numero massimo di risultati / the maximum number of results
     * @return lista di risultati con metadati / list of results with metadata
     */
    List<Map<String, Object>> search(float[] query, int topK);
}
