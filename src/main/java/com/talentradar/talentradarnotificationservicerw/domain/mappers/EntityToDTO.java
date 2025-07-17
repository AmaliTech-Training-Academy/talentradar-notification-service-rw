package com.talentradar.talentradarnotificationservicerw.domain.mappers;

import com.talentradar.talentradarnotificationservicerw.domain.dtos.NotificationDTO;
import com.talentradar.talentradarnotificationservicerw.domain.entities.Notification;

public class EntityToDTO {
    public static NotificationDTO notificationEntityToDTO(Notification notificationEntity) {
        return NotificationDTO.builder()
                .id(notificationEntity.getId())
                .title(notificationEntity.getTitle())
                .category(notificationEntity.getCategory())
                .content(notificationEntity.getContent())
                .eventType(notificationEntity.getEventType())
                .sentAt(notificationEntity.getSentAt())
                .readAt(notificationEntity.getReadAt())
                .build();
    }
}
