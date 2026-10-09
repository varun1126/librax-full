package com.librax.exception;

/**
 * Thrown when a repository/DAO operation fails at the persistence layer
 * (e.g. an underlying {@code java.sql.SQLException}, a constraint
 * violation, or a connection failure).
 *
 * <p>Repository implementations catch {@code SQLException} and rethrow it
 * wrapped in this type so that service-layer and UI code never needs to
 * import {@code java.sql} directly.</p>
 */
public class DatabaseException extends LibraXException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
