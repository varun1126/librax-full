package com.librax.repository.impl;

import com.librax.config.DatabaseManager;
import com.librax.exception.DatabaseException;
import com.librax.model.Loan;
import com.librax.model.LoanStatus;
import com.librax.repository.LoanRepository;
import com.librax.util.DateUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcLoanRepository implements LoanRepository {

    private final DatabaseManager db;

    public JdbcLoanRepository(DatabaseManager db) {
        this.db = db;
    }

    private static final String SELECT_BASE = """
        SELECT l.*, b.title AS book_title, m.full_name AS member_name
        FROM loans l
        JOIN book_copies bc ON bc.id = l.book_copy_id
        JOIN books b ON b.id = bc.book_id
        JOIN members m ON m.id = l.member_id
    """;

    @Override
    public Loan save(Loan loan) {
        String sql = """
            INSERT INTO loans (book_copy_id, member_id, issue_date, due_date, return_date, status, fine_amount)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, loan.getBookCopyId());
            ps.setInt(2, loan.getMemberId());
            ps.setString(3, DateUtils.format(loan.getIssueDate()));
            ps.setString(4, DateUtils.format(loan.getDueDate()));
            ps.setString(5, DateUtils.format(loan.getReturnDate()));
            ps.setString(6, loan.getStatus().name());
            ps.setBigDecimal(7, loan.getFineAmount() == null ? BigDecimal.ZERO : loan.getFineAmount());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) loan.setId(keys.getInt(1));
            }
            return loan;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save loan", e);
        }
    }

    @Override
    public Optional<Loan> findById(int id) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + " WHERE l.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find loan by id", e);
        }
    }

    @Override
    public List<Loan> findActiveByMember(int memberId) {
        String sql = SELECT_BASE + " WHERE l.member_id = ? AND l.status IN ('ACTIVE','OVERDUE') ORDER BY l.due_date";
        return queryByMember(sql, memberId);
    }

    @Override
    public List<Loan> findAllByMember(int memberId) {
        String sql = SELECT_BASE + " WHERE l.member_id = ? ORDER BY l.issue_date DESC";
        return queryByMember(sql, memberId);
    }

    private List<Loan> queryByMember(String sql, int memberId) {
        List<Loan> results = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query loans by member", e);
        }
    }

    @Override
    public List<Loan> findAll() {
        List<Loan> results = new ArrayList<>();
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE + " ORDER BY l.issue_date DESC")) {
            while (rs.next()) results.add(mapRow(rs));
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list loans", e);
        }
    }

    @Override
    public List<Loan> findOverdue() {
        String sql = SELECT_BASE + " WHERE l.status IN ('ACTIVE','OVERDUE') AND l.due_date < ? ORDER BY l.due_date";
        List<Loan> results = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, DateUtils.format(java.time.LocalDate.now()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find overdue loans", e);
        }
    }

    @Override
    public void update(Loan loan) {
        String sql = "UPDATE loans SET return_date = ?, status = ?, fine_amount = ? WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, DateUtils.format(loan.getReturnDate()));
            ps.setString(2, loan.getStatus().name());
            ps.setBigDecimal(3, loan.getFineAmount() == null ? BigDecimal.ZERO : loan.getFineAmount());
            ps.setInt(4, loan.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update loan", e);
        }
    }

    @Override
    public long countActiveByMember(int memberId) {
        String sql = "SELECT COUNT(*) FROM loans WHERE member_id = ? AND status IN ('ACTIVE','OVERDUE')";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count active loans", e);
        }
    }

    private Loan mapRow(ResultSet rs) throws SQLException {
        Loan loan = new Loan();
        loan.setId(rs.getInt("id"));
        loan.setBookCopyId(rs.getInt("book_copy_id"));
        loan.setMemberId(rs.getInt("member_id"));
        loan.setBookTitle(rs.getString("book_title"));
        loan.setMemberName(rs.getString("member_name"));
        loan.setIssueDate(DateUtils.parseDate(rs.getString("issue_date")));
        loan.setDueDate(DateUtils.parseDate(rs.getString("due_date")));
        loan.setReturnDate(DateUtils.parseDate(rs.getString("return_date")));
        loan.setStatus(LoanStatus.valueOf(rs.getString("status")));
        loan.setFineAmount(rs.getBigDecimal("fine_amount"));
        return loan;
    }
}
