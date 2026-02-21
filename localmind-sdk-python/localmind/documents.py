"""Document management client for LocalMind."""

from __future__ import annotations

import os
from typing import TYPE_CHECKING, Any, Dict, List

if TYPE_CHECKING:
    from .client import LocalMindClient


class DocumentClient:
    """Client for document management operations.

    Example::

        docs = client.documents
        all_docs = docs.list()
        uploaded = docs.upload("/path/to/report.pdf")
        docs.delete(uploaded["id"])
    """

    def __init__(self, client: "LocalMindClient"):
        self._client = client

    def list(self) -> List[Dict[str, Any]]:
        """List all documents.

        Returns:
            List of document objects with id, filename, filePath, mimeType,
            fileSize, status, createdAt, indexedAt, ocrConfidence.
        """
        return self._client._get("/documents")

    def get(self, doc_id: str) -> Dict[str, Any]:
        """Get a document by ID.

        Args:
            doc_id: Document UUID.

        Returns:
            Document object.
        """
        return self._client._get(f"/documents/{doc_id}")

    def upload(self, file_path: str) -> Dict[str, Any]:
        """Upload a file to LocalMind for indexing.

        Args:
            file_path: Absolute or relative path to the file.

        Returns:
            Created document object.
        """
        filename = os.path.basename(file_path)
        with open(file_path, "rb") as f:
            files = {"file": (filename, f)}
            return self._client._post_multipart("/documents/upload", files=files)

    def delete(self, doc_id: str) -> None:
        """Delete a document by ID.

        Args:
            doc_id: Document UUID.
        """
        self._client._delete(f"/documents/{doc_id}")
