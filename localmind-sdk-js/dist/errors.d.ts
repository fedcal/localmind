/**
 * Exception thrown when a LocalMind API call fails.
 */
export declare class LocalMindException extends Error {
    /** HTTP status code (0 if the error occurred before receiving a response). */
    readonly statusCode: number;
    /** Raw response body from the server. */
    readonly responseBody: string | null;
    constructor(message: string, statusCode?: number, responseBody?: string | null);
}
//# sourceMappingURL=errors.d.ts.map