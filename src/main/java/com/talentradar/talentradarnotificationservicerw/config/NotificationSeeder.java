package com.talentradar.talentradarnotificationservicerw.config;

import com.talentradar.talentradarnotificationservicerw.domain.entities.Notification;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationCategory;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationEventType;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationType;
import com.talentradar.talentradarnotificationservicerw.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationSeeder implements CommandLineRunner {
    private final NotificationRepository notificationRepository;

    @Override
    public void run(String... args) throws Exception {
        if (notificationRepository.count() == 0) {
            List<Notification> notifications = List.of(
                    Notification.builder()
                            .title("New feedback submitted")
                            .content("Your manager has reviewed your performance and left a feedback")
                            .type(NotificationType.IN_APP)
                            .category(NotificationCategory.INFO)
                            .recipientId("user1")
                            .eventType(NotificationEventType.FEEDBACK)
                            .retryCount(0)
                            .dismissed(false)
                            .readAt(null)
                            .sentAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build(),

                    Notification.builder()
                            .title("New assessment submitted")
                            .content("Your self assessment has been saved")
                            .type(NotificationType.IN_APP)
                            .category(NotificationCategory.INFO)
                            .recipientId("user2")
                            .eventType(NotificationEventType.ASSESSMENT)
                            .retryCount(1)
                            .dismissed(false)
                            .readAt(LocalDateTime.now().minusDays(1))
                            .sentAt(LocalDateTime.now().minusDays(2))
                            .createdAt(LocalDateTime.now().minusDays(2))
                            .build(),

                    Notification.builder()
                            .title("New feedback submitted")
                            .content("Your manager has reviewed your performance and left a feedback")
                            .type(NotificationType.IN_APP)
                            .category(NotificationCategory.SUCCESS)
                            .recipientId("user1")
                            .eventType(NotificationEventType.FEEDBACK)
                            .retryCount(0)
                            .dismissed(false)
                            .readAt(null)
                            .sentAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build()
            );
            notificationRepository.saveAll(notifications);
            System.out.println("📥 Seeded " + notifications.size() + " notifications.");

        }
    }
}

