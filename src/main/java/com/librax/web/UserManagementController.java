package com.librax.web;

import com.librax.model.Role;
import com.librax.model.User;
import com.librax.model.UserStatus;
import com.librax.repository.UserRepository;
import com.librax.security.PasswordHasher;
import com.librax.security.RequiresPermission;
import com.librax.web.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserManagementController(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @GetMapping
    @RequiresPermission("USER_VIEW")
    public ApiResponse<List<User>> getAllUsers() {
        return ApiResponse.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    @RequiresPermission("USER_VIEW")
    public ApiResponse<User> getUserById(@PathVariable("id") int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.librax.exception.ValidationException("User not found"));
        return ApiResponse.ok(user);
    }

    @PostMapping
    @RequiresPermission("USER_CREATE")
    public ApiResponse<User> createUser(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");
        String fullName = payload.get("fullName");
        String email = payload.get("email");
        String roleStr = payload.get("role");

        if (username == null || password == null || fullName == null || roleStr == null) {
            throw new com.librax.exception.ValidationException("username, password, fullName, and role are required");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new com.librax.exception.ValidationException("Username is already taken");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPasswordHash(passwordHasher.hash(password.toCharArray()));
        user.setFullName(fullName.trim());
        user.setEmail(email != null ? email.trim() : null);
        user.setRole(Role.valueOf(roleStr.toUpperCase()));
        user.setStatus(UserStatus.ACTIVE);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        return ApiResponse.ok(saved, "User created successfully");
    }

    @PutMapping("/{id}")
    @RequiresPermission("USER_UPDATE")
    public ApiResponse<User> updateUser(@PathVariable("id") int id, @RequestBody Map<String, String> payload) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.librax.exception.ValidationException("User not found"));

        if (payload.containsKey("fullName")) user.setFullName(payload.get("fullName"));
        if (payload.containsKey("email")) user.setEmail(payload.get("email"));
        if (payload.containsKey("role")) user.setRole(Role.valueOf(payload.get("role").toUpperCase()));
        if (payload.containsKey("status")) {
            UserStatus st = UserStatus.valueOf(payload.get("status").toUpperCase());
            user.setStatus(st);
            user.setActive(st == UserStatus.ACTIVE);
        }

        userRepository.update(user);
        return ApiResponse.ok(user, "User updated successfully");
    }

    @PostMapping("/{id}/status")
    @RequiresPermission("USER_DISABLE")
    public ApiResponse<Void> updateStatus(@PathVariable("id") int id, @RequestBody Map<String, String> payload) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.librax.exception.ValidationException("User not found"));
        String statusStr = payload.get("status");
        if (statusStr == null) throw new com.librax.exception.ValidationException("status is required");

        UserStatus status = UserStatus.valueOf(statusStr.toUpperCase());
        user.setStatus(status);
        user.setActive(status == UserStatus.ACTIVE);
        userRepository.update(user);

        return ApiResponse.ok(null, "User status updated to " + status);
    }

    @PostMapping("/{id}/reset-password")
    @RequiresPermission("USER_UPDATE")
    public ApiResponse<Void> resetPassword(@PathVariable("id") int id, @RequestBody Map<String, String> payload) {
        String newPassword = payload.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            throw new com.librax.exception.ValidationException("newPassword must be at least 6 characters");
        }
        String hash = passwordHasher.hash(newPassword.toCharArray());
        userRepository.updatePassword(id, hash);
        return ApiResponse.ok(null, "Password reset successfully");
    }
}
