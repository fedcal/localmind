package com.localmind.domain.common.event;

/**
 * Evento emesso quando un documento viene eliminato.
 * Event emitted when a document is deleted.
 */
public class DocumentDeletedEvent extends DomainEvent {

    private final String documentId;
    private final String documentTitle;

    public DocumentDeletedEvent(String documentId, String documentTitle) {
        super("DOCUMENT_DELETED");
        this.documentId = documentId;
        this.documentTitle = documentTitle;
    }

    public String getDocumentId() { return documentId; }
    public String getDocumentTitle() { return documentTitle; }
}
