package com.librax.web;

import com.librax.config.DatabaseConfig;
import com.librax.config.DatabaseManager;
import com.librax.logging.AuditLogger;
import com.librax.security.RequiresPermission;
import com.librax.security.SecurityContextHolder;
import com.librax.web.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/settings")
@SuppressWarnings("null")
public class SettingsController {

    private final DatabaseManager databaseManager;
    private final DatabaseConfig databaseConfig;
    private final AuditLogger auditLogger;

    public SettingsController(DatabaseManager databaseManager,
                              DatabaseConfig databaseConfig,
                              AuditLogger auditLogger) {
        this.databaseManager = databaseManager;
        this.databaseConfig = databaseConfig;
        this.auditLogger = auditLogger;
    }

    @GetMapping
    @RequiresPermission("SETTINGS_VIEW")
    public ApiResponse<Map<String, String>> getSettings() {
        Map<String, String> settings = new LinkedHashMap<>();
        String sql = "SELECT key, value FROM library_settings ORDER BY key";
        try (Connection c = databaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                settings.put(rs.getString("key"), rs.getString("value"));
            }
            return ApiResponse.ok(settings);
        } catch (SQLException e) {
            throw new com.librax.exception.DatabaseException("Failed to read settings", e);
        }
    }

    @PutMapping
    @RequiresPermission("SETTINGS_UPDATE")
    public ApiResponse<Void> updateSettings(@RequestBody Map<String, String> payload) {
        String sql = "INSERT OR REPLACE INTO library_settings (key, value) VALUES (?, ?)";
        try (Connection c = databaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (Map.Entry<String, String> entry : payload.entrySet()) {
                ps.setString(1, entry.getKey());
                ps.setString(2, entry.getValue());
                ps.executeUpdate();
            }
            Integer userId = SecurityContextHolder.getUser() != null ? SecurityContextHolder.getUser().getId() : null;
            auditLogger.log(userId, "SETTINGS_UPDATED", "SETTINGS", null, "Updated library settings: " + payload.keySet());
            return ApiResponse.ok(null, "Settings updated successfully");
        } catch (SQLException e) {
            throw new com.librax.exception.DatabaseException("Failed to save settings", e);
        }
    }

    @GetMapping("/backups")
    @RequiresPermission("BACKUP_CREATE")
    public ApiResponse<List<Map<String, Object>>> listBackups() {
        Path backupDir = databaseConfig.getDatabaseDirectory().resolve("backups");
        List<Map<String, Object>> list = new ArrayList<>();
        if (Files.exists(backupDir)) {
            File[] files = backupDir.toFile().listFiles((dir, name) -> name.endsWith(".db"));
            if (files != null) {
                Arrays.sort(files, Comparator.comparingLong((File f) -> f.lastModified()).reversed());
                for (File f : files) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("fileName", f.getName());
                    map.put("sizeBytes", f.length());
                    map.put("lastModified", new Date(f.lastModified()));
                    list.add(map);
                }
            }
        }
        return ApiResponse.ok(list);
    }

    @PostMapping({"/backups", "/backups/create"})
    @RequiresPermission("BACKUP_CREATE")
    public ApiResponse<Map<String, Object>> createBackup() {
        Path backupDir = databaseConfig.getDatabaseDirectory().resolve("backups");
        try {
            Files.createDirectories(backupDir);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = "librax_backup_" + timestamp + ".db";
            Path backupPath = backupDir.resolve(backupFileName);

            Path sourceDb = databaseConfig.getDatabaseFile();
            Files.copy(sourceDb, backupPath, StandardCopyOption.REPLACE_EXISTING);

            Integer userId = SecurityContextHolder.getUser() != null ? SecurityContextHolder.getUser().getId() : null;
            auditLogger.log(userId, "BACKUP_CREATED", "DATABASE", null, "Created database backup: " + backupFileName);

            Map<String, Object> resp = new HashMap<>();
            resp.put("fileName", backupFileName);
            resp.put("sizeBytes", Files.size(backupPath));
            resp.put("timestamp", LocalDateTime.now().toString());

            return ApiResponse.ok(resp, "Database backup created successfully");
        } catch (IOException e) {
            throw new com.librax.exception.DatabaseException("Failed to generate database backup", e);
        }
    }
}
