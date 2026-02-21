"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.LocalMindException = void 0;
/**
 * Exception thrown when a LocalMind API call fails.
 */
class LocalMindException extends Error {
    constructor(message, statusCode = 0, responseBody = null) {
        super(message);
        this.name = 'LocalMindException';
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
}
exports.LocalMindException = LocalMindException;
//# sourceMappingURL=errors.js.map