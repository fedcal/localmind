package com.localmind.sdk;

/**
 * A RAG (Retrieval-Augmented Generation) source document chunk
 * returned alongside a chat response when RAG is enabled.
 */
public class RagSource {

    private String documentId;
    private String filename;
    private int chunkIndex;
    private double score;
    private String contentPreview;

    public RagSource() {
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getContentPreview() {
        return contentPreview;
    }

    public void setContentPreview(String contentPreview) {
        this.contentPreview = contentPreview;
    }

    @Override
    public String toString() {
        return "RagSource{documentId='" + documentId + "'"
                + ", filename='" + filename + "'"
                + ", score=" + score + "}";
    }
}
