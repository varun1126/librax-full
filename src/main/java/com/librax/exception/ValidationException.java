package com.librax.exception;

/**
 * Thrown when user- or caller-supplied data fails a business validation
 * rule (e.g. an invalid ISBN, a missing required field, a duplicate
 * membership number).
 *
 * <p>The message on this exception is always safe to show directly to
 * the end user.</p>
 */
public class ValidationException extends LibraXException {

    public ValidationException(String message) {
        super(message);
    }
}
