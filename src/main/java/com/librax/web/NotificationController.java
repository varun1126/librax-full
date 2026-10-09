package com.librax.web;

import com.librax.model.Notification;
import com.librax.model.User;
import com.librax.security.SecurityContextHolder;
import com.librax.service.NotificationService;
import com.librax.web.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getNotifications(@RequestParam(value = "limit", defaultValue = "30") int limit) {
        User user = SecurityContextHolder.getUser();
        if (user == null) {
            return ApiResponse.error("UNAUTHORIZED", "Not logged in");
        }
        List<Notification> list = notificationService.getUserNotifications(user.getId(), limit);
        int unreadCount = notificationService.getUnreadCount(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("notifications", list);
        data.put("unreadCount", unreadCount);
        return ApiResponse.ok(data);
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable("id") int id) {
        User user = SecurityContextHolder.getUser();
        if (user != null) {
            notificationService.markAsRead(id, user.getId());
        }
        return ApiResponse.ok(null, "Notification marked as read");
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markAllAsRead() {
        User user = SecurityContextHolder.getUser();
        if (user != null) {
            notificationService.markAllAsRead(user.getId());
        }
        return ApiResponse.ok(null, "All notifications marked as read");
    }
}
