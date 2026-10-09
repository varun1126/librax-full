package com.librax.exception;

/**
 * Root of the LibraX custom exception hierarchy.
 *
 * <p>All application-specific exceptions extend this type so that
 * higher layers (UI, controllers) can catch a single family of
 * exceptions when they only care that "something LibraX-specific went
 * wrong", while still being able to catch more specific subtypes
 * ({@link ValidationException}, {@link DatabaseException}, etc.) when
 * they need to react differently.</p>
 *
 * <p>This is an unchecked exception by design: checked exceptions from
 * lower-level APIs (e.g. {@code java.sql.SQLException}) are caught at
 * the repository boundary and rethrown wrapped in a {@code LibraXException}
 * subtype, keeping service and UI method signatures clean.</p>
 */
public class LibraXException extends RuntimeException {

    public LibraXException(String message) {
        super(message);
    }

    public LibraXException(String message, Throwable cause) {
        super(message, cause);
    }
}
