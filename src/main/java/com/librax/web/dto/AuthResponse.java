package com.librax.web.dto;

import com.librax.model.Member;
import com.librax.model.Role;
import com.librax.model.User;

import java.time.LocalDateTime;
import java.util.Set;

public class AuthResponse {

    private String token;
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private Role role;
    private Set<String> permissions;
    private LocalDateTime lastLogin;
    private Member member;

    public AuthResponse() {}

    public AuthResponse(String token, User user, Member member) {
        this.token = token;
        this.userId = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.permissions = user.getPermissions();
        this.lastLogin = user.getLastLogin();
        this.member = member;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
}
