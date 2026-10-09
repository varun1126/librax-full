package com.librax.service;

import com.librax.exception.AuthenticationException;
import com.librax.logging.AuditLogger;
import com.librax.model.Role;
import com.librax.model.User;
import com.librax.repository.UserRepository;
import com.librax.security.PasswordHasher;
import com.librax.security.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Handles login, first-run administrator setup, and failed-login
 * tracking. Business rules only — no Swing here.
 */
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final SessionManager sessionManager;
    private final AuditLogger auditLogger;

    public AuthenticationService(UserRepository userRepository, PasswordHasher passwordHasher,
                                  SessionManager sessionManager, AuditLogger auditLogger) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.sessionManager = sessionManager;
        this.auditLogger = auditLogger;
    }

    /** @return true if no users exist yet and first-run admin setup should run. */
    public boolean isFirstRun() {
        return !userRepository.existsAnyUser();
    }

    /**
     * Creates the very first administrator account. Only callable when
     * {@link #isFirstRun()} is true; the caller (UI) is responsible for
     * collecting a real, non-hardcoded password from the user.
     */
    public User createFirstAdministrator(String username, char[] password, String fullName, String email) {
        if (!isFirstRun()) {
            throw new AuthenticationException("An administrator account already exists.");
        }
        if (password == null || password.length < 8) {
            throw new AuthenticationException("Password must be at least 8 characters.");
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordHasher.hash(password));
        user.setRole(Role.ADMIN);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        auditLogger.log(saved.getId(), "ADMIN_CREATED", "USER", saved.getId(), "First-run administrator created");
        log.info("First-run administrator '{}' created", username);
        return saved;
    }

    /**
     * Attempts to authenticate. On success, establishes the session. On
     * failure, increments the account's failed-attempt counter and locks
     * it after {@value #MAX_FAILED_ATTEMPTS} consecutive failures.
     */
    public User authenticate(String username, char[] password) {
        Optional<User> maybeUser = userRepository.findByUsername(username);
        if (maybeUser.isEmpty()) {
            auditLogger.log(null, "LOGIN_FAILED", "USER", null, "Unknown username: " + username);
            throw new AuthenticationException("Invalid username or password.");
        }
        User user = maybeUser.get();
        if (!user.isActive()) {
            auditLogger.log(user.getId(), "LOGIN_FAILED", "USER", user.getId(), "Account inactive");
            throw new AuthenticationException("This account has been deactivated.");
        }
        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            auditLogger.log(user.getId(), "LOGIN_FAILED", "USER", user.getId(), "Account locked");
            throw new AuthenticationException("Account locked after too many failed attempts. Contact an administrator.");
        }
        if (!passwordHasher.verify(password, user.getPasswordHash())) {
            userRepository.updateFailedAttempts(user.getId(), user.getFailedLoginAttempts() + 1);
            auditLogger.log(user.getId(), "LOGIN_FAILED", "USER", user.getId(), "Incorrect password");
            throw new AuthenticationException("Invalid username or password.");
        }
        userRepository.updateFailedAttempts(user.getId(), 0);
        sessionManager.login(user);
        auditLogger.log(user.getId(), "LOGIN_SUCCESS", "USER", user.getId(), "User logged in");
        log.info("User '{}' logged in", username);
        return user;
    }

    public void logout() {
        User current = sessionManager.getCurrentUser();
        if (current != null) {
            auditLogger.log(current.getId(), "LOGOUT", "USER", current.getId(), "User logged out");
        }
        sessionManager.logout();
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    /** ADMIN-only: create a librarian or additional admin account. */
    public User createUser(String username, char[] password, Role role, String fullName, String email) {
        requireAdmin();
        if (userRepository.findByUsername(username).isPresent()) {
            throw new AuthenticationException("Username already exists.");
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordHasher.hash(password));
        user.setRole(role);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        auditLogger.log(sessionManager.getCurrentUser().getId(), "USER_CREATED", "USER", saved.getId(),
                "Created user " + username + " (" + role + ")");
        return saved;
    }

    public void deactivateUser(int userId) {
        requireAdmin();
        userRepository.findById(userId).ifPresent(u -> {
            u.setActive(false);
            userRepository.update(u);
            auditLogger.log(sessionManager.getCurrentUser().getId(), "USER_DEACTIVATED", "USER", userId, "User deactivated");
        });
    }

    private void requireAdmin() {
        if (!sessionManager.isAdmin()) {
            throw new AuthenticationException("This action requires administrator privileges.");
        }
    }
}
