package com.localmind.sdk;

/**
 * A semantic search result from LocalMind's document search.
 */
public class SearchResult {

    private String documentId;
    private String filename;
    private String content;
    private double score;
    private int chunkIndex;

    public SearchResult() {
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    @Override
    public String toString() {
        return "SearchResult{documentId='" + documentId + "'"
                + ", filename='" + filename + "'"
                + ", score=" + score + "}";
    }
}
