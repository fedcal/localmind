package com.localmind.domain.common.event;

/**
 * Evento emesso quando viene generato un embedding per un chunk di documento.
 * Event emitted when an embedding is generated for a document chunk.
 */
public class EmbeddingGeneratedEvent extends DomainEvent {

    private final String documentId;
    private final String chunkId;
    private final int vectorDimension;

    public EmbeddingGeneratedEvent(String documentId, String chunkId, int vectorDimension) {
        super("EMBEDDING_GENERATED");
        this.documentId = documentId;
        this.chunkId = chunkId;
        this.vectorDimension = vectorDimension;
    }

    public String getDocumentId() { return documentId; }
    public String getChunkId() { return chunkId; }
    public int getVectorDimension() { return vectorDimension; }
}
