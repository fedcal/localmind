"""Main client for the LocalMind Python SDK."""

from __future__ import annotations

from typing import Any, Dict, List, Optional

import requests

from .conversations import ConversationClient
from .documents import DocumentClient
from .exceptions import LocalMindException


class LocalMindClient:
    """Main entry point for the LocalMind Python SDK.

    Example::

        from localmind import LocalMindClient

        client = LocalMindClient()
        response = client.chat("Riassumi il documento...")
        print(response["content"])

    Args:
        base_url: Base URL of the LocalMind API (default: http://localhost:8080/api/v1).
        auth_token: Pre-set JWT authentication token.
        timeout: Request timeout in seconds (default: 120). LLM responses can be slow.
    """

    def __init__(
        self,
        base_url: str = "http://localhost:8080/api/v1",
        auth_token: Optional[str] = None,
        timeout: int = 120,
    ):
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout
        self._session = requests.Session()
        if auth_token:
            self._session.headers["Authorization"] = f"Bearer {auth_token}"

        self.documents = DocumentClient(self)
        self.conversations = ConversationClient(self)

    # ---- Authentication ----

    def login(self, password: str) -> str:
        """Authenticate with the LocalMind instance.

        The token is stored internally and used for subsequent requests.

        Args:
            password: The configured password.

        Returns:
            The JWT token string.
        """
        data = self._post("/auth/login", json={"password": password})
        token = data["token"]
        self._session.headers["Authorization"] = f"Bearer {token}"
        return token

    # ---- Chat ----

    def chat(
        self,
        message: str,
        provider: Optional[str] = None,
        model: Optional[str] = None,
        conversation_id: Optional[str] = None,
        system_prompt: Optional[str] = None,
        temperature: float = 0.7,
        max_tokens: int = 0,
        enable_rag: bool = False,
        enable_tool_calling: bool = False,
    ) -> Dict[str, Any]:
        """Send a chat message to LocalMind.

        Args:
            message: The user message.
            provider: LLM provider override (OLLAMA, OPENAI, ANTHROPIC, GOOGLE).
            model: Model name override (e.g. "llama3", "gpt-4o").
            conversation_id: Existing conversation ID to continue.
            system_prompt: System prompt for the conversation.
            temperature: Generation temperature (0.0-2.0, default 0.7).
            max_tokens: Maximum tokens to generate (0 = provider default).
            enable_rag: Enable RAG from indexed documents.
            enable_tool_calling: Enable MCP tool calling.

        Returns:
            Dict with keys: content, model, provider, conversationId,
            tokenUsage, latencyMs, ragSources.
        """
        payload: Dict[str, Any] = {"message": message}
        if provider:
            payload["provider"] = provider
        if model:
            payload["model"] = model
        if conversation_id:
            payload["conversationId"] = conversation_id
        if system_prompt:
            payload["systemPrompt"] = system_prompt
        payload["temperature"] = temperature
        if max_tokens > 0:
            payload["maxTokens"] = max_tokens
        payload["enableRag"] = enable_rag
        payload["enableToolCalling"] = enable_tool_calling

        return self._post("/chat", json=payload)

    # ---- Search ----

    def search(self, query: str, top_k: int = 5) -> List[Dict[str, Any]]:
        """Semantic search across indexed documents.

        Args:
            query: Natural language search query.
            top_k: Maximum number of results to return (default: 5).

        Returns:
            List of search result dicts with keys: documentId, filename,
            content, score, chunkIndex.
        """
        return self._post("/documents/search", json={"query": query, "topK": top_k})

    # ---- Health ----

    def health(self) -> Dict[str, Any]:
        """Check the health status of the LocalMind platform.

        Returns:
            Dict with keys: status ("UP" or "DEGRADED"), services (dict of service statuses).
        """
        return self._get("/dashboard/health")

    # ---- Internal HTTP methods ----

    def _get(self, path: str, params: Optional[Dict] = None) -> Any:
        resp = self._session.get(
            f"{self.base_url}{path}", params=params, timeout=self.timeout
        )
        self._check_response(resp)
        if resp.status_code == 204 or not resp.content:
            return None
        return resp.json()

    def _get_bytes(self, path: str, params: Optional[Dict] = None) -> bytes:
        resp = self._session.get(
            f"{self.base_url}{path}", params=params, timeout=self.timeout
        )
        self._check_response(resp)
        return resp.content

    def _post(self, path: str, json: Optional[Dict] = None) -> Any:
        resp = self._session.post(
            f"{self.base_url}{path}", json=json, timeout=self.timeout
        )
        self._check_response(resp)
        if resp.status_code == 204 or not resp.content:
            return None
        return resp.json()

    def _post_multipart(self, path: str, files: Any) -> Any:
        resp = self._session.post(
            f"{self.base_url}{path}", files=files, timeout=self.timeout
        )
        self._check_response(resp)
        if resp.status_code == 204 or not resp.content:
            return None
        return resp.json()

    def _patch(self, path: str, json: Optional[Dict] = None) -> Any:
        resp = self._session.patch(
            f"{self.base_url}{path}", json=json, timeout=self.timeout
        )
        self._check_response(resp)
        if resp.status_code == 204 or not resp.content:
            return None
        return resp.json()

    def _delete(self, path: str) -> None:
        resp = self._session.delete(f"{self.base_url}{path}", timeout=self.timeout)
        self._check_response(resp)

    def _delete_with_response(self, path: str) -> Any:
        resp = self._session.delete(f"{self.base_url}{path}", timeout=self.timeout)
        self._check_response(resp)
        if resp.status_code == 204 or not resp.content:
            return None
        return resp.json()

    def _check_response(self, resp: requests.Response) -> None:
        if resp.status_code >= 400:
            body = resp.text
            raise LocalMindException(
                f"HTTP {resp.status_code} {resp.request.method} {resp.url}",
                status_code=resp.status_code,
                response_body=body,
            )
