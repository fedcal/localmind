package com.localmind.sdk;

/**
 * A document stored in LocalMind.
 */
public class Document {

    private String id;
    private String filename;
    private String filePath;
    private String mimeType;
    private long fileSize;
    private String status;
    private String createdAt;
    private String indexedAt;
    private Double ocrConfidence;

    public Document() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getIndexedAt() {
        return indexedAt;
    }

    public void setIndexedAt(String indexedAt) {
        this.indexedAt = indexedAt;
    }

    public Double getOcrConfidence() {
        return ocrConfidence;
    }

    public void setOcrConfidence(Double ocrConfidence) {
        this.ocrConfidence = ocrConfidence;
    }

    @Override
    public String toString() {
        return "Document{id='" + id + "'"
                + ", filename='" + filename + "'"
                + ", status='" + status + "'"
                + ", fileSize=" + fileSize + "}";
    }
}
