# User Guide - Chat

## Access

Navigate to **Chat** from the sidebar or visit `http://localhost:4200/chat`.

## Overview

The Chat page is the main interface for conversing with AI models. It allows you to select the provider and model, send messages, and receive responses in real time.

## Provider and Model Selection

Two dropdown menus are available in the top bar:

### Provider
Select the LLM provider to use from those available:
- **OLLAMA** - Local models via Ollama
- **OPENAI** - OpenAI models (requires API key)
- **ANTHROPIC** - Anthropic Claude models (requires API key)

### Model
Select the specific model. Default options include:
- llama3.2, llama3.1, mistral, codellama, phi3, gemma2

> **Note**: the actually available models depend on those downloaded on Ollama or on the cloud provider configuration.

### New Chat

The **New Chat** button on the right side of the header clears the message history and starts a new conversation. The message count for the current conversation is displayed next to it.

## Message Area

### Initial State (no messages)

When the chat is empty, a welcome message is shown with three clickable suggestions:
- "Explain how RAG works"
- "Summarize the uploaded documents"
- "What models are available?"

Clicking a suggestion automatically sends the text as a message.

### Messages in the Conversation

Messages are displayed in chronological order:
- **User messages**: blue background, right-aligned, with avatar icon "U"
- **Assistant messages**: white background with shadow, left-aligned, with avatar icon "AI"

Message content supports basic formatting:
- **Bold text** (enclosed in `**`)
- `Inline code` (enclosed in single backticks)
- Code blocks (enclosed in triple backticks) with dark background and monospace font

### Loading Indicator

When the model is processing a response, a pulsing three-dot animation is shown below the last message.

### Errors

In case of a connection or server error, a red bar appears at the bottom of the message area with the error text.

## Sending Messages

### Input Area

At the bottom of the page there is an expandable text field with the placeholder "Write a message...".

### Keyboard Commands
- **Enter** (`Enter`): sends the message
- **Shift + Enter** (`Shift+Enter`): inserts a new line without sending

### Send Button

The button with the arrow icon to the right of the text field sends the message. It is disabled (gray) when:
- The text field is empty
- A response is already being processed (loading state active)
