package com.librax.web;

import com.librax.config.DatabaseManager;
import com.librax.logging.AuditLogger;
import com.librax.model.Loan;
import com.librax.model.LoanStatus;
import com.librax.model.Member;
import com.librax.model.MemberStatus;
import com.librax.security.RequiresPermission;
import com.librax.service.LoanService;
import com.librax.service.MemberService;
import com.librax.web.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final MemberService memberService;
    private final LoanService loanService;
    private final DatabaseManager databaseManager;
    private final AuditLogger auditLogger;

    public StudentController(MemberService memberService,
                             LoanService loanService,
                             DatabaseManager databaseManager,
                             AuditLogger auditLogger) {
        this.memberService = memberService;
        this.loanService = loanService;
        this.databaseManager = databaseManager;
        this.auditLogger = auditLogger;
    }

    @GetMapping
    @RequiresPermission("STUDENT_VIEW")
    public ApiResponse<Map<String, Object>> getStudents(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {

        List<Member> all = memberService.search(search);
        int total = all.size();
        int fromIndex = Math.min((page - 1) * limit, total);
        int toIndex = Math.min(fromIndex + limit, total);
        List<Member> paged = all.subList(fromIndex, toIndex);

        Map<String, Object> response = new HashMap<>();
        response.put("items", paged);
        response.put("students", paged);
        response.put("total", total);
        response.put("page", page);
        response.put("limit", limit);
        response.put("totalPages", (int) Math.ceil((double) total / limit));

        return ApiResponse.ok(response);
    }

    @GetMapping("/{id}")
    @RequiresPermission("STUDENT_VIEW")
    public ApiResponse<Member> getStudent(@PathVariable("id") int id) {
        Member member = memberService.findById(id)
                .orElseThrow(() -> new com.librax.exception.ValidationException("Student record not found"));
        return ApiResponse.ok(member);
    }

    @PostMapping
    @RequiresPermission("STUDENT_CREATE")
    public ApiResponse<Member> createStudent(@RequestBody Member member) {
        if (member.getFullName() == null || member.getFullName().isBlank()) {
            if (member.getName() != null && !member.getName().isBlank()) {
                member.setFullName(member.getName().trim());
            }
        }
        if (member.getMembershipNumber() == null || member.getMembershipNumber().isBlank()) {
            String memNo;
            boolean exists;
            do {
                memNo = "LIB-STU-" + (int) (1000 + Math.random() * 9000);
                String checkNo = memNo;
                exists = memberService.search(checkNo).stream().anyMatch(m -> checkNo.equalsIgnoreCase(m.getMembershipNumber()));
            } while (exists);
            member.setMembershipNumber(memNo);
        }
        if (member.getRegistrationDate() == null) {
            member.setRegistrationDate(LocalDate.now());
        }
        if (member.getMembershipExpiry() == null) {
            member.setMembershipExpiry(LocalDate.now().plusYears(1));
        }
        if (member.getStatus() == null) {
            member.setStatus(MemberStatus.ACTIVE);
        }

        // Resolve or create department if name provided
        if (member.getDepartmentName() != null && !member.getDepartmentName().isBlank()) {
            int deptId = resolveOrCreateDepartment(member.getDepartmentName().trim());
            member.setDepartmentId(deptId);
        }

        // Create linked student login user account if needed
        if (member.getUserId() == null) {
            int userId = createStudentUserAccount(member);
            if (userId > 0) {
                member.setUserId(userId);
            }
        }

        Member saved = memberService.createMember(member);
        return ApiResponse.ok(saved, "Student membership created");
    }

    @PutMapping("/{id}")
    @RequiresPermission("STUDENT_UPDATE")
    public ApiResponse<Member> updateStudent(@PathVariable("id") int id, @RequestBody Member member) {
        Member existing = memberService.findById(id)
                .orElseThrow(() -> new com.librax.exception.ValidationException("Student record not found"));

        if (member.getFullName() != null && !member.getFullName().isBlank()) {
            existing.setFullName(member.getFullName().trim());
        }
        if (member.getEmail() != null && !member.getEmail().isBlank()) {
            existing.setEmail(member.getEmail().trim());
        }
        if (member.getPhone() != null && !member.getPhone().isBlank()) {
            existing.setPhone(member.getPhone().trim());
        }
        if (member.getAddress() != null) {
            existing.setAddress(member.getAddress().trim());
        }
        if (member.getStatus() != null) {
            existing.setStatus(member.getStatus());
        }
        if (member.getMaxBooksAllowed() > 0) {
            existing.setMaxBooksAllowed(member.getMaxBooksAllowed());
        }
        if (member.getDepartmentName() != null && !member.getDepartmentName().isBlank()) {
            int deptId = resolveOrCreateDepartment(member.getDepartmentName().trim());
            existing.setDepartmentId(deptId);
            existing.setDepartmentName(member.getDepartmentName().trim());
        }

        memberService.updateMember(existing);

        if (existing.getUserId() != null) {
            try (Connection c = databaseManager.getConnection();
                 PreparedStatement ps = c.prepareStatement("UPDATE users SET full_name = ?, email = ? WHERE id = ?")) {
                ps.setString(1, existing.getFullName());
                ps.setString(2, existing.getEmail());
                ps.setInt(3, existing.getUserId());
                ps.executeUpdate();
            } catch (SQLException ignored) {}
        }

        return ApiResponse.ok(memberService.findById(id).orElse(existing), "Student profile updated");
    }

    @RequestMapping(value = {"/{id}/status", "/{id}/suspend"}, method = {RequestMethod.PATCH, RequestMethod.POST, RequestMethod.PUT})
    @RequiresPermission("STUDENT_SUSPEND")
    public ApiResponse<Member> updateStudentStatus(@PathVariable("id") int id, @RequestBody(required = false) Map<String, String> body) {
        Member m = memberService.findById(id)
                .orElseThrow(() -> new com.librax.exception.ValidationException("Student not found"));

        String statusStr = body != null ? body.get("status") : null;
        if (statusStr != null && !statusStr.isBlank()) {
            m.setStatus(MemberStatus.valueOf(statusStr.trim().toUpperCase()));
        } else {
            m.setStatus(m.getStatus() == MemberStatus.ACTIVE ? MemberStatus.SUSPENDED : MemberStatus.ACTIVE);
        }
        memberService.updateMember(m);

        if (m.getUserId() != null) {
            try (Connection c = databaseManager.getConnection();
                 PreparedStatement ps = c.prepareStatement("UPDATE users SET status = ?, is_active = ? WHERE id = ?")) {
                ps.setString(1, m.getStatus() == MemberStatus.ACTIVE ? "ACTIVE" : "SUSPENDED");
                ps.setInt(2, m.getStatus() == MemberStatus.ACTIVE ? 1 : 0);
                ps.setInt(3, m.getUserId());
                ps.executeUpdate();
            } catch (SQLException ignored) {}
        }

        return ApiResponse.ok(m, "Student membership status updated to " + m.getStatus());
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("STUDENT_UPDATE")
    public ApiResponse<Void> deleteStudent(@PathVariable("id") int id) {
        Member member = memberService.findById(id)
                .orElseThrow(() -> new com.librax.exception.ValidationException("Student record not found"));

        List<Loan> activeLoans = loanService.findAllByMember(id).stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.OVERDUE)
                .toList();

        if (!activeLoans.isEmpty()) {
            throw new com.librax.exception.ValidationException(
                    "Cannot delete student '" + member.getFullName() + "': student currently has "
                            + activeLoans.size() + " active borrowed book(s). Please return all books before deleting.");
        }

        try (Connection c = databaseManager.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM payments WHERE fine_id IN (SELECT id FROM fines WHERE member_id = ?)")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM fines WHERE member_id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM loans WHERE member_id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM reservations WHERE member_id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM members WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            if (member.getUserId() != null) {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM user_permissions WHERE user_id = ?")) {
                    ps.setInt(1, member.getUserId());
                    ps.executeUpdate();
                } catch (Exception ignored) {}
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM login_attempts WHERE user_id = ?")) {
                    ps.setInt(1, member.getUserId());
                    ps.executeUpdate();
                } catch (Exception ignored) {}
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM notifications WHERE user_id = ?")) {
                    ps.setInt(1, member.getUserId());
                    ps.executeUpdate();
                } catch (Exception ignored) {}
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id = ?")) {
                    ps.setInt(1, member.getUserId());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new com.librax.exception.DatabaseException("Failed to delete member and associated records", e);
        }

        auditLogger.log(null, "MEMBER_DELETED", "MEMBER", id, "Permanently deleted student member: " + member.getFullName());
        return ApiResponse.ok(null, "Student membership permanently deleted");
    }

    private int resolveOrCreateDepartment(String deptName) {
        try (Connection c = databaseManager.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM departments WHERE LOWER(name) = LOWER(?)")) {
                ps.setString(1, deptName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
            String code = deptName.replaceAll("[^a-zA-Z]", "").toUpperCase();
            if (code.length() > 6) code = code.substring(0, 6);
            if (code.isBlank()) code = "DEPT" + (int) (100 + Math.random() * 900);
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO departments (name, code) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, deptName);
                ps.setString(2, code);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException ignored) {}
        return 1;
    }

    private int createStudentUserAccount(Member member) {
        String email = member.getEmail() != null ? member.getEmail().trim().toLowerCase() : "";
        String username = member.getMembershipNumber() != null ? member.getMembershipNumber().toLowerCase() : "";
        if (username.isBlank() && !email.isBlank()) {
            username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        }
        String password = member.getInitialPassword() != null && !member.getInitialPassword().isBlank()
                ? member.getInitialPassword() : "Student@123";

        String hash = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults().hashToString(12, password.toCharArray());
        try (Connection c = databaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 INSERT INTO users (username, password_hash, role_id, full_name, email, status, is_active, created_at)
                 VALUES (?, ?, 4, ?, ?, 'ACTIVE', 1, ?)
             """, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, member.getFullName());
            ps.setString(4, member.getEmail());
            ps.setString(5, java.time.LocalDateTime.now().toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ignored) {}
        return 0;
    }
}
