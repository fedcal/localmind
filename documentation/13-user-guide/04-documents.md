# User Guide - Documents

## Access

Navigate to **Documents** from the sidebar or visit `http://localhost:4200/documents`.

## Overview

The Documents section allows you to upload, view, and manage files that are indexed for semantic search (RAG). Uploaded documents are processed, split into chunks, and stored in the vector store for subsequent searches.

## Uploading Documents

### How to Upload

1. Click the **Upload Document** button in the page header
2. The operating system file selector opens
3. Select one or more files

### Supported Formats

| Format | Extensions |
|--------|------------|
| PDF | .pdf |
| Word Documents | .doc, .docx |
| Plain Text | .txt |
| Markdown | .md |
| CSV | .csv |
| JSON | .json |

### Upload Progress

During upload, a progress bar is shown with:
- Animated spinner icon
- Name of the file being uploaded
- The message "Upload in progress..."

### Notifications

Upon upload completion:
- **Success**: green notification "Document uploaded successfully"
- **Error**: red notification with the error message

## Status Filters

Below the header, tabs are available to filter documents by status:

| Filter | Description | Badge Color |
|--------|-------------|-------------|
| **All** | Shows all documents | - |
| **Indexed** | Documents processed and ready for search | Green |
| **Pending** | Documents uploaded and awaiting processing | Yellow |
| **Processing** | Documents currently being processed | Blue |
| **Failed** | Documents whose processing has failed | Red |

Each tab shows the document count for that status. The active tab is highlighted.

## Document List

Each document is displayed as a card containing:

- **File icon** with document symbol
- **File name** in bold
- **Status badge** color-coded (Indexed, Pending, Processing, Failed, Archived)
- **File size** in readable format (B, KB, MB)
- **MIME type** of the file (e.g., application/pdf)
- **Upload date** with time

### Document Deletion

Each card has a **trash** button (red icon) to delete the document. Clicking it:

1. A confirmation dialog appears with the document name
2. The message asks: "Are you sure you want to delete [file name]? This action cannot be undone."
3. Two buttons available:
   - **Cancel**: closes the dialog without deleting
   - **Delete**: proceeds with permanent deletion

## List Refresh

The **Refresh** button (circular arrow icon) in the header reloads the document list from the backend.

## Empty States

- **Loading**: spinner with text "Loading documents..."
- **No documents**: centered card with icon, title "No documents" and message "Upload your first document to get started."
- **No results in filter**: message "No documents with status [selected status]"
