package com.librax.repository.impl;

import com.librax.config.DatabaseManager;
import com.librax.exception.DatabaseException;
import com.librax.model.AuditLog;
import com.librax.repository.AuditLogRepository;
import com.librax.util.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcAuditLogRepository implements AuditLogRepository {

    private final DatabaseManager db;

    public JdbcAuditLogRepository(DatabaseManager db) {
        this.db = db;
    }

    private static final String SELECT_BASE = """
        SELECT a.*, u.username FROM audit_logs a LEFT JOIN users u ON u.id = a.user_id
    """;

    @Override
    public void save(AuditLog log) {
        String sql = "INSERT INTO audit_logs (user_id, action, entity, entity_id, timestamp, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (log.getUserId() != null) ps.setInt(1, log.getUserId()); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, log.getAction());
            ps.setString(3, log.getEntity());
            if (log.getEntityId() != null) ps.setInt(4, log.getEntityId()); else ps.setNull(4, Types.INTEGER);
            ps.setString(5, DateUtils.format(log.getTimestamp()));
            ps.setString(6, log.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to write audit log", e);
        }
    }

    @Override
    public List<AuditLog> findAll() {
        List<AuditLog> results = new ArrayList<>();
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE + " ORDER BY a.timestamp DESC")) {
            while (rs.next()) results.add(mapRow(rs));
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list audit logs", e);
        }
    }

    @Override
    public List<AuditLog> findRecent(int limit) {
        List<AuditLog> results = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + " ORDER BY a.timestamp DESC LIMIT ?")) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list recent audit logs", e);
        }
    }

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog a = new AuditLog();
        a.setId(rs.getInt("id"));
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) a.setUserId(userId);
        a.setUsername(rs.getString("username"));
        a.setAction(rs.getString("action"));
        a.setEntity(rs.getString("entity"));
        int entityId = rs.getInt("entity_id");
        if (!rs.wasNull()) a.setEntityId(entityId);
        a.setTimestamp(DateUtils.parseDateTime(rs.getString("timestamp")));
        a.setDescription(rs.getString("description"));
        return a;
    }
}
