package com.localmind.sdk;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

/**
 * Client for document management operations.
 *
 * <pre>{@code
 * DocumentClient docs = client.documents();
 * List<Document> all = docs.list();
 * Document uploaded = docs.upload(new File("report.pdf"));
 * docs.delete(uploaded.getId());
 * }</pre>
 */
public class DocumentClient {

    private final LocalMindClient client;

    DocumentClient(LocalMindClient client) {
        this.client = client;
    }

    /**
     * List all documents.
     */
    public List<Document> list() {
        String json = client.get("/documents");
        return client.parseJson(json, new TypeReference<>() {});
    }

    /**
     * Get a document by ID.
     */
    public Document get(String id) {
        String json = client.get("/documents/" + id);
        return client.parseJson(json, Document.class);
    }

    /**
     * Upload a file to LocalMind for indexing.
     *
     * @param file the file to upload
     * @return the created document metadata
     */
    public Document upload(File file) {
        try {
            String boundary = UUID.randomUUID().toString();
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            byte[] multipartBody = buildMultipartBody(boundary, file.getName(), mimeType, fileBytes);

            HttpRequest request = client.requestBuilder("/documents/upload")
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                    .build();

            String json = client.executeRequest(request);
            return client.parseJson(json, Document.class);
        } catch (IOException e) {
            throw new LocalMindException("Failed to read file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Delete a document by ID.
     */
    public void delete(String id) {
        client.delete("/documents/" + id);
    }

    private byte[] buildMultipartBody(String boundary, String filename, String mimeType, byte[] fileBytes) {
        String header = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n"
                + "Content-Type: " + mimeType + "\r\n\r\n";
        String footer = "\r\n--" + boundary + "--\r\n";

        byte[] headerBytes = header.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] footerBytes = footer.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        byte[] body = new byte[headerBytes.length + fileBytes.length + footerBytes.length];
        System.arraycopy(headerBytes, 0, body, 0, headerBytes.length);
        System.arraycopy(fileBytes, 0, body, headerBytes.length, fileBytes.length);
        System.arraycopy(footerBytes, 0, body, headerBytes.length + fileBytes.length, footerBytes.length);

        return body;
    }
}
