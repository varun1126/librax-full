package com.librax.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * A system user (administrator, librarian, or student).
 * Passwords are always stored as a BCrypt hash — never plaintext, never logged.
 */
public class User {
    private Integer id;
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;
    private Role role;
    private String fullName;
    private String email;
    private UserStatus status = UserStatus.ACTIVE;
    private boolean active = true;
    private int failedLoginAttempts = 0;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<String> permissions = new HashSet<>();

    public User() {}

    public User(Integer id, String username, String passwordHash, Role role, String fullName,
                String email, boolean active, int failedLoginAttempts, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.active = active;
        this.status = active ? UserStatus.ACTIVE : UserStatus.DISABLED;
        this.failedLoginAttempts = failedLoginAttempts;
        this.createdAt = createdAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @JsonIgnore
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) {
        this.status = status;
        this.active = (status == UserStatus.ACTIVE);
    }

    public boolean isActive() {
        return active && status == UserStatus.ACTIVE;
    }
    public void setActive(boolean active) {
        this.active = active;
        if (!active && this.status == UserStatus.ACTIVE) {
            this.status = UserStatus.DISABLED;
        } else if (active && this.status == UserStatus.DISABLED) {
            this.status = UserStatus.ACTIVE;
        }
    }

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public boolean isAccountLocked() {
        if (status == UserStatus.LOCKED) return true;
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }

    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
}
