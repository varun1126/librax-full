package com.librax.web;

import com.librax.model.Loan;
import com.librax.model.Member;
import com.librax.model.NotificationType;
import com.librax.model.User;
import com.librax.repository.MemberRepository;
import com.librax.security.RequiresPermission;
import com.librax.security.SecurityContextHolder;
import com.librax.service.LoanService;
import com.librax.service.NotificationService;
import com.librax.web.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    public LoanController(LoanService loanService, MemberRepository memberRepository, NotificationService notificationService) {
        this.loanService = loanService;
        this.memberRepository = memberRepository;
        this.notificationService = notificationService;
    }

    @GetMapping
    @RequiresPermission("LOAN_VIEW")
    public ApiResponse<Map<String, Object>> getLoans(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {

        User currentUser = SecurityContextHolder.getUser();
        List<Loan> all;
        if (currentUser != null && currentUser.getRole() == com.librax.model.Role.STUDENT) {
            Member member = memberRepository.findByEmail(currentUser.getEmail()).orElse(null);
            all = (member != null) ? loanService.findAllByMember(member.getId()) : List.of();
        } else {
            all = loanService.findAll();
        }

        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            all = all.stream()
                    .filter(l -> l.getStatus().name().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.isBlank()) {
            String s = search.toLowerCase();
            all = all.stream()
                    .filter(l -> (l.getBookTitle() != null && l.getBookTitle().toLowerCase().contains(s))
                            || (l.getMemberName() != null && l.getMemberName().toLowerCase().contains(s)))
                    .collect(Collectors.toList());
        }

        int total = all.size();
        int fromIndex = Math.min((page - 1) * limit, total);
        int toIndex = Math.min(fromIndex + limit, total);
        List<Loan> paged = all.subList(fromIndex, toIndex);

        Map<String, Object> map = new HashMap<>();
        map.put("items", paged);
        map.put("loans", paged);
        map.put("total", total);
        map.put("page", page);
        map.put("limit", limit);
        map.put("totalPages", (int) Math.ceil((double) total / limit));

        return ApiResponse.ok(map);
    }

    @PostMapping("/issue")
    public ApiResponse<Loan> issueBook(@RequestBody Map<String, Object> payload) {
        User currentUser = SecurityContextHolder.getUser();
        if (currentUser == null) {
            return ApiResponse.error("AUTH_REQUIRED", "Authentication required");
        }

        Integer memberId = null;
        Object mIdObj = payload.get("memberId");
        if (mIdObj instanceof Number n) {
            memberId = n.intValue();
        } else if (mIdObj != null) {
            try { memberId = Integer.parseInt(mIdObj.toString().trim()); } catch (NumberFormatException ignored) {}
        }

        Integer bookId = null;
        Object bIdObj = payload.get("bookId");
        if (bIdObj instanceof Number n) {
            bookId = n.intValue();
        } else if (bIdObj != null) {
            try { bookId = Integer.parseInt(bIdObj.toString().trim()); } catch (NumberFormatException ignored) {}
        }

        if (currentUser.getRole() == com.librax.model.Role.STUDENT) {
            // For students, find their own linked member record (prevents IDOR)
            Member member = null;
            if (currentUser.getId() != null) {
                member = memberRepository.findByUserId(currentUser.getId()).orElse(null);
            }
            if (member == null && currentUser.getEmail() != null) {
                member = memberRepository.findByEmail(currentUser.getEmail()).orElse(null);
            }
            if (member == null) {
                throw new com.librax.exception.ValidationException("Student library membership record not found.");
            }
            memberId = member.getId();
        } else {
            // For staff/admin, verify permission
            if (currentUser.getRole() != com.librax.model.Role.SUPER_ADMIN && !currentUser.hasPermission("LOAN_CREATE")) {
                return ApiResponse.error("FORBIDDEN", "Missing required permission 'LOAN_CREATE'");
            }
            if (memberId == null) {
                throw new com.librax.exception.ValidationException("memberId is required");
            }
        }

        if (bookId == null) {
            throw new com.librax.exception.ValidationException("bookId is required");
        }

        Loan loan = loanService.issueBook(memberId, bookId);
        Loan fullLoan = loanService.findById(loan.getId()).orElse(loan);

        // Notify member if user linked
        memberRepository.findById(memberId).ifPresent(m -> {
            if (m.getUserId() != null) {
                notificationService.notify(m.getUserId(), NotificationType.BOOK_DUE_SOON,
                        "Book Issued: " + fullLoan.getBookTitle(),
                        "You borrowed '" + fullLoan.getBookTitle() + "'. Due date is " + fullLoan.getDueDate() + ".");
            }
        });

        return ApiResponse.ok(fullLoan, "Book successfully issued");
    }

    @PostMapping("/{id}/return")
    public ApiResponse<Loan> returnBook(@PathVariable("id") int id) {
        User currentUser = SecurityContextHolder.getUser();
        if (currentUser == null) {
            return ApiResponse.error("AUTH_REQUIRED", "Authentication required");
        }

        Loan existingLoan = loanService.findById(id)
                .orElseThrow(() -> new com.librax.exception.ValidationException("Loan record not found"));

        if (currentUser.getRole() == com.librax.model.Role.STUDENT) {
            Member member = null;
            if (currentUser.getId() != null) {
                member = memberRepository.findByUserId(currentUser.getId()).orElse(null);
            }
            if (member == null && currentUser.getEmail() != null) {
                member = memberRepository.findByEmail(currentUser.getEmail()).orElse(null);
            }
            if (member == null || !Objects.equals(existingLoan.getMemberId(), member.getId())) {
                return ApiResponse.error("FORBIDDEN", "You are not authorized to return loans belonging to other students.");
            }
        } else {
            if (currentUser.getRole() != com.librax.model.Role.SUPER_ADMIN && !currentUser.hasPermission("LOAN_RETURN")) {
                return ApiResponse.error("FORBIDDEN", "Missing required permission 'LOAN_RETURN'");
            }
        }

        Loan loan = loanService.returnBook(id);

        memberRepository.findById(loan.getMemberId()).ifPresent(m -> {
            if (m.getUserId() != null) {
                String fineNote = loan.getFineAmount() != null && loan.getFineAmount().signum() > 0
                        ? " Overdue fine assessed: ₹" + loan.getFineAmount()
                        : "";
                notificationService.notify(m.getUserId(), NotificationType.SYSTEM_ALERT,
                        "Book Returned",
                        "Loan #" + loan.getId() + " has been marked returned." + fineNote);
            }
        });

        return ApiResponse.ok(loan, "Book returned successfully");
    }

    @PostMapping("/{id}/renew")
    @RequiresPermission("LOAN_RENEW")
    public ApiResponse<Loan> renewLoan(@PathVariable("id") int id) {
        User currentUser = SecurityContextHolder.getUser();
        if (currentUser != null && currentUser.getRole() == com.librax.model.Role.STUDENT) {
            Member member = memberRepository.findByEmail(currentUser.getEmail()).orElse(null);
            Loan loan = loanService.findById(id)
                    .orElseThrow(() -> new com.librax.exception.ValidationException("Loan not found"));
            if (member == null || !Objects.equals(loan.getMemberId(), member.getId())) {
                return ApiResponse.error("FORBIDDEN", "You are not authorized to renew loans belonging to other students.");
            }
        }
        Loan loan = loanService.renewLoan(id);
        return ApiResponse.ok(loan, "Loan successfully renewed until " + loan.getDueDate());
    }

    @GetMapping("/my-loans")
    public ApiResponse<List<Loan>> getMyLoans() {
        User user = SecurityContextHolder.getUser();
        if (user == null) {
            return ApiResponse.error("UNAUTHORIZED", "Not logged in");
        }
        Member member = memberRepository.findByEmail(user.getEmail()).orElse(null);
        if (member == null) {
            return ApiResponse.ok(List.of());
        }
        return ApiResponse.ok(loanService.findAllByMember(member.getId()));
    }
}
