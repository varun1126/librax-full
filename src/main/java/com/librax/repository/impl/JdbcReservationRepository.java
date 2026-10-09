package com.librax.repository.impl;

import com.librax.config.DatabaseManager;
import com.librax.exception.DatabaseException;
import com.librax.model.Reservation;
import com.librax.model.ReservationStatus;
import com.librax.repository.ReservationRepository;
import com.librax.util.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcReservationRepository implements ReservationRepository {

    private final DatabaseManager db;

    public JdbcReservationRepository(DatabaseManager db) {
        this.db = db;
    }

    private static final String SELECT_BASE = """
        SELECT r.*, b.title AS book_title, m.full_name AS member_name
        FROM reservations r
        JOIN books b ON b.id = r.book_id
        JOIN members m ON m.id = r.member_id
    """;

    @Override
    public Reservation save(Reservation r) {
        String sql = "INSERT INTO reservations (book_id, member_id, reservation_date, status) VALUES (?, ?, ?, ?)";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getBookId());
            ps.setInt(2, r.getMemberId());
            ps.setString(3, DateUtils.format(r.getReservationDate()));
            ps.setString(4, r.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setId(keys.getInt(1));
            }
            return r;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save reservation", e);
        }
    }

    @Override
    public Optional<Reservation> findById(int id) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + " WHERE r.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find reservation", e);
        }
    }

    @Override
    public List<Reservation> findAll() {
        List<Reservation> results = new ArrayList<>();
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE + " ORDER BY r.reservation_date")) {
            while (rs.next()) results.add(mapRow(rs));
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list reservations", e);
        }
    }

    @Override
    public List<Reservation> findWaitingByBook(int bookId) {
        String sql = SELECT_BASE + " WHERE r.book_id = ? AND r.status = 'WAITING' ORDER BY r.reservation_date";
        List<Reservation> results = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find waiting reservations", e);
        }
    }

    @Override
    public void update(Reservation r) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE reservations SET status = ? WHERE id = ?")) {
            ps.setString(1, r.getStatus().name());
            ps.setInt(2, r.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update reservation", e);
        }
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setBookId(rs.getInt("book_id"));
        r.setMemberId(rs.getInt("member_id"));
        r.setBookTitle(rs.getString("book_title"));
        r.setMemberName(rs.getString("member_name"));
        r.setReservationDate(DateUtils.parseDate(rs.getString("reservation_date")));
        r.setStatus(ReservationStatus.valueOf(rs.getString("status")));
        return r;
    }
}
