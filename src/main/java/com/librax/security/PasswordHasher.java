package com.librax.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Thin wrapper around BCrypt for password hashing/verification. Never
 * stores or logs plaintext passwords.
 */
public final class PasswordHasher {

    private static final int COST_FACTOR = 12;

    public String hash(char[] plainPassword) {
        return BCrypt.withDefaults().hashToString(COST_FACTOR, plainPassword);
    }

    public boolean verify(char[] plainPassword, String hash) {
        if (hash == null || hash.isBlank()) {
            return false;
        }
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword, hash);
        return result.verified;
    }
}
