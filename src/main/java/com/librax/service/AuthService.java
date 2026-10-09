package com.librax.service;

import com.librax.exception.AuthenticationException;
import com.librax.exception.ValidationException;
import com.librax.logging.AuditLogger;
import com.librax.model.*;
import com.librax.repository.MemberRepository;
import com.librax.repository.UserRepository;
import com.librax.security.JwtTokenProvider;
import com.librax.security.PasswordHasher;
import com.librax.web.dto.AuthResponse;
import com.librax.web.dto.LoginRequest;
import com.librax.web.dto.PasswordChangeRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import com.librax.web.dto.StudentRegistrationRequest;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordHasher passwordHasher;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditLogger auditLogger;

    public AuthService(UserRepository userRepository,
                       MemberRepository memberRepository,
                       PasswordHasher passwordHasher,
                       JwtTokenProvider jwtTokenProvider,
                       AuditLogger auditLogger) {
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.passwordHasher = passwordHasher;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditLogger = auditLogger;
    }

    public AuthResponse authenticate(LoginRequest request, HttpServletRequest httpRequest, boolean studentPortal) {
        String identifier = request.getIdentifier().trim();
        String ip = extractClientIp(httpRequest);
        String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : "Unknown";

        // 1. Try finding user by username or email
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(identifier);

        // If not found and student portal, try finding member by student ID / membership number
        Member member = null;
        if (userOpt.isEmpty()) {
            Optional<Member> memberOpt = memberRepository.findByMembershipNumber(identifier);
            if (memberOpt.isPresent() && memberOpt.get().getUserId() != null) {
                userOpt = userRepository.findById(memberOpt.get().getUserId());
                member = memberOpt.get();
            }
        }

        if (userOpt.isEmpty()) {
            userRepository.logLoginAttempt(null, identifier, ip, userAgent, "FAILED", "User not found");
            auditLogger.log(null, "LOGIN_FAILED", "USER", null, "Failed login attempt for unknown user: " + identifier);
            throw new AuthenticationException("Invalid username, student ID, or password.");
        }

        User user = userOpt.get();

        // 2. Check portal access rules
        if (studentPortal && user.getRole() != Role.STUDENT && user.getRole() != Role.SUPER_ADMIN) {
            // Staff logging into student portal: allowed, but note role
        } else if (!studentPortal && user.getRole() == Role.STUDENT) {
            userRepository.logLoginAttempt(user.getId(), identifier, ip, userAgent, "FAILED", "Student denied access to Admin portal");
            auditLogger.log(user.getId(), "LOGIN_FAILED", "USER", user.getId(), "Student attempted access to Admin Portal");
            throw new AuthenticationException("Students cannot log in through the Admin Portal. Please use the Student Portal.");
        }

        // 3. Check status & lockouts
        if (user.getStatus() == UserStatus.DISABLED) {
            userRepository.logLoginAttempt(user.getId(), identifier, ip, userAgent, "FAILED", "Account disabled");
            throw new AuthenticationException("This account has been disabled. Please contact the library administrator.");
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            userRepository.logLoginAttempt(user.getId(), identifier, ip, userAgent, "FAILED", "Account suspended");
            throw new AuthenticationException("This account is currently suspended.");
        }
        if (user.isAccountLocked()) {
            userRepository.logLoginAttempt(user.getId(), identifier, ip, userAgent, "FAILED", "Account locked");
            throw new AuthenticationException("Account is temporarily locked due to multiple failed attempts. Please try again after " + user.getLockedUntil());
        }

        // 4. Verify password
        boolean valid = passwordHasher.verify(request.getPassword().toCharArray(), user.getPasswordHash());
        if (!valid) {
            int newAttempts = user.getFailedLoginAttempts() + 1;
            if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES);
                userRepository.lockAccount(user.getId(), lockedUntil);
                userRepository.logLoginAttempt(user.getId(), identifier, ip, userAgent, "LOCKED", "Exceeded max failed attempts");
                auditLogger.log(user.getId(), "ACCOUNT_LOCKED", "USER", user.getId(), "Account locked for 15 minutes due to 5 failed login attempts");
                throw new AuthenticationException("Account has been locked for 15 minutes due to multiple failed login attempts.");
            } else {
                userRepository.updateFailedAttempts(user.getId(), newAttempts);
                userRepository.logLoginAttempt(user.getId(), identifier, ip, userAgent, "FAILED", "Incorrect password");
                auditLogger.log(user.getId(), "LOGIN_FAILED", "USER", user.getId(), "Failed login attempt (" + newAttempts + "/5)");
                throw new AuthenticationException("Invalid password. Attempt " + newAttempts + " of " + MAX_FAILED_ATTEMPTS + ".");
            }
        }

        // 5. Success
        LocalDateTime loginTime = LocalDateTime.now();
        userRepository.recordLoginSuccess(user.getId(), loginTime);
        user.setLastLogin(loginTime);
        user.setPermissions(userRepository.findPermissionsByUserId(user.getId()));

        userRepository.logLoginAttempt(user.getId(), identifier, ip, userAgent, "SUCCESS", "Authentication successful");
        auditLogger.log(user.getId(), "LOGIN_SUCCESS", "USER", user.getId(), "User logged in via " + (studentPortal ? "Student" : "Admin") + " Portal");

        // Load linked student member details if student
        if (member == null && (user.getRole() == Role.STUDENT || user.getRole() == Role.SUPER_ADMIN)) {
            member = memberRepository.findByEmail(user.getEmail()).orElse(null);
        }

        String token = jwtTokenProvider.generateToken(user);
        return new AuthResponse(token, user, member);
    }

    public AuthResponse registerStudent(StudentRegistrationRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail().trim().toLowerCase();
        String fullName = request.getFullName().trim();
        String rawUsername = request.getUsername();
        if (rawUsername == null || rawUsername.isBlank()) {
            rawUsername = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        }
        String username = rawUsername.trim().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new ValidationException("An account with email " + email + " already exists.");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            int counter = 1;
            String base = username;
            while (userRepository.findByUsername(username).isPresent()) {
                username = base + counter++;
            }
        }

        String membershipNumber = request.getMembershipNumber();
        if (membershipNumber == null || membershipNumber.isBlank()) {
            membershipNumber = "LIB-STU-" + (int)(1000 + Math.random() * 9000);
            while (memberRepository.findByMembershipNumber(membershipNumber).isPresent()) {
                membershipNumber = "LIB-STU-" + (int)(1000 + Math.random() * 9000);
            }
        } else {
            membershipNumber = membershipNumber.trim().toUpperCase();
            if (memberRepository.findByMembershipNumber(membershipNumber).isPresent()) {
                throw new ValidationException("A student with ID/Membership Number " + membershipNumber + " already exists.");
            }
        }

        String phone = request.getPhone();
        if (phone == null || phone.isBlank()) {
            phone = "+91 98765 00000";
        }

        // 1. Create and save User
        String passwordHash = passwordHasher.hash(request.getPassword().toCharArray());
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setRole(Role.STUDENT);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setStatus(UserStatus.ACTIVE);
        user.setActive(true);
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        // Populate permissions
        savedUser.setPermissions(userRepository.findPermissionsByUserId(savedUser.getId()));

        // 2. Create and save Member
        Member member = new Member();
        member.setUserId(savedUser.getId());
        member.setMembershipNumber(membershipNumber);
        member.setFullName(fullName);
        member.setEmail(email);
        member.setPhone(phone);
        member.setAddress("Campus Student");
        member.setDepartmentName(request.getDepartmentName() != null && !request.getDepartmentName().isBlank() ? request.getDepartmentName().trim() : "General");
        member.setCourseName(request.getCourseName() != null && !request.getCourseName().isBlank() ? request.getCourseName().trim() : "Undergraduate");
        member.setRegistrationDate(LocalDate.now());
        member.setMembershipExpiry(LocalDate.now().plusYears(1));
        member.setStatus(MemberStatus.ACTIVE);
        Member savedMember = memberRepository.save(member);

        // Audit & Log
        String ip = extractClientIp(httpRequest);
        String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : "Unknown";
        userRepository.logLoginAttempt(savedUser.getId(), username, ip, userAgent, "SUCCESS", "Student self-registration");
        auditLogger.log(savedUser.getId(), "STUDENT_REGISTERED", "USER", savedUser.getId(),
                "New student self-registered: " + fullName + " (" + membershipNumber + ")");

        String token = jwtTokenProvider.generateToken(savedUser);
        return new AuthResponse(token, savedUser, savedMember);
    }

    public void changePassword(User currentUser, PasswordChangeRequest req) {
        if (currentUser == null) {
            throw new AuthenticationException("User is not authenticated.");
        }
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ValidationException("User not found."));

        boolean valid = passwordHasher.verify(req.getCurrentPassword().toCharArray(), user.getPasswordHash());
        if (!valid) {
            throw new ValidationException("Current password is incorrect.");
        }

        String newHash = passwordHasher.hash(req.getNewPassword().toCharArray());
        userRepository.updatePassword(user.getId(), newHash);
        auditLogger.log(user.getId(), "PASSWORD_CHANGED", "USER", user.getId(), "User changed password");
        log.info("Password successfully updated for user {}", user.getUsername());
    }

    public void logout(User currentUser, HttpServletRequest httpRequest) {
        if (currentUser != null) {
            String ip = extractClientIp(httpRequest);
            auditLogger.log(currentUser.getId(), "LOGOUT", "USER", currentUser.getId(), "User logged out [IP: " + ip + "]");
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}
