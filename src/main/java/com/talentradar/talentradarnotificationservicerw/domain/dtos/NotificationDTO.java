package com.talentradar.talentradarnotificationservicerw.domain.dtos;

import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationCategory;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationEventType;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record NotificationDTO(
        UUID id,
        NotificationType type,
        NotificationCategory category,
        NotificationEventType eventType,
        String title,
        String content,
        LocalDateTime sentAt,
        LocalDateTime readAt
) {
}
