package com.talentradar.talentradarnotificationservicerw.domain.entities;

import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationCategory;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationEventType;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collation = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    private String id;

    private String title;
    private String content;
    private NotificationType type;
    private NotificationCategory category;
    private String recipientId;
    private NotificationEventType eventType;
    private int retryCount;
    private boolean dismissed;

    @Column(nullable = true)
    private LocalDateTime readAt;

    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
