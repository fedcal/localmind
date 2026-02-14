package com.localmind.domain.document.port.in;

import com.localmind.domain.document.model.Document;

import java.io.InputStream;
import java.util.List;

public interface DocumentIngestionPipelineUseCase {
    Document ingestFile(String filename, InputStream content, String mimeType,
                        long fileSize, String fileHash);
    List<Document> ingestFromFolder(String folderId);
}
