package com.librax.web;

import com.librax.model.*;
import com.librax.repository.FineRepository;
import com.librax.repository.MemberRepository;
import com.librax.security.RequiresPermission;
import com.librax.security.SecurityContextHolder;
import com.librax.service.FineService;
import com.librax.service.NotificationService;
import com.librax.web.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fines")
public class FinanceController {

    private final FineService fineService;
    private final FineRepository fineRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    public FinanceController(FineService fineService,
                             FineRepository fineRepository,
                             MemberRepository memberRepository,
                             NotificationService notificationService) {
        this.fineService = fineService;
        this.fineRepository = fineRepository;
        this.memberRepository = memberRepository;
        this.notificationService = notificationService;
    }

    @GetMapping
    @RequiresPermission("FINE_VIEW")
    public ApiResponse<List<Fine>> getAllFines(@RequestParam(value = "status", required = false) String status) {
        User user = SecurityContextHolder.getUser();
        List<Fine> fines;
        if (user != null && user.getRole() == com.librax.model.Role.STUDENT) {
            Member member = memberRepository.findByEmail(user.getEmail()).orElse(null);
            if (member == null) {
                return ApiResponse.ok(List.of());
            }
            final int mId = member.getId();
            fines = fineService.findAll().stream()
                    .filter(f -> f.getMemberId() == mId)
                    .collect(Collectors.toList());
        } else {
            fines = fineService.findAll();
        }

        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            fines = fines.stream()
                    .filter(f -> f.getStatus().name().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }
        return ApiResponse.ok(fines);
    }

    @PostMapping("/{id}/waive")
    @RequiresPermission("FINE_WAIVE")
    public ApiResponse<Void> waiveFine(@PathVariable("id") int id) {
        fineService.waiveFine(id);

        fineRepository.findById(id).ifPresent(fine -> {
            memberRepository.findById(fine.getMemberId()).ifPresent(m -> {
                if (m.getUserId() != null) {
                    notificationService.notify(m.getUserId(), NotificationType.SYSTEM_ALERT,
                            "Fine Waived",
                            "Your fine of ₹" + fine.getAmount() + " (Fine #" + fine.getId() + ") has been waived by the library administration.");
                }
            });
        });

        return ApiResponse.ok(null, "Fine waived successfully");
    }

    @PostMapping("/pay")
    @RequiresPermission("FINE_PAYMENT")
    public ApiResponse<Payment> recordPayment(@RequestBody Map<String, Object> payload) {
        Integer fineId = (Integer) payload.get("fineId");
        Number amountNum = (Number) payload.get("amount");
        String methodStr = (String) payload.get("method");

        if (fineId == null || amountNum == null || methodStr == null) {
            throw new com.librax.exception.ValidationException("fineId, amount, and method are required");
        }

        BigDecimal amount = BigDecimal.valueOf(amountNum.doubleValue());
        PaymentMethod method = PaymentMethod.valueOf(methodStr.toUpperCase());

        Payment payment = fineService.recordPayment(fineId, amount, method);

        fineRepository.findById(fineId).ifPresent(fine -> {
            memberRepository.findById(fine.getMemberId()).ifPresent(m -> {
                if (m.getUserId() != null) {
                    notificationService.notify(m.getUserId(), NotificationType.PAYMENT_RECEIVED,
                            "Payment Receipt: " + payment.getReference(),
                            "Received ₹" + amount + " via " + method + " for Fine #" + fineId + ".");
                }
            });
        });

        return ApiResponse.ok(payment, "Payment successfully recorded");
    }

    @GetMapping("/my-fines")
    public ApiResponse<List<Fine>> getMyFines() {
        User user = SecurityContextHolder.getUser();
        if (user == null) {
            return ApiResponse.error("UNAUTHORIZED", "Not logged in");
        }
        Member member = memberRepository.findByEmail(user.getEmail()).orElse(null);
        if (member == null) {
            return ApiResponse.ok(List.of());
        }
        return ApiResponse.ok(fineService.findOutstandingByMember(member.getId()));
    }
}
