package com.librax.security;

import com.librax.model.User;

public final class SecurityContextHolder {

    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();

    private SecurityContextHolder() {}

    public static void setUser(User user) {
        CURRENT_USER.set(user);
    }

    public static User getUser() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
