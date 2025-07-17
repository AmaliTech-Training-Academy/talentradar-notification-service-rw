package com.talentradar.talentradarnotificationservicerw.services;

import com.talentradar.talentradarnotificationservicerw.domain.dtos.SimpleResponseDTO;
import com.talentradar.talentradarnotificationservicerw.domain.dtos.SingleNotificationResponseDTO;
import com.talentradar.talentradarnotificationservicerw.domain.entities.Notification;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NotificationServices {
    Page<Notification> findNotifications(String recipient, Optional<NotificationCategory> category, Optional<String> status, Pageable pageable);

    Optional<SingleNotificationResponseDTO> getNotification(String notificationId, String userId);

    Page<Notification> searchNotification(String searchTerm, String userId, Pageable pageable);

    SimpleResponseDTO readNotification(String notificationId);

    Notification saveNotification(Notification notification);

    SimpleResponseDTO dismissNotification(String notificationId);
}
