package com.librax.service;

import com.librax.model.Book;
import com.librax.model.Loan;
import com.librax.model.LoanStatus;
import com.librax.repository.BookRepository;
import com.librax.repository.FineRepository;
import com.librax.repository.LoanRepository;
import com.librax.repository.MemberRepository;
import com.librax.repository.ReservationRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Read-only aggregate queries backing the dashboard KPIs and charts.
 * Kept separate from the transactional services so dashboard queries
 * never accidentally mutate state.
 */
@SuppressWarnings("null")
public class AnalyticsService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;
    private final FineRepository fineRepository;

    public AnalyticsService(BookRepository bookRepository, MemberRepository memberRepository,
                             LoanRepository loanRepository, ReservationRepository reservationRepository,
                             FineRepository fineRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
        this.reservationRepository = reservationRepository;
        this.fineRepository = fineRepository;
    }

    public int totalBooks() {
        return bookRepository.findAll().size();
    }

    public int totalAvailableCopies() {
        return bookRepository.findAll().stream().mapToInt(b -> b.getAvailableCopies()).sum();
    }

    public long issuedBooksCount() {
        return loanRepository.findAll().stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.OVERDUE)
                .count();
    }

    public long overdueBooksCount() {
        return loanRepository.findOverdue().size();
    }

    public int totalMembers() {
        return memberRepository.findAll().size();
    }

    public long activeReservationsCount() {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == com.librax.model.ReservationStatus.WAITING
                        || r.getStatus() == com.librax.model.ReservationStatus.READY)
                .count();
    }

    public BigDecimal outstandingFinesTotal() {
        return fineRepository.findAll().stream()
                .map(f -> f.getOutstanding())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
    }

    public long todaysTransactionsCount() {
        LocalDate today = LocalDate.now();
        List<Loan> loans = loanRepository.findAll();
        return loans.stream().filter(l ->
                today.equals(l.getIssueDate()) || today.equals(l.getReturnDate())
        ).count();
    }

    /** Book count grouped by category name, for the "Books by Category" chart. */
    public Map<String, Long> booksByCategory() {
        return bookRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        b -> b.getCategoryName() == null ? "Uncategorized" : b.getCategoryName(),
                        LinkedHashMap::new, Collectors.counting()));
    }

    /** Top borrowed books by number of loans, for the "Most Borrowed Books" chart. */
    public Map<String, Long> mostBorrowedBooks(int limit) {
        Map<String, Long> counts = loanRepository.findAll().stream()
                .collect(Collectors.groupingBy(l -> l.getBookTitle(), Collectors.counting()));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                        (a, b) -> a, LinkedHashMap::new));
    }

    /** Monthly issue counts for the last N months, for the issue/return trend chart. */
    public Map<String, Long> monthlyIssueStats(int months) {
        Map<String, Long> result = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        List<Loan> loans = loanRepository.findAll();
        for (int i = months - 1; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String key = month.getMonth().toString().substring(0, 3) + " " + month.getYear();
            long count = loans.stream()
                    .filter(l -> l.getIssueDate() != null
                            && l.getIssueDate().getMonth() == month.getMonth()
                            && l.getIssueDate().getYear() == month.getYear())
                    .count();
            result.put(key, count);
        }
        return result;
    }
}
