package com.librax.service;

import com.librax.model.Notification;
import com.librax.model.NotificationType;
import com.librax.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getUserNotifications(int userId, int limit) {
        return notificationRepository.findByUserId(userId, limit);
    }

    public int getUnreadCount(int userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    public void markAsRead(int notificationId, int userId) {
        notificationRepository.markAsRead(notificationId, userId);
    }

    public void markAllAsRead(int userId) {
        notificationRepository.markAllAsRead(userId);
    }

    public void notify(int userId, NotificationType type, String title, String message) {
        notificationRepository.createNotification(userId, type, title, message);
    }
}
