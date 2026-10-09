package com.librax.web;

import com.librax.model.Member;
import com.librax.model.User;
import com.librax.repository.MemberRepository;
import com.librax.repository.UserRepository;
import com.librax.security.SecurityContextHolder;
import com.librax.service.AuthService;
import com.librax.web.dto.ApiResponse;
import com.librax.web.dto.AuthResponse;
import com.librax.web.dto.LoginRequest;
import com.librax.web.dto.PasswordChangeRequest;
import com.librax.web.dto.StudentRegistrationRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    public AuthController(AuthService authService, UserRepository userRepository, MemberRepository memberRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        AuthResponse response = authService.authenticate(request, httpRequest, false);
        return ApiResponse.ok(response, "Welcome back, " + response.getFullName());
    }

    @PostMapping("/student-login")
    public ApiResponse<AuthResponse> studentLogin(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        AuthResponse response = authService.authenticate(request, httpRequest, true);
        return ApiResponse.ok(response, "Welcome to the Student Portal, " + response.getFullName());
    }

    @PostMapping({"/register", "/student-register"})
    public ApiResponse<AuthResponse> register(@Valid @RequestBody StudentRegistrationRequest request, HttpServletRequest httpRequest) {
        AuthResponse response = authService.registerStudent(request, httpRequest);
        return ApiResponse.ok(response, "Registration successful! Welcome to LibraX, " + response.getFullName());
    }

    @GetMapping("/me")
    public ApiResponse<AuthResponse> getCurrentUser() {
        User user = SecurityContextHolder.getUser();
        if (user == null) {
            return ApiResponse.error("UNAUTHORIZED", "Not logged in");
        }
        User fullUser = userRepository.findById(user.getId()).orElse(user);
        Member member = memberRepository.findByEmail(fullUser.getEmail()).orElse(null);
        return ApiResponse.ok(new AuthResponse(null, fullUser, member));
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        User user = SecurityContextHolder.getUser();
        authService.changePassword(user, request);
        return ApiResponse.ok(null, "Password successfully changed.");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest httpRequest) {
        User user = SecurityContextHolder.getUser();
        authService.logout(user, httpRequest);
        return ApiResponse.ok(null, "Logged out successfully.");
    }
}
