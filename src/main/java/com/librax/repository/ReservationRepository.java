package com.librax.repository;

import com.librax.model.Reservation;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(int id);
    List<Reservation> findAll();
    List<Reservation> findWaitingByBook(int bookId);
    void update(Reservation reservation);
}
