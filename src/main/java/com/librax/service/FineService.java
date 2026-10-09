package com.librax.service;

import com.librax.config.AppConfig;
import com.librax.exception.ValidationException;
import com.librax.logging.AuditLogger;
import com.librax.model.*;
import com.librax.repository.FineRepository;
import com.librax.security.SessionManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class FineService {

    private final FineRepository fineRepository;
    private final AuditLogger auditLogger;
    private final SessionManager sessionManager;
    private final AppConfig appConfig;
    private final FineCalculationStrategy strategy = new DailyFlatRateStrategy();

    public FineService(FineRepository fineRepository, AuditLogger auditLogger,
                        SessionManager sessionManager, AppConfig appConfig) {
        this.fineRepository = fineRepository;
        this.auditLogger = auditLogger;
        this.sessionManager = sessionManager;
        this.appConfig = appConfig;
    }

    public BigDecimal calculateFineAmount(long overdueDays) {
        return strategy.calculate(overdueDays, appConfig.getFinePerDay());
    }

    /** Creates a Fine record for a loan if the calculated amount is greater than zero. */
    public Fine recordFineForLoan(int loanId, int memberId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return null;
        }
        Fine fine = new Fine();
        fine.setLoanId(loanId);
        fine.setMemberId(memberId);
        fine.setAmount(amount);
        fine.setAmountPaid(BigDecimal.ZERO);
        fine.setStatus(FineStatus.OUTSTANDING);
        fine.setCreatedAt(LocalDateTime.now());
        Fine saved = fineRepository.save(fine);
        audit("FINE_CREATED", saved.getId(), "Overdue fine of " + amount + " recorded");
        return saved;
    }

    public void adjustFine(int fineId, BigDecimal newAmount) {
        Fine fine = fineRepository.findById(fineId).orElseThrow(() -> new ValidationException("Fine not found."));
        if (newAmount.compareTo(fine.getAmountPaid()) < 0) {
            throw new ValidationException("Adjusted amount cannot be less than what has already been paid.");
        }
        fine.setAmount(newAmount);
        fine.setStatus(newAmount.compareTo(fine.getAmountPaid()) == 0 ? FineStatus.PAID : FineStatus.PARTIALLY_PAID);
        fineRepository.update(fine);
        audit("FINE_ADJUSTED", fineId, "Fine manually adjusted to " + newAmount);
    }

    public Payment recordPayment(int fineId, BigDecimal amount, PaymentMethod method) {
        if (amount == null || amount.signum() <= 0) {
            throw new ValidationException("Payment amount must be greater than zero.");
        }
        Fine fine = fineRepository.findById(fineId).orElseThrow(() -> new ValidationException("Fine not found."));
        BigDecimal outstanding = fine.getOutstanding();
        if (amount.compareTo(outstanding) > 0) {
            throw new ValidationException("Payment exceeds the outstanding balance of " + outstanding + ".");
        }
        Payment payment = new Payment();
        payment.setFineId(fineId);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setPaidAt(LocalDateTime.now());
        payment.setStudentId(fine.getMemberId());
        payment.setReference("PAY-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 900 + 100));

        Integer recordedBy = null;
        if (sessionManager.getCurrentUser() != null) {
            recordedBy = sessionManager.getCurrentUser().getId();
        } else if (com.librax.security.SecurityContextHolder.getUser() != null) {
            recordedBy = com.librax.security.SecurityContextHolder.getUser().getId();
        }
        payment.setRecordedBy(recordedBy != null ? recordedBy : 1);

        Payment saved = fineRepository.savePayment(payment);

        fine.setAmountPaid(fine.getAmountPaid().add(amount));
        fine.setStatus(fine.getAmountPaid().compareTo(fine.getAmount()) >= 0 ? FineStatus.PAID : FineStatus.PARTIALLY_PAID);
        fineRepository.update(fine);

        audit("FINE_PAID", fineId, "Payment of " + amount + " (" + method + ") recorded with reference " + payment.getReference());
        return saved;
    }

    public void waiveFine(int fineId) {
        Fine fine = fineRepository.findById(fineId).orElseThrow(() -> new ValidationException("Fine not found."));
        fine.setStatus(FineStatus.WAIVED);
        fineRepository.update(fine);
        audit("FINE_WAIVED", fineId, "Fine #" + fineId + " waived by administrator");
    }

    public List<Fine> findOutstandingByMember(int memberId) {
        return fineRepository.findOutstandingByMember(memberId);
    }

    public List<Fine> findAll() {
        return fineRepository.findAll();
    }

    public boolean hasBlockingUnpaidFines(int memberId) {
        return appConfig.isFineBlockingIssue() && !findOutstandingByMember(memberId).isEmpty();
    }

    private void audit(String action, Integer entityId, String description) {
        Integer userId = null;
        if (sessionManager.getCurrentUser() != null) {
            userId = sessionManager.getCurrentUser().getId();
        } else if (com.librax.security.SecurityContextHolder.getUser() != null) {
            userId = com.librax.security.SecurityContextHolder.getUser().getId();
        }
        auditLogger.log(userId, action, "FINE", entityId, description);
    }
}
