package com.librax.exception;

/**
 * Thrown for failures interacting with the external Google Books API:
 * network/connectivity failures, timeouts, rate limiting, non-2xx HTTP
 * responses, or malformed/unexpected JSON payloads.
 *
 * <p>Callers should show a short, friendly message to the user (the
 * message on this exception is written to be user-safe) while the
 * originating technical cause is logged via SLF4J.</p>
 */
public class ApiException extends LibraXException {

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
