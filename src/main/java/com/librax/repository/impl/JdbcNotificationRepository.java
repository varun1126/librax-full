package com.librax.repository.impl;

import com.librax.config.DatabaseManager;
import com.librax.exception.DatabaseException;
import com.librax.model.Notification;
import com.librax.model.NotificationType;
import com.librax.repository.NotificationRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcNotificationRepository implements NotificationRepository {

    private final DatabaseManager db;

    public JdbcNotificationRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public Notification save(Notification n) {
        String sql = """
            INSERT INTO notifications (user_id, type, title, message, is_read, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n.getUserId());
            ps.setString(2, n.getType().name());
            ps.setString(3, n.getTitle());
            ps.setString(4, n.getMessage());
            ps.setInt(5, n.isRead() ? 1 : 0);
            ps.setString(6, LocalDateTime.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) n.setId(keys.getInt(1));
            }
            return n;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save notification", e);
        }
    }

    @Override
    public List<Notification> findByUserId(int userId, int limit) {
        String sql = """
            SELECT * FROM notifications WHERE user_id = ?
            ORDER BY created_at DESC LIMIT ?
        """;
        List<Notification> list = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit > 0 ? limit : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getInt("id"));
                    n.setUserId(rs.getInt("user_id"));
                    n.setType(NotificationType.valueOf(rs.getString("type")));
                    n.setTitle(rs.getString("title"));
                    n.setMessage(rs.getString("message"));
                    n.setRead(rs.getInt("is_read") == 1);
                    n.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
                    String readAt = rs.getString("read_at");
                    if (readAt != null && !readAt.isBlank()) {
                        n.setReadAt(LocalDateTime.parse(readAt));
                    }
                    list.add(n);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load notifications", e);
        }
    }

    @Override
    public int countUnreadByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count unread notifications", e);
        }
    }

    @Override
    public void markAsRead(int notificationId, int userId) {
        String sql = "UPDATE notifications SET is_read = 1, read_at = ? WHERE id = ? AND user_id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, LocalDateTime.now().toString());
            ps.setInt(2, notificationId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to mark notification as read", e);
        }
    }

    @Override
    public void markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = 1, read_at = ? WHERE user_id = ? AND is_read = 0";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, LocalDateTime.now().toString());
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to mark all notifications as read", e);
        }
    }

    @Override
    public void createNotification(int userId, NotificationType type, String title, String message) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setRead(false);
        save(n);
    }
}
