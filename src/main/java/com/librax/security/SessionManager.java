package com.librax.security;

import com.librax.model.User;

/**
 * Tracks the currently authenticated user for this running application
 * instance. LibraX is a single-user-at-a-time desktop app, so a simple
 * in-memory holder (not a web-style session store) is sufficient and
 * intentionally kept minimal per RULES.md's guidance against unnecessary
 * shared mutable state.
 */
public final class SessionManager {

    private User currentUser;

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == com.librax.model.Role.ADMIN;
    }
}
