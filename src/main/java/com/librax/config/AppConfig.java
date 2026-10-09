package com.librax.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Properties;

/**
 * Centralized application configuration.
 *
 * <p>Loads {@code application.properties} from the classpath once at
 * startup and exposes strongly typed accessors for every configurable
 * value in the system (library policy, database location, API settings,
 * UI preferences). No other class in LibraX should read
 * {@code application.properties} directly or scatter magic numbers/
 * strings for these values — see {@code RULES.md} §5.</p>
 *
 * <p>This class is a deliberately narrow, justified Singleton: exactly
 * one immutable configuration should exist per running application
 * instance, and it has no mutable shared state beyond the values loaded
 * once at construction.</p>
 */
public final class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    private static final String CONFIG_FILE = "application.properties";

    private static volatile AppConfig instance;

    private final Properties properties;

    private AppConfig(Properties properties) {
        this.properties = properties;
    }

    /**
     * Returns the shared, lazily initialized {@code AppConfig} instance,
     * loading {@code application.properties} from the classpath on first
     * access.
     *
     * @return the application configuration
     * @throws IllegalStateException if the configuration file cannot be found or read
     */
    public static AppConfig getInstance() {
        AppConfig result = instance;
        if (result == null) {
            synchronized (AppConfig.class) {
                result = instance;
                if (result == null) {
                    instance = result = load();
                }
            }
        }
        return result;
    }

    private static AppConfig load() {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new IllegalStateException("Missing required classpath resource: " + CONFIG_FILE);
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
        }
        log.info("Application configuration loaded from {}", CONFIG_FILE);
        return new AppConfig(props);
    }

    // --- Library identity -------------------------------------------------

    public String getLibraryName() {
        return getString("library.name", "LibraX Library");
    }

    // --- Database -----------------------------------------------------------

    public String getDatabasePath() {
        return getString("library.database.path", "librax-data/librax.db");
    }

    // --- Loan policy ---------------------------------------------------------

    public int getLoanPeriodDays() {
        return getInt("library.loan.periodDays", 14);
    }

    public int getMaxBooksPerMember() {
        return getInt("library.loan.maxBooksPerMember", 5);
    }

    public BigDecimal getFinePerDay() {
        return getBigDecimal("library.fine.perDay", new BigDecimal("5.00"));
    }

    public boolean isFineBlockingIssue() {
        return getBoolean("library.fine.blocksIssueWhenUnpaid", true);
    }

    // --- UI -----------------------------------------------------------------

    public String getUiTheme() {
        return getString("ui.theme", "light");
    }

    public int getPaginationSize() {
        return getInt("ui.pagination.size", 25);
    }

    // --- Generic accessors ---------------------------------------------------

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String raw = properties.getProperty(key);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid integer for config key '{}': '{}', using default {}", key, raw, defaultValue);
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String raw = properties.getProperty(key);
        return raw == null || raw.isBlank() ? defaultValue : Boolean.parseBoolean(raw.trim());
    }

    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        String raw = properties.getProperty(key);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid decimal for config key '{}': '{}', using default {}", key, raw, defaultValue);
            return defaultValue;
        }
    }
}
