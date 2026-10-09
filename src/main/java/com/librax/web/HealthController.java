package com.librax.web;

import com.librax.config.DatabaseManager;
import com.librax.web.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check endpoint for monitoring, deployment verification,
 * and automated liveness/readiness probes.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DatabaseManager databaseManager;

    public HealthController(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getHealth() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("status", "UP");
        details.put("timestamp", Instant.now().toString());
        details.put("application", "LibraX Intelligent Library System");
        details.put("version", "1.0.0");

        boolean dbOk = false;
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            if (rs.next()) {
                dbOk = true;
            }
        } catch (Exception e) {
            dbOk = false;
            details.put("databaseError", e.getMessage());
        }

        details.put("database", dbOk ? "UP" : "DOWN");

        if (!dbOk) {
            return ApiResponse.error("SERVICE_UNAVAILABLE", "Database connection check failed");
        }
        return ApiResponse.ok(details, "System is operational and ready");
    }
}
