package com.localmind.domain.document.port.in;

import com.localmind.domain.document.model.Document;

import java.io.InputStream;
import java.util.List;

public interface DocumentIngestionUseCase {
    Document ingest(String filename, InputStream content, String mimeType);
    List<Document> ingestFromFolder(String folderId);
}
