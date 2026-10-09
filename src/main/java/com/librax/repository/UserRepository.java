package com.librax.repository;

import com.librax.model.Permission;
import com.librax.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(int id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String identifier);
    List<User> findAll();
    void update(User user);
    void updatePassword(int userId, String passwordHash);
    void updateFailedAttempts(int userId, int attempts);
    void recordLoginSuccess(int userId, LocalDateTime timestamp);
    void lockAccount(int userId, LocalDateTime lockedUntil);
    void unlockAccount(int userId);
    void delete(int id);
    boolean existsAnyUser();
    Set<String> findPermissionsByRoleId(int roleId);
    Set<String> findPermissionsByUserId(int userId);
    List<Permission> findAllPermissions();
    void logLoginAttempt(Integer userId, String username, String ip, String userAgent, String status, String reason);
}
