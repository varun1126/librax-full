package com.librax.repository.impl;

import com.librax.config.DatabaseManager;
import com.librax.exception.DatabaseException;
import com.librax.model.*;
import com.librax.repository.FineRepository;
import com.librax.util.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcFineRepository implements FineRepository {

    private final DatabaseManager db;

    public JdbcFineRepository(DatabaseManager db) {
        this.db = db;
    }

    private static final String SELECT_BASE = """
        SELECT f.*, m.full_name AS member_name FROM fines f
        JOIN members m ON m.id = f.member_id
    """;

    @Override
    public Fine save(Fine fine) {
        String sql = """
            INSERT INTO fines (loan_id, member_id, amount, amount_paid, status, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fine.setCreatedAt(fine.getCreatedAt() == null ? java.time.LocalDateTime.now() : fine.getCreatedAt());
            ps.setInt(1, fine.getLoanId());
            ps.setInt(2, fine.getMemberId());
            ps.setBigDecimal(3, fine.getAmount());
            ps.setBigDecimal(4, fine.getAmountPaid());
            ps.setString(5, fine.getStatus().name());
            ps.setString(6, DateUtils.format(fine.getCreatedAt()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) fine.setId(keys.getInt(1));
            }
            return fine;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save fine", e);
        }
    }

    @Override
    public Optional<Fine> findById(int id) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + " WHERE f.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find fine", e);
        }
    }

    @Override
    public List<Fine> findAll() {
        List<Fine> results = new ArrayList<>();
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE + " ORDER BY f.created_at DESC")) {
            while (rs.next()) results.add(mapRow(rs));
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list fines", e);
        }
    }

    @Override
    public List<Fine> findOutstandingByMember(int memberId) {
        String sql = SELECT_BASE + " WHERE f.member_id = ? AND f.status IN ('OUTSTANDING','PARTIALLY_PAID')";
        List<Fine> results = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find outstanding fines", e);
        }
    }

    @Override
    public void update(Fine fine) {
        String sql = "UPDATE fines SET amount = ?, amount_paid = ?, status = ? WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, fine.getAmount());
            ps.setBigDecimal(2, fine.getAmountPaid());
            ps.setString(3, fine.getStatus().name());
            ps.setInt(4, fine.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update fine", e);
        }
    }

    @Override
    public Payment savePayment(Payment payment) {
        String sql = "INSERT INTO payments (fine_id, amount, method, paid_at, recorded_by) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getFineId());
            ps.setBigDecimal(2, payment.getAmount());
            ps.setString(3, payment.getMethod().name());
            ps.setString(4, DateUtils.format(payment.getPaidAt()));
            ps.setInt(5, payment.getRecordedBy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) payment.setId(keys.getInt(1));
            }
            return payment;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save payment", e);
        }
    }

    @Override
    public List<Payment> findPaymentsByFine(int fineId) {
        List<Payment> results = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM payments WHERE fine_id = ? ORDER BY paid_at")) {
            ps.setInt(1, fineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Payment p = new Payment();
                    p.setId(rs.getInt("id"));
                    p.setFineId(rs.getInt("fine_id"));
                    p.setAmount(rs.getBigDecimal("amount"));
                    p.setMethod(PaymentMethod.valueOf(rs.getString("method")));
                    p.setPaidAt(DateUtils.parseDateTime(rs.getString("paid_at")));
                    p.setRecordedBy(rs.getInt("recorded_by"));
                    results.add(p);
                }
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list payments", e);
        }
    }

    private Fine mapRow(ResultSet rs) throws SQLException {
        Fine f = new Fine();
        f.setId(rs.getInt("id"));
        f.setLoanId(rs.getInt("loan_id"));
        f.setMemberId(rs.getInt("member_id"));
        f.setMemberName(rs.getString("member_name"));
        f.setAmount(rs.getBigDecimal("amount"));
        f.setAmountPaid(rs.getBigDecimal("amount_paid"));
        f.setStatus(FineStatus.valueOf(rs.getString("status")));
        f.setCreatedAt(DateUtils.parseDateTime(rs.getString("created_at")));
        return f;
    }
}
