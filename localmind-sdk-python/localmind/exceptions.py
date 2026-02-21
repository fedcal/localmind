"""Exceptions for the LocalMind SDK."""


class LocalMindException(Exception):
    """Exception raised when a LocalMind API call fails.

    Attributes:
        status_code: HTTP status code (0 if the error occurred before receiving a response).
        response_body: Raw response body from the server.
    """

    def __init__(self, message: str, status_code: int = 0, response_body: str = None):
        super().__init__(message)
        self.status_code = status_code
        self.response_body = response_body
