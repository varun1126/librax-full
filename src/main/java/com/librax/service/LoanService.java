package com.librax.service;

import com.librax.config.AppConfig;
import com.librax.exception.BookNotAvailableException;
import com.librax.exception.ValidationException;
import com.librax.logging.AuditLogger;
import com.librax.model.*;
import com.librax.repository.BookRepository;
import com.librax.repository.LoanRepository;
import com.librax.repository.MemberRepository;
import com.librax.security.SessionManager;
import com.librax.util.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implements the 10-step issue flow and 8-step return flow from the
 * project brief. All eligibility checks happen here, not in the UI.
 */
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final FineService fineService;
    private final ReservationService reservationService;
    private final AuditLogger auditLogger;
    private final SessionManager sessionManager;
    private final AppConfig appConfig;

    public LoanService(LoanRepository loanRepository, BookRepository bookRepository,
                        MemberRepository memberRepository, MemberService memberService,
                        FineService fineService, ReservationService reservationService,
                        AuditLogger auditLogger, SessionManager sessionManager, AppConfig appConfig) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.memberService = memberService;
        this.fineService = fineService;
        this.reservationService = reservationService;
        this.auditLogger = auditLogger;
        this.sessionManager = sessionManager;
        this.appConfig = appConfig;
    }

    public Loan issueBook(int memberId, int bookId) {
        // 1-2. Select member/book (caller supplies ids)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ValidationException("Member not found."));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ValidationException("Book not found."));

        // 4. Check member status
        if (!memberService.isEligibleToBorrow(member)) {
            throw new ValidationException("Member is not eligible to borrow (inactive or expired membership).");
        }
        // 5. Check borrowing limit
        if (loanRepository.countActiveByMember(memberId) >= appConfig.getMaxBooksPerMember()) {
            throw new ValidationException("Member has reached the maximum number of books allowed ("
                    + appConfig.getMaxBooksPerMember() + ").");
        }
        // 6. Check unpaid fines if blocking
        if (fineService.hasBlockingUnpaidFines(memberId)) {
            throw new ValidationException("Member has outstanding unpaid fines that must be settled first.");
        }
        // 3. Check availability
        BookCopy copy = bookRepository.findAvailableCopy(bookId)
                .orElseThrow(() -> new BookNotAvailableException("No available copies of '" + book.getTitle() + "'."));

        // 7. Create loan
        Loan loan = new Loan();
        loan.setBookCopyId(copy.getId());
        loan.setMemberId(memberId);
        loan.setIssueDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(appConfig.getLoanPeriodDays()));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setFineAmount(BigDecimal.ZERO);
        Loan saved = loanRepository.save(loan);

        // 8. Reduce available copies
        bookRepository.updateCopyStatus(copy.getId(), CopyStatus.ISSUED);
        bookRepository.adjustAvailableCopies(bookId, -1);

        // 9-10. Transaction record + audit log
        audit("BOOK_ISSUED", saved.getId(), "Issued '" + book.getTitle() + "' to " + member.getFullName());
        return saved;
    }

    public Loan returnBook(int loanId) {
        // 1. Find active loan
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ValidationException("Loan not found."));
        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new ValidationException("This loan has already been returned.");
        }

        // 2. Record return date
        LocalDate returnDate = LocalDate.now();
        loan.setReturnDate(returnDate);

        // 3. Calculate overdue days + fine
        long overdueDays = DateUtils.daysBetween(loan.getDueDate(), returnDate);
        BigDecimal fine = fineService.calculateFineAmount(overdueDays);
        loan.setFineAmount(fine);

        // 5. Update loan status
        loan.setStatus(LoanStatus.RETURNED);
        loanRepository.update(loan);

        if (fine.signum() > 0) {
            fineService.recordFineForLoan(loan.getId(), loan.getMemberId(), fine);
        }

        // 6. Increase available copies
        Optional<Book> book = findBookForCopy(loan.getBookCopyId());
        bookRepository.updateCopyStatus(loan.getBookCopyId(), CopyStatus.AVAILABLE);
        book.ifPresent(b -> bookRepository.adjustAvailableCopies(b.getId(), 1));

        // Notify next waiting reservation, if any
        book.ifPresent(b -> reservationService.notifyNextWaiting(b.getId()));

        // 7-8. Transaction + audit log
        audit("BOOK_RETURNED", loan.getId(), "Returned loan #" + loan.getId()
                + (fine.signum() > 0 ? " with fine " + fine : ""));
        return loan;
    }

    public Loan renewLoan(int loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ValidationException("Loan not found."));
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new ValidationException("Only active loans can be renewed.");
        }
        if (loan.getDueDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Cannot renew an overdue loan. Please settle outstanding fines and return the book.");
        }
        loan.setDueDate(loan.getDueDate().plusDays(appConfig.getLoanPeriodDays()));
        loanRepository.update(loan);
        audit("LOAN_RENEWED", loan.getId(), "Renewed loan #" + loan.getId() + " new due date: " + loan.getDueDate());
        return loan;
    }

    private Optional<Book> findBookForCopy(int copyId) {
        // Simple approach: scan all books' copies. Adequate for a
        // desktop-scale catalog; could be optimized with a direct query
        // if the catalog grows very large.
        for (Book b : bookRepository.findAll()) {
            for (BookCopy c : bookRepository.findCopiesByBook(b.getId())) {
                if (c.getId() == copyId) {
                    return Optional.of(b);
                }
            }
        }
        return Optional.empty();
    }

    public List<Loan> findActiveByMember(int memberId) {
        return loanRepository.findActiveByMember(memberId);
    }

    public List<Loan> findAllByMember(int memberId) {
        return loanRepository.findAllByMember(memberId);
    }

    public List<Loan> findAll() {
        return loanRepository.findAll();
    }

    public Optional<Loan> findById(int id) {
        return loanRepository.findById(id);
    }

    public List<Loan> findOverdue() {
        return loanRepository.findOverdue();
    }

    private void audit(String action, Integer entityId, String description) {
        Integer userId = sessionManager.getCurrentUser() != null ? sessionManager.getCurrentUser().getId() : null;
        auditLogger.log(userId, action, "LOAN", entityId, description);
    }
}
