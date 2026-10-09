package com.librax.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librax.config.AppConfig;
import com.librax.config.DatabaseConfig;
import com.librax.config.DatabaseManager;
import com.librax.model.Role;
import com.librax.model.User;
import com.librax.web.HealthController;
import com.librax.web.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityAuditTest {

    private ObjectMapper objectMapper;
    private DatabaseManager databaseManager;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        AppConfig appConfig = AppConfig.getInstance();
        DatabaseConfig dbConfig = new DatabaseConfig(appConfig);
        databaseManager = new DatabaseManager(dbConfig);
        databaseManager.initializeSchema();
    }

    @Test
    void testPasswordHashOmittedFromSerialization() throws Exception {
        User user = new User();
        user.setId(99);
        user.setUsername("testuser");
        user.setPasswordHash("$2a$12$SecureBcryptHashStringExample1234567890");
        user.setRole(Role.STUDENT);
        user.setFullName("Test User");
        user.setEmail("test@librax.edu");

        String json = objectMapper.writeValueAsString(user);

        assertFalse(json.contains("$2a$12$SecureBcryptHashStringExample1234567890"),
                "Security violation: passwordHash must NOT be present in serialized JSON");
        assertFalse(json.contains("passwordHash"),
                "Security violation: passwordHash field name must not appear in JSON");
        assertTrue(json.contains("testuser"));
    }

    @Test
    void testJwtTokenProviderGeneratesAndValidates() {
        JwtTokenProvider provider = new JwtTokenProvider();

        User user = new User();
        user.setId(1);
        user.setUsername("admin");
        user.setRole(Role.ADMIN);
        user.setFullName("Administrator");
        user.setEmail("admin@librax.edu");

        String token = provider.generateToken(user);
        assertNotNull(token);
        assertTrue(token.length() > 20);

        assertTrue(provider.validateToken(token));
        assertEquals("admin", provider.getClaims(token).getSubject());
        assertFalse(provider.validateToken("invalid.jwt.token.string"));
    }

    @Test
    void testHealthEndpointReturnsOperationalStatus() {
        HealthController healthController = new HealthController(databaseManager);
        ApiResponse<Map<String, Object>> response = healthController.getHealth();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("UP", response.getData().get("status"));
        assertEquals("UP", response.getData().get("database"));
    }
}
