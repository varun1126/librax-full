package com.librax.exception;

/**
 * Thrown when a book issue is attempted but no copy of the requested
 * book is currently available (all copies issued, lost, damaged, or
 * retired).
 */
public class BookNotAvailableException extends LibraXException {

    public BookNotAvailableException(String message) {
        super(message);
    }
}
