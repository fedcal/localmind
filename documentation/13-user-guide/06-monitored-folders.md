# User Guide - Monitored Folders

## Access

Navigate to **Folders** from the sidebar or visit `http://localhost:4200/folders`.

## Overview

The Monitored Folders section allows you to configure local filesystem folders that will be automatically scanned to find new documents to index. The system checks configured folders every 15 minutes and automatically indexes new files found.

## Adding a Folder

1. Click the **+ Add Folder** button in the header
2. Fill out the form that appears:

### Form Fields

| Field | Required | Description | Example |
|-------|----------|-------------|---------|
| **Folder path** | Yes | Absolute path of the folder to monitor | `/home/user/documents` |
| **File pattern** | No | Glob pattern to filter file types | `*.pdf,*.md,*.txt` |
| **Include subfolders** | No | Checkbox for recursive scanning | Checked = also scans subfolders |

3. Click **Add** to save the configuration

> **Note**: the Add button is disabled until the path field is filled in.

## List of Configured Folders

Each configured folder is displayed as a card containing:

### Displayed Information

- **Folder icon** and full path (monospace font)
- **Status badge** with color coding:
  - **ACTIVE** (green): folder is active and regularly monitored
  - **SYNCING** (blue): synchronization in progress
  - **ERROR** (red): scanning error (e.g., invalid path)
  - **PAUSED** (gray): monitoring paused
- **Number of documents** found in the folder
- **Pattern** applied (if configured, in monospace font)
- **Scan mode**: "Recursive" or "Root only"
- **Last synchronization**: date and time of the last completed scan

### Available Actions

| Action | Icon | Description |
|--------|------|-------------|
| **Sync** | Circular arrow | Starts an immediate scan of the folder (disabled during syncing) |
| **Delete** | Trash (red) | Removes the folder configuration |

## Automatic Operation

Configured folders are automatically scanned by the backend every **15 minutes** (configurable via `localmind.batch.cron-folder-scan` in the configuration file). New files matching the configured patterns are:

1. Detected during the scan
2. Added to the processing queue
3. Processed (text extraction, chunking, embedding)
4. Indexed in the vector store for semantic search

## Empty States

- **Loading**: spinner with text "Loading..."
- **No folders**: card with message "No folders configured" and a suggestion to add one
