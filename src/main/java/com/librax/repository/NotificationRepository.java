package com.librax.repository;

import com.librax.model.Notification;
import com.librax.model.NotificationType;

import java.util.List;

public interface NotificationRepository {
    Notification save(Notification notification);
    List<Notification> findByUserId(int userId, int limit);
    int countUnreadByUserId(int userId);
    void markAsRead(int notificationId, int userId);
    void markAllAsRead(int userId);
    void createNotification(int userId, NotificationType type, String title, String message);
}
