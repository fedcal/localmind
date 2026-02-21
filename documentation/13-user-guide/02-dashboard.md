# User Guide - Dashboard

## Access

Navigate to **Dashboard** from the sidebar or visit `http://localhost:4200/dashboard`.

## Overview

The Dashboard provides an overall view of the system status and quick links to the main functions. It is divided into two sections: **status indicators** and **quick actions**.

## Status Indicators

The upper section displays 4 informational cards arranged in a grid:

### API Status
- Shows **UP** (green) if the backend is reachable and operational
- Shows **DOWN** (red) if the backend is not responding
- Data loaded from the `/dashboard/health` endpoint

### Documents
- Total number of documents uploaded to the system
- Includes documents in all states (pending, processing, indexed, failed)

### MCP Servers
- Number of registered MCP servers
- Includes servers in any connection state

### LLM Provider
- Name of the currently configured default LLM provider (e.g., "Ollama")

## Quick Actions

The lower section displays 4 clickable cards for quickly navigating to the main functions:

| Action | Destination | Description |
|--------|-------------|-------------|
| **New Chat** | /chat | Opens the AI conversation interface |
| **Upload Document** | /documents | Go to document management to upload files |
| **RAG Search** | /search | Opens semantic search in documents |
| **MCP Management** | /mcp | Go to MCP server configuration |

## Data Refresh

Clicking the **Refresh** button in the page header reloads all indicators from the backend.
