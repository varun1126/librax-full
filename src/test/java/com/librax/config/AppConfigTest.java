package com.librax.config;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 1 smoke test: verifies {@link AppConfig} loads
 * {@code application.properties} from the classpath and exposes the
 * expected default values.
 */
class AppConfigTest {

    @Test
    void loadsDefaultLibraryName() {
        AppConfig config = AppConfig.getInstance();

        assertEquals("LibraX University Library", config.getLibraryName());
    }

    @Test
    void loadsDefaultLoanPolicy() {
        AppConfig config = AppConfig.getInstance();

        assertEquals(14, config.getLoanPeriodDays());
        assertEquals(5, config.getMaxBooksPerMember());
        assertEquals(new BigDecimal("5.00"), config.getFinePerDay());
        assertTrue(config.isFineBlockingIssue());
    }

    @Test
    void loadsDefaultApiConfig() {
        AppConfig appConfig = AppConfig.getInstance();
        ApiConfig apiConfig = new ApiConfig(appConfig);

        assertEquals("https://www.googleapis.com/books/v1/volumes", apiConfig.getBaseUrl());
        assertFalse(apiConfig.getApiKey().isPresent(), "no API key configured by default");
    }

    @Test
    void missingIntFallsBackToDefault() {
        AppConfig config = AppConfig.getInstance();

        assertEquals(42, config.getInt("does.not.exist", 42));
    }

    @Test
    void databaseConfigProducesSqliteJdbcUrl() {
        AppConfig appConfig = AppConfig.getInstance();
        DatabaseConfig databaseConfig = new DatabaseConfig(appConfig);

        assertTrue(databaseConfig.getJdbcUrl().startsWith("jdbc:sqlite:"));
    }
}
