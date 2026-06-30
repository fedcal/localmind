package com.localmind.domain.common.event;

/**
 * Evento emesso quando un documento viene indicizzato con successo.
 * Event emitted when a document is successfully indexed.
 */
public class DocumentIndexedEvent extends DomainEvent {

    private final String documentId;
    private final String documentTitle;
    private final int chunkCount;
    private final String sourceFolder;

    public DocumentIndexedEvent(String documentId, String documentTitle, int chunkCount, String sourceFolder) {
        super("DOCUMENT_INDEXED");
        this.documentId = documentId;
        this.documentTitle = documentTitle;
        this.chunkCount = chunkCount;
        this.sourceFolder = sourceFolder;
    }

    public String getDocumentId() { return documentId; }
    public String getDocumentTitle() { return documentTitle; }
    public int getChunkCount() { return chunkCount; }
    public String getSourceFolder() { return sourceFolder; }
}
