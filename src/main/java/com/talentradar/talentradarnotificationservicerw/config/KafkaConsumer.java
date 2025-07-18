package com.talentradar.talentradarnotificationservicerw.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentradar.talentradarnotificationservicerw.domain.entities.Notification;
import com.talentradar.talentradarnotificationservicerw.domain.enums.NotificationEventType;
import com.talentradar.talentradarnotificationservicerw.domain.events.EventLog;
import com.talentradar.talentradarnotificationservicerw.domain.events.FeedBackServiceEvent;
import com.talentradar.talentradarnotificationservicerw.services.NotificationServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {
    private final ObjectMapper objectMapper;
    private final NotificationServices notificationServices;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "feedback.submitted", groupId = "feedback-service")
    public void feedbackListener(String message) {
        try {
            FeedBackServiceEvent event = objectMapper.readValue(message, FeedBackServiceEvent.class);
            Notification notification = Notification.builder()
                    .category(event.category())
                    .content(event.content())
                    .recipientId(event.recipientId())
                    .title(event.title())
                    .type(event.type())
                    .eventType(NotificationEventType.FEEDBACK)
                    .build();

            Notification savedNotification = notificationServices.saveNotification(notification);
            sendWebSocketPush(savedNotification, event);
            log.info(
                    EventLog.builder()
                            .trigger("Event triggered by a new assessment submitted")
                            .event(event)
                            .build()
                            .toString()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @KafkaListener(topics = "assessment.submitted", groupId = "feedback-service")
    public void assessmentListener(String message) {
        try {
            FeedBackServiceEvent event = objectMapper.readValue(message, FeedBackServiceEvent.class);
            Notification notification = Notification.builder()
                    .category(event.category())
                    .content(event.content())
                    .recipientId(event.recipientId())
                    .title(event.title())
                    .type(event.type())
                    .eventType(NotificationEventType.ASSESSMENT)
                    .build();

            Notification savedNotification = notificationServices.saveNotification(notification);
            sendWebSocketPush(savedNotification, event);

            log.info(
                    EventLog.builder()
                            .trigger("Event triggered by a new feedback submitted")
                            .event(event)
                            .build()
                            .toString()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void sendWebSocketPush(Notification notification, FeedBackServiceEvent event) {
        messagingTemplate.convertAndSendToUser(
                event.recipientId(),
                "/queue/notifications",
                notification
        );
    }
}
