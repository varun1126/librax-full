package com.librax.web;

import com.librax.config.DatabaseManager;
import com.librax.model.AuditLog;
import com.librax.repository.AuditLogRepository;
import com.librax.security.RequiresPermission;
import com.librax.web.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditController {

    private final AuditLogRepository auditLogRepository;
    private final DatabaseManager databaseManager;

    public AuditController(AuditLogRepository auditLogRepository, DatabaseManager databaseManager) {
        this.auditLogRepository = auditLogRepository;
        this.databaseManager = databaseManager;
    }

    @GetMapping
    @RequiresPermission("AUDIT_VIEW")
    public ApiResponse<List<AuditLog>> getAuditLogs(
            @RequestParam(value = "limit", defaultValue = "100") int limit,
            @RequestParam(value = "entity", required = false) String entity,
            @RequestParam(value = "action", required = false) String action) {

        List<AuditLog> logs = auditLogRepository.findRecent(limit);
        if (entity != null && !entity.isBlank()) {
            logs = logs.stream().filter(l -> entity.equalsIgnoreCase(l.getEntity())).toList();
        }
        if (action != null && !action.isBlank()) {
            logs = logs.stream().filter(l -> action.equalsIgnoreCase(l.getAction())).toList();
        }
        return ApiResponse.ok(logs);
    }

    @GetMapping("/login-history")
    @RequiresPermission("AUDIT_VIEW")
    public ApiResponse<List<Map<String, Object>>> getLoginHistory(@RequestParam(value = "limit", defaultValue = "50") int limit) {
        String sql = "SELECT * FROM login_history ORDER BY timestamp DESC LIMIT ?";
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection c = databaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getInt("id"));
                    map.put("username", rs.getString("username"));
                    map.put("ipAddress", rs.getString("ip_address"));
                    map.put("userAgent", rs.getString("user_agent"));
                    map.put("status", rs.getString("status"));
                    map.put("failureReason", rs.getString("failure_reason"));
                    map.put("timestamp", rs.getString("timestamp"));
                    results.add(map);
                }
            }
            return ApiResponse.ok(results);
        } catch (SQLException e) {
            throw new com.librax.exception.DatabaseException("Failed to read login history", e);
        }
    }
}
