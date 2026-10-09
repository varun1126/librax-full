package com.librax.web;

import com.librax.model.AuditLog;
import com.librax.repository.AuditLogRepository;
import com.librax.security.RequiresPermission;
import com.librax.service.AnalyticsService;
import com.librax.web.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AnalyticsService analyticsService;
    private final AuditLogRepository auditLogRepository;

    public AdminDashboardController(AnalyticsService analyticsService, AuditLogRepository auditLogRepository) {
        this.analyticsService = analyticsService;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/stats")
    @RequiresPermission("USER_VIEW")
    public ApiResponse<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // KPI Counts from real database
        Number totalBooks = analyticsService.totalBooks();
        Number availableCopies = analyticsService.totalAvailableCopies();
        Number issuedCopies = analyticsService.issuedBooksCount();
        Number overdueBooks = analyticsService.overdueBooksCount();
        Number activeStudents = analyticsService.totalMembers();
        Number reservationsCount = analyticsService.activeReservationsCount();
        java.math.BigDecimal outstandingFines = analyticsService.outstandingFinesTotal();

        stats.put("totalBooks", totalBooks);
        stats.put("availableCopies", availableCopies);
        stats.put("issuedCopies", issuedCopies);
        stats.put("activeLoans", issuedCopies);
        stats.put("overdueBooks", overdueBooks);
        stats.put("overdueLoans", overdueBooks);
        stats.put("activeStudents", activeStudents);
        stats.put("reservationsCount", reservationsCount);
        stats.put("pendingReservations", reservationsCount);
        stats.put("outstandingFines", outstandingFines);
        stats.put("unpaidFinesAmount", outstandingFines != null ? outstandingFines.doubleValue() : 0.0);
        stats.put("totalFinesCollected", 0.0);
        stats.put("lowStockCount", 0);
        stats.put("todayTransactions", analyticsService.todaysTransactionsCount());

        // Visualizations & Aggregates
        stats.put("booksByCategory", analyticsService.booksByCategory());
        stats.put("mostBorrowedBooks", analyticsService.mostBorrowedBooks(5));
        stats.put("monthlyTrends", analyticsService.monthlyIssueStats(6));

        // Recent Audit Activity Feed
        List<AuditLog> recentLogs = auditLogRepository.findRecent(10);
        stats.put("recentActivity", recentLogs);

        return ApiResponse.ok(stats);
    }
}
