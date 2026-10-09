package com.librax.web;

import com.librax.config.DatabaseManager;
import com.librax.model.Permission;
import com.librax.model.Role;
import com.librax.repository.UserRepository;
import com.librax.security.RequiresPermission;
import com.librax.web.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/api/rbac")
@SuppressWarnings("null")
public class RbacController {

    private final UserRepository userRepository;
    private final DatabaseManager databaseManager;

    public RbacController(UserRepository userRepository, DatabaseManager databaseManager) {
        this.userRepository = userRepository;
        this.databaseManager = databaseManager;
    }

    @GetMapping("/roles")
    public ApiResponse<List<String>> getRoles() {
        return ApiResponse.ok(Arrays.stream(Role.values()).map(r -> r.name()).toList());
    }

    @GetMapping("/permissions")
    public ApiResponse<List<Permission>> getPermissions() {
        return ApiResponse.ok(userRepository.findAllPermissions());
    }

    @GetMapping("/matrix")
    @RequiresPermission("USER_VIEW")
    public ApiResponse<Map<String, Object>> getPermissionMatrix() {
        List<Permission> allPerms = userRepository.findAllPermissions();
        Map<String, Set<String>> rolePermissions = new LinkedHashMap<>();

        // Role IDs: SUPER_ADMIN=1, ADMIN=2, LIBRARIAN=3, STUDENT=4
        rolePermissions.put("SUPER_ADMIN", userRepository.findPermissionsByRoleId(1));
        rolePermissions.put("ADMIN", userRepository.findPermissionsByRoleId(2));
        rolePermissions.put("LIBRARIAN", userRepository.findPermissionsByRoleId(3));
        rolePermissions.put("STUDENT", userRepository.findPermissionsByRoleId(4));

        Map<String, Object> result = new HashMap<>();
        result.put("permissions", allPerms);
        result.put("rolePermissions", rolePermissions);
        return ApiResponse.ok(result);
    }

    @PostMapping("/roles/{roleName}/permissions")
    @RequiresPermission("USER_UPDATE")
    public ApiResponse<Void> updateRolePermissions(@PathVariable("roleName") String roleName, @RequestBody List<String> permissionNames) {
        Role role = Role.valueOf(roleName.toUpperCase());
        int roleId = switch (role) {
            case SUPER_ADMIN -> 1;
            case ADMIN -> 2;
            case LIBRARIAN -> 3;
            case STUDENT -> 4;
        };

        if (roleId == 1) {
            throw new com.librax.exception.ValidationException("SUPER_ADMIN role permissions are immutable.");
        }

        try (Connection c = databaseManager.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement del = c.prepareStatement("DELETE FROM role_permissions WHERE role_id = ?");
                 PreparedStatement ins = c.prepareStatement("""
                     INSERT INTO role_permissions (role_id, permission_id)
                     SELECT ?, id FROM permissions WHERE name = ?
                 """)) {

                del.setInt(1, roleId);
                del.executeUpdate();

                for (String pName : permissionNames) {
                    ins.setInt(1, roleId);
                    ins.setString(2, pName);
                    ins.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
            return ApiResponse.ok(null, "Permissions updated for role " + roleName);
        } catch (SQLException e) {
            throw new com.librax.exception.DatabaseException("Failed to update role permissions", e);
        }
    }
}
