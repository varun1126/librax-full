package com.librax.service;

import com.librax.exception.ValidationException;
import com.librax.logging.AuditLogger;
import com.librax.model.Reservation;
import com.librax.model.ReservationStatus;
import com.librax.repository.BookRepository;
import com.librax.repository.ReservationRepository;
import com.librax.security.SessionManager;

import java.time.LocalDate;
import java.util.List;

public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final AuditLogger auditLogger;
    private final SessionManager sessionManager;

    public ReservationService(ReservationRepository reservationRepository, BookRepository bookRepository,
                               AuditLogger auditLogger, SessionManager sessionManager) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
        this.auditLogger = auditLogger;
        this.sessionManager = sessionManager;
    }

    public Reservation reserve(int bookId, int memberId) {
        bookRepository.findById(bookId).orElseThrow(() -> new ValidationException("Book not found."));
        Reservation r = new Reservation();
        r.setBookId(bookId);
        r.setMemberId(memberId);
        r.setReservationDate(LocalDate.now());
        r.setStatus(ReservationStatus.WAITING);
        Reservation saved = reservationRepository.save(r);
        audit("RESERVATION_CREATED", saved.getId(), "Reservation created for book #" + bookId);
        return saved;
    }

    public void cancel(int reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ValidationException("Reservation not found."));
        r.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.update(r);
        audit("RESERVATION_CANCELLED", reservationId, "Reservation cancelled");
    }

    /** Promotes the oldest WAITING reservation for a book to READY when a copy becomes available. */
    public void notifyNextWaiting(int bookId) {
        List<Reservation> waiting = reservationRepository.findWaitingByBook(bookId);
        if (!waiting.isEmpty()) {
            Reservation next = waiting.get(0);
            next.setStatus(ReservationStatus.READY);
            reservationRepository.update(next);
            audit("RESERVATION_READY", next.getId(), "Reservation ready for pickup");
        }
    }

    public void markFulfilled(int reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ValidationException("Reservation not found."));
        r.setStatus(ReservationStatus.FULFILLED);
        reservationRepository.update(r);
        audit("RESERVATION_FULFILLED", reservationId, "Reservation fulfilled");
    }

    public java.util.Optional<Reservation> findById(int id) {
        return reservationRepository.findById(id);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    private void audit(String action, Integer entityId, String description) {
        Integer userId = sessionManager.getCurrentUser() != null ? sessionManager.getCurrentUser().getId() : null;
        auditLogger.log(userId, action, "RESERVATION", entityId, description);
    }
}
