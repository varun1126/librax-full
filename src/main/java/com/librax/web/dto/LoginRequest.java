package com.librax.web.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Username, email, or student ID is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;

    private boolean rememberMe;

    public LoginRequest() {}

    public LoginRequest(String identifier, String password, boolean rememberMe) {
        this.identifier = identifier;
        this.password = password;
        this.rememberMe = rememberMe;
    }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getUsername() { return identifier; }
    public void setUsername(String username) {
        if (username != null && !username.isBlank()) {
            this.identifier = username;
        }
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isRememberMe() { return rememberMe; }
    public void setRememberMe(boolean rememberMe) { this.rememberMe = rememberMe; }
}
