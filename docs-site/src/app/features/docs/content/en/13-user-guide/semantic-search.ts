export const content = `# User Guide - Semantic Search

## Access

Navigate to **Search** from the sidebar or visit \`http://localhost:4200/search\`.

## Overview

Semantic Search allows you to search for information in indexed documents using natural language. Unlike a traditional text search, this function understands the **meaning** of the query and returns the most relevant passages even if they do not contain the exact words searched for.

> **Prerequisite**: to get results, you need to have uploaded and indexed at least one document in the Documents section.

## How to Perform a Search

### Search Bar

1. Type your question in the search field (placeholder: "E.g., How does the authentication system work?")
2. Optionally, adjust the **Top K** parameter from the dropdown menu next to it
3. Click the **Search** button or press Enter

### Top K Parameter

The **Top K** value determines how many results are returned. The available options are:
- **3** - Returns the 3 most relevant passages
- **5** - Default value, good balance
- **10** - Broader search
- **20** - Maximum number of results

A higher value returns more results but potentially less relevant ones.

### Suggestions

When no search has been performed yet, three clickable suggestions are shown:
- "Summarize the content of the documents"
- "What are the main concepts?"
- "Find information about configuration"

Clicking a suggestion automatically starts the search with that text.

## Results

### Results Header

After a search, the number of results found and the query used are displayed (e.g., "3 results for: authentication system").

### Result Card

Each result is presented as a card containing:

#### Score Bar
- A colored horizontal bar that visually represents the relevance of the result
- The percentage score is shown on the right (e.g., "87.5%")
- The higher the score, the more relevant the passage is to the query

#### Document Name
- File icon followed by the name of the source document
- Allows identifying which file the passage comes from

#### Content
- Preview of the passage text (truncated to 4 lines)
- Shows the document chunk that matches the search

## Loading State

During search execution, a spinner with the text "Searching..." is displayed.

## No Results

If the search produces no results, the message "No results found. Try rephrasing your search." is displayed.
`;
