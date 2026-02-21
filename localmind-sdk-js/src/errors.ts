/**
 * Exception thrown when a LocalMind API call fails.
 */
export class LocalMindException extends Error {
  /** HTTP status code (0 if the error occurred before receiving a response). */
  readonly statusCode: number;
  /** Raw response body from the server. */
  readonly responseBody: string | null;

  constructor(message: string, statusCode: number = 0, responseBody: string | null = null) {
    super(message);
    this.name = 'LocalMindException';
    this.statusCode = statusCode;
    this.responseBody = responseBody;
  }
}
