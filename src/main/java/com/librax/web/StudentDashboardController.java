package com.librax.web;

import com.librax.model.*;
import com.librax.repository.MemberRepository;
import com.librax.security.SecurityContextHolder;
import com.librax.service.*;
import com.librax.web.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student/dashboard")
@SuppressWarnings("null")
public class StudentDashboardController {

    private final MemberRepository memberRepository;
    private final LoanService loanService;
    private final FineService fineService;
    private final ReservationService reservationService;
    private final NotificationService notificationService;
    private final BookService bookService;

    public StudentDashboardController(MemberRepository memberRepository,
                                      LoanService loanService,
                                      FineService fineService,
                                      ReservationService reservationService,
                                      NotificationService notificationService,
                                      BookService bookService) {
        this.memberRepository = memberRepository;
        this.loanService = loanService;
        this.fineService = fineService;
        this.reservationService = reservationService;
        this.notificationService = notificationService;
        this.bookService = bookService;
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> getStudentSummary() {
        User user = SecurityContextHolder.getUser();
        if (user == null) {
            return ApiResponse.error("UNAUTHORIZED", "Not logged in");
        }

        Member member = memberRepository.findByEmail(user.getEmail()).orElse(null);
        Map<String, Object> summary = new HashMap<>();

        summary.put("studentName", user.getFullName());
        summary.put("studentId", member != null ? member.getMembershipNumber() : "N/A");
        summary.put("department", member != null ? member.getDepartmentName() : "General");
        summary.put("course", member != null ? member.getCourseName() : "Degree");
        summary.put("membershipStatus", member != null ? member.getStatus().name() : "ACTIVE");
        summary.put("expiryDate", member != null ? member.getMembershipExpiry() : LocalDate.now().plusYears(1));

        int memberId = member != null ? member.getId() : -1;
        List<Loan> loans = memberId > 0 ? loanService.findAllByMember(memberId) : List.of();
        List<Loan> activeLoans = loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.OVERDUE)
                .collect(Collectors.toList());

        LocalDate now = LocalDate.now();
        List<Loan> dueSoon = activeLoans.stream()
                .filter(l -> !l.getDueDate().isBefore(now) && l.getDueDate().isBefore(now.plusDays(4)))
                .collect(Collectors.toList());

        List<Loan> overdue = activeLoans.stream()
                .filter(l -> l.getDueDate().isBefore(now))
                .collect(Collectors.toList());

        List<Fine> fines = memberId > 0 ? fineService.findOutstandingByMember(memberId) : List.of();
        BigDecimal totalFine = fines.stream()
                .map(f -> f.getOutstanding() != null ? f.getOutstanding() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        List<Reservation> reservations = reservationService.findAll().stream()
                .filter(r -> r.getMemberId() == memberId && (r.getStatus() == ReservationStatus.WAITING || r.getStatus() == ReservationStatus.READY))
                .collect(Collectors.toList());

        int unreadNotifs = notificationService.getUnreadCount(user.getId());

        summary.put("activeLoansCount", activeLoans.size());
        summary.put("activeLoans", activeLoans);
        summary.put("dueSoonCount", dueSoon.size());
        summary.put("dueSoonLoans", dueSoon);
        summary.put("overdueCount", overdue.size());
        summary.put("overdueLoans", overdue);
        summary.put("outstandingFineTotal", totalFine);
        summary.put("reservationsCount", reservations.size());
        summary.put("reservations", reservations);
        summary.put("unreadNotificationsCount", unreadNotifs);

        // Recommended books (popular available books)
        List<Book> recommended = bookService.findAll().stream()
                .filter(b -> b.getAvailableCopies() > 0)
                .limit(6)
                .collect(Collectors.toList());
        summary.put("recommendedBooks", recommended);

        return ApiResponse.ok(summary);
    }
}
