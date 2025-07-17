package com.talentradar.talentradarnotificationservicerw.services;

import com.talentradar.talentradarnotificationservicerw.domain.entities.Notification;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NotificationService {
    Page<Notification> findNotifications(String recipient, Optional<NotificationCategory> category, Optional<String> status, Pageable pageable);

    Optional<Notification> getNotification(String notificationId);

    Page<Notification> searchNotification(String searchTerm, String userId, Pageable pageable);

    void readNotification(String notificationId);

    Notification saveNotification(Notification notification);

    void dismissNotification(String notificationId);
}
