# User Guide - Introduction

## Interface Overview

LocalMind is a single-page web application accessible at `http://localhost:4200`. The interface is organized with a **sidebar navigation panel** on the left and a **main content area** on the right.

## Navigation Structure

The sidebar contains the following items, each corresponding to a section of the system:

| Item | Description |
|------|-------------|
| **Dashboard** | System status overview and quick access to main functions |
| **Chat** | Conversation interface with the configured LLM models |
| **Documents** | Management of uploaded documents (upload, status viewing, deletion) |
| **Search** | Semantic search in indexed documents via RAG |
| **Folders** | Configuration of local folders to monitor for automatic indexing |
| **MCP** | Management of MCP (Model Context Protocol) servers and tools |
| **Settings** | Configuration of LLM providers (Ollama, OpenAI, Anthropic, Google Gemini) |

### Collapsible Sidebar

The sidebar can be collapsed by clicking the arrow button at the top right of the panel. In collapsed mode, only icons are shown, freeing up space for the main content. The system version (v0.1.0) is visible at the bottom of the sidebar.

## First Launch

On first access, the application automatically redirects to the **Chat** page. To start using the system, it is recommended to:

1. Check the system status on the **Dashboard**
2. Configure at least one LLM provider in **Settings**
3. Upload documents in the **Documents** section or configure **Monitored Folders**
4. Use the **Chat** to interact with the AI models
5. Use **Semantic Search** to find information in indexed documents
