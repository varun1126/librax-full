package com.librax.repository.impl;

import com.librax.config.DatabaseManager;
import com.librax.exception.DatabaseException;
import com.librax.model.Permission;
import com.librax.model.Role;
import com.librax.model.User;
import com.librax.model.UserStatus;
import com.librax.repository.UserRepository;
import com.librax.util.DateUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class JdbcUserRepository implements UserRepository {

    private final DatabaseManager db;

    public JdbcUserRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public User save(User user) {
        String sql = """
            INSERT INTO users (username, password_hash, role_id, full_name, email, status, is_active,
                                failed_login_attempts, created_at)
            VALUES (?, ?, (SELECT id FROM roles WHERE name = ?), ?, ?, ?, ?, ?, ?)
        """;
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            user.setCreatedAt(user.getCreatedAt() == null ? LocalDateTime.now() : user.getCreatedAt());
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getStatus() != null ? user.getStatus().name() : "ACTIVE");
            ps.setInt(7, user.isActive() ? 1 : 0);
            ps.setInt(8, user.getFailedLoginAttempts());
            ps.setString(9, DateUtils.format(user.getCreatedAt()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save user", e);
        }
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = """
            SELECT u.*, r.name AS role_name FROM users u
            JOIN roles r ON r.id = u.role_id WHERE u.id = ?
        """;
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapRow(rs);
                    user.setPermissions(findPermissionsByUserId(user.getId()));
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by id", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = """
            SELECT u.*, r.name AS role_name FROM users u
            JOIN roles r ON r.id = u.role_id WHERE u.username = ?
        """;
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapRow(rs);
                    user.setPermissions(findPermissionsByUserId(user.getId()));
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by username", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = """
            SELECT u.*, r.name AS role_name FROM users u
            JOIN roles r ON r.id = u.role_id WHERE u.email = ?
        """;
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapRow(rs);
                    user.setPermissions(findPermissionsByUserId(user.getId()));
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by email", e);
        }
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String identifier) {
        String sql = """
            SELECT u.*, r.name AS role_name FROM users u
            JOIN roles r ON r.id = u.role_id WHERE u.username = ? OR u.email = ?
        """;
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapRow(rs);
                    user.setPermissions(findPermissionsByUserId(user.getId()));
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by identifier", e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = """
            SELECT u.*, r.name AS role_name FROM users u
            JOIN roles r ON r.id = u.role_id ORDER BY u.full_name
        """;
        List<User> results = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User user = mapRow(rs);
                user.setPermissions(findPermissionsByUserId(user.getId()));
                results.add(user);
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list users", e);
        }
    }

    @Override
    public void update(User user) {
        String sql = """
            UPDATE users SET username = ?, role_id = (SELECT id FROM roles WHERE name = ?),
                              full_name = ?, email = ?, status = ?, is_active = ?, updated_at = ?
            WHERE id = ?
        """;
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getRole().name());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getStatus() != null ? user.getStatus().name() : "ACTIVE");
            ps.setInt(6, user.isActive() ? 1 : 0);
            ps.setString(7, LocalDateTime.now().toString());
            ps.setInt(8, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update user", e);
        }
    }

    @Override
    public void updatePassword(int userId, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ?, updated_at = ? WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setString(2, LocalDateTime.now().toString());
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update password", e);
        }
    }

    @Override
    public void updateFailedAttempts(int userId, int attempts) {
        String sql = "UPDATE users SET failed_login_attempts = ? WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, attempts);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update failed login attempts", e);
        }
    }

    @Override
    public void recordLoginSuccess(int userId, LocalDateTime timestamp) {
        String sql = "UPDATE users SET failed_login_attempts = 0, last_login = ?, locked_until = NULL WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, timestamp.toString());
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to record login success", e);
        }
    }

    @Override
    public void lockAccount(int userId, LocalDateTime lockedUntil) {
        String sql = "UPDATE users SET status = 'LOCKED', locked_until = ? WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, lockedUntil.toString());
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to lock account", e);
        }
    }

    @Override
    public void unlockAccount(int userId) {
        String sql = "UPDATE users SET status = 'ACTIVE', locked_until = NULL, failed_login_attempts = 0 WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to unlock account", e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete user", e);
        }
    }

    @Override
    public boolean existsAnyUser() {
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check for existing users", e);
        }
    }

    @Override
    public Set<String> findPermissionsByRoleId(int roleId) {
        String sql = """
            SELECT p.name FROM permissions p
            JOIN role_permissions rp ON rp.permission_id = p.id
            WHERE rp.role_id = ?
        """;
        Set<String> perms = new HashSet<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    perms.add(rs.getString("name"));
                }
            }
            return perms;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load permissions for role", e);
        }
    }

    @Override
    public Set<String> findPermissionsByUserId(int userId) {
        String sql = """
            SELECT p.name FROM permissions p
            JOIN role_permissions rp ON rp.permission_id = p.id
            JOIN users u ON u.role_id = rp.role_id
            WHERE u.id = ?
        """;
        Set<String> perms = new HashSet<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    perms.add(rs.getString("name"));
                }
            }
            return perms;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load permissions for user", e);
        }
    }

    @Override
    public List<Permission> findAllPermissions() {
        String sql = "SELECT * FROM permissions ORDER BY category, name";
        List<Permission> list = new ArrayList<>();
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Permission(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("description")
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list all permissions", e);
        }
    }

    @Override
    public void logLoginAttempt(Integer userId, String username, String ip, String userAgent, String status, String reason) {
        String sql = """
            INSERT INTO login_history (user_id, username, ip_address, user_agent, status, failure_reason, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (userId != null) ps.setInt(1, userId);
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, username);
            ps.setString(3, ip);
            ps.setString(4, userAgent);
            ps.setString(5, status);
            ps.setString(6, reason);
            ps.setString(7, LocalDateTime.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            // Do not block user flow on history log failure
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(Role.valueOf(rs.getString("role_name")));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        try {
            String statusStr = rs.getString("status");
            if (statusStr != null) {
                user.setStatus(UserStatus.valueOf(statusStr));
            }
        } catch (Exception ignored) {}
        user.setActive(rs.getInt("is_active") == 1);
        user.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
        user.setCreatedAt(DateUtils.parseDateTime(rs.getString("created_at")));
        try {
            String lockedUntilStr = rs.getString("locked_until");
            if (lockedUntilStr != null && !lockedUntilStr.isBlank()) {
                user.setLockedUntil(LocalDateTime.parse(lockedUntilStr));
            }
            String lastLoginStr = rs.getString("last_login");
            if (lastLoginStr != null && !lastLoginStr.isBlank()) {
                user.setLastLogin(LocalDateTime.parse(lastLoginStr));
            }
            String updatedAtStr = rs.getString("updated_at");
            if (updatedAtStr != null && !updatedAtStr.isBlank()) {
                user.setUpdatedAt(LocalDateTime.parse(updatedAtStr));
            }
        } catch (Exception ignored) {}
        return user;
    }
}
