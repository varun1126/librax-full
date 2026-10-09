package com.librax.exception;

/**
 * Thrown for authentication/authorization failures: invalid credentials,
 * inactive accounts, expired sessions, or insufficient role permissions
 * for an attempted operation.
 */
public class AuthenticationException extends LibraXException {

    public AuthenticationException(String message) {
        super(message);
    }
}
