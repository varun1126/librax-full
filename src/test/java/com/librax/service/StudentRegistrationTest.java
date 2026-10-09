package com.librax.service;

import com.librax.config.AppConfig;
import com.librax.config.DatabaseConfig;
import com.librax.config.DatabaseManager;
import com.librax.logging.AuditLogger;
import com.librax.model.Member;
import com.librax.model.Role;
import com.librax.repository.AuditLogRepository;
import com.librax.repository.MemberRepository;
import com.librax.repository.UserRepository;
import com.librax.repository.impl.JdbcAuditLogRepository;
import com.librax.repository.impl.JdbcMemberRepository;
import com.librax.repository.impl.JdbcUserRepository;
import com.librax.security.JwtTokenProvider;
import com.librax.security.PasswordHasher;
import com.librax.web.dto.AuthResponse;
import com.librax.web.dto.StudentRegistrationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StudentRegistrationTest {

    private AuthService authService;
    private MemberRepository memberRepository;

    private String lastCreatedEmail;
    private DatabaseManager db;

    @BeforeEach
    void setUp() {
        AppConfig appConfig = AppConfig.getInstance();
        DatabaseConfig databaseConfig = new DatabaseConfig(appConfig);
        db = new DatabaseManager(databaseConfig);
        db.initializeSchema();

        UserRepository userRepository = new JdbcUserRepository(db);
        memberRepository = new JdbcMemberRepository(db);
        AuditLogRepository auditLogRepository = new JdbcAuditLogRepository(db);

        PasswordHasher passwordHasher = new PasswordHasher();
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
        AuditLogger auditLogger = new AuditLogger(auditLogRepository);

        authService = new AuthService(userRepository, memberRepository, passwordHasher, jwtTokenProvider, auditLogger);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (lastCreatedEmail != null && db != null) {
            try (java.sql.Connection c = db.getConnection()) {
                try (java.sql.PreparedStatement ps = c.prepareStatement("DELETE FROM members WHERE email = ?")) {
                    ps.setString(1, lastCreatedEmail);
                    ps.executeUpdate();
                }
                try (java.sql.PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE email = ?")) {
                    ps.setString(1, lastCreatedEmail);
                    ps.executeUpdate();
                }
            } catch (Exception ignored) {}
        }
    }

    @Test
    void testStudentRegistrationFlow() {
        String testEmail = "teststudent_" + System.currentTimeMillis() + "@campus.edu";
        lastCreatedEmail = testEmail;
        StudentRegistrationRequest req = new StudentRegistrationRequest();
        req.setFullName("Test Student " + System.currentTimeMillis());
        req.setEmail(testEmail);
        req.setPassword("Password@123");
        req.setDepartment("Computer Engineering");
        req.setPhone("+91 9988776655");

        AuthResponse res = authService.registerStudent(req, null);

        assertNotNull(res);
        assertNotNull(res.getToken());
        assertEquals(Role.STUDENT, res.getRole());
        assertEquals(testEmail, res.getEmail());

        Member member = res.getMember();
        assertNotNull(member);
        assertNotNull(member.getMembershipNumber());
        assertTrue(member.getMembershipNumber().startsWith("LIB-STU-"));
        assertEquals(testEmail, member.getEmail());

        // Verify member appears in memberRepository
        assertTrue(memberRepository.findByEmail(testEmail).isPresent());
    }
}
