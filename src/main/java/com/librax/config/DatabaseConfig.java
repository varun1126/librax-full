package com.librax.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves the on-disk location of the SQLite database file and the
 * JDBC URL used to connect to it.
 *
 * <p>The configured {@code library.database.path} (see
 * {@link AppConfig#getDatabasePath()}) is resolved relative to the
 * current user's home directory when it is not already absolute, so the
 * application "just works" per-user without extra setup, while still
 * being overridable for testing or deployment.</p>
 *
 * <p>Actual connection creation, schema initialization, and the
 * {@code PRAGMA foreign_keys = ON} enforcement live in the
 * {@code DatabaseManager} introduced in Phase 2 — this class is
 * intentionally limited to path/URL resolution so it has no JDBC
 * dependency footprint in Phase 1.</p>
 */
public final class DatabaseConfig {

    private final Path databaseFile;

    public DatabaseConfig(AppConfig appConfig) {
        this.databaseFile = resolve(appConfig.getDatabasePath());
    }

    private static Path resolve(String configuredPath) {
        Path path = Paths.get(configuredPath);
        if (path.isAbsolute()) {
            return path;
        }
        String userHome = System.getProperty("user.home", ".");
        return Paths.get(userHome, ".librax").resolve(path.getFileName() == null ? path : path);
    }

    /**
     * @return the absolute path to the SQLite database file.
     */
    public Path getDatabaseFile() {
        return databaseFile;
    }

    /**
     * @return the directory that must exist before the database file can
     *         be created.
     */
    public Path getDatabaseDirectory() {
        Path parent = databaseFile.getParent();
        return parent != null ? parent : Paths.get(".");
    }

    /**
     * @return the SQLite JDBC connection URL for the configured database file.
     */
    public String getJdbcUrl() {
        return "jdbc:sqlite:" + databaseFile.toAbsolutePath();
    }
}
