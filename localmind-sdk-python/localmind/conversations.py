"""Conversation management client for LocalMind."""

from __future__ import annotations

from typing import TYPE_CHECKING, Any, Dict, List, Optional

if TYPE_CHECKING:
    from .client import LocalMindClient


class ConversationClient:
    """Client for conversation management operations.

    Example::

        convos = client.conversations
        all_convos = convos.list()
        full = convos.get("abc-123")
        exported = convos.export("abc-123", format="pdf")
        convos.delete("abc-123")
    """

    def __init__(self, client: "LocalMindClient"):
        self._client = client

    def list(self, tag: Optional[str] = None) -> List[Dict[str, Any]]:
        """List all conversations, optionally filtered by tag.

        Args:
            tag: Optional tag to filter conversations.

        Returns:
            List of conversation summary objects.
        """
        params = {}
        if tag:
            params["tag"] = tag
        return self._client._get("/conversations", params=params)

    def get(self, conv_id: str) -> Dict[str, Any]:
        """Get a conversation with full message history.

        Args:
            conv_id: Conversation UUID.

        Returns:
            Full conversation object including messages.
        """
        return self._client._get(f"/conversations/{conv_id}")

    def rename(self, conv_id: str, title: str) -> Dict[str, Any]:
        """Rename a conversation.

        Args:
            conv_id: Conversation UUID.
            title: New title.

        Returns:
            Updated conversation object.
        """
        return self._client._patch(f"/conversations/{conv_id}", json={"title": title})

    def update_system_prompt(self, conv_id: str, system_prompt: str) -> Dict[str, Any]:
        """Update the system prompt for a conversation.

        Args:
            conv_id: Conversation UUID.
            system_prompt: New system prompt.

        Returns:
            Updated conversation object.
        """
        return self._client._patch(
            f"/conversations/{conv_id}/system-prompt",
            json={"systemPrompt": system_prompt},
        )

    def add_tag(self, conv_id: str, tag: str) -> Dict[str, Any]:
        """Add a tag to a conversation.

        Args:
            conv_id: Conversation UUID.
            tag: Tag string.

        Returns:
            Updated conversation object.
        """
        return self._client._post(f"/conversations/{conv_id}/tags", json={"tag": tag})

    def remove_tag(self, conv_id: str, tag: str) -> Dict[str, Any]:
        """Remove a tag from a conversation.

        Args:
            conv_id: Conversation UUID.
            tag: Tag to remove.

        Returns:
            Updated conversation object.
        """
        return self._client._delete_with_response(f"/conversations/{conv_id}/tags/{tag}")

    def export(self, conv_id: str, format: str = "json") -> bytes:
        """Export a conversation in the specified format.

        Args:
            conv_id: Conversation UUID.
            format: Export format - "json", "md", "markdown", or "pdf".

        Returns:
            Raw bytes of the exported file.
        """
        return self._client._get_bytes(
            f"/conversations/{conv_id}/export", params={"format": format}
        )

    def delete(self, conv_id: str) -> None:
        """Delete a conversation.

        Args:
            conv_id: Conversation UUID.
        """
        self._client._delete(f"/conversations/{conv_id}")
