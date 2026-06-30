"""LocalMind Python SDK - chat, documents, semantic search, and conversation management."""

from .client import LocalMindClient
from .exceptions import LocalMindException

__version__ = "1.0.0"
__all__ = ["LocalMindClient", "LocalMindException"]
