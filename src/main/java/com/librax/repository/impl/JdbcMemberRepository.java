package com.librax.repository.impl;

import com.librax.config.DatabaseManager;
import com.librax.exception.DatabaseException;
import com.librax.model.Member;
import com.librax.model.MemberStatus;
import com.librax.repository.MemberRepository;
import com.librax.util.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMemberRepository implements MemberRepository {

    private final DatabaseManager db;

    public JdbcMemberRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public Member save(Member m) {
        String sql = """
            INSERT INTO members (user_id, membership_number, full_name, email, phone, address,
                                  department_id, course_id, registration_date, membership_expiry, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (m.getUserId() != null) ps.setInt(1, m.getUserId());
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, m.getMembershipNumber());
            ps.setString(3, m.getFullName());
            ps.setString(4, m.getEmail());
            ps.setString(5, m.getPhone());
            ps.setString(6, m.getAddress());
            if (m.getDepartmentId() != null) ps.setInt(7, m.getDepartmentId());
            else ps.setNull(7, Types.INTEGER);
            if (m.getCourseId() != null) ps.setInt(8, m.getCourseId());
            else ps.setNull(8, Types.INTEGER);
            ps.setString(9, DateUtils.format(m.getRegistrationDate()));
            ps.setString(10, DateUtils.format(m.getMembershipExpiry()));
            ps.setString(11, m.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) m.setId(keys.getInt(1));
            }
            return m;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save member", e);
        }
    }

    private static final String BASE_SELECT = """
        SELECT m.*, d.name AS department_name, c.name AS course_name,
               (SELECT COUNT(*) FROM loans l WHERE l.member_id = m.id AND l.status IN ('ACTIVE', 'OVERDUE')) AS current_borrowed_count,
               (SELECT COALESCE(SUM(f.amount - f.amount_paid), 0.0) FROM fines f WHERE f.member_id = m.id AND f.status IN ('OUTSTANDING', 'PARTIALLY_PAID')) AS outstanding_fines
        FROM members m
        LEFT JOIN departments d ON d.id = m.department_id
        LEFT JOIN courses c ON c.id = m.course_id
    """;

    @Override
    public Optional<Member> findById(int id) {
        String sql = BASE_SELECT + " WHERE m.id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find member by id", e);
        }
    }

    @Override
    public Optional<Member> findByMembershipNumber(String membershipNumber) {
        String sql = BASE_SELECT + " WHERE m.membership_number = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, membershipNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find member by membership number", e);
        }
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        String sql = BASE_SELECT + " WHERE m.email = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find member by email", e);
        }
    }

    @Override
    public Optional<Member> findByUserId(int userId) {
        String sql = BASE_SELECT + " WHERE m.user_id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find member by user_id", e);
        }
    }

    @Override
    public List<Member> findAll() {
        String sql = BASE_SELECT + " ORDER BY m.full_name";
        List<Member> results = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) results.add(mapRow(rs));
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list members", e);
        }
    }

    @Override
    public List<Member> search(String query) {
        String sql = BASE_SELECT + """
            WHERE m.membership_number LIKE ? OR m.full_name LIKE ? OR m.email LIKE ? OR m.phone LIKE ?
            ORDER BY m.full_name
        """;
        String wildcard = "%" + query + "%";
        List<Member> results = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, wildcard);
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);
            ps.setString(4, wildcard);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search members", e);
        }
    }

    @Override
    public void update(Member m) {
        String sql = """
            UPDATE members SET user_id = ?, membership_number = ?, full_name = ?, email = ?,
                               phone = ?, address = ?, department_id = ?, course_id = ?,
                               registration_date = ?, membership_expiry = ?, status = ?
            WHERE id = ?
        """;
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (m.getUserId() != null) ps.setInt(1, m.getUserId());
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, m.getMembershipNumber());
            ps.setString(3, m.getFullName());
            ps.setString(4, m.getEmail());
            ps.setString(5, m.getPhone());
            ps.setString(6, m.getAddress());
            if (m.getDepartmentId() != null) ps.setInt(7, m.getDepartmentId());
            else ps.setNull(7, Types.INTEGER);
            if (m.getCourseId() != null) ps.setInt(8, m.getCourseId());
            else ps.setNull(8, Types.INTEGER);
            ps.setString(9, DateUtils.format(m.getRegistrationDate()));
            ps.setString(10, DateUtils.format(m.getMembershipExpiry()));
            ps.setString(11, m.getStatus().name());
            ps.setInt(12, m.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update member", e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM members WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete member", e);
        }
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setId(rs.getInt("id"));
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) m.setUserId(userId);
        m.setMembershipNumber(rs.getString("membership_number"));
        m.setFullName(rs.getString("full_name"));
        m.setEmail(rs.getString("email"));
        m.setPhone(rs.getString("phone"));
        m.setAddress(rs.getString("address"));
        try {
            int deptId = rs.getInt("department_id");
            if (!rs.wasNull()) m.setDepartmentId(deptId);
            m.setDepartmentName(rs.getString("department_name"));
            int courseId = rs.getInt("course_id");
            if (!rs.wasNull()) m.setCourseId(courseId);
            m.setCourseName(rs.getString("course_name"));
        } catch (Exception ignored) {}
        m.setRegistrationDate(DateUtils.parseDate(rs.getString("registration_date")));
        m.setMembershipExpiry(DateUtils.parseDate(rs.getString("membership_expiry")));
        m.setStatus(MemberStatus.valueOf(rs.getString("status")));
        try {
            m.setCurrentBorrowedCount(rs.getInt("current_borrowed_count"));
        } catch (Exception ignored) {}
        try {
            m.setOutstandingFines(rs.getDouble("outstanding_fines"));
        } catch (Exception ignored) {}
        return m;
    }
}
