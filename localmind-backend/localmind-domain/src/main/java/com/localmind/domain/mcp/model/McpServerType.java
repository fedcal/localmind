package com.localmind.domain.mcp.model;

public enum McpServerType {
    STDIO("Standard Input/Output"),
    SSE("Server-Sent Events");

    private final String displayName;

    McpServerType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
