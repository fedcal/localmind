# User Guide - LLM Provider Settings

## Access

Navigate to **Settings** from the sidebar or visit `http://localhost:4200/settings`.

## Overview

The Settings section allows you to configure the LLM (Large Language Model) providers used by the system. It is possible to add multiple providers, each with its own credentials and default model. API keys are persistently saved in the database.

## Adding a Provider

1. Click **+ Add Provider** in the header
2. Fill out the "New LLM Provider" form

### Form Fields

#### Name (required)
Descriptive name for the provider (e.g., "Local Ollama", "OpenAI Production").

#### Type (required)
Select the provider type from the dropdown menu:

| Type | Description | Requires API Key |
|------|-------------|------------------|
| **Ollama** | Local LLM models via Ollama | No |
| **OpenAI** | GPT-4, GPT-4o, GPT-3.5 and other OpenAI models | Yes |
| **Anthropic** | Claude and other Anthropic models | Yes |
| **Google Gemini** | Gemini Pro, Flash and other Google models | Yes |

Changing the type automatically updates the base URL and placeholders.

#### Base URL (required)
The provider service URL. It is pre-filled based on the selected type:

| Type | Default URL |
|------|-------------|
| Ollama | `http://localhost:11434` |
| OpenAI | `https://api.openai.com` |
| Anthropic | `https://api.anthropic.com` |
| Google Gemini | `https://generativelanguage.googleapis.com` |

For Ollama, if the service is on a different host or port, modify the URL accordingly.

#### API Key (required for cloud providers)
The API Key field appears only for non-Ollama providers. Enter the API key:
- **OpenAI**: format `sk-...`
- **Anthropic**: format `sk-ant-...`
- **Google**: format `AIza...`

The field is of password type (text is masked). The key is saved in the database.

#### Default Model

The behavior of this field changes based on the provider type:

**For Ollama (dynamic dropdown):**
- The field shows a **dropdown menu** automatically populated with the models downloaded on the Ollama instance
- The list is automatically loaded when selecting the Ollama type or when modifying the base URL
- A **refresh** button (circular arrow icon) next to the dropdown allows reloading the list
- During loading, the dropdown is disabled and shows a spinner
- If no models are found, the message appears: "No models found. Check the URL and that Ollama is running."
- The first model in the list is automatically selected

**For OpenAI, Anthropic, Google (free text field):**
- Text field with suggested placeholder:
  - OpenAI: `gpt-4o-mini`
  - Anthropic: `claude-sonnet-4-20250514`
  - Google: `gemini-2.0-flash`

### Form Validation

The **Save Provider** button is enabled only when:
- The Name field is filled in
- The Base URL field is filled in
- For cloud providers: the API Key field is filled in

## List of Configured Providers

Saved providers are displayed as cards with the following information:

### Card Header
- **Type icon**: colored circle with the provider's initial
  - Ollama: green
  - OpenAI: teal
  - Anthropic: light brown
  - Google: blue
- **Name** of the provider
- **Base URL** in monospace font
- **Status badge**: "Active" (green) or "Disabled" (gray)

### Details
- **Type**: badge with the provider type name
- **Default model**: name of the default model (if configured)
- **Models**: list of tags with available models (if present)

### Actions

| Action | Description |
|--------|-------------|
| **Test Connection** | Verifies that the provider is reachable and operational. For Ollama, shows the number of available models. For cloud providers, verifies that the API key is configured. |
| **Remove** | Deletes the provider from the configuration |

The Test Connection button shows a spinner during the test. The result is communicated via notification:
- **Success**: "[name]: Connection successful"
- **Error**: "[name]: [error message]"

## Notifications

Notifications appear at the top of the page and automatically disappear after 4 seconds:
- **Green** (success): provider added, removed, or test successful
- **Red** (error): error in saving, removal, or failed test
