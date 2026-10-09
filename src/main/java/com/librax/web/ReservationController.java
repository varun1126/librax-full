package com.librax.web;

import com.librax.model.Member;
import com.librax.model.NotificationType;
import com.librax.model.Reservation;
import com.librax.model.User;
import com.librax.repository.MemberRepository;
import com.librax.security.RequiresPermission;
import com.librax.security.SecurityContextHolder;
import com.librax.service.NotificationService;
import com.librax.service.ReservationService;
import com.librax.web.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    public ReservationController(ReservationService reservationService,
                                 MemberRepository memberRepository,
                                 NotificationService notificationService) {
        this.reservationService = reservationService;
        this.memberRepository = memberRepository;
        this.notificationService = notificationService;
    }

    @GetMapping
    @RequiresPermission("RESERVATION_VIEW")
    public ApiResponse<List<Reservation>> getReservations() {
        User user = SecurityContextHolder.getUser();
        if (user != null && user.getRole() == com.librax.model.Role.STUDENT) {
            Member member = memberRepository.findByEmail(user.getEmail()).orElse(null);
            if (member == null) {
                return ApiResponse.ok(List.of());
            }
            List<Reservation> userRes = reservationService.findAll().stream()
                    .filter(r -> r.getMemberId() == member.getId())
                    .collect(Collectors.toList());
            return ApiResponse.ok(userRes);
        }
        return ApiResponse.ok(reservationService.findAll());
    }

    @PostMapping
    @RequiresPermission("RESERVATION_CREATE")
    public ApiResponse<Reservation> createReservation(@RequestBody Map<String, Integer> payload) {
        Integer bookId = payload.get("bookId");
        Integer memberId = payload.get("memberId");

        User user = SecurityContextHolder.getUser();
        if (user != null && user.getRole() == com.librax.model.Role.STUDENT) {
            Member m = memberRepository.findByEmail(user.getEmail()).orElse(null);
            if (m == null) {
                throw new com.librax.exception.ValidationException("Student record not found for logged in user.");
            }
            memberId = m.getId(); // Enforce calling student's own ID
        } else if (memberId == null && user != null) {
            Member m = memberRepository.findByEmail(user.getEmail()).orElse(null);
            if (m != null) memberId = m.getId();
        }

        if (bookId == null || memberId == null) {
            throw new com.librax.exception.ValidationException("bookId and memberId are required");
        }

        Reservation res = reservationService.reserve(bookId, memberId);

        final int mId = memberId;
        memberRepository.findById(mId).ifPresent(m -> {
            if (m.getUserId() != null) {
                notificationService.notify(m.getUserId(), NotificationType.SYSTEM_ALERT,
                        "Reservation Confirmed",
                        "Your reservation for Book #" + bookId + " is in the queue (Status: WAITING).");
            }
        });

        return ApiResponse.ok(res, "Reservation placed successfully");
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancelReservation(@PathVariable("id") int id) {
        User user = SecurityContextHolder.getUser();
        if (user == null) {
            return ApiResponse.error("AUTH_REQUIRED", "Authentication required");
        }
        Reservation res = reservationService.findById(id)
                .orElseThrow(() -> new com.librax.exception.ValidationException("Reservation not found"));

        if (user.getRole() == com.librax.model.Role.STUDENT) {
            Member member = memberRepository.findByEmail(user.getEmail()).orElse(null);
            if (member == null || res.getMemberId() != member.getId()) {
                return ApiResponse.error("FORBIDDEN", "You are not authorized to cancel reservations belonging to other students.");
            }
        } else {
            if (user.getRole() != com.librax.model.Role.SUPER_ADMIN && !user.hasPermission("RESERVATION_CANCEL")) {
                return ApiResponse.error("FORBIDDEN", "Missing required permission 'RESERVATION_CANCEL'");
            }
        }
        reservationService.cancel(id);
        return ApiResponse.ok(null, "Reservation cancelled");
    }

    @PostMapping("/{id}/fulfill")
    @RequiresPermission("RESERVATION_MANAGE")
    public ApiResponse<Void> fulfillReservation(@PathVariable("id") int id) {
        reservationService.markFulfilled(id);
        return ApiResponse.ok(null, "Reservation marked fulfilled");
    }

    @GetMapping("/my-reservations")
    public ApiResponse<List<Reservation>> getMyReservations() {
        User user = SecurityContextHolder.getUser();
        if (user == null) {
            return ApiResponse.error("UNAUTHORIZED", "Not logged in");
        }
        Member member = memberRepository.findByEmail(user.getEmail()).orElse(null);
        if (member == null) {
            return ApiResponse.ok(List.of());
        }
        List<Reservation> userRes = reservationService.findAll().stream()
                .filter(r -> r.getMemberId() == member.getId())
                .collect(Collectors.toList());
        return ApiResponse.ok(userRes);
    }
}
